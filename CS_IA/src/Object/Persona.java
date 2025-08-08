package Object;

public class Persona {

	private String Nombre;
	private String Apellido;
	private String ID;
	
	public Persona(String nombre, String apellido, String id) {
		Nombre = nombre;
		Apellido = apellido;
		ID = id;
	}
	
	public String GetNombre() {
		return Nombre;
	}
	
	public String GetApellido() {
		return Apellido;
	}
	
	public String GetID() {
		return ID;
	}
	
	public void SetNombre(String Pnombre) {
		Nombre = Pnombre;
	}
	
	public void SetApellido(String Papellido) {
		Apellido = Papellido;
	}
	
	
	
}
