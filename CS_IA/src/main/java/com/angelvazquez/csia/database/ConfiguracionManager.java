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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ConfiguracionManager {

    private static final String DIRECTORIO_CONFIG = "config";
    private static final String FICHERO_CONFIG = "configuracion.xml";

    public ConfigDB inicializarConfiguracion() {
        try {
            Path rutaConfiguracion = obtenerRutaConfiguracion();

            if (Files.exists(rutaConfiguracion)) {
                System.out.println(
                        "Fichero de configuración encontrado en: "
                                + rutaConfiguracion.toAbsolutePath()
                );
                return leerConfiguracion(rutaConfiguracion);
            }

            System.out.println("No existe el fichero de configuración.");
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

    public Path obtenerRutaConfiguracion() throws URISyntaxException {
        Path rutaAplicacion = Paths.get(
                ConfiguracionManager.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
        );

        if (Files.isRegularFile(rutaAplicacion)) {
            rutaAplicacion = rutaAplicacion.getParent();
        }

        return rutaAplicacion
                .resolve(DIRECTORIO_CONFIG)
                .resolve(FICHERO_CONFIG);
    }

    private ConfigDB leerConfiguracion(Path ruta) throws Exception {
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
                    "El fichero no contiene el elemento <baseDatos>."
            );
        }

        ConfigDB configuracion = new ConfigDB();
        configuracion.driver = obtenerValor(baseDatos, "driver");
        configuracion.url = obtenerValor(baseDatos, "url");
        configuracion.user = obtenerValor(baseDatos, "usuario");
        configuracion.password = obtenerValor(baseDatos, "password");
        configuracion.db = obtenerValor(baseDatos, "db");
        configuracion.databaseType = detectarTipoBaseDatos(
                configuracion.url,
                configuracion.driver
        );

        return configuracion;
    }

    private ConfigDB crearNuevaConfiguracion(Path rutaConfiguracion)
            throws Exception {
        Path directorio = rutaConfiguracion.getParent();

        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

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
                JOptionPane.INFORMATION_MESSAGE
        );

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
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }

        String driver = campoDriver.getText().trim();
        String url = campoUrl.getText().trim();
        String db = campoDB.getText().trim();
        String usuario = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());

        DatabaseType databaseType = detectarTipoBaseDatos(url, driver);
        boolean sqlite = databaseType == DatabaseType.SQLITE;

        if (driver.isBlank()
                || url.isBlank()
                || db.isBlank()
                || (!sqlite && usuario.isBlank())) {
            mostrarError(
                    sqlite
                            ? "Driver, URL y base de datos son obligatorios."
                            : "Driver, URL, base de datos y usuario son obligatorios."
            );
            return solicitarConfiguracionUsuario();
        }

        ConfigDB configuracion = new ConfigDB();
        configuracion.databaseType = databaseType;
        configuracion.driver = driver;
        configuracion.url = url;
        configuracion.db = db;
        configuracion.user = usuario;
        configuracion.password = password;
        return configuracion;
    }

    private DatabaseType detectarTipoBaseDatos(String url, String driver) {
        if ((url != null && url.trim().toLowerCase().startsWith("jdbc:sqlite:"))
                || (driver != null && driver.toLowerCase().contains("sqlite"))) {
            return DatabaseType.SQLITE;
        }
        return DatabaseType.MYSQL;
    }

    private void guardarConfiguracion(Path ruta, ConfigDB configuracion)
            throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element configuracionXml = document.createElement("configuracion");
        document.appendChild(configuracionXml);

        Element baseDatos = document.createElement("baseDatos");
        configuracionXml.appendChild(baseDatos);

        agregarElemento(document, baseDatos, "driver", configuracion.driver);
        agregarElemento(document, baseDatos, "url", configuracion.url);
        agregarElemento(document, baseDatos, "usuario", configuracion.user);
        agregarElemento(document, baseDatos, "password", configuracion.password);
        agregarElemento(document, baseDatos, "db", configuracion.db);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4"
        );

        transformer.transform(
                new DOMSource(document),
                new StreamResult(ruta.toFile())
        );
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

    private String obtenerValor(Element padre, String etiqueta)
            throws IOException {
        if (padre.getElementsByTagName(etiqueta).getLength() == 0) {
            throw new IOException(
                    "Falta el elemento <" + etiqueta + "> en configuracion.xml"
            );
        }

        return padre
                .getElementsByTagName(etiqueta)
                .item(0)
                .getTextContent()
                .trim();
    }

    private void configurarParserSeguro(DocumentBuilderFactory factory) {
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

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                null,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
