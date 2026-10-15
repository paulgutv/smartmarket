package com.smartmarket.vista;

import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.servicio.UsuarioServicio;
import com.smartmarket.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;


public class FrmMenu extends JFrame {

    private static final long serialVersionUID = 1L;

    private final JButton btnProductos = new JButton("Productos");
    private final JButton btnVentas = new JButton("Ventas");
    private final JButton btnReportes = new JButton("Reportes");
    private final JButton btnUsuarios = new JButton("Usuarios");
    private final JButton btnClave = new JButton("Cambiar contraseña");
    private final JButton btnCerrarSesion = new JButton("Cerrar sesión");
    private FrmProducto frmProducto; 
    private FrmVenta frmVenta;
    private FrmReporte frmReporte;
    private FrmUsuario frmUsuario;

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();
    private final PedirClave pedirClave;
    private final Usuario usuario;

    public FrmMenu() {
        this(new DialogoClave());
    }

   
    public FrmMenu(PedirClave pedirClave) {
        super("SmartMarket - Menú principal");
        this.pedirClave = pedirClave;
        usuario = Sesion.getUsuario();
        if (usuario == null) {
            throw new IllegalStateException("No hay una sesión iniciada.");
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
        aplicarPermisos(usuario.getRol());
        pack();
        setSize(Math.max(getWidth(), 460), getHeight());
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JLabel titulo = new JLabel("SMARTMARKET", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        JLabel bienvenida = new JLabel(
                "Bienvenido, " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")",
                SwingConstants.CENTER);

        JPanel cabecera = new JPanel(new GridLayout(2, 1, 0, 5));
        cabecera.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));
        cabecera.add(titulo);
        cabecera.add(bienvenida);

        JPanel botones = new JPanel(new GridLayout(0, 2, 12, 12));
        botones.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        botones.add(btnProductos);
        botones.add(btnVentas);
        botones.add(btnReportes);
        botones.add(btnUsuarios);
        botones.add(btnClave);
        botones.add(btnCerrarSesion);

        btnProductos.addActionListener(e -> abrirProductos());
        btnVentas.addActionListener(e -> abrirVentas());
        btnReportes.addActionListener(e -> abrirReportes());
        btnUsuarios.addActionListener(e -> abrirUsuarios());
        btnClave.addActionListener(e -> cambiarMiClave());
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        add(cabecera, BorderLayout.NORTH);
        add(botones, BorderLayout.CENTER);
    }

    
    private void aplicarPermisos(Rol rol) {
        btnProductos.setEnabled(rol == Rol.ADMINISTRADOR || rol == Rol.INVENTARIO);
        btnVentas.setEnabled(rol == Rol.ADMINISTRADOR || rol == Rol.VENTAS);
        btnReportes.setEnabled(rol == Rol.ADMINISTRADOR);
        btnUsuarios.setEnabled(rol == Rol.ADMINISTRADOR);
        
    }

    private void abrirProductos() {
        if (frmProducto == null || !frmProducto.isDisplayable()) {
            frmProducto = new FrmProducto();
        }
        frmProducto.setVisible(true);
        frmProducto.toFront();
    }

    private void abrirVentas() {
        if (frmVenta == null || !frmVenta.isDisplayable()) {
            frmVenta = new FrmVenta();
        }
        frmVenta.setVisible(true);
        frmVenta.toFront();
    }

    private void abrirReportes() {
        if (frmReporte == null || !frmReporte.isDisplayable()) {
            frmReporte = new FrmReporte();
        }
        frmReporte.setVisible(true);
        frmReporte.toFront();
    }

    private void abrirUsuarios() {
        if (frmUsuario == null || !frmUsuario.isDisplayable()) {
            frmUsuario = new FrmUsuario(pedirClave);
        }
        frmUsuario.setVisible(true);
        frmUsuario.toFront();
    }

   
    private void cambiarMiClave() {
        boolean cambiada = pedirClave.solicitarYAplicar(this, "Cambiar mi contraseña", true,
                (actual, nueva, confirmacion) ->
                        usuarioServicio.cambiarClavePropia(usuario, actual, nueva, confirmacion));
        if (cambiada) {
            JOptionPane.showMessageDialog(this, "Su contraseña se actualizó correctamente.",
                    "Contraseña actualizada", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void cerrarSesion() {
        if (frmVenta != null && frmVenta.isDisplayable() && frmVenta.tieneVentaEnCurso()) {
            JOptionPane.showMessageDialog(this,
                    "Hay una venta en curso. Regístrela o descártela antes de cerrar sesión.",
                    "Venta en curso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Sesion.cerrar();
        
        for (JFrame ventana : new JFrame[]{frmProducto, frmVenta, frmReporte, frmUsuario}) {
            if (ventana != null) {
                ventana.dispose();
            }
        }
        new FrmLogin().setVisible(true);
        dispose();
    }
}
