package com.smartmarket.dao;

import com.smartmarket.modelo.Producto;
import com.smartmarket.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    private static final String COLUMNAS = "idProducto, codigo, nombre, precio, stock, stockMinimo, activo";

    public List<Producto> listar(String filtro) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM producto "
                   + "WHERE activo = 1 AND (codigo LIKE ? OR nombre LIKE ?) ORDER BY nombre";
        String patron = "%" + escaparLike(filtro) + "%";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Producto buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM producto WHERE codigo = ? AND activo = 1";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean existeCodigo(String codigo, int idExcluido) throws SQLException {
        String sql = "SELECT 1 FROM producto WHERE codigo = ? AND idProducto <> ? LIMIT 1";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, stockMinimo) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setIdProducto(keys.getInt(1));
                }
            }
        }
    }

    
    public boolean actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, precio = ?, stockMinimo = ? "
                   + "WHERE idProducto = ? AND activo = 1";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStockMinimo());
            ps.setInt(5, p.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idProducto) throws SQLException {
        String sql = "UPDATE producto SET activo = 0 WHERE idProducto = ? AND activo = 1";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("idProducto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock"),
                rs.getInt("stockMinimo"),
                rs.getBoolean("activo"));
    }

    // para evitar que símbolos se usen como comodines
    private String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
