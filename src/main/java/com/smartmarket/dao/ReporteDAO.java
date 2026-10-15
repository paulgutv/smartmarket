package com.smartmarket.dao;

import com.smartmarket.reporte.FilaInventario;
import com.smartmarket.reporte.FilaVenta;
import com.smartmarket.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    public List<FilaInventario> inventario() throws SQLException {
        String sql = "SELECT codigo, nombre, precio, stock, stockMinimo FROM producto "
                   + "WHERE activo = 1 ORDER BY nombre";
        List<FilaInventario> filas = new ArrayList<>();
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new FilaInventario(rs.getString("codigo"), rs.getString("nombre"),
                        rs.getBigDecimal("precio"), rs.getInt("stock"), rs.getInt("stockMinimo")));
            }
        }
        return filas;
    }


    public List<FilaVenta> ventas(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT v.idVenta, v.fecha, u.nombreCompleto, v.total, "
                   + "COALESCE((SELECT SUM(d.cantidad) FROM detalleventa d WHERE d.idVenta = v.idVenta), 0) AS unidades "
                   + "FROM venta v JOIN usuario u ON u.idUsuario = v.idUsuario "
                   + "WHERE v.fecha >= ? AND v.fecha < ? ORDER BY v.fecha, v.idVenta";
        List<FilaVenta> filas = new ArrayList<>();
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new FilaVenta(rs.getInt("idVenta"), rs.getTimestamp("fecha"),
                            rs.getString("nombreCompleto"), rs.getInt("unidades"), rs.getBigDecimal("total")));
                }
            }
        }
        return filas;
    }
}
