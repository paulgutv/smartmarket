package com.smartmarket.vista;

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
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class DialogoClave implements PedirClave {
	
	/**
     * @wbp.parser.entryPoint
     */
    @Override
    public char[][] pedir(Component padre, String titulo, boolean pedirActual) {
        Window dueno = (padre instanceof Window) ? (Window) padre
                : (padre == null ? null : SwingUtilities.getWindowAncestor(padre));
        JDialog dialogo = new JDialog(dueno, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPasswordField pwdActual = new JPasswordField(18);
        JPasswordField pwdNueva = new JPasswordField(18);
        JPasswordField pwdConfirmar = new JPasswordField(18);
        pwdActual.setName("pwdActual");
        pwdNueva.setName("pwdNueva");
        pwdConfirmar.setName("pwdConfirmar");
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnCancelar = new JButton("Cancelar");
        btnAceptar.setName("btnAceptar");
        btnCancelar.setName("btnCancelar");

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int fila = 0;
        
        if (pedirActual) { 
            gbc.gridy = fila;
            gbc.gridx = 0; formulario.add(new JLabel("Contraseña actual:"), (GridBagConstraints) gbc.clone());
            gbc.gridx = 1; formulario.add(pwdActual, (GridBagConstraints) gbc.clone());
            fila++;
        }
        
        gbc.gridy = fila;
        gbc.gridx = 0; formulario.add(new JLabel("Contraseña nueva:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(pwdNueva, (GridBagConstraints) gbc.clone());
        fila++;
        
        gbc.gridy = fila;
        gbc.gridx = 0; formulario.add(new JLabel("Confirmar contraseña:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(pwdConfirmar, (GridBagConstraints) gbc.clone());
        fila++;

        JLabel reglas = new JLabel(PoliticaClave.REGLAS);
        reglas.setForeground(Color.GRAY);
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formulario.add(reglas, gbc);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        botones.add(btnAceptar);
        botones.add(btnCancelar);

        dialogo.add(formulario, BorderLayout.CENTER);
        dialogo.add(botones, BorderLayout.SOUTH);

        char[][][] resultado = new char[1][][];
        btnAceptar.addActionListener(e -> {
            resultado[0] = new char[][]{
                pedirActual ? pwdActual.getPassword() : null,
                pwdNueva.getPassword(),
                pwdConfirmar.getPassword()};
            dialogo.dispose();
        });
        btnCancelar.addActionListener(e -> dialogo.dispose());
        dialogo.getRootPane().setDefaultButton(btnAceptar);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialogo.pack();
        dialogo.setResizable(false);
        dialogo.setLocationRelativeTo(padre);
        dialogo.setVisible(true);
        return resultado[0];
    }
}
