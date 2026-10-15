package com.smartmarket.servicio;

import com.smartmarket.dao.UsuarioDAO;
import com.smartmarket.dao.UsuarioException;
import com.smartmarket.modelo.Rol;
import com.smartmarket.modelo.Usuario;
import com.smartmarket.util.PoliticaClave;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;

public class UsuarioServicio {

    private static final Pattern NOMBRE_USUARIO = Pattern.compile("[A-Za-z0-9._-]{3,30}");
    private static final int COSTO_BCRYPT = 10;

    private final UsuarioDAO dao = new UsuarioDAO();

    //es true si todavía no hay ningún usuario
    public boolean sinUsuarios() throws SQLException {
        return dao.contar() == 0;
    }

    public List<Usuario> listar() throws SQLException {
        return dao.listar();
    }

    //para crear el primer administrador
    public Usuario crearPrimerAdministrador(String nombreUsuario, String nombreCompleto,
                                            char[] clave, char[] confirmacion) throws UsuarioException, SQLException {
        if (!sinUsuarios()) {
            throw new UsuarioException("El sistema ya tiene usuarios registrados.");
        }
        return crear(nombreUsuario, nombreCompleto, Rol.ADMINISTRADOR, clave, confirmacion);
    }

    public Usuario crear(String nombreUsuario, String nombreCompleto, Rol rol,
                         char[] clave, char[] confirmacion) throws UsuarioException, SQLException {
        String usuario = validarNombreUsuario(nombreUsuario);
        String nombre = validarNombreCompleto(nombreCompleto);
        if (rol == null) {
            throw new UsuarioException("Seleccione el rol del usuario.");
        }
        validarClaveNueva(clave, confirmacion, usuario);
        if (dao.existeNombreUsuario(usuario)) {
            throw duplicado(usuario);
        }
        try {
            int id = dao.insertar(usuario, hash(clave), nombre, rol);
            return new Usuario(id, usuario, nombre, rol, true);
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())) { // otro administrador lo creó justo antes
                throw duplicado(usuario);
            }
            throw ex;
        }
    }

    public void actualizarDatos(int id, String nombreCompleto, Rol rol, Usuario actor)
            throws UsuarioException, SQLException {
        String nombre = validarNombreCompleto(nombreCompleto);
        if (rol == null) {
            throw new UsuarioException("Seleccione el rol del usuario.");
        }
        if (actor != null && actor.getId() == id && actor.getRol() != rol) {
            throw new UsuarioException("No puede cambiar su propio rol.");
        }
        dao.modificar(id, nombre, rol, null);
    }

    public void cambiarEstado(int id, boolean activo, Usuario actor) throws UsuarioException, SQLException {
        if (actor != null && actor.getId() == id && !activo) {
            throw new UsuarioException("No puede desactivar su propia cuenta.");
        }
        dao.modificar(id, null, null, activo);
    }

    public void restablecerClave(int id, char[] nueva, char[] confirmacion) throws UsuarioException, SQLException {
        Usuario usuario = dao.buscarPorId(id);
        if (usuario == null) {
            throw new UsuarioException("El usuario ya no existe.");
        }
        validarClaveNueva(nueva, confirmacion, usuario.getNombreUsuario());
        dao.actualizarHash(id, hash(nueva));
    }

    public void cambiarClavePropia(Usuario actor, char[] actual, char[] nueva, char[] confirmacion)
            throws UsuarioException, SQLException {
        String hashActual = dao.obtenerHash(actor.getId());
        if (hashActual == null) {
            throw new UsuarioException("Su usuario ya no existe.");
        }
        if (actual == null || actual.length == 0 || !coincide(actual, hashActual)) {
            throw new UsuarioException("La contraseña actual es incorrecta.");
        }
        if (Arrays.equals(actual, nueva)) {
            throw new UsuarioException("La nueva contraseña debe ser distinta de la actual.");
        }
        validarClaveNueva(nueva, confirmacion, actor.getNombreUsuario());
        dao.actualizarHash(actor.getId(), hash(nueva));
    }


    private String validarNombreUsuario(String texto) throws UsuarioException {
        String usuario = texto == null ? "" : texto.trim();
        if (usuario.isEmpty()) {
            throw new UsuarioException("Ingrese el nombre de usuario.");
        }
        if (!NOMBRE_USUARIO.matcher(usuario).matches()) {
            throw new UsuarioException("El nombre de usuario debe tener entre 3 y 30 caracteres y usar solo "
                    + "letras, números, punto, guion o guion bajo (sin espacios ni tildes).");
        }
        return usuario.toLowerCase(Locale.ROOT);
    }

    private String validarNombreCompleto(String texto) throws UsuarioException {
        String nombre = texto == null ? "" : texto.trim().replaceAll("\\s+", " ");
        if (nombre.isEmpty()) {
            throw new UsuarioException("Ingrese el nombre completo del usuario.");
        }
        if (nombre.length() < 3 || nombre.length() > 100) {
            throw new UsuarioException("El nombre completo debe tener entre 3 y 100 caracteres.");
        }
        return nombre;
    }

    private void validarClaveNueva(char[] clave, char[] confirmacion, String nombreUsuario) throws UsuarioException {
        if (clave == null || confirmacion == null || !Arrays.equals(clave, confirmacion)) {
            throw new UsuarioException("Las contraseñas no coinciden.");
        }
        String problema = PoliticaClave.validar(clave, nombreUsuario);
        if (problema != null) {
            throw new UsuarioException(problema);
        }
    }

    private UsuarioException duplicado(String usuario) {
        return new UsuarioException("Ya existe un usuario con el nombre \"" + usuario + "\".");
    }

    private String hash(char[] clave) {
        return BCrypt.hashpw(new String(clave), BCrypt.gensalt(COSTO_BCRYPT));
    }

    private boolean coincide(char[] clave, String hash) {
        try {
            return BCrypt.checkpw(new String(clave), hash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
