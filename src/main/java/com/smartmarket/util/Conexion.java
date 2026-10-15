package com.smartmarket.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class Conexion {

	private static Properties config;
	
	private Conexion() {
		
	}
	
	
	public static Connection conectar() throws SQLException {
	    Properties p = cargarConfiguracion();
	    String url = p.getProperty("db.url");
	    String usuario = p.getProperty("db.user");
	    String clave = p.getProperty("db.password", "");
	    if (url == null || url.isBlank() || usuario == null || usuario.isBlank()) {
	        throw new SQLException("db.properties debe definir db.url y db.user");
	    }
	    return DriverManager.getConnection(url, usuario, clave);
	}
	
	private static synchronized Properties cargarConfiguracion() throws SQLException {
	    if (config != null) {
	        return config;
	    }
	    try (InputStream in = Conexion.class.getResourceAsStream("/db.properties")) {
	        if (in == null) {
	            throw new SQLException("No se encontró db.properties");
	        }
	        Properties p = new Properties();
	        p.load(in);
	        config = p;
	        return config;
	    } catch (IOException e) {
	        throw new SQLException("No se pudo leer db.properties", e);
	    }
	}
}
