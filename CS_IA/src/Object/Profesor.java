package Object;
import java.time.*;

public class Profesor extends Persona {
	
	
	public LocalTime F_Alta = LocalTime.now();
	public LocalTime F_Baja = LocalTime.now();
	
	
	public Profesor(String nombre, String apellido, String dni,String falta,String fbaja, String id) {
		super(nombre, apellido, dni, falta, fbaja, id);	
	}
	
	public LocalTime GetF_Alta() {
		return F_Alta;
	}
	
	public LocalTime GetF_Baja() {
		return F_Baja;
	}
	
	public void SetF_Alta(LocalTime f_alta) {
		F_Alta = f_alta;
	}
	
	public void SetF_Baja(LocalTime f_baja) {
		F_Baja = f_baja;
		
	}

}
