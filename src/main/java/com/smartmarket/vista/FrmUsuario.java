package com.smartmarket.vista;

import com.smartmarket.dao.UsuarioException;
import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.servicio.UsuarioServicio;
import com.smartmarket.util.PoliticaClave;
import com.smartmarket.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class FrmUsuario extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FrmUsuario.class.getName());

    private final UsuarioServicio servicio = new UsuarioServicio();
    private final PedirClave pedirClave;
    private final Usuario actor;

    private final JTextField txtUsuario = new JTextField(16);
    private final JTextField txtNombre = new JTextField(26);
    private final JComboBox<Rol> cboRol = new JComboBox<>(Rol.values());
    private final JPasswordField pwdClave = new JPasswordField(16);
    private final JPasswordField pwdConfirmar = new JPasswordField(16);
    private final JLabel lblNota = new JLabel(" ");

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEstado = new JButton("Desactivar");
    private final JButton btnClave = new JButton("Restablecer contraseña");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Usuario", "Nombre completo", "Rol", "Estado"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private List<Usuario> usuarios = new ArrayList<>();
    private Usuario seleccionado;  

    public FrmUsuario() {
        this(new DialogoClave());
    }

  
    public FrmUsuario(PedirClave pedirClave) {
        super("SmartMarket - Usuarios");
        this.pedirClave = pedirClave;
        actor = validarPermiso();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterfaz();
        pack();
        setLocationRelativeTo(null);
        recargar();
        nuevo();
    }

    private Usuario validarPermiso() {
        Usuario u = Sesion.getUsuario();
        if (u == null || u.getRol() != Rol.ADMINISTRADOR) {
            throw new IllegalStateException("El usuario no tiene permiso para gestionar usuarios.");
        }
        return u;
    }


    private void construirInterfaz() {
    	JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del usuario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 1;

        gbc.gridy = 0;
        gbc.gridx = 0; formulario.add(new JLabel("Usuario:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(txtUsuario, (GridBagConstraints) gbc.clone());

        gbc.gridx = 2; formulario.add(new JLabel("Nombre completo:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 3; formulario.add(txtNombre, (GridBagConstraints) gbc.clone());

        gbc.gridy = 1;
        gbc.gridx = 0; formulario.add(new JLabel("Rol:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(cboRol, (GridBagConstraints) gbc.clone());

        gbc.gridy = 2;
        gbc.gridx = 0; formulario.add(new JLabel("Contraseña:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 1; formulario.add(pwdClave, (GridBagConstraints) gbc.clone());

        gbc.gridx = 2; formulario.add(new JLabel("Confirmar contraseña:"), (GridBagConstraints) gbc.clone());
        gbc.gridx = 3; formulario.add(pwdConfirmar, (GridBagConstraints) gbc.clone());

        lblNota.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        formulario.add(lblNota, (GridBagConstraints) gbc.clone());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEstado);
        botones.add(btnClave);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderizadorInactivos());
        tabla.getColumnModel().getColumn(0).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(280);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(700, 230));

        JLabel leyenda = new JLabel("En gris: usuarios desactivados (no pueden iniciar sesión).");
        leyenda.setForeground(Color.GRAY);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(leyenda, BorderLayout.SOUTH);

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnEstado.addActionListener(e -> cambiarEstado());
        btnClave.addActionListener(e -> restablecerClave());

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    // en gris los usuarios desactivados
    private class RenderizadorInactivos extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            boolean inactivo = fila < usuarios.size() && !usuarios.get(fila).isActivo();
            if (!seleccionada) {
                c.setForeground(inactivo ? Color.GRAY : t.getForeground());
            }
            return c;
        }
    }


    private void recargar() {
        try {
            usuarios = servicio.listar();
        } catch (SQLException ex) {
            errorBD("cargar los usuarios", ex);
            return;
        }
        modeloTabla.setRowCount(0);
        for (Usuario u : usuarios) {
            modeloTabla.addRow(new Object[]{u.getNombreUsuario(), u.getNombreCompleto(), u.getRol(),
                u.isActivo() ? "Activo" : "Inactivo"});
        }
    }

    private void nuevo() {
        seleccionado = null;
        tabla.clearSelection();
        txtUsuario.setText("");
        txtUsuario.setEditable(true);
        txtNombre.setText("");
        cboRol.setSelectedItem(Rol.VENTAS);
        cboRol.setEnabled(true);
        limpiarClaves();
        pwdClave.setEnabled(true);
        pwdConfirmar.setEnabled(true);
        pwdClave.setBackground(UIManager.getColor("PasswordField.background"));
        pwdConfirmar.setBackground(UIManager.getColor("PasswordField.background"));
        btnEstado.setEnabled(false);
        btnEstado.setText("Desactivar");
        btnClave.setEnabled(false);
        lblNota.setText("<html>" + PoliticaClave.REGLAS + " El usuario se escribe en minúsculas, sin espacios ni tildes.</html>");
        txtUsuario.requestFocusInWindow();
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= usuarios.size()) {
            return;
        }
        seleccionado = usuarios.get(fila);
        boolean esElMismo = seleccionado.getId() == actor.getId();
        txtUsuario.setText(seleccionado.getNombreUsuario());
        txtUsuario.setEditable(false);          // el nombre de usuario no se cambia
        txtNombre.setText(seleccionado.getNombreCompleto());
        cboRol.setSelectedItem(seleccionado.getRol());
        cboRol.setEnabled(!esElMismo);           // nadie puede cambiar su propio rol
        limpiarClaves();
        pwdClave.setEnabled(false);              
        pwdConfirmar.setEnabled(false);
        pwdClave.setBackground(new Color(235, 235, 235)); 
        pwdConfirmar.setBackground(new Color(235, 235, 235));
        btnEstado.setText(seleccionado.isActivo() ? "Desactivar" : "Activar");
        btnEstado.setEnabled(!esElMismo);        // nadie puede desactivar su propia cuenta
        btnClave.setEnabled(!esElMismo);         
        lblNota.setText(esElMismo
                ? "Es su propia cuenta: no puede cambiar su rol ni desactivarla. Cambie su contraseña desde el menú."
                : " ");
    }

    private void limpiarClaves() {
        pwdClave.setText("");
        pwdConfirmar.setText("");
    }


    private void guardar() {
        char[] clave = pwdClave.getPassword();
        char[] confirmacion = pwdConfirmar.getPassword();
        try {
            if (seleccionado == null) {
                servicio.crear(txtUsuario.getText(), txtNombre.getText(), (Rol) cboRol.getSelectedItem(),
                        clave, confirmacion);
                informar("Usuario creado correctamente.");
            } else {
                servicio.actualizarDatos(seleccionado.getId(), txtNombre.getText(),
                        (Rol) cboRol.getSelectedItem(), actor);
                informar("Cambios guardados correctamente.");
            }
        } catch (UsuarioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException ex) {
            errorBD("guardar el usuario", ex);
            return;
        } finally {
            PedirClave.borrar(clave, confirmacion);
        }
        recargar();
        nuevo();
    }

    private void cambiarEstado() {
        if (seleccionado == null) {
            return;
        }
        boolean activar = !seleccionado.isActivo();
        String pregunta = activar
                ? "¿Activar al usuario \"" + seleccionado.getNombreUsuario() + "\"?\nPodrá volver a iniciar sesión."
                : "¿Desactivar al usuario \"" + seleccionado.getNombreUsuario() + "\"?\n"
                  + "No podrá iniciar sesión, pero se conserva su historial.";
        int respuesta = JOptionPane.showConfirmDialog(this, pregunta, activar ? "Activar usuario" : "Desactivar usuario",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            servicio.cambiarEstado(seleccionado.getId(), activar, actor);
        } catch (UsuarioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo cambiar el estado", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException ex) {
            errorBD("cambiar el estado del usuario", ex);
            return;
        }
        recargar();
        nuevo();
    }

    private void restablecerClave() {
        if (seleccionado == null) {
            return;
        }
        Usuario objetivo = seleccionado;
        boolean guardada = pedirClave.solicitarYAplicar(this,
                "Restablecer contraseña de " + objetivo.getNombreUsuario(), false,
                (actual, nueva, confirmacion) -> servicio.restablecerClave(objetivo.getId(), nueva, confirmacion));
        if (guardada) {
            informar("La contraseña de \"" + objetivo.getNombreUsuario() + "\" se actualizó correctamente.");
        }
    }


    private void informar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void errorBD(String accion, SQLException ex) {
        LOG.log(Level.SEVERE, "Error al " + accion, ex);
        JOptionPane.showMessageDialog(this,
                "No se pudo " + accion + ".\nVerifique la conexión con la base de datos.",
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
