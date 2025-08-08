package Object;

public class Alumno extends Persona {

	private double Precio;
	
	
	public Alumno(String nombre, String apellido, String id, double precio) {
		super(nombre, apellido, id);
		Precio = precio;
		
	}
	
	
	public double GetPrecio() {
		return Precio;
	}
	
	public void SetPrecio(double APrecio) {
		Precio = APrecio;
	}
	
}
