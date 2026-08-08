package Object;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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
//	private static String db = "CSIA";
//	private static String user = "root";
//	private static String password = "12345678";
//	private static String url_ = "jdbc:mysql://localhost:3306/"+ db;
	
	private static Connection connection;
	private static Statement st = null;
	
	private static ConfigDB confDB = new ConfigDB();
	
	
	public ConectionSQL() {
		LeerConf();
		try {
			//Class.forName("com.mysql.jdbc.Driver");
			//Class.forName("com.mysql.cj.jdbc.Driver");
			Class.forName(confDB.driver);
			//connection = DriverManager.getConnection(url_,user,password);
			connection = DriverManager.getConnection(confDB.url,confDB.user,confDB.password);
			if(connection != null) {
				st = connection.createStatement();
				System.out.println("Se ha conectado con exito a la base de datos "+confDB.db);
				
			}
			else {
				System.out.println("No a sido plosible establecer conexion");
			}
		} catch (SQLException e) {e.printStackTrace();}
		catch (ClassNotFoundException e) {e.printStackTrace();}
		catch (Exception e) {e.printStackTrace();}
		
		
	}
	
	public static void Conection() throws SQLException{
		
		connection = DriverManager.getConnection(confDB.url,confDB.user,confDB.password);
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
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(archivo);
			doc.getDocumentElement().normalize();
			 Element baseDatos = (Element) doc.getElementsByTagName("baseDatos").item(0);
			 driver = baseDatos.getElementsByTagName("driver").item(0).getTextContent();
			 url = baseDatos.getElementsByTagName("url").item(0).getTextContent();
			 usuario = baseDatos.getElementsByTagName("usuario").item(0).getTextContent();
		     password = baseDatos.getElementsByTagName("password").item(0).getTextContent();
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
        System.out.println("Password: " + password);
        System.out.println("DB: " + DB);  
	}
	
	public static void AsignarProf(String profe, String alumno) {
		ResultSet rs;
		
		try {
			Conection();
			st=connection.createStatement();
			st.executeUpdate("UPDATE ALUMNOS SET PROFESOR ='"+profe+"' WHERE NOMBRE = '"+alumno+"';");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void FillAlumPop() {
		ResultSet rs;
		
		try {
			Conection();
			st=connection.createStatement();
			rs = st.executeQuery("SELECT NOMBRE AS NOM FROM ALUMNOS;");
			while(rs.next()) {
				AsignarTab.AddAlumPop(rs.getString("NOM"));
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void FillProfePop() {
		ResultSet rs;
		
		try {
			Conection();
			st=connection.createStatement();
			rs = st.executeQuery("SELECT NOMBRE AS NOM FROM PROFESORES;");
			while(rs.next()) {
				AsignarTab.AddProfePop(rs.getString("NOM"));
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void AddProfe(String nombre, String Apellido, String DNI, String fAlta, String fBaja) {
		ResultSet rs;
		String id = "";
		int count;
		
		try {
			Conection();
			st=connection.createStatement();
			rs = st.executeQuery("SELECT COUNT(*) AS NUM_PRO FROM PROFESORES;");
			while(rs.next()) {
				id = rs.getString("NUM_PRO");
				id = Algoritmos.GenerateID("PR",id);				
				/*count = Integer.parseInt(id)+1;				
				id="PR"+String.format("%04d", count);
				System.out.println(id);*/
				}
			st.executeUpdate("INSERT INTO PROFESORES VALUES('"+id+"','"+nombre+"','"+Apellido+"','"+DNI+"','"+fAlta+"','"+fBaja+"');");			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ProfeFillTable() {
		ResultSet rs;	
		try {
			Conection();
			st=connection.createStatement();
			rs = st.executeQuery("SELECT NOMBRE AS NOM,APELLIDOS AS APE,DNI AS DN, FALTA AS FA, FBAJA AS FB FROM PROFESORES");
			while(rs.next()) {
				VisualizarProfesores.AddRow(rs.getString("NOM"), rs.getString("APE"), rs.getString("DN"), rs.getString("FA"), rs.getString("FB"));
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void AlumFillTable() {
		ResultSet rs;	
		try {
			Conection();
			st=connection.createStatement();
			rs = st.executeQuery("SELECT NOMBRE AS NOM,APELLIDO AS APE,DNI AS DN,PROFESOR AS PROF FROM ALUMNOS");
			while(rs.next()) {
				VisualizarAlumnos.AddRow(rs.getString("NOM"), rs.getString("APE"),
						rs.getString("DN"), rs.getString("PROF"));
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static void ModProfe(String nombre, String Apellido, String DNI, String fAlta, String fBaja) {
		ResultSet resultSet;
		try {
			System.out.println("mod");
			Conection();
			st=connection.createStatement();
			
			st.executeUpdate("UPDATE PROFESORES SET NOMBRE ='"+nombre+"',APELLIDOS='"+Apellido+"', FALTA='"+fAlta+"', FBAJA='"+fBaja+"'WHERE DNI='"+DNI+"';");
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	public static void DeleteProfe(String nombre, String Apellido, String DNI) {
		try {
			Conection();
			st=connection.createStatement();
			st.executeUpdate("DELETE FROM PROFESORES WHERE NOMBRE='"+nombre+"'AND APELLIDOS='"+Apellido+"'AND DNI='"+DNI+"';");
			
			//st.executeUpdate("INSERT INTO ALUMNOS VALUES('"+id+"','"+nombre+"','"+Apellido+"','"+DNI+"');");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void AddAlumno(String nombre, String Apellido, String DNI) {
		ResultSet rs;
		String id = "";
		int count;
		try {
			Conection();
			st=connection.createStatement();
			rs = st.executeQuery("SELECT COUNT(*) AS NUM_PRO FROM ALUMNOS;");
			while(rs.next()) {
				id = rs.getString("NUM_PRO");
				id = Algoritmos.GenerateID("AL",id);		
				/*count = Integer.parseInt(id)+1;
				id="PR"+String.format("%04d", count);
				System.out.println(id);*/
				}
			st.executeUpdate("INSERT INTO ALUMNOS VALUES('"+id+"','"+nombre+"','"+Apellido+"','"+DNI+"');");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void ModAlumno(String nombre, String Apellido, String DNI) {
		ResultSet resultSet;
		try {
			Conection();
			st=connection.createStatement();
			
			st.executeUpdate("UPDATE ALUMNOS SET NOMBRE ='"+nombre+"',APELLIDO='"+Apellido+"'WHERE DNI='"+DNI+"';");
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}
	public static void DeleteAlumno(String nombre, String Apellido, String DNI) {
		try {
			Conection();
			st=connection.createStatement();
			st.executeUpdate("DELETE FROM ALUMNOS WHERE NOMBRE='"+nombre+"'AND APELLIDO='"+Apellido+"'AND DNI='"+DNI+"';");
			
			//st.executeUpdate("INSERT INTO ALUMNOS VALUES('"+id+"','"+nombre+"','"+Apellido+"','"+DNI+"');");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
/*
	public static void RecuperaDatos() {
		ResultSet resultSet;
		try {
			st=connection.createStatement();
			resultSet= st.executeQuery("SELECT CONTRASEÑA FROM USUARIOS");
			while(resultSet.next()) {
				System.out.println(resultSet.getString("CONTRASEÑA"));	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	*/
public static String RecuperaPassword(String user) {	
		ResultSet resultSet;
		String passwordHash="";
		try {
			Conection();
			st=connection.createStatement();
			//se hace de esta manera para evitar ataques de injeccion sql
			resultSet= st.executeQuery("SELECT CONTRASEÑA FROM USUARIOS WHERE USUARIO = '"
			+user.toUpperCase()+"';");
			
			
			while(resultSet.next()) {
				passwordHash = resultSet.getString("CONTRASEÑA");
				System.out.println(passwordHash);	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return passwordHash;
		
	}
	
	
	public static void RegistrarUsuario(String User, String Password) {
		String userMayuscula = User.toUpperCase();
		String cadenaAux="7";
		Integer numUsuarios = -1;
		String sql = "";
		ResultSet resultSet;
		System.out.println("Conectado con exito");
		try {
			Conection();
			st=connection.createStatement();
			
			sql = "SELECT COUNT(USUARIO) AS NUM_USUARIOS FROM USUARIOS WHERE USUARIO = '"+userMayuscula+"'";
			//resultSet= st.executeQuery("SELECT CONTRASEÑA FROM USUARIOS");
			resultSet = st.executeQuery(sql);
			resultSet.next();
			cadenaAux=resultSet.getString("NUM_USUARIOS");
			
			numUsuarios = Integer.parseInt(cadenaAux);
			
			//System.out.println("a");
			System.out.println("El usuario "+ User+" aparece "+numUsuarios);
			
			if(numUsuarios == 0) {
				
				sql = "INSERT INTO USUARIOS(USUARIO,CONTRASEÑA) VALUES('"+userMayuscula+"','"+Password+"')";
				st.executeUpdate(sql);
				System.out.println("Usuario registrado con exito");
			}else {
				System.out.println("El usuario "+User+" ya existe");
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
	}

	public static Boolean existeDNI(Integer type, String DNI) {
		ResultSet resultSet;
		String tDNI = "";
		try {
			Conection();
			st=connection.createStatement();
			if (type == 2) {
				resultSet= st.executeQuery("SELECT COUNT(DNI) FROM STUDENTS WHERE DNI = "+DNI+"");
				if (resultSet.getInt("DNI") != 0) {
					return true;
				}
			}else if (type == 1) {
				resultSet= st.executeQuery("SELECT COUNT(DNI) FROM TEACHERS WHERE DNI = "+DNI+"");
				if (resultSet.getInt("DNI") != 0) {
					return true;
				}
			} 
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	
	
}