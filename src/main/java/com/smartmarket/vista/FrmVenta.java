package com.smartmarket.vista;

import com.smartmarket.dao.ProductoDAO;
import com.smartmarket.dao.VentaDAO;
import com.smartmarket.dao.VentaException;
import com.smartmarket.modelo.DetalleVenta;
import com.smartmarket.modelo.Producto;
import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.modelo.Venta;
import com.smartmarket.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FrmVenta extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FrmVenta.class.getName());
    private static final int MAX_OPCIONES = 50;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final VentaDAO ventaDAO = new VentaDAO();
    private final Usuario usuario;

    private final JTextField txtCodigo = new JTextField(16);
    private final JTextField txtCantidad = new JTextField("1", 5);
    private final JLabel lblProducto = new JLabel(" ");
    private final JLabel lblTotal = new JLabel("S/ 0.00");
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnAgregar = new JButton("Agregar");
    private final JButton btnQuitar = new JButton("Quitar ítem");
    private final JButton btnVaciar = new JButton("Vaciar venta");
    private final JButton btnRegistrar = new JButton("REGISTRAR VENTA");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Código", "Producto", "Cantidad", "Precio (S/)", "Subtotal (S/)"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private Venta venta;              
    private Producto productoActual;   

    public FrmVenta() {
        super("SmartMarket - Registro de ventas");
        usuario = validarPermiso();
        venta = new Venta(usuario.getId());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrar();
            }
        });
        construirInterfaz();
        pack();
        setLocationRelativeTo(null);
        refrescarVenta();
        txtCodigo.requestFocusInWindow();
    }

    private Usuario validarPermiso() {
        Usuario u = Sesion.getUsuario();
        if (u == null || (u.getRol() != Rol.ADMINISTRADOR && u.getRol() != Rol.VENTAS)) {
            throw new IllegalStateException("El usuario no tiene permiso para registrar ventas.");
        }
        return u;
    }

    public boolean tieneVentaEnCurso() {
        return !venta.getDetalles().isEmpty();
    }


    private void construirInterfaz() {
        JLabel vendedor = new JLabel("Vendedor: " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ")");
        vendedor.setForeground(Color.GRAY);

        JPanel entrada = new JPanel(new GridBagLayout());
        entrada.setBorder(BorderFactory.createTitledBorder("Agregar producto"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridy = 0;
        gbc.gridx = 0;
        entrada.add(new JLabel("Código o nombre:"), (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 1;
        entrada.add(txtCodigo, (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 2;
        entrada.add(btnBuscar, (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 3;
        entrada.add(new JLabel("Cantidad:"), (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 4;
        entrada.add(txtCantidad, (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 5;
        entrada.add(btnAgregar, (GridBagConstraints) gbc.clone());
        
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 6;
        entrada.add(lblProducto, (GridBagConstraints) gbc.clone());

        JPanel norte = new JPanel(new BorderLayout(0, 5));
        norte.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        norte.add(vendedor, BorderLayout.NORTH);
        norte.add(entrada, BorderLayout.CENTER);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c = 2; c <= 4; c++) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(derecha);
        }
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(720, 240));

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 26f));
        btnRegistrar.setFont(btnRegistrar.getFont().deriveFont(Font.BOLD));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.add(btnQuitar);
        izquierda.add(btnVaciar);
        JPanel derechaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        derechaPanel.add(new JLabel("TOTAL:"));
        derechaPanel.add(lblTotal);
        derechaPanel.add(btnRegistrar);
        JPanel sur = new JPanel(new BorderLayout());
        sur.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        sur.add(izquierda, BorderLayout.WEST);
        sur.add(derechaPanel, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(sur, BorderLayout.SOUTH);

        txtCodigo.addActionListener(e -> buscar());   
        btnBuscar.addActionListener(e -> buscar());
        txtCantidad.addActionListener(e -> agregar());  
        btnAgregar.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitar());
        btnVaciar.addActionListener(e -> vaciar());
        btnRegistrar.addActionListener(e -> registrar());

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }


    //busca por código o por nombre y deja elegir si hay varios
    private void buscar() {
        String texto = txtCodigo.getText().trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el código o nombre del producto.",
                    "Falta el producto", JOptionPane.WARNING_MESSAGE);
            txtCodigo.requestFocusInWindow();
            return;
        }
        Producto encontrado;
        try {
            encontrado = productoDAO.buscarPorCodigo(texto.toUpperCase());
            if (encontrado == null) {
                encontrado = buscarPorNombre(texto);
            }
        } catch (SQLException ex) {
            errorBD("buscar el producto", ex);
            return;
        }
        productoActual = encontrado;
        if (encontrado == null) {
            lblProducto.setText(" ");
            return;
        }
        lblProducto.setText(encontrado.getCodigo() + " - " + encontrado.getNombre()
                + "     Precio: S/ " + dinero(encontrado.getPrecio())
                + "     Stock disponible: " + encontrado.getStock());
        txtCodigo.setText(encontrado.getCodigo());
        txtCantidad.requestFocusInWindow();
        txtCantidad.selectAll();
    }

    private Producto buscarPorNombre(String texto) throws SQLException {
        List<Producto> coincidencias = productoDAO.listar(texto);
        if (coincidencias.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontró ningún producto con \"" + texto + "\".",
                    "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        if (coincidencias.size() == 1) {
            return coincidencias.get(0);
        }
        if (coincidencias.size() > MAX_OPCIONES) {
            JOptionPane.showMessageDialog(this, "Hay demasiados resultados (" + coincidencias.size()
                    + "). Escriba más letras del nombre.", "Demasiados resultados", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        List<String> etiquetas = new ArrayList<>();
        for (Producto p : coincidencias) {
            etiquetas.add(p.getCodigo() + " - " + p.getNombre()
                    + "  (S/ " + dinero(p.getPrecio()) + ", stock " + p.getStock() + ")");
        }
        Object elegido = JOptionPane.showInputDialog(this, "Varios productos coinciden. Seleccione uno:",
                "Seleccionar producto", JOptionPane.QUESTION_MESSAGE, null,
                etiquetas.toArray(), etiquetas.get(0));
        int indice = etiquetas.indexOf(elegido);
        return indice < 0 ? null : coincidencias.get(indice);
    }

    private void agregar() {
        String escrito = txtCodigo.getText().trim().toUpperCase();
        if (productoActual == null || !productoActual.getCodigo().equals(escrito)) {
            buscar(); 
            if (productoActual == null) {
                return;
            }
        }
        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException ex) {
            avisarCantidad("La cantidad debe ser un número entero.");
            return;
        }
        if (cantidad <= 0) {
            avisarCantidad("La cantidad debe ser mayor que cero.");
            return;
        }
        int yaEnVenta = venta.cantidadDe(productoActual.getIdProducto());
        if (yaEnVenta + cantidad > productoActual.getStock()) {
            String detalle = yaEnVenta > 0 ? " (ya tiene " + yaEnVenta + " en esta venta)" : "";
            avisarCantidad("Stock insuficiente de \"" + productoActual.getNombre() + "\": disponible "
                    + productoActual.getStock() + detalle + ".");
            return;
        }
        venta.agregar(new DetalleVenta(productoActual.getIdProducto(), productoActual.getCodigo(),
                productoActual.getNombre(), cantidad, productoActual.getPrecio()));
        limpiarEntrada();
        refrescarVenta();
    }

    private void quitar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione el ítem que desea quitar.",
                    "Falta seleccionar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        venta.quitar(fila);
        refrescarVenta();
    }

    private void vaciar() {
        if (!tieneVentaEnCurso()) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "¿Descartar todos los productos de esta venta?",
                "Vaciar venta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            venta = new Venta(usuario.getId());
            limpiarEntrada();
            refrescarVenta();
        }
    }


    private void registrar() {
        if (!tieneVentaEnCurso()) {
            return;
        }
        BigDecimal esperado = venta.getTotal();
        try {
            ventaDAO.registrar(venta);
        } catch (VentaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "No se pudo registrar la venta", JOptionPane.WARNING_MESSAGE);
            return;
        } catch (SQLException ex) {
            errorBD("registrar la venta", ex);
            return;
        }
        String mensaje = "Venta N° " + venta.getIdVenta() + " registrada.\nTotal: S/ " + dinero(venta.getTotal());
        if (venta.getTotal().compareTo(esperado) != 0) {
            mensaje += "\n\nAtención: el total cambió porque se actualizaron precios durante la venta.";
        }
        JOptionPane.showMessageDialog(this, mensaje, "Venta registrada", JOptionPane.INFORMATION_MESSAGE);
        venta = new Venta(usuario.getId());
        limpiarEntrada();
        refrescarVenta();
    }

    private void cerrar() {
        if (tieneVentaEnCurso()) {
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "Hay una venta sin registrar. ¿Desea descartarla y cerrar?",
                    "Venta en curso", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }


    private void refrescarVenta() {
        modeloTabla.setRowCount(0);
        for (DetalleVenta d : venta.getDetalles()) {
            modeloTabla.addRow(new Object[]{d.getCodigo(), d.getNombre(), d.getCantidad(),
                dinero(d.getPrecioUnitario()), dinero(d.getSubtotal())});
        }
        lblTotal.setText("S/ " + dinero(venta.getTotal()));
        boolean hayItems = tieneVentaEnCurso();
        btnRegistrar.setEnabled(hayItems);
        btnQuitar.setEnabled(hayItems);
        btnVaciar.setEnabled(hayItems);
    }

    private void limpiarEntrada() {
        productoActual = null;
        txtCodigo.setText("");
        txtCantidad.setText("1");
        lblProducto.setText(" ");
        txtCodigo.requestFocusInWindow();
    }

    private void avisarCantidad(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Cantidad inválida", JOptionPane.WARNING_MESSAGE);
        txtCantidad.requestFocusInWindow();
        txtCantidad.selectAll();
    }

    private static String dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void errorBD(String accion, SQLException ex) {
        LOG.log(Level.SEVERE, "Error al " + accion, ex);
        JOptionPane.showMessageDialog(this,
                "No se pudo " + accion + ".\nVerifique la conexión con la base de datos e inténtelo nuevamente.",
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
