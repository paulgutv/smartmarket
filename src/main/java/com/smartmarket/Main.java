package com.smartmarket;

import com.smartmarket.servicio.UsuarioServicio;
import com.smartmarket.vista.DialogoPrimerAdmin;
import com.smartmarket.vista.FrmLogin;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ex) {
            LOG.log(Level.WARNING, "No se pudo aplicar el aspecto del sistema. Se usa el predeterminado.", ex);
        }
        SwingUtilities.invokeLater(Main::iniciar);
    }

    // para la primera vez que se usa el sistema pide crear el administrador   
    private static void iniciar() {
        try {
            if (new UsuarioServicio().sinUsuarios()) {
                JOptionPane.showMessageDialog(null,
                        "Bienvenido a SmartMarket.\nEs la primera vez que se usa el sistema: "
                        + "cree ahora el usuario administrador.",
                        "Bienvenido", JOptionPane.INFORMATION_MESSAGE);
                if (!DialogoPrimerAdmin.mostrar(null)) {
                    System.exit(0); // sin administrador no se puede usar el sistema
                }
            }
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "No se pudo consultar la base de datos al iniciar", ex);
            JOptionPane.showMessageDialog(null,
                    "No se pudo conectar con la base de datos.\n"
                    + "Verifique que MySQL esté en ejecución y que db.properties sea correcto,\n"
                    + "y vuelva a abrir la aplicación.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        new FrmLogin().setVisible(true);
    }
}
