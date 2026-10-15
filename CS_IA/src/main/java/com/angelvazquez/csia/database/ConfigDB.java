package com.angelvazquez.csia.database;

public class ConfigDB {

    public DatabaseType databaseType;
    public String driver;
    public String url;
    public String db;
    public String user;
    public String password;
	public static final String DRIVER_MYSQL = "com.mysql.cj.jdbc.Driver";
	public static final String URL_MYSQL = "jdbc:mysql://localhost:3306/";
	public static final String DRIVER_SQLITE = "org.sqlite.JDBC";
	public static final String NOMBRE_DB_SQLITE = "CSIA";
	public static final String PREFIJO_URL_SQLITE = "jdbc:sqlite:data/";

    public ConfigDB() {
        databaseType = DatabaseType.SQLITE;
        driver = "";
        url = "";
        db = "";
        user = "";
        password = "";
    }
}
