package com.angelvazquez.csia.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.angelvazquez.csia.config.AppConfig;
import com.angelvazquez.csia.i18n.I18n;
import com.angelvazquez.csia.i18n.Idioma;

/** Lee y guarda la configuración XML, manteniendo compatibilidad con ficheros sin idioma o tipo de motor. */
public class ConfiguracionManager {

    private static final String DIRECTORIO_CONFIG = "config";
    private static final String FICHERO_CONFIG = "configuracion.xml";

    private final Path directorioAplicacion;

    public ConfiguracionManager() {
        this.directorioAplicacion = null;
    }

    ConfiguracionManager(Path directorioAplicacion) {
        this.directorioAplicacion = directorioAplicacion
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Lee la configuración existente o solicita y guarda una nueva mediante diálogos.
     *
     * @return configuración de base de datos, o {@code null} si se cancela o falla;
     *         los errores se muestran al usuario
     */
    public ConfigDB inicializarConfiguracion() {
        try {
            Path rutaConfiguracion = obtenerRutaConfiguracion();

            if (Files.isRegularFile(rutaConfiguracion)) {
                System.out.println(I18n.get(
                        "config.fileFound",
                        rutaConfiguracion.toAbsolutePath()
                ));
                return leerConfiguracion(rutaConfiguracion);
            }

            System.out.println(I18n.get(
                    "config.fileNotFound",
                    rutaConfiguracion.toAbsolutePath()
            ));
            return crearNuevaConfiguracion(rutaConfiguracion);
        } catch (Exception e) {
            mostrarError(I18n.get("config.initError", e.getMessage()));
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Localiza config/configuracion.xml respecto al directorio de la aplicación.
     *
     * @return ruta absoluta normalizada; este método no crea el fichero
     * @throws URISyntaxException si la ubicación del código no se puede convertir en URI
     */
    public Path obtenerRutaConfiguracion() throws URISyntaxException {
        Path rutaAplicacion = directorioAplicacion;

        if (rutaAplicacion == null) {
            Path ubicacionCodigo = Paths.get(
                    ConfiguracionManager.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            );
            rutaAplicacion = resolverDirectorioAplicacion(
                    ubicacionCodigo,
                    Paths.get(System.getProperty("user.dir"))
            );
        }

        return rutaAplicacion
                .resolve(DIRECTORIO_CONFIG)
                .resolve(FICHERO_CONFIG)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Usa el directorio del JAR si el código está en un fichero; en desarrollo busca
     * el primer ancestro con pom.xml. Si no lo encuentra, usa el directorio de trabajo.
     */
    static Path resolverDirectorioAplicacion(
            Path ubicacionCodigo, Path directorioTrabajo) {
        Path ubicacion = ubicacionCodigo.toAbsolutePath().normalize();

        if (Files.isRegularFile(ubicacion)) {
            return ubicacion.getParent();
        }

        for (Path candidato = ubicacion;
             candidato != null;
             candidato = candidato.getParent()) {
            if (Files.isRegularFile(candidato.resolve("pom.xml"))) {
                return candidato;
            }
        }

        return directorioTrabajo.toAbsolutePath().normalize();
    }

    ConfigDB leerConfiguracion(Path ruta) throws Exception {
        return leerConfiguracionAplicacion(ruta).getDatabase();
    }

    /**
     * Lee idioma y base de datos sin modificar el XML ni abrir conexiones JDBC.
     *
     * @param ruta fichero XML que se desea leer
     * @return configuración; puede carecer de idioma si el fichero es antiguo
     * @throws Exception si falla la lectura, el análisis XML o la validación de valores
     */
    public AppConfig leerConfiguracionAplicacion(Path ruta) throws Exception {
        Document document = leerDocumento(ruta);
        Idioma idioma = leerIdioma(document);
        ConfigDB database = leerConfiguracionBaseDatos(document);
        return new AppConfig(idioma, database);
    }

    private Document leerDocumento(Path ruta) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        configurarParserSeguro(factory);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(ruta.toFile());
        document.getDocumentElement().normalize();
        return document;
    }

    // La ausencia de idioma permite que StartupManager solicite y persista la elección.
    private Idioma leerIdioma(Document document) throws IOException {
        Element aplicacion = (Element) document
                .getElementsByTagName("aplicacion")
                .item(0);
        if (aplicacion == null) {
            return null;
        }

        String codigo = obtenerValorOpcional(aplicacion, "idioma");
        if (codigo == null || codigo.isBlank()) {
            return null;
        }

        return Idioma.desdeCodigo(codigo)
                .orElseThrow(() -> new IOException(
                        I18n.get("config.unsupportedLanguage", codigo)
                ));
    }

    private ConfigDB leerConfiguracionBaseDatos(Document document)
            throws IOException {
        Element baseDatos = (Element) document
                .getElementsByTagName("baseDatos")
                .item(0);

        if (baseDatos == null) {
            throw new IOException(I18n.get("config.missingDatabaseElement"));
        }

        ConfigDB configuracion = new ConfigDB();
        String tipo = obtenerValorOpcional(baseDatos, "tipo");

        // Los XML antiguos no incluyen tipo: se infiere de la URL o del driver.
        if (tipo == null || tipo.isBlank()) {
            configuracion.databaseType = detectarTipoBaseDatos(
                    obtenerValorOpcional(baseDatos, "url"),
                    obtenerValorOpcional(baseDatos, "driver")
            );
        } else {
            try {
                configuracion.databaseType = DatabaseType.fromConfigValue(tipo);
            } catch (IllegalArgumentException e) {
                throw new IOException(e.getMessage(), e);
            }
        }

        validarMotorHabilitado(configuracion.databaseType);

        configuracion.driver = obtenerValor(baseDatos, "driver");
        configuracion.url = obtenerValor(baseDatos, "url");
        configuracion.db = obtenerValor(baseDatos, "db");

        if (configuracion.databaseType == DatabaseType.SQLITE) {
            configuracion.user = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "usuario", "");
            configuracion.password = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "password", "");
        } else {
            configuracion.user = obtenerValor(baseDatos, "usuario");
            configuracion.password = obtenerValor(baseDatos, "password");
        }

        return configuracion;
    }

    private ConfigDB crearNuevaConfiguracion(Path rutaConfiguracion)
            throws Exception {
        ConfigDB configuracion = solicitarConfiguracionUsuario();
        if (configuracion == null) {
            return null;
        }

        guardarConfiguracion(rutaConfiguracion, configuracion);
        JOptionPane.showMessageDialog(
                null,
                I18n.get("config.savedPath", rutaConfiguracion.toAbsolutePath()),
                I18n.get("config.title"),
                JOptionPane.INFORMATION_MESSAGE
        );
        return configuracion;
    }

    private ConfigDB solicitarConfiguracionUsuario() {
        ConfiguracionInicialPanel panel = new ConfiguracionInicialPanel();

        while (true) {
            int resultado = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    I18n.get("database.config.title"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (resultado != JOptionPane.OK_OPTION) {
                return null;
            }

            String error = panel.validar();
            if (error == null) {
                return panel.crearConfiguracion();
            }
            mostrarError(error);
        }
    }

    // Escritura de solo base de datos usada antes de completar el idioma en el arranque.
    void guardarConfiguracion(Path ruta, ConfigDB configuracion)
            throws Exception {
        guardarDocumento(ruta, new AppConfig(null, configuracion), false);
    }

    /**
     * Guarda idioma y base de datos, creando los directorios y reemplazando el XML.
     * Reconstruye el documento: no conserva elementos ajenos a esta configuración.
     *
     * @param ruta destino del fichero de configuración
     * @param configuracion valores que se van a persistir, con idioma configurado
     * @throws Exception si falta el idioma, el motor no está habilitado o falla la escritura
     */
    public void guardarConfiguracionAplicacion(Path ruta, AppConfig configuracion)
            throws Exception {
        if (configuracion == null) {
            throw new IllegalArgumentException(I18n.get("config.null"));
        }
        if (!configuracion.tieneIdiomaConfigurado()) {
            throw new IOException(I18n.get("config.languageRequired"));
        }
        guardarDocumento(ruta, configuracion, true);
    }

    private void guardarDocumento(Path ruta, AppConfig configuracion,
            boolean incluirAplicacion) throws Exception {
        ConfigDB database = configuracion.getDatabase();
        validarMotorHabilitado(database.databaseType);

        Path directorio = ruta.toAbsolutePath().normalize().getParent();
        if (directorio == null) {
            throw new IOException(I18n.get("config.directoryUnknown"));
        }
        Files.createDirectories(directorio);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element configuracionXml = document.createElement("configuracion");
        document.appendChild(configuracionXml);

        if (incluirAplicacion) {
            Element aplicacion = document.createElement("aplicacion");
            configuracionXml.appendChild(aplicacion);
            agregarElemento(
                    document,
                    aplicacion,
                    "idioma",
                    configuracion.getIdioma().getCodigo()
            );
        }

        Element baseDatos = document.createElement("baseDatos");
        configuracionXml.appendChild(baseDatos);

        agregarElemento(
                document, baseDatos, "tipo",
                database.databaseType.getConfigValue()
        );
        agregarElemento(document, baseDatos, "driver", database.driver);
        agregarElemento(document, baseDatos, "url", database.url);
        agregarElemento(document, baseDatos, "usuario", database.user);
        agregarElemento(document, baseDatos, "password", database.password);
        agregarElemento(document, baseDatos, "db", database.db);

        Transformer transformer = TransformerFactory
                .newInstance()
                .newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(
                new DOMSource(document),
                new StreamResult(ruta.toFile())
        );
    }

    private DatabaseType detectarTipoBaseDatos(String url, String driver) {
        if ((url != null && url.trim().toLowerCase().startsWith("jdbc:sqlite:"))
                || (driver != null && driver.toLowerCase().contains("sqlite"))) {
            return DatabaseType.SQLITE;
        }
        return DatabaseType.MYSQL;
    }

    private void validarMotorHabilitado(DatabaseType tipo) throws IOException {
        if (tipo == null) {
            throw new IOException(I18n.get("database.validation.noEngine"));
        }
        if (!tipo.isEnabled()) {
            throw new IOException(I18n.get("database.engine.disabled", tipo));
        }
    }

    private void agregarElemento(Document document, Element padre,
            String nombre, String valor) {
        Element elemento = document.createElement(nombre);
        elemento.setTextContent(valor != null ? valor : "");
        padre.appendChild(elemento);
    }

    private String obtenerValor(Element padre, String etiqueta)
            throws IOException {
        String valor = obtenerValorOpcional(padre, etiqueta);
        if (valor == null) {
            throw new IOException(I18n.get("config.missingElement", etiqueta));
        }
        return valor;
    }

    private String obtenerValorOpcional(Element padre, String etiqueta) {
        if (padre.getElementsByTagName(etiqueta).getLength() == 0) {
            return null;
        }
        return padre.getElementsByTagName(etiqueta)
                .item(0)
                .getTextContent()
                .trim();
    }

    private String obtenerValorOpcionalConPredeterminado(
            Element padre, String etiqueta, String predeterminado) {
        String valor = obtenerValorOpcional(padre, etiqueta);
        return valor == null ? predeterminado : valor;
    }

    /**
     * Intenta bloquear DTD, entidades externas y XInclude para evitar resolver
     * recursos externos al leer el XML. Si el parser rechaza una opción, se emite
     * un aviso y continúa la lectura; las opciones posteriores no se aplican.
     */
    private void configurarParserSeguro(DocumentBuilderFactory factory) {
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (Exception e) {
            System.err.println(I18n.get("config.xmlSecurityWarning"));
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                null, mensaje, I18n.get("app.error"), JOptionPane.ERROR_MESSAGE);
    }
}
