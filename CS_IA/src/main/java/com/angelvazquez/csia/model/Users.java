package com.angelvazquez.csia.model;

public class Users {
	private static String User;
	private static String Pasword;
	
	public Users(String user, String pasword) {
		Pasword = pasword;
		User = user;
	}
	
	public static  String GetUser() {
		return User;
		
	}
	
	public static String GetPasword() {
		return Pasword;
	}
	
	public void SetUser(String user) {
		User = user;
	}
	
	public void SetPasword(String pasword) {
		Pasword = pasword;
	}

}
