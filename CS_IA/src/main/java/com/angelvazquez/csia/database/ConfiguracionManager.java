package com.angelvazquez.csia.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
//import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ConfiguracionManager {

    private static final String DIRECTORIO_CONFIG = "config";
    private static final String FICHERO_CONFIG = "configuracion.xml";
    private static final String DRIVER_SQLITE =
            ConfiguracionInicialPanel.DRIVER_SQLITE;
    private static final String URL_SQLITE_PREDETERMINADA =
            ConfiguracionInicialPanel.URL_SQLITE;

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
     * Método principal que debe llamarse durante el arranque de la aplicación.
     *
     * Si existe configuracion.xml, lo lee.
     * Si no existe, solicita los datos al usuario y genera el fichero.
     *
     * @return configuración cargada, o null si el usuario cancela
     *         o se produce un error.
     */
    public ConfigDB inicializarConfiguracion() {

        try {
            Path rutaConfiguracion = obtenerRutaConfiguracion();

            if (Files.isRegularFile(rutaConfiguracion)) {

                System.out.println(
                        "Fichero de configuración encontrado en: "
                                + rutaConfiguracion.toAbsolutePath()
                );

                return leerConfiguracion(rutaConfiguracion);
            }

            System.out.println(
                    "No existe el fichero de configuración en: "
                            + rutaConfiguracion.toAbsolutePath()
            );

            return crearNuevaConfiguracion(rutaConfiguracion);

        } catch (Exception e) {

            mostrarError(
                    "No se ha podido inicializar la configuración.\n\n"
                            + e.getMessage()
            );

            e.printStackTrace();

            return null;
        }
    }

    /**
     * Devuelve la ruta:
     *
     * <directorio-aplicacion>/config/configuracion.xml
     */
    public Path obtenerRutaConfiguracion()
            throws URISyntaxException {

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
     * Resuelve la carpeta que contiene el JAR. Durante el desarrollo,
     * localiza la raíz Maven que contiene pom.xml para evitar
     * target/classes/config.
     */
    static Path resolverDirectorioAplicacion(
            Path ubicacionCodigo,
            Path directorioTrabajo) {

        Path ubicacion =
                ubicacionCodigo.toAbsolutePath().normalize();

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

        return directorioTrabajo
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Lee configuracion.xml.
     */
    ConfigDB leerConfiguracion(Path ruta)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        configurarParserSeguro(factory);

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        Document document =
                builder.parse(ruta.toFile());

        document.getDocumentElement().normalize();

        Element baseDatos =
                (Element) document
                        .getElementsByTagName("baseDatos")
                        .item(0);

        if (baseDatos == null) {
            throw new IOException(
                    "El fichero no contiene el elemento <baseDatos>."
            );
        }

        ConfigDB configuracion = new ConfigDB();

        String tipo = obtenerValorOpcional(
                baseDatos,
                "tipo"
        );

        try {
            configuracion.databaseType =
                    DatabaseType.fromConfigValue(tipo);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage(), e);
        }

        if (configuracion.databaseType == DatabaseType.SQLITE) {
            configuracion.driver = obtenerValorOpcionalConPredeterminado(
                    baseDatos,
                    "driver",
                    DRIVER_SQLITE
            );

            configuracion.url = obtenerValorOpcionalConPredeterminado(
                    baseDatos,
                    "url",
                    URL_SQLITE_PREDETERMINADA
            );

            configuracion.user = obtenerValorOpcionalConPredeterminado(
                    baseDatos,
                    "usuario",
                    ""
            );

            configuracion.password = obtenerValorOpcionalConPredeterminado(
                    baseDatos,
                    "password",
                    ""
            );

            configuracion.db = obtenerValorOpcionalConPredeterminado(
                    baseDatos,
                    "db",
                    ""
            );
        } else {
            configuracion.driver =
                    obtenerValor(baseDatos, "driver");

            configuracion.url =
                    obtenerValor(baseDatos, "url");

            configuracion.user =
                    obtenerValor(baseDatos, "usuario");

            configuracion.password =
                    obtenerValor(baseDatos, "password");

            configuracion.db =
                    obtenerValor(baseDatos, "db");
        }

        return configuracion;
    }

    /**
     * Crea una nueva configuración solicitando los datos
     * al usuario.
     */
    private ConfigDB crearNuevaConfiguracion(
            Path rutaConfiguracion)
            throws Exception {

        ConfigDB configuracion =
                solicitarConfiguracionUsuario();

        if (configuracion == null) {
            return null;
        }

        guardarConfiguracion(
                rutaConfiguracion,
                configuracion
        );

        JOptionPane.showMessageDialog(
                null,
                "Configuración guardada correctamente en:\n"
                        + rutaConfiguracion.toAbsolutePath(),
                "Configuración",
                JOptionPane.INFORMATION_MESSAGE
        );

        return configuracion;
    }

    /**
     * Solicita al usuario los datos de conexión.
     */
    private ConfigDB solicitarConfiguracionUsuario() {
        ConfiguracionInicialPanel panel =
                new ConfiguracionInicialPanel();

        while (true) {
            int resultado = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Configuración inicial de base de datos",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (resultado != JOptionPane.OK_OPTION) {
                return null;
            }

            String errorValidacion = panel.validar();

            if (errorValidacion == null) {
                return panel.crearConfiguracion();
            }

            mostrarError(errorValidacion);
        }
    }

    /**
     * Guarda ConfigDB como XML.
     */
    void guardarConfiguracion(
            Path ruta,
            ConfigDB configuracion)
            throws Exception {

        Path directorio = ruta.toAbsolutePath().normalize().getParent();

        if (directorio == null) {
            throw new IOException(
                    "No se puede determinar el directorio de configuración."
            );
        }

        Files.createDirectories(directorio);

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        Document document =
                builder.newDocument();

        Element configuracionXml =
                document.createElement("configuracion");

        document.appendChild(configuracionXml);

        Element baseDatos =
                document.createElement("baseDatos");

        configuracionXml.appendChild(baseDatos);

        agregarElemento(
                document,
                baseDatos,
                "tipo",
                configuracion.databaseType == DatabaseType.SQLITE
                        ? "sqlite"
                        : "mysql"
        );

        agregarElemento(
                document,
                baseDatos,
                "driver",
                configuracion.driver
        );

        agregarElemento(
                document,
                baseDatos,
                "url",
                configuracion.url
        );

        agregarElemento(
                document,
                baseDatos,
                "usuario",
                configuracion.user
        );

        agregarElemento(
                document,
                baseDatos,
                "password",
                configuracion.password
        );

        agregarElemento(
                document,
                baseDatos,
                "db",
                configuracion.db
        );

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        Transformer transformer =
                transformerFactory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.INDENT,
                "yes"
        );

        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
        );

        DOMSource source =
                new DOMSource(document);

        StreamResult result =
                new StreamResult(ruta.toFile());

        transformer.transform(
                source,
                result
        );
    }

    /**
     * Añade un elemento XML.
     */
    private void agregarElemento(
            Document document,
            Element padre,
            String nombre,
            String valor) {

        Element elemento =
                document.createElement(nombre);

        elemento.setTextContent(
                valor != null ? valor : ""
        );

        padre.appendChild(elemento);
    }

    /**
     * Obtiene el contenido de una etiqueta opcional.
     */
    private String obtenerValorOpcional(
            Element padre,
            String etiqueta) {

        if (padre
                .getElementsByTagName(etiqueta)
                .getLength() == 0) {

            return null;
        }

        return padre
                .getElementsByTagName(etiqueta)
                .item(0)
                .getTextContent()
                .trim();
    }

    /**
     * Obtiene una etiqueta opcional o un valor predeterminado.
     */
    private String obtenerValorOpcionalConPredeterminado(
            Element padre,
            String etiqueta,
            String valorPredeterminado) {

        String valor = obtenerValorOpcional(
                padre,
                etiqueta
        );

        return valor == null || valor.isBlank()
                ? valorPredeterminado
                : valor;
    }

    /**
     * Obtiene el contenido de una etiqueta obligatoria.
     */
    private String obtenerValor(
            Element padre,
            String etiqueta)
            throws IOException {

        if (padre
                .getElementsByTagName(etiqueta)
                .getLength() == 0) {

            throw new IOException(
                    "Falta el elemento <"
                            + etiqueta
                            + "> en configuracion.xml"
            );
        }

        return padre
                .getElementsByTagName(etiqueta)
                .item(0)
                .getTextContent()
                .trim();
    }

    /**
     * Configuración básica de seguridad para evitar
     * que el parser XML procese entidades externas.
     */
    private void configurarParserSeguro(
            DocumentBuilderFactory factory) {

        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false
            );

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false
            );

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

        } catch (Exception e) {
            System.err.println(
                    "Advertencia: no se han podido configurar "
                            + "todas las opciones de seguridad XML."
            );
        }
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {

        JOptionPane.showMessageDialog(
                null,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
