package Object.Utilidades;

public class Algoritmos {

	public static String GenerateID(String cadena, int sequencial) {
		int secuancial_num;
		secuancial_num = sequencial+1;
		
		cadena=cadena+String.format("%04d", secuancial_num);
		System.out.println(cadena);
		return cadena;
	}
	
	public static String GenerateID(String cadena, String sequencial) {
		int secuancial_num;
		secuancial_num = Integer.parseInt(sequencial)+1;
		
		cadena=cadena+String.format("%04d", secuancial_num);
		System.out.println(cadena);
		return cadena;
	}
}
