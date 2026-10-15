package com.smartmarket.vista;

import com.smartmarket.dao.UsuarioDAO;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.util.Sesion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class FrmLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(FrmLogin.class.getName());
    private static final int MAX_INTENTOS = 3;

    private final JTextField txtUsuario = new JTextField(15);
    private final JPasswordField txtPassword = new JPasswordField(15);
    private final JButton btnIngresar = new JButton("INGRESAR");
    private final JButton btnSalir = new JButton("SALIR");
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private int intentos = 0;

    public FrmLogin() {
        super("SmartMarket - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);        
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void construirInterfaz() {
        JLabel titulo = new JLabel("SMARTMARKET", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        GridBagConstraints gbcLblUsuario = new GridBagConstraints();
        gbcLblUsuario.insets = new Insets(6, 6, 6, 6);
        gbcLblUsuario.anchor = GridBagConstraints.WEST;
        gbcLblUsuario.gridx = 0;
        gbcLblUsuario.gridy = 0;
        formulario.add(new JLabel("Usuario:"), gbcLblUsuario);

        GridBagConstraints gbcTxtUsuario = new GridBagConstraints();
        gbcTxtUsuario.insets = new Insets(6, 6, 6, 6);
        gbcTxtUsuario.anchor = GridBagConstraints.WEST;
        gbcTxtUsuario.gridx = 1;
        gbcTxtUsuario.gridy = 0;
        formulario.add(txtUsuario, gbcTxtUsuario);

        GridBagConstraints gbcLblPassword = new GridBagConstraints();
        gbcLblPassword.insets = new Insets(6, 6, 6, 6);
        gbcLblPassword.anchor = GridBagConstraints.WEST;
        gbcLblPassword.gridx = 0;
        gbcLblPassword.gridy = 1;
        formulario.add(new JLabel("Contraseña:"), gbcLblPassword);

        GridBagConstraints gbcTxtPassword = new GridBagConstraints();
        gbcTxtPassword.insets = new Insets(6, 6, 6, 6);
        gbcTxtPassword.anchor = GridBagConstraints.WEST;
        gbcTxtPassword.gridx = 1;
        gbcTxtPassword.gridy = 1;
        formulario.add(txtPassword, gbcTxtPassword);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botones.add(btnIngresar);
        botones.add(btnSalir);

        btnIngresar.addActionListener(e -> ingresar());
        btnSalir.addActionListener(e -> salir());

        add(titulo, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        char[] clave = txtPassword.getPassword();
        try {
            if (usuario.isEmpty() || clave.length == 0) {
                JOptionPane.showMessageDialog(this, "Ingrese su usuario y contraseña.",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Usuario u = usuarioDAO.validar(usuario, clave);
            if (u == null) {
                credencialesIncorrectas();
                return;
            }
            Sesion.iniciar(u);
            new FrmMenu().setVisible(true);
            dispose();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "Error al validar el usuario", ex);
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con la base de datos.\n"
                    + "Verifique que MySQL esté en ejecución y que db.properties sea correcto.",
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(clave, '\0'); // borra la contraseña de memoria
        }
    }

    private void credencialesIncorrectas() {
        intentos++;
        if (intentos >= MAX_INTENTOS) {
            JOptionPane.showMessageDialog(this, "Superó los " + MAX_INTENTOS + " intentos permitidos.",
                    "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        
        JOptionPane.showMessageDialog(this,
                "Usuario o contraseña incorrectos (intento " + intentos + " de " + MAX_INTENTOS + ").",
                "Error de acceso", JOptionPane.ERROR_MESSAGE);
        txtPassword.setText("");
        txtPassword.requestFocus();
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea salir del sistema?",
                "Salir", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
