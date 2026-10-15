package com.angelvazquez.csia.database;

public class ConfigDB {

    private DatabaseType databaseType;
    private String driver;
    private String url;
    private String db;
    private String user;
    private String password;
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

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
