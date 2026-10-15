package com.smartmarket.vista;

import com.smartmarket.dao.ProductoDAO;
import com.smartmarket.modelo.Producto;
import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


public class FrmProducto extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FrmProducto.class.getName());
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    private final ProductoDAO productoDAO = new ProductoDAO();

    private final JTextField txtCodigo = new JTextField(12);
    private final JTextField txtNombre = new JTextField(26);
    private final JTextField txtPrecio = new JTextField(10);
    private final JTextField txtStock = new JTextField(10);
    private final JLabel lblStock = new JLabel("Stock inicial:");
    private final JTextField txtStockMinimo = new JTextField(10);
    private final JTextField txtBuscar = new JTextField(22);

    private final JButton btnNuevo = new JButton("Nuevo");
    private final JButton btnGuardar = new JButton("Guardar");
    private final JButton btnEliminar = new JButton("Dar de baja");
    private final JButton btnBuscar = new JButton("Buscar");

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Código", "Nombre", "Precio (S/)", "Stock", "Stock mín."}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private List<Producto> productos = new ArrayList<>(); 
    private int idSeleccionado = 0;                        // 0 = se está creando un producto nuevo

    public FrmProducto() {
        super("SmartMarket - Productos");
        validarPermiso();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); 
        construirInterfaz();
        pack();
        setLocationRelativeTo(null);
        cargarTabla();
        nuevo();
    }

    // solo administrador o inventario pueden entrar
    private void validarPermiso() {
        Usuario u = Sesion.getUsuario();
        if (u == null || (u.getRol() != Rol.ADMINISTRADOR && u.getRol() != Rol.INVENTARIO)) {
            throw new IllegalStateException("El usuario no tiene permiso para gestionar productos.");
        }
    }

    

    private void construirInterfaz() {
    	JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del producto"));
        txtStock.setDisabledTextColor(Color.DARK_GRAY);

        GridBagConstraints gbcCodLbl = new GridBagConstraints();
        gbcCodLbl.insets = new Insets(5, 8, 5, 8);
        gbcCodLbl.anchor = GridBagConstraints.WEST;
        gbcCodLbl.gridx = 0; gbcCodLbl.gridy = 0;
        formulario.add(new JLabel("Código:"), gbcCodLbl);

        GridBagConstraints gbcCodTxt = new GridBagConstraints();
        gbcCodTxt.insets = new Insets(5, 8, 5, 8);
        gbcCodTxt.anchor = GridBagConstraints.WEST;
        gbcCodTxt.gridx = 1; gbcCodTxt.gridy = 0;
        formulario.add(txtCodigo, gbcCodTxt);

        GridBagConstraints gbcNomLbl = new GridBagConstraints();
        gbcNomLbl.insets = new Insets(5, 8, 5, 8);
        gbcNomLbl.anchor = GridBagConstraints.WEST;
        gbcNomLbl.gridx = 2; gbcNomLbl.gridy = 0;
        formulario.add(new JLabel("Nombre:"), gbcNomLbl);

        GridBagConstraints gbcNomTxt = new GridBagConstraints();
        gbcNomTxt.insets = new Insets(5, 8, 5, 8);
        gbcNomTxt.anchor = GridBagConstraints.WEST;
        gbcNomTxt.gridx = 3; gbcNomTxt.gridy = 0;
        formulario.add(txtNombre, gbcNomTxt);

        GridBagConstraints gbcPreLbl = new GridBagConstraints();
        gbcPreLbl.insets = new Insets(5, 8, 5, 8);
        gbcPreLbl.anchor = GridBagConstraints.WEST;
        gbcPreLbl.gridx = 0; gbcPreLbl.gridy = 1;
        formulario.add(new JLabel("Precio (S/):"), gbcPreLbl);

        GridBagConstraints gbcPreTxt = new GridBagConstraints();
        gbcPreTxt.insets = new Insets(5, 8, 5, 8);
        gbcPreTxt.anchor = GridBagConstraints.WEST;
        gbcPreTxt.gridx = 1; gbcPreTxt.gridy = 1;
        formulario.add(txtPrecio, gbcPreTxt);

        GridBagConstraints gbcStkLbl = new GridBagConstraints();
        gbcStkLbl.insets = new Insets(5, 8, 5, 8);
        gbcStkLbl.anchor = GridBagConstraints.WEST;
        gbcStkLbl.gridx = 2; gbcStkLbl.gridy = 1;
        formulario.add(lblStock, gbcStkLbl);

        GridBagConstraints gbcStkTxt = new GridBagConstraints();
        gbcStkTxt.insets = new Insets(5, 8, 5, 8);
        gbcStkTxt.anchor = GridBagConstraints.WEST;
        gbcStkTxt.gridx = 3; gbcStkTxt.gridy = 1;
        formulario.add(txtStock, gbcStkTxt);

        GridBagConstraints gbcMinLbl = new GridBagConstraints();
        gbcMinLbl.insets = new Insets(5, 8, 5, 8);
        gbcMinLbl.anchor = GridBagConstraints.WEST;
        gbcMinLbl.gridx = 0; gbcMinLbl.gridy = 2;
        formulario.add(new JLabel("Stock mínimo:"), gbcMinLbl);

        GridBagConstraints gbcMinTxt = new GridBagConstraints();
        gbcMinTxt.insets = new Insets(5, 8, 5, 8);
        gbcMinTxt.anchor = GridBagConstraints.WEST;
        gbcMinTxt.gridx = 1; gbcMinTxt.gridy = 2;
        formulario.add(txtStockMinimo, gbcMinTxt);

        JLabel nota = new JLabel("<html>El stock de un producto existente solo cambia con ventas, ingresos y ajustes.</html>");
        nota.setForeground(Color.GRAY);
        GridBagConstraints gbcNota = new GridBagConstraints();
        gbcNota.insets = new Insets(5, 8, 5, 8);
        gbcNota.anchor = GridBagConstraints.WEST;
        gbcNota.gridx = 2; gbcNota.gridy = 2;
        gbcNota.gridwidth = 2; 
        formulario.add(nota, gbcNota);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(botones, BorderLayout.SOUTH);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        busqueda.add(new JLabel("Buscar por código o nombre:"));
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderizadorStockBajo());
        tabla.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new java.awt.Dimension(720, 260));

        JLabel leyenda = new JLabel("En rojo: productos con stock igual o menor al mínimo.");
        leyenda.setForeground(Color.GRAY);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        centro.add(busqueda, BorderLayout.NORTH);
        centro.add(scroll, BorderLayout.CENTER);
        centro.add(leyenda, BorderLayout.SOUTH);

        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> darDeBaja());
        btnBuscar.addActionListener(e -> cargarTabla());
        txtBuscar.addActionListener(e -> cargarTabla()); 

        add(norte, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    // en rojo las filas con stock al mínimo
    private class RenderizadorStockBajo extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, columna);
            setHorizontalAlignment(columna >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
            boolean bajo = fila < productos.size() && productos.get(fila).tieneStockBajo();
            if (!seleccionada) {
                c.setForeground(bajo ? new Color(190, 30, 30) : t.getForeground());
            }
            return c;
        }
    }



    private void cargarTabla() {
        try {
            productos = productoDAO.listar(txtBuscar.getText().trim());
        } catch (SQLException ex) {
            errorBD("cargar los productos", ex);
            return;
        }
        modeloTabla.setRowCount(0);
        for (Producto p : productos) {
            modeloTabla.addRow(new Object[]{
                p.getCodigo(), p.getNombre(), p.getPrecio().setScale(2).toPlainString(),
                p.getStock(), p.getStockMinimo()});
        }
    }

    private void nuevo() {
        idSeleccionado = 0;
        tabla.clearSelection();
        txtCodigo.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtStock.setText("0");
        txtStockMinimo.setText("0");
        txtStock.setEnabled(true);    
        lblStock.setText("Stock inicial:");
        txtStock.setBackground(UIManager.getColor("TextField.background"));
        btnEliminar.setEnabled(false);
        txtCodigo.requestFocusInWindow();
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= productos.size()) {
            return;
        }
        Producto p = productos.get(fila);
        idSeleccionado = p.getIdProducto();
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(p.getPrecio().setScale(2).toPlainString());
        txtStock.setText(String.valueOf(p.getStock()));
        txtStockMinimo.setText(String.valueOf(p.getStockMinimo()));
        txtStock.setEnabled(false);   
        lblStock.setText("Stock actual:");
        txtStock.setBackground(new Color(235, 235, 235)); 
        btnEliminar.setEnabled(true);
    }

    private void guardar() {
        Producto p = leerFormulario();
        if (p == null) {
            return; 
        }
        try {
            if (productoDAO.existeCodigo(p.getCodigo(), idSeleccionado)) {
                avisar("Ya existe un producto con el código " + p.getCodigo() + ".", txtCodigo);
                return;
            }
            if (idSeleccionado == 0) {
                productoDAO.insertar(p);
            } else if (!productoDAO.actualizar(p)) {
                JOptionPane.showMessageDialog(this, "El producto ya no existe o fue dado de baja.",
                        "No se actualizó", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            errorBD("guardar el producto", ex);
            return;
        }
        JOptionPane.showMessageDialog(this, "Producto guardado correctamente.",
                "Guardado", JOptionPane.INFORMATION_MESSAGE);
        cargarTabla();
        nuevo();
    }

    private void darDeBaja() {
        if (idSeleccionado == 0) {
            return;
        }
        String aviso = "¿Dar de baja el producto \"" + txtNombre.getText().trim() + "\"?\n"
                + "Dejará de mostrarse, pero se conserva su historial de ventas.";
        if (!txtStock.getText().trim().equals("0")) {
            aviso += "\nAtención: todavía tiene " + txtStock.getText().trim() + " unidades en stock.";
        }
        int respuesta = JOptionPane.showConfirmDialog(this, aviso, "Confirmar baja",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            productoDAO.desactivar(idSeleccionado);
        } catch (SQLException ex) {
            errorBD("dar de baja el producto", ex);
            return;
        }
        cargarTabla();
        nuevo();
    }

    // validación

    
    private Producto leerFormulario() {
        String codigo = txtCodigo.getText().trim().toUpperCase();
        String nombre = txtNombre.getText().trim();
        if (codigo.isEmpty()) {
            avisar("Ingrese el código del producto.", txtCodigo);
            return null;
        }
        if (codigo.length() > 30) {
            avisar("El código no puede superar los 30 caracteres.", txtCodigo);
            return null;
        }
        if (nombre.isEmpty()) {
            avisar("Ingrese el nombre del producto.", txtNombre);
            return null;
        }
        if (nombre.length() > 100) {
            avisar("El nombre no puede superar los 100 caracteres.", txtNombre);
            return null;
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            avisar("El precio debe ser un número (ejemplo: 12.50).", txtPrecio);
            return null;
        }
        if (precio.signum() <= 0) {
            avisar("El precio debe ser mayor que cero.", txtPrecio);
            return null;
        }
        if (precio.stripTrailingZeros().scale() > 2) {
            avisar("El precio admite como máximo 2 decimales.", txtPrecio);
            return null;
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            avisar("El precio es demasiado alto.", txtPrecio);
            return null;
        }
        Integer stockMinimo = leerEntero(txtStockMinimo, "stock mínimo");
        if (stockMinimo == null) {
            return null;
        }
        int stock = 0;
        if (idSeleccionado == 0) { 
            Integer valor = leerEntero(txtStock, "stock inicial");
            if (valor == null) {
                return null;
            }
            stock = valor;
        }
        Producto p = new Producto();
        p.setIdProducto(idSeleccionado);
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setPrecio(precio.setScale(2, java.math.RoundingMode.UNNECESSARY));
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        return p;
    }

    private Integer leerEntero(JTextField campo, String nombreCampo) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor < 0) {
                avisar("El " + nombreCampo + " no puede ser negativo.", campo);
                return null;
            }
            return valor;
        } catch (NumberFormatException ex) {
            avisar("El " + nombreCampo + " debe ser un número entero.", campo);
            return null;
        }
    }

    private void avisar(String mensaje, JComponent campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        campo.requestFocusInWindow();
    }

    private void errorBD(String accion, SQLException ex) {
        LOG.log(Level.SEVERE, "Error al " + accion, ex);
        JOptionPane.showMessageDialog(this,
                "No se pudo " + accion + ".\nVerifique la conexión con la base de datos.",
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}
