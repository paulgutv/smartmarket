package com.smartmarket.dao;

import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.util.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioDAO {

    private static final Logger LOG = Logger.getLogger(UsuarioDAO.class.getName());
    private static final String COLUMNAS = "idUsuario, nombreUsuario, nombreCompleto, rol, activo";

    // valida las credenciales
    public Usuario validar(String nombreUsuario, char[] password) throws SQLException {
        String sql = "SELECT " + COLUMNAS + ", passwordHash FROM usuario WHERE nombreUsuario = ? AND activo = 1";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!coincide(password, rs.getString("passwordHash"))) {
                    return null;
                }
                return mapear(rs);
            }
        }
    }

    // cuenta toda la cantidad de usuarios
    public int contar() throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuario ORDER BY activo DESC, nombreCompleto";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuario WHERE idUsuario = ?";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }
    
    // para saber si ya existe el usuario
    public boolean existeNombreUsuario(String nombreUsuario) throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM usuario WHERE nombreUsuario = ? LIMIT 1")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // inserta un usuario con hash
    public int insertar(String nombreUsuario, String passwordHash, String nombreCompleto, Rol rol) throws SQLException {
        String sql = "INSERT INTO usuario (nombreUsuario, passwordHash, nombreCompleto, rol) VALUES (?, ?, ?, ?)";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, passwordHash);
            ps.setString(3, nombreCompleto);
            ps.setString(4, rol.name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se obtuvo el id del usuario creado.");
                }
                return keys.getInt(1);
            }
        }
    }


    public void modificar(int id, String nombreCompleto, Rol rol, Boolean activo) throws SQLException, UsuarioException {
        try (Connection cn = Conexion.conectar()) {
            cn.setAutoCommit(false);
            try {
                Set<Integer> administradoresActivos = new HashSet<>();
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT idUsuario FROM usuario WHERE rol = 'ADMINISTRADOR' AND activo = 1 FOR UPDATE");
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        administradoresActivos.add(rs.getInt(1));
                    }
                }
                Rol rolActual;
                boolean activoActual;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT rol, activo FROM usuario WHERE idUsuario = ? FOR UPDATE")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new UsuarioException("El usuario ya no existe.");
                        }
                        rolActual = Rol.valueOf(rs.getString("rol"));
                        activoActual = rs.getBoolean("activo");
                    }
                }
                Rol rolFinal = (rol != null) ? rol : rolActual;
                boolean activoFinal = (activo != null) ? activo : activoActual;
                boolean eraAdministradorActivo = rolActual == Rol.ADMINISTRADOR && activoActual;
                boolean seraAdministradorActivo = rolFinal == Rol.ADMINISTRADOR && activoFinal;
                if (eraAdministradorActivo && !seraAdministradorActivo && administradoresActivos.size() <= 1) {
                    throw new UsuarioException("Debe existir al menos un administrador activo en el sistema.");
                }
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE usuario SET nombreCompleto = COALESCE(?, nombreCompleto), rol = ?, activo = ? "
                        + "WHERE idUsuario = ?")) {
                    ps.setString(1, nombreCompleto);
                    ps.setString(2, rolFinal.name());
                    ps.setBoolean(3, activoFinal);
                    ps.setInt(4, id);
                    ps.executeUpdate();
                }
                cn.commit();
            } catch (SQLException | UsuarioException | RuntimeException ex) {
                revertir(cn);
                throw ex;
            }
        }
    }

    public String obtenerHash(int id) throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement("SELECT passwordHash FROM usuario WHERE idUsuario = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public boolean actualizarHash(int id, String passwordHash) throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement("UPDATE usuario SET passwordHash = ? WHERE idUsuario = ?")) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("idUsuario"),
                rs.getString("nombreUsuario"),
                rs.getString("nombreCompleto"),
                Rol.valueOf(rs.getString("rol")),
                rs.getBoolean("activo"));
    }
    
    // para ver si el hash coincide con el generado por bycript
    private boolean coincide(char[] password, String hash) {
        try {
            return BCrypt.checkpw(new String(password), hash);
        } catch (IllegalArgumentException ex) {
            // El hash guardado no tiene formato BCrypt (p. ej. un dato mal insertado).
            LOG.log(Level.WARNING, "El hash almacenado no tiene formato BCrypt válido.", ex);
            return false;
        }
    }

    private void revertir(Connection cn) {
        try {
            cn.rollback();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "No se pudo revertir la transacción de usuarios", ex);
        }
    }
}
