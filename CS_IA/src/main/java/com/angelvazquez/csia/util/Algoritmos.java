package com.angelvazquez.csia.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

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
	
	public static String hashAlgorithm (String password) {
		
		if(password != null) {
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte [] bytesTexto = password.getBytes(StandardCharsets.UTF_8);
				byte [] bytesHash = digest.digest(bytesTexto);
				return HexFormat.of().formatHex(bytesHash);
				
				
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	}
		return null;
	}
	
}
