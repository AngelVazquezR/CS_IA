package Object;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import Object.Utilidades.Algoritmos;
import Object.Ventanas.AsignarTab;
import Object.Ventanas.VisualizarProfesores;
import Object.Ventanas.VisualizarAlumnos;

import org.w3c.dom.Document;

public class ConectionSQL {
	private static final ConfigDB confDB = new ConfigDB();

	public ConectionSQL() {
		LeerConf();
		try {
			Class.forName(confDB.driver);
			try (Connection connection = openConnection()) {
				System.out.println("Se ha conectado con exito a la base de datos " + confDB.db);
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	private static Connection openConnection() throws SQLException {
		return DriverManager.getConnection(confDB.url, confDB.user, confDB.password);
	}
	
	public void LeerConf() {
		String driver = "";
		String url = "";
		String usuario = "";
		String password = "";
		String DB = "";
		
		File archivo = new File("bin/Assets/configuracion.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
		try {
			dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbFactory.setXIncludeAware(false);
			dbFactory.setExpandEntityReferences(false);
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(archivo);
			doc.getDocumentElement().normalize();
			 Element baseDatos = (Element) doc.getElementsByTagName("baseDatos").item(0);
			 driver = baseDatos.getElementsByTagName("driver").item(0).getTextContent();
			 url = baseDatos.getElementsByTagName("url").item(0).getTextContent();
			 usuario = baseDatos.getElementsByTagName("usuario").item(0).getTextContent();
		     password = System.getenv("CSIA_DB_PASSWORD");
		     if (password == null || password.isBlank()) {
		     	throw new IllegalStateException("Define la variable de entorno CSIA_DB_PASSWORD");
		     }
		     DB = baseDatos.getElementsByTagName("db").item(0).getTextContent();
		     
		     confDB.driver = driver;
		     confDB.url = url;
		     confDB.db = DB;
		     confDB.user = usuario;
		     confDB.password = password;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Mostramos los valores leidos
		System.out.println("Driver: " + driver);
		System.out.println("URL: " + url);
        System.out.println("Usuario: " + usuario);
        System.out.println("DB: " + DB);  
	}
	
	public static void AsignarProf(String profe, String alumno) {
		String sql = "UPDATE ALUMNOS SET PROFESOR = ? WHERE NOMBRE = ?";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, profe);
			statement.setString(2, alumno);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void FillAlumPop() {
		String sql = "SELECT NOMBRE AS NOM FROM ALUMNOS";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				AsignarTab.AddAlumPop(resultSet.getString("NOM"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void FillProfePop() {
		String sql = "SELECT NOMBRE AS NOM FROM PROFESORES";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				AsignarTab.AddProfePop(resultSet.getString("NOM"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void AddProfe(String nombre, String apellido, String dni, String fAlta, String fBaja) {
		String countSql = "SELECT COUNT(*) AS NUM_PRO FROM PROFESORES";
		String insertSql = "INSERT INTO PROFESORES (ID, NOMBRE, APELLIDOS, DNI, FALTA, FBAJA) VALUES (?, ?, ?, ?, ?, ?)";
		try (Connection connection = openConnection();
			 PreparedStatement countStatement = connection.prepareStatement(countSql);
			 ResultSet resultSet = countStatement.executeQuery()) {
			if (!resultSet.next()) {
				throw new SQLException("No se pudo generar el ID del profesor");
			}
			String id = Algoritmos.GenerateID("PR", resultSet.getString("NUM_PRO"));
			try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
				insertStatement.setString(1, id);
				insertStatement.setString(2, nombre);
				insertStatement.setString(3, apellido);
				insertStatement.setString(4, dni);
				insertStatement.setString(5, fAlta);
				insertStatement.setString(6, fBaja);
				insertStatement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void ProfeFillTable() {
		String sql = "SELECT NOMBRE AS NOM, APELLIDOS AS APE, DNI AS DN, FALTA AS FA, FBAJA AS FB FROM PROFESORES";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				VisualizarProfesores.AddRow(resultSet.getString("NOM"), resultSet.getString("APE"),
						resultSet.getString("DN"), resultSet.getString("FA"), resultSet.getString("FB"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void AlumFillTable() {
		String sql = "SELECT NOMBRE AS NOM, APELLIDO AS APE, DNI AS DN, PROFESOR AS PROF FROM ALUMNOS";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {
			while (resultSet.next()) {
				VisualizarAlumnos.AddRow(resultSet.getString("NOM"), resultSet.getString("APE"),
						resultSet.getString("DN"), resultSet.getString("PROF"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void ModProfe(String nombre, String apellido, String dni, String fAlta, String fBaja) {
		String sql = "UPDATE PROFESORES SET NOMBRE = ?, APELLIDOS = ?, FALTA = ?, FBAJA = ? WHERE DNI = ?";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, nombre);
			statement.setString(2, apellido);
			statement.setString(3, fAlta);
			statement.setString(4, fBaja);
			statement.setString(5, dni);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void DeleteProfe(String nombre, String apellido, String dni) {
		String sql = "DELETE FROM PROFESORES WHERE NOMBRE = ? AND APELLIDOS = ? AND DNI = ?";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, nombre);
			statement.setString(2, apellido);
			statement.setString(3, dni);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void AddAlumno(String nombre, String apellido, String dni) {
		String countSql = "SELECT COUNT(*) AS NUM_ALUMNOS FROM ALUMNOS";
		String insertSql = "INSERT INTO ALUMNOS (ID, NOMBRE, APELLIDO, DNI) VALUES (?, ?, ?, ?)";
		try (Connection connection = openConnection();
			 PreparedStatement countStatement = connection.prepareStatement(countSql);
			 ResultSet resultSet = countStatement.executeQuery()) {
			if (!resultSet.next()) {
				throw new SQLException("No se pudo generar el ID del alumno");
			}
			String id = Algoritmos.GenerateID("AL", resultSet.getString("NUM_ALUMNOS"));
			try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
				insertStatement.setString(1, id);
				insertStatement.setString(2, nombre);
				insertStatement.setString(3, apellido);
				insertStatement.setString(4, dni);
				insertStatement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void ModAlumno(String nombre, String apellido, String dni) {
		String sql = "UPDATE ALUMNOS SET NOMBRE = ?, APELLIDO = ? WHERE DNI = ?";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, nombre);
			statement.setString(2, apellido);
			statement.setString(3, dni);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void DeleteAlumno(String nombre, String apellido, String dni) {
		String sql = "DELETE FROM ALUMNOS WHERE NOMBRE = ? AND APELLIDO = ? AND DNI = ?";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, nombre);
			statement.setString(2, apellido);
			statement.setString(3, dni);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
public static String RecuperaPassword(String user) {
		String sql = "SELECT CONTRASEÑA FROM USUARIOS WHERE USUARIO = ?";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, user.trim().toUpperCase());
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? resultSet.getString("CONTRASEÑA") : "";
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static void RegistrarUsuario(String user, String password) {
		String normalizedUser = user.trim().toUpperCase();
		String existsSql = "SELECT 1 FROM USUARIOS WHERE USUARIO = ? LIMIT 1";
		String insertSql = "INSERT INTO USUARIOS (USUARIO, CONTRASEÑA) VALUES (?, ?)";
		try (Connection connection = openConnection();
			 PreparedStatement existsStatement = connection.prepareStatement(existsSql)) {
			existsStatement.setString(1, normalizedUser);
			try (ResultSet resultSet = existsStatement.executeQuery()) {
				if (resultSet.next()) {
					System.out.println("El usuario " + normalizedUser + " ya existe");
					return;
				}
			}
			try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
				insertStatement.setString(1, normalizedUser);
				insertStatement.setString(2, password);
				insertStatement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Boolean existeDNI(Integer type, String dni) {
		String table;
		if (type == 1) {
			table = "PROFESORES";
		} else if (type == 2) {
			table = "ALUMNOS";
		} else {
			throw new IllegalArgumentException("Tipo de persona no valido: " + type);
		}
		String sql = "SELECT 1 FROM " + table + " WHERE DNI = ? LIMIT 1";
		try (Connection connection = openConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, dni);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
		
	
	
}