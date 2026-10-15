package com.smartmarket.reporte;

import com.smartmarket.dao.ReporteDAO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class ReporteService {

    private static final Logger LOG = Logger.getLogger(ReporteService.class.getName());
    private static final String NEGOCIO = "SmartMarket";
    private static final Locale LOCALE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<String, JasperReport> PLANTILLAS = new HashMap<>();

    private final ReporteDAO dao = new ReporteDAO();

    public int contarProductos() throws SQLException {
        return dao.inventario().size();
    }

    public int contarVentas(LocalDate desde, LocalDate hasta) throws SQLException {
        return dao.ventas(desde, hasta).size();
    }

    //stock actual
    public File generarInventario(File destino, String generadoPor)
            throws ReporteException, SQLException {
        List<FilaInventario> filas = dao.inventario();
        if (filas.isEmpty()) {
            throw new SinDatosException("No hay productos registrados para mostrar en el inventario.");
        }
        Map<String, Object> parametros = parametrosBase(generadoPor);
        parametros.put("PERIODO", "Stock al " + LocalDate.now().format(FECHA));
        return exportar("/reportes/inventario.jrxml", parametros, filas, destino);
    }

    //ventas entre fechas
    public File generarVentas(LocalDate desde, LocalDate hasta, File destino, String generadoPor)
            throws ReporteException, SQLException {
        if (desde.isAfter(hasta)) {
            throw new ReporteException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        List<FilaVenta> filas = dao.ventas(desde, hasta);
        if (filas.isEmpty()) {
            throw new SinDatosException("No hay ventas registradas entre el " + desde.format(FECHA)
                    + " y el " + hasta.format(FECHA) + ".");
        }
        Map<String, Object> parametros = parametrosBase(generadoPor);
        parametros.put("PERIODO", "Del " + desde.format(FECHA) + " al " + hasta.format(FECHA));
        return exportar("/reportes/ventas.jrxml", parametros, filas, destino);
    }

    private Map<String, Object> parametrosBase(String generadoPor) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("NEGOCIO", NEGOCIO);
        parametros.put("GENERADO_POR", generadoPor);
        parametros.put("FECHA_GENERACION", LocalDateTime.now().format(FECHA_HORA));
        parametros.put("REPORT_LOCALE", LOCALE); // separadores de miles y decimales independientes del equipo
        return parametros;
    }

    private File exportar(String plantilla, Map<String, Object> parametros, List<?> filas, File destino)
            throws ReporteException {
        try {
            JasperReport compilada = obtenerPlantilla(plantilla);
            JasperPrint impreso = JasperFillManager.fillReport(compilada, parametros,
                    new JRBeanCollectionDataSource(filas));
            JasperExportManager.exportReportToPdfFile(impreso, destino.getAbsolutePath());
            return destino;
        } catch (JRException | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Error al generar el reporte " + plantilla, ex);
            throw new ReporteException("No se pudo generar el PDF. Verifique que el archivo no esté abierto "
                    + "en otro programa y que tenga permiso para guardar en esa carpeta.", ex);
        }
    }

    private static synchronized JasperReport obtenerPlantilla(String recurso) throws JRException {
        JasperReport compilada = PLANTILLAS.get(recurso);
        if (compilada == null) {
            try (InputStream in = ReporteService.class.getResourceAsStream(recurso)) {
                if (in == null) {
                    throw new JRException("No se encontró la plantilla " + recurso);
                }
                compilada = JasperCompileManager.compileReport(in);
            } catch (IOException ex) {
                throw new JRException("No se pudo leer la plantilla " + recurso, ex);
            }
            PLANTILLAS.put(recurso, compilada);
        }
        return compilada;
    }
}
