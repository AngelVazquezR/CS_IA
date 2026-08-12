package com.angelvazquez.csia.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import com.angelvazquez.csia.database.repository.AlumnoRepository;
import com.angelvazquez.csia.database.repository.ProfesorRepository;
import com.angelvazquez.csia.database.repository.UsuarioRepository;
import com.angelvazquez.csia.ui.ventanas.AsignarTab;
import com.angelvazquez.csia.ui.ventanas.VisualizarAlumnos;
import com.angelvazquez.csia.ui.ventanas.VisualizarProfesores;

public class ConectionSQL {

	private static Connection connection;
	private static ConfigDB confDB = new ConfigDB();
	private static final DatabaseConnectionFactory CONNECTION_FACTORY =
			new DatabaseConnectionFactory();

	/**
	 * Conserva el constructor anterior para no romper llamadas existentes.
	 * La lectura del XML queda centralizada en ConfiguracionManager.
	 */
	@Deprecated
	public ConectionSQL() {
		this(loadConfiguration());
	}

	/**
	 * Recibe la configuración que ya fue cargada durante el arranque.
	 */
	public ConectionSQL(ConfigDB configuration) {
		confDB = Objects.requireNonNull(
				configuration,
				"La configuración de base de datos no puede ser null."
		);

		try {
			connection = CONNECTION_FACTORY.open(confDB);

			if (connection != null) {
				System.out.println(
						"Se ha conectado con éxito a "
								+ confDB.databaseType.getConfigValue()
								+ (confDB.db.isBlank()
										? ""
										: " (" + confDB.db + ")")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static ConfigDB loadConfiguration() {
		ConfigDB configuration =
				new ConfiguracionManager().inicializarConfiguracion();

		if (configuration == null) {
			throw new IllegalStateException(
					"La configuración de base de datos no está disponible."
			);
		}

		return configuration;
	}

	public static void Conection() throws SQLException {
		connection = CONNECTION_FACTORY.open(confDB);
	}

	public static void AsignarProf(String profe, String alumno) {
		try {
			alumnoRepository().asignarProfesor(profe, alumno);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void FillAlumPop() {
		try {
			for (String nombre : alumnoRepository().listarNombres()) {
				AsignarTab.AddAlumPop(nombre);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void FillProfePop() {
		try {
			for (String nombre : profesorRepository().listarNombres()) {
				AsignarTab.AddProfePop(nombre);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void AddProfe(String nombre, String Apellido, String DNI, String fAlta, String fBaja) {
		try {
			profesorRepository().agregar(
					nombre,
					Apellido,
					DNI,
					fAlta,
					fBaja
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void ProfeFillTable() {
		try {
			for (ProfesorRepository.ProfesorData profesor
					: profesorRepository().listar()) {
				VisualizarProfesores.AddRow(
						profesor.nombre(),
						profesor.apellidos(),
						profesor.dni(),
						profesor.fechaAlta(),
						profesor.fechaBaja()
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void AlumFillTable() {
		try {
			for (AlumnoRepository.AlumnoData alumno
					: alumnoRepository().listar()) {
				VisualizarAlumnos.AddRow(
						alumno.nombre(),
						alumno.apellido(),
						alumno.dni(),
						alumno.profesor()
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void ModProfe(String nombre, String Apellido, String DNI, String fAlta, String fBaja) {
		try {
			profesorRepository().modificar(
					nombre,
					Apellido,
					DNI,
					fAlta,
					fBaja
			);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void DeleteProfe(String nombre, String Apellido, String DNI) {
		try {
			profesorRepository().eliminar(nombre, Apellido, DNI);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void AddAlumno(String nombre, String Apellido, String DNI) {
		try {
			alumnoRepository().agregar(nombre, Apellido, DNI);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void ModAlumno(String nombre, String Apellido, String DNI) {
		try {
			alumnoRepository().modificar(nombre, Apellido, DNI);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public static void DeleteAlumno(String nombre, String Apellido, String DNI) {
		try {
			alumnoRepository().eliminar(nombre, Apellido, DNI);
		} catch (SQLException e) {
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
		try {
			return usuarioRepository().recuperarPassword(user);
		} catch (SQLException e) {
			e.printStackTrace();

			return "";
		}
	}

	public static void RegistrarUsuario(String user, String password) {
		try {
			boolean registrado =
					usuarioRepository().registrar(user, password);

			if (registrado) {
				System.out.println("Usuario registrado con exito");
			} else {
				System.out.println(
						"El usuario " + user + " ya existe"
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static UsuarioRepository usuarioRepository() {
		return new UsuarioRepository(CONNECTION_FACTORY, confDB);
	}

	private static ProfesorRepository profesorRepository() {
		return new ProfesorRepository(CONNECTION_FACTORY, confDB);
	}

	private static AlumnoRepository alumnoRepository() {
		return new AlumnoRepository(CONNECTION_FACTORY, confDB);
	}

	public static Boolean existeDNI(Integer type, String DNI) {
		try {
			if (type == 1) {
				return profesorRepository().existeDni(DNI);
			}
			if (type == 2) {
				return alumnoRepository().existeDni(DNI);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	
}
