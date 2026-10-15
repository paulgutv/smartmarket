package com.smartmarket.vista;

import com.smartmarket.dao.UsuarioException;
import com.smartmarket.servicio.UsuarioServicio;
import com.smartmarket.util.PoliticaClave;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

// crea al primer administrador
public final class DialogoPrimerAdmin {

    private static final Logger LOG = Logger.getLogger(DialogoPrimerAdmin.class.getName());

    private DialogoPrimerAdmin() {
    }

    // retorna true si se creó el administrador, false si el usuario decidió salir
    /**
     * @wbp.parser.entryPoint
     */
    public static boolean mostrar(Component padre) {
        UsuarioServicio servicio = new UsuarioServicio();
        Window dueno = (padre instanceof Window) ? (Window) padre
                : (padre == null ? null : SwingUtilities.getWindowAncestor(padre));
        JDialog dialogo = new JDialog(dueno, "SmartMarket - Configuración inicial", Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextField txtUsuario = new JTextField(20);
        JTextField txtNombre = new JTextField(20);
        JPasswordField pwdClave = new JPasswordField(20);
        JPasswordField pwdConfirmar = new JPasswordField(20);
        txtUsuario.setName("txtUsuario");
        txtNombre.setName("txtNombre");
        pwdClave.setName("pwdClave");
        pwdConfirmar.setName("pwdConfirmar");
        JButton btnCrear = new JButton("Crear administrador");
        JButton btnSalir = new JButton("Salir");
        btnCrear.setName("btnCrear");
        btnSalir.setName("btnSalir");

        JLabel intro = new JLabel("<html>No hay usuarios registrados. Cree el administrador del sistema:<br>"
                + "con esa cuenta podrá crear los demás usuarios.</html>");
        intro.setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 20));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        gbc.gridx = 0; formulario.add(new JLabel("Usuario:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(txtUsuario, (GridBagConstraints) gbc.clone());

        gbc.gridy = 1;
        gbc.gridx = 0; formulario.add(new JLabel("Nombre completo:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(txtNombre, (GridBagConstraints) gbc.clone());

        gbc.gridy = 2;
        gbc.gridx = 0; formulario.add(new JLabel("Contraseña:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(pwdClave, (GridBagConstraints) gbc.clone());

        gbc.gridy = 3;
        gbc.gridx = 0; formulario.add(new JLabel("Confirmar contraseña:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(pwdConfirmar, (GridBagConstraints) gbc.clone());

        JLabel reglas = new JLabel(PoliticaClave.REGLAS);
        reglas.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        formulario.add(reglas, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        botones.add(btnCrear);
        botones.add(btnSalir);

        dialogo.add(intro, BorderLayout.NORTH);
        dialogo.add(formulario, BorderLayout.CENTER);
        dialogo.add(botones, BorderLayout.SOUTH);

        boolean[] creado = new boolean[1];
        btnCrear.addActionListener(e -> {
            char[] clave = pwdClave.getPassword();
            char[] confirmacion = pwdConfirmar.getPassword();
            try {
                servicio.crearPrimerAdministrador(txtUsuario.getText(), txtNombre.getText(), clave, confirmacion);
                JOptionPane.showMessageDialog(dialogo,
                        "Administrador creado correctamente.\nYa puede iniciar sesión con esa cuenta.",
                        "Configuración inicial", JOptionPane.INFORMATION_MESSAGE);
                creado[0] = true;
                dialogo.dispose();
            } catch (UsuarioException ex) {
                JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                LOG.log(Level.SEVERE, "Error al crear el administrador inicial", ex);
                JOptionPane.showMessageDialog(dialogo,
                        "No se pudo guardar el usuario.\nVerifique la conexión con la base de datos.",
                        "Error de base de datos", JOptionPane.ERROR_MESSAGE);
            } finally {
                PedirClave.borrar(clave, confirmacion);
            }
        });
        btnSalir.addActionListener(e -> dialogo.dispose());
        dialogo.getRootPane().setDefaultButton(btnCrear);

        dialogo.pack();
        dialogo.setResizable(false);
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true); // se queda aquí hasta que se cree el administrador o se salga
        return creado[0];
    }
}
