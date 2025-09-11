package Object;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;

import Object.Ventanas.AsignarTab;
import Object.Ventanas.GestionarTab;
import Object.Ventanas.LoginPage;
import Object.Ventanas.RegistarTab;
import Object.Ventanas.VisualizarAlumnos;
import Object.Ventanas.VisualizarProfesores;
import Object.Ventanas.WelcomePage;

public class Main {

	public static Users user1;
	static Random rand = new Random();
	static HashMap<String,String> map = new LinkedHashMap();
	

	public static void main(String[] args) {
		
		IDandPasswords idandpasswords = new IDandPasswords();
		ConectionSQL conectionSQL = new ConectionSQL();
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
