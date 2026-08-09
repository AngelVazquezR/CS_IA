package com.angelvazquez.csia.model;

public class Persona {

	public String Nombre;
	public String Apellido;
	private String ID;
	public String DNI;
	public String fAlta;
	public String fBaja;

	
	
	public Persona(String nombre, String apellido, String dni, String falta, String fbaja, String id) {
		Nombre = nombre;
		Apellido = apellido;
		ID = id;
		DNI = dni;
		fAlta=falta;
		fBaja = fbaja;
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
	
	public String GetDNI() {
		return DNI;
	}
	
	public String GetfAlta() {
		return fAlta;
	}
	
	public String GetfBaja() {
		return fBaja;
	}
	
	
	public void SetNombre(String Pnombre) {
		Nombre = Pnombre;
	}
	
	public void SetApellido(String Papellido) {
		Apellido = Papellido;
	}
	
	public void SetfAlta(String falta) {
		fAlta = falta;
	}
	
	public void SetfBaja(String fbaja) {
		fBaja = fbaja;
	}
	
	
}
