package com.angelvazquez.csia.database;

public class ConfigDB {

	public DatabaseType databaseType;
	public String driver;
	public String url;
	public String db;
	public String user;
	public String password;
	
	
	public ConfigDB() {
		databaseType = DatabaseType.MYSQL;
		driver = "";
		url  = "";
		db = "";
		user = "";
		password = "";
		
	}
}
