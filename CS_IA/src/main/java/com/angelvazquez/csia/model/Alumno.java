package com.angelvazquez.csia.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Alumno extends Persona {

	public String profAsing;
	
	
	public Alumno(String nombre, String apellido, String DNI,String fAlta, String fBaja ,String id,String profasing) {
		super(nombre, apellido, DNI, fAlta, fBaja, id);
		profAsing = profasing;
		
	}
	
	
	public String GetProf() {
		return profAsing;
	}
	
	public void SetProf(String AprofAsing) {
		profAsing = AprofAsing;
	}
	
	public void CargaDatos(ResultSet rs) {
		try {
			super.SetNombre(rs.getString("¨NOMBRE"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
