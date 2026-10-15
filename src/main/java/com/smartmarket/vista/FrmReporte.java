package com.smartmarket.vista;

import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.reporte.ReporteException;
import com.smartmarket.reporte.ReporteService;
import com.smartmarket.reporte.SinDatosException;
import com.smartmarket.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FrmReporte extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(FrmReporte.class.getName());
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String INVENTARIO = "Inventario actual";
    private static final String VENTAS = "Ventas por período";

    // elige dónde guardar el pdf
    public interface SelectorArchivo {
        File elegir(Component padre, String nombreSugerido);
    }

    private final ReporteService servicio = new ReporteService();
    private final SelectorArchivo selector;
    private final Usuario usuario;

    private final JComboBox<String> cboTipo = new JComboBox<>(new String[]{INVENTARIO, VENTAS});
    private final JSpinner spDesde = crearSelectorFecha(LocalDate.now().withDayOfMonth(1));
    private final JSpinner spHasta = crearSelectorFecha(LocalDate.now());
    private final JButton btnGenerar = new JButton("Generar PDF");
    private final JLabel lblEstado = new JLabel("Seleccione el reporte y pulse \"Generar PDF\".");

    public FrmReporte() {
        this(new SelectorPdf());
    }

    public FrmReporte(SelectorArchivo selector) {
        super("SmartMarket - Reportes");
        this.selector = selector;
        usuario = validarPermiso();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirInterfaz();
        pack();
        setSize(Math.max(getWidth(), 520), getHeight());
        setResizable(false);
        setLocationRelativeTo(null);
        actualizarCampos();
    }

    //solo el administrador obtiene reportes
    private Usuario validarPermiso() {
        Usuario u = Sesion.getUsuario();
        if (u == null || u.getRol() != Rol.ADMINISTRADOR) {
            throw new IllegalStateException("El usuario no tiene permiso para generar reportes.");
        }
        return u;
    }


    private void construirInterfaz() {
        JLabel titulo = new JLabel("Reportes en PDF");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 18f));
        titulo.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formulario.add(new JLabel("Tipo de reporte:"), (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        formulario.add(cboTipo, (GridBagConstraints) gbc.clone());
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        formulario.add(new JLabel("Desde:"), (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 1;
        formulario.add(spDesde, (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 2;
        formulario.add(new JLabel("Hasta:"), (GridBagConstraints) gbc.clone());
        
        gbc.gridx = 3;
        formulario.add(spHasta, (GridBagConstraints) gbc.clone());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        botones.add(btnGenerar);

        lblEstado.setForeground(Color.GRAY);
        lblEstado.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 20));

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(formulario, BorderLayout.CENTER);
        centro.add(botones, BorderLayout.SOUTH);

        add(titulo, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(lblEstado, BorderLayout.SOUTH);

        cboTipo.addActionListener(e -> actualizarCampos());
        btnGenerar.addActionListener(e -> generar());
    }

    private static JSpinner crearSelectorFecha(LocalDate inicial) {
        Date fecha = Date.from(inicial.atStartOfDay(ZoneId.systemDefault()).toInstant());
        JSpinner spinner = new JSpinner(new SpinnerDateModel(fecha, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    private void actualizarCampos() {
        boolean ventas = VENTAS.equals(cboTipo.getSelectedItem());
        spDesde.setEnabled(ventas);
        spHasta.setEnabled(ventas);
    }


    private void generar() {
        boolean ventas = VENTAS.equals(cboTipo.getSelectedItem());
        LocalDate desde = null;
        LocalDate hasta = null;
        if (ventas) {
            desde = leerFecha(spDesde, "inicial");
            hasta = (desde == null) ? null : leerFecha(spHasta, "final");
            if (desde == null || hasta == null) {
                return;
            }
            if (desde.isAfter(hasta)) {
                avisar("La fecha inicial no puede ser posterior a la fecha final.", spDesde);
                return;
            }
        }
        //primero se verifica si hay datos
        try {
            if (ventas && servicio.contarVentas(desde, hasta) == 0) {
                informar("No hay ventas registradas entre el " + desde.format(FECHA) + " y el " + hasta.format(FECHA) + ".");
                return;
            }
            if (!ventas && servicio.contarProductos() == 0) {
                informar("No hay productos registrados para mostrar en el inventario.");
                return;
            }
        } catch (SQLException ex) {
            errorBD(ex);
            return;
        }
        String sugerido = ventas
                ? "reporte_ventas_" + desde + "_a_" + hasta + ".pdf"
                : "reporte_inventario_" + LocalDate.now() + ".pdf";
        File destino = selector.elegir(this, sugerido);
        if (destino == null) {
            return;
        }
        generarEnSegundoPlano(ventas, desde, hasta, destino);
    }

    private void generarEnSegundoPlano(boolean ventas, LocalDate desde, LocalDate hasta, File destino) {
        String generadoPor = usuario.getNombreCompleto();
        ocupado(true);
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                return ventas
                        ? servicio.generarVentas(desde, hasta, destino, generadoPor)
                        : servicio.generarInventario(destino, generadoPor);
            }

            @Override
            protected void done() {
                ocupado(false);
                try {
                    reporteGenerado(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    fallo(ex.getCause());
                }
            }
        }.execute();
    }

    private void ocupado(boolean ocupado) {
        btnGenerar.setEnabled(!ocupado);
        cboTipo.setEnabled(!ocupado);
        lblEstado.setText(ocupado
                ? "Generando el reporte... (la primera vez puede tardar unos segundos)"
                : "Listo. Seleccione otro reporte o cierre la ventana.");
        setCursor(Cursor.getPredefinedCursor(ocupado ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void reporteGenerado(File archivo) {
        int opcion = JOptionPane.showOptionDialog(this,
                "Reporte generado correctamente:\n" + archivo.getAbsolutePath(),
                "Reporte generado", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new Object[]{"Abrir PDF", "Cerrar"}, "Abrir PDF");
        if (opcion == 0) {
            abrir(archivo);
        }
    }

    private void abrir(File archivo) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo);
                return;
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "No se pudo abrir el PDF automáticamente", ex);
        }
        JOptionPane.showMessageDialog(this,
                "No se pudo abrir el archivo automáticamente.\nÁbralo desde:\n" + archivo.getAbsolutePath(),
                "Abrir PDF", JOptionPane.INFORMATION_MESSAGE);
    }

    private void fallo(Throwable causa) {
        if (causa instanceof SinDatosException) {
            informar(causa.getMessage());
        } else if (causa instanceof ReporteException) {
            JOptionPane.showMessageDialog(this, causa.getMessage(),
                    "No se pudo generar el reporte", JOptionPane.WARNING_MESSAGE);
        } else if (causa instanceof SQLException) {
            errorBD((SQLException) causa);
        } else {
            LOG.log(Level.SEVERE, "Error inesperado al generar el reporte", causa);
            JOptionPane.showMessageDialog(this, "Ocurrió un error inesperado al generar el reporte.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private LocalDate leerFecha(JSpinner spinner, String cual) {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) {
            avisar("La fecha " + cual + " no es válida. Use el formato dd/mm/aaaa.", spinner);
            return null;
        }
        Date valor = (Date) spinner.getValue();
        return valor.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void avisar(String mensaje, JComponent campo) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        campo.requestFocusInWindow();
    }

    private void informar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Sin datos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void errorBD(SQLException ex) {
        LOG.log(Level.SEVERE, "Error de base de datos al preparar el reporte", ex);
        JOptionPane.showMessageDialog(this,
                "No se pudo consultar la base de datos.\nVerifique la conexión e inténtelo nuevamente.",
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    private static class SelectorPdf implements SelectorArchivo {
        @Override
        public File elegir(Component padre, String nombreSugerido) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar reporte como");
            chooser.setFileFilter(new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));
            chooser.setSelectedFile(new File(nombreSugerido));
            while (true) {
                if (chooser.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
                    return null;
                }
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".pdf");
                }
                if (archivo.exists()) {
                    int r = JOptionPane.showConfirmDialog(padre,
                            "El archivo \"" + archivo.getName() + "\" ya existe. ¿Desea reemplazarlo?",
                            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (r != JOptionPane.YES_OPTION) {
                        continue;
                    }
                }
                return archivo;
            }
        }
    }
}
