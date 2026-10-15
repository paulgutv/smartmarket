package com.smartmarket.util;

import com.smartmarket.modelo.Usuario;

public final class Sesion {

    private static Usuario usuarioActual;

    private Sesion() {
    }

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static void cerrar() {
        usuarioActual = null;
    }
}
