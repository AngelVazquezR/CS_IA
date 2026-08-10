package com.angelvazquez.csia.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfiguracionManager {

    private static final String DIRECTORIO_CONFIG = "config";
    private static final String FICHERO_CONFIG = "configuracion.xml";
    private static final String DRIVER_SQLITE = "org.sqlite.JDBC";
    private static final String URL_SQLITE_PREDETERMINADA =
            "jdbc:sqlite:data/CSIA.db";

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
     * Carga la configuración externa durante el arranque. Si todavía no existe,
     * solicita los datos al usuario y la crea en el mismo directorio de la
     * aplicación.
     */
    public ConfigDB inicializarConfiguracion() {
        try {
            Path rutaConfiguracion = obtenerRutaConfiguracion();

            if (Files.isRegularFile(rutaConfiguracion)) {
                System.out.println(
                        "Fichero de configuración encontrado en: "
                                + rutaConfiguracion.toAbsolutePath());
                return leerConfiguracion(rutaConfiguracion);
            }

            System.out.println(
                    "No existe el fichero de configuración en: "
                            + rutaConfiguracion.toAbsolutePath());
            return crearNuevaConfiguracion(rutaConfiguracion);
        } catch (Exception e) {
            mostrarError(
                    "No se ha podido inicializar la configuración.\n\n"
                            + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Devuelve &lt;directorio-aplicacion&gt;/config/configuracion.xml.
     *
     * Desde un JAR, el directorio de aplicación es la carpeta que contiene el
     * JAR. Desde Eclipse/Maven se localiza la raíz del proyecto que contiene el
     * pom.xml, en lugar de utilizar target/classes.
     */
    public Path obtenerRutaConfiguracion() throws URISyntaxException {
        Path base = directorioAplicacion;
        if (base == null) {
            Path ubicacionCodigo = Paths.get(
                    ConfiguracionManager.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());
            base = resolverDirectorioAplicacion(
                    ubicacionCodigo,
                    Paths.get(System.getProperty("user.dir")));
        }

        return base.resolve(DIRECTORIO_CONFIG)
                .resolve(FICHERO_CONFIG)
                .toAbsolutePath()
                .normalize();
    }

    static Path resolverDirectorioAplicacion(
            Path ubicacionCodigo,
            Path directorioTrabajo) {

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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        configurarParserSeguro(factory);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(ruta.toFile());
        document.getDocumentElement().normalize();

        Element baseDatos = (Element) document
                .getElementsByTagName("baseDatos")
                .item(0);
        if (baseDatos == null) {
            throw new IOException(
                    "El fichero no contiene el elemento <baseDatos>.");
        }

        ConfigDB configuracion = new ConfigDB();
        String tipo = obtenerValorOpcional(baseDatos, "tipo");
        try {
            configuracion.databaseType = DatabaseType.fromConfigValue(tipo);
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage(), e);
        }

        if (configuracion.databaseType == DatabaseType.SQLITE) {
            configuracion.driver = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "driver", DRIVER_SQLITE);
            configuracion.url = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "url", URL_SQLITE_PREDETERMINADA);
            configuracion.user = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "usuario", "");
            configuracion.password = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "password", "");
            configuracion.db = obtenerValorOpcionalConPredeterminado(
                    baseDatos, "db", "");
        } else {
            configuracion.driver = obtenerValor(baseDatos, "driver");
            configuracion.url = obtenerValor(baseDatos, "url");
            configuracion.user = obtenerValor(baseDatos, "usuario");
            configuracion.password = obtenerValor(baseDatos, "password");
            configuracion.db = obtenerValor(baseDatos, "db");
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
                "Configuración guardada correctamente en:\n"
                        + rutaConfiguracion.toAbsolutePath(),
                "Configuración",
                JOptionPane.INFORMATION_MESSAGE);
        return configuracion;
    }

    private ConfigDB solicitarConfiguracionUsuario() {
        JTextField campoDriver = new JTextField("com.mysql.cj.jdbc.Driver");
        JTextField campoUrl = new JTextField("jdbc:mysql://localhost:3306/");
        JTextField campoDB = new JTextField();
        JTextField campoUsuario = new JTextField();
        JPasswordField campoPassword = new JPasswordField();

        Object[] campos = {
                "Driver JDBC:", campoDriver,
                "URL:", campoUrl,
                "Base de datos:", campoDB,
                "Usuario:", campoUsuario,
                "Contraseña:", campoPassword
        };

        int resultado = JOptionPane.showConfirmDialog(
                null,
                campos,
                "Configuración inicial de base de datos",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }

        String driver = campoDriver.getText().trim();
        String url = campoUrl.getText().trim();
        String db = campoDB.getText().trim();
        String usuario = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());

        if (driver.isBlank() || url.isBlank()
                || db.isBlank() || usuario.isBlank()) {
            mostrarError(
                    "Driver, URL, base de datos y usuario son obligatorios.");
            return solicitarConfiguracionUsuario();
        }

        ConfigDB configuracion = new ConfigDB();
        configuracion.databaseType = DatabaseType.MYSQL;
        configuracion.driver = driver;
        configuracion.url = url;
        configuracion.db = db;
        configuracion.user = usuario;
        configuracion.password = password;
        return configuracion;
    }

    void guardarConfiguracion(Path ruta, ConfigDB configuracion)
            throws Exception {

        Path directorio = ruta.toAbsolutePath().normalize().getParent();
        if (directorio == null) {
            throw new IOException(
                    "No se puede determinar el directorio de configuración.");
        }
        Files.createDirectories(directorio);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element configuracionXml = document.createElement("configuracion");
        document.appendChild(configuracionXml);
        Element baseDatos = document.createElement("baseDatos");
        configuracionXml.appendChild(baseDatos);

        agregarElemento(
                document,
                baseDatos,
                "tipo",
                configuracion.databaseType == DatabaseType.SQLITE
                        ? "sqlite" : "mysql");
        agregarElemento(document, baseDatos, "driver", configuracion.driver);
        agregarElemento(document, baseDatos, "url", configuracion.url);
        agregarElemento(document, baseDatos, "usuario", configuracion.user);
        agregarElemento(
                document, baseDatos, "password", configuracion.password);
        agregarElemento(document, baseDatos, "db", configuracion.db);

        Transformer transformer = TransformerFactory
                .newInstance()
                .newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(
                new DOMSource(document),
                new StreamResult(ruta.toFile()));
    }

    private void agregarElemento(
            Document document,
            Element padre,
            String nombre,
            String valor) {

        Element elemento = document.createElement(nombre);
        elemento.setTextContent(valor != null ? valor : "");
        padre.appendChild(elemento);
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
            Element padre,
            String etiqueta,
            String valorPredeterminado) {

        String valor = obtenerValorOpcional(padre, etiqueta);
        return valor == null || valor.isBlank()
                ? valorPredeterminado : valor;
    }

    private String obtenerValor(Element padre, String etiqueta)
            throws IOException {

        if (padre.getElementsByTagName(etiqueta).getLength() == 0) {
            throw new IOException(
                    "Falta el elemento <" + etiqueta
                            + "> en configuracion.xml");
        }
        return padre.getElementsByTagName(etiqueta)
                .item(0)
                .getTextContent()
                .trim();
    }

    private void configurarParserSeguro(DocumentBuilderFactory factory) {
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (Exception e) {
            System.err.println(
                    "Advertencia: no se han podido configurar "
                            + "todas las opciones de seguridad XML.");
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
