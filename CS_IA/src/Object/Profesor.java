package Object;
import java.time.zone.*;

public class Profesor extends Persona {
	
	public String DNI;
	public double[] F_Alta = new double[3];
	public double[] F_Baja = new double[3];
	
	
	public Profesor(String nombre, String apellido, String id, String dni, double[] f_Alta, double[] f_Baja) {
		super(nombre, apellido, id);
		DNI = dni;
		F_Alta[1] = f_Alta[1];
		F_Alta[2] = f_Alta[2];
		F_Alta[3] = f_Alta[3];
		F_Baja[1] = f_Baja[1];
		F_Baja[2] = f_Baja[2];
		F_Baja[3] = f_Baja[3];
		
	}
	
	public String GetDNI() {
		return DNI;
	}
	
	public double[] GetF_Alta() {
		return F_Alta;
	}
	
	public double[] GetF_Baja() {
		return F_Baja;
	}
	
	public void SetF_Alta(double[] f_alta) {
		F_Alta[1] = f_alta[1];
		F_Alta[2] = f_alta[2];
		F_Alta[3] = f_alta[3];
		
	}
	
	public void SetF_Baja(double[] f_baja) {
		F_Baja[1] = f_baja[1];
		F_Baja[2] = f_baja[2];
		F_Baja[3] = f_baja[3];
		
	}

}
