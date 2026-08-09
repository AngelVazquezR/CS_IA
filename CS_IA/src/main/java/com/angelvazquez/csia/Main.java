package com.angelvazquez.csia;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import com.angelvazquez.csia.database.ConfigDB;
import com.angelvazquez.csia.database.ConfiguracionManager;

import com.angelvazquez.csia.database.ConectionSQL;
import com.angelvazquez.csia.database.IDandPasswords;
import com.angelvazquez.csia.model.Profesor;
import com.angelvazquez.csia.model.Users;
import com.angelvazquez.csia.ui.ventanas.AsignarTab;
import com.angelvazquez.csia.ui.ventanas.GestionarTab;
import com.angelvazquez.csia.ui.ventanas.LoginPage;
import com.angelvazquez.csia.ui.ventanas.RegistarTab;
import com.angelvazquez.csia.ui.ventanas.VisualizarAlumnos;
import com.angelvazquez.csia.ui.ventanas.VisualizarProfesores;
import com.angelvazquez.csia.ui.ventanas.WelcomePage;

public class Main {
	public static Users user1;
	static Random rand = new Random();
	static HashMap<String,String> map = new LinkedHashMap();
	
	public static void main(String[] args) {
		
		// Comprobamos si existe el fichero de configuracion
		ConfiguracionManager manager = new ConfiguracionManager();
		ConfigDB configuracion = manager.inicializarConfiguracion();
		if (configuracion == null) {
            System.out.println("Arranque cancelado por falta de configuración.");

            return;
        }
		
		IDandPasswords idandpasswords = new IDandPasswords();
		ConectionSQL conectionSQL = new ConectionSQL(configuracion);
		LoginPage loginpage = new LoginPage(idandpasswords.getLoginInfo());
	}
	
	public static String randomChar(String[] array) {
		System.out.println(rand.nextInt(array.length));
		return array[rand.nextInt(array.length)];
	}
	
	public static void LogIn() {
		IDandPasswords idandpasswords = new IDandPasswords();
		LoginPage loginpage = new LoginPage(idandpasswords.getLoginInfo());
	}
	public static void Asignar() {
		AsignarTab asignarTab = new AsignarTab();
		asignarTab.setVisible(true);
	}
	public static void Gestionar() {
		GestionarTab gestionarTab = new GestionarTab();
		gestionarTab.setVisible(true);
	}
	public static void Welcome() {
		WelcomePage welcomePage = new WelcomePage();
		welcomePage.setVisible(true);
	}
	public static void AlumTabla() {
		VisualizarAlumnos vis = new VisualizarAlumnos();
		//vis.setVisible(true);
	}
	public static void ProfeTabla() {
		VisualizarProfesores v = new VisualizarProfesores();
		//vis.setVisible(true);
	}
	public static void RegistrarUser() {
		RegistarTab registrarTab = new RegistarTab();
		registrarTab.setVisible(true);
	}
		
	public static void CreateProfesor(String id, String name, String apellido, String DNI) {
		Profesor profe = new Profesor(name, apellido, id, DNI, "", "");
		map.put(profe.GetID(), profe.GetNombre());
	}

}
