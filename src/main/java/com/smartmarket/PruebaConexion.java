package com.smartmarket;

import com.smartmarket.util.Conexion;
import java.sql.Connection;

// un main solo para probar la conexión con la base de datos
public class PruebaConexion {
    public static void main(String[] args) {
        try (Connection cn = Conexion.conectar()) {
            System.out.println("Conexión exitosa a: " + cn.getCatalog());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}