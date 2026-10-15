package com.smartmarket.vista;

import com.smartmarket.dao.UsuarioException;
import java.awt.Component;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

//interfaz para pedir contraseña
public interface PedirClave {

    char[][] pedir(Component padre, String titulo, boolean pedirActual);

    @FunctionalInterface
    interface Accion {
        void aplicar(char[] actual, char[] nueva, char[] confirmacion) throws UsuarioException, SQLException;
    }

    
    default boolean solicitarYAplicar(Component padre, String titulo, boolean pedirActual, Accion accion) {
        while (true) {
            char[][] claves = pedir(padre, titulo, pedirActual);
            if (claves == null) {
                return false;
            }
            try {
                accion.aplicar(claves[0], claves[1], claves[2]);
                return true;
            } catch (UsuarioException ex) {
                JOptionPane.showMessageDialog(padre, ex.getMessage(),
                        "Contraseña no válida", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                Logger.getLogger(PedirClave.class.getName())
                        .log(Level.SEVERE, "Error de base de datos al guardar la contraseña", ex);
                JOptionPane.showMessageDialog(padre,
                        "No se pudo guardar la contraseña.\nVerifique la conexión con la base de datos.",
                        "Error de base de datos", JOptionPane.ERROR_MESSAGE);
                return false;
            } finally {
                borrar(claves);
            }
        }
    }

    // borra las contraseñas de la memoria
    static void borrar(char[]... claves) {
        for (char[] clave : claves) {
            if (clave != null) {
                Arrays.fill(clave, '\0');
            }
        }
    }
}
