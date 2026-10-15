package com.smartmarket.dao;

import com.smartmarket.modelo.DetalleVenta;
import com.smartmarket.modelo.Venta;
import com.smartmarket.util.Conexion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VentaDAO {

    private static final Logger LOG = Logger.getLogger(VentaDAO.class.getName());

    public void registrar(Venta venta) throws SQLException, VentaException {
        if (venta.getDetalles().isEmpty()) {
            throw new VentaException("La venta no tiene productos.");
        }
        for (DetalleVenta d : venta.getDetalles()) {
            if (d.getCantidad() <= 0) {
                throw new VentaException("La cantidad de \"" + d.getNombre() + "\" debe ser mayor que cero.");
            }
        }
        List<DetalleVenta> ordenados = new ArrayList<>(venta.getDetalles());
        ordenados.sort(Comparator.comparingInt(DetalleVenta::getIdProducto));

        try (Connection cn = Conexion.conectar()) {
            cn.setAutoCommit(false);
            try {
                Map<Integer, BigDecimal> precios = new HashMap<>();
                BigDecimal total = BigDecimal.ZERO;
                for (DetalleVenta d : ordenados) {
                    BigDecimal precio = validarYDescontar(cn, d);
                    precios.put(d.getIdProducto(), precio);
                    total = total.add(precio.multiply(BigDecimal.valueOf(d.getCantidad())));
                }
                int idVenta = insertarCabecera(cn, total, venta.getIdUsuario());
                insertarDetalle(cn, idVenta, venta.getDetalles(), precios);
                java.time.LocalDateTime fecha = leerFecha(cn, idVenta);
                cn.commit();

                for (DetalleVenta d : venta.getDetalles()) {
                    d.setPrecioUnitario(precios.get(d.getIdProducto()));
                }
                venta.setTotal(total);
                venta.setIdVenta(idVenta);
                venta.setFecha(fecha);
            } catch (SQLException | VentaException | RuntimeException ex) {
                revertir(cn);
                throw ex;
            }
        }
    }

    private BigDecimal validarYDescontar(Connection cn, DetalleVenta d) throws SQLException, VentaException {
        String consulta = "SELECT nombre, precio, stock FROM producto WHERE idProducto = ? AND activo = 1 FOR UPDATE";
        BigDecimal precio;
        try (PreparedStatement ps = cn.prepareStatement(consulta)) {
            ps.setInt(1, d.getIdProducto());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new VentaException("El producto \"" + d.getNombre() + "\" ya no está disponible.");
                }
                int stock = rs.getInt("stock");
                if (stock < d.getCantidad()) {
                    throw new VentaException("Stock insuficiente de \"" + rs.getString("nombre") + "\": "
                            + "disponible " + stock + ", solicitado " + d.getCantidad() + ".");
                }
                precio = rs.getBigDecimal("precio");
            }
        }
        try (PreparedStatement ps = cn.prepareStatement("UPDATE producto SET stock = stock - ? WHERE idProducto = ?")) {
            ps.setInt(1, d.getCantidad());
            ps.setInt(2, d.getIdProducto());
            ps.executeUpdate();
        }
        return precio;
    }

    private int insertarCabecera(Connection cn, BigDecimal total, int idUsuario) throws SQLException {
        String sql = "INSERT INTO venta (total, idUsuario) VALUES (?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, total);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el número de la venta.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void insertarDetalle(Connection cn, int idVenta, List<DetalleVenta> detalles,
                                 Map<Integer, BigDecimal> precios) throws SQLException {
        String sql = "INSERT INTO detalleventa (idVenta, idProducto, cantidad, precioUnitario, subtotal) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            for (DetalleVenta d : detalles) {
                BigDecimal precio = precios.get(d.getIdProducto());
                ps.setInt(1, idVenta);
                ps.setInt(2, d.getIdProducto());
                ps.setInt(3, d.getCantidad());
                ps.setBigDecimal(4, precio);
                ps.setBigDecimal(5, precio.multiply(BigDecimal.valueOf(d.getCantidad())));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private java.time.LocalDateTime leerFecha(Connection cn, int idVenta) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT fecha FROM venta WHERE idVenta = ?")) {
            ps.setInt(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getTimestamp(1).toLocalDateTime() : java.time.LocalDateTime.now();
            }
        }
    }

    private void revertir(Connection cn) {
        try {
            cn.rollback();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "No se pudo revertir la transacción de la venta", ex);
        }
    }
}
