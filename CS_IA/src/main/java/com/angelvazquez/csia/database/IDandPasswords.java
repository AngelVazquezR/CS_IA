package com.angelvazquez.csia.database;

import java.util.HashMap;

public class IDandPasswords {
	HashMap<String,String> logininfo = new HashMap<String,String>();
	
	public IDandPasswords(){
		logininfo.put("Hola", "Mundo");
		logininfo.put("Tu", "No");
		logininfo.put("Silksong", "Tomorrow");
		logininfo.put("", "");
	}
	
	public HashMap getLoginInfo(){
		return logininfo;
	}
	
	
	
	
}
