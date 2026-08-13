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
        String tipo = obtenerValorOpcional(baseDatos, "tipo");

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
                "Configuración guardada correctamente en:\n"
                        + rutaConfiguracion.toAbsolutePath(),
                "Configuración",
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
                    "Configuración inicial de base de datos",
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

    void guardarConfiguracion(Path ruta, ConfigDB configuracion)
            throws Exception {
        Path directorio = ruta.toAbsolutePath().normalize().getParent();
        if (directorio == null) {
            throw new IOException(
                    "No se puede determinar el directorio de configuración."
            );
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
                document, baseDatos, "tipo",
                configuracion.databaseType.getConfigValue()
        );
        agregarElemento(document, baseDatos, "driver", configuracion.driver);
        agregarElemento(document, baseDatos, "url", configuracion.url);
        agregarElemento(document, baseDatos, "usuario", configuracion.user);
        agregarElemento(document, baseDatos, "password", configuracion.password);
        agregarElemento(document, baseDatos, "db", configuracion.db);

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
            throw new IOException(
                    "Falta el elemento <" + etiqueta + "> en configuracion.xml"
            );
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
            System.err.println(
                    "Advertencia: no se han podido configurar todas las opciones de seguridad XML."
            );
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                null, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
