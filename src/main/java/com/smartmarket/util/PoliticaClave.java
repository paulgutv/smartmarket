package com.smartmarket.util;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

public final class PoliticaClave {

    public static final int MINIMO = 8;
    public static final int MAXIMO_BYTES = 72;

    public static final String REGLAS = "Mínimo 8 caracteres, con al menos una letra y un número.";

    private static final Set<String> COMUNES = Set.of(
            "password1", "contraseña1", "contrasena1", "admin1234", "admin123", "qwerty123",
            "abc12345", "12345678a", "a12345678", "123456789a", "smartmarket1", "minimarket1");

    private PoliticaClave() {
    }


    public static String validar(char[] clave, String nombreUsuario) {
        if (clave == null || clave.length == 0) {
            return "Ingrese la contraseña.";
        }
        if (clave.length < MINIMO) {
            return "La contraseña debe tener al menos " + MINIMO + " caracteres.";
        }
        String texto = new String(clave);
        if (texto.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES) {
            return "La contraseña es demasiado larga (máximo 72 caracteres).";
        }
        boolean hayLetra = false;
        boolean hayNumero = false;
        for (char c : clave) {
            if (Character.isLetter(c)) {
                hayLetra = true;
            } else if (Character.isDigit(c)) {
                hayNumero = true;
            }
        }
        if (!hayLetra) {
            return "La contraseña debe incluir al menos una letra.";
        }
        if (!hayNumero) {
            return "La contraseña debe incluir al menos un número.";
        }
        if (nombreUsuario != null && texto.equalsIgnoreCase(nombreUsuario)) {
            return "La contraseña no puede ser igual al nombre de usuario.";
        }
        if (COMUNES.contains(texto.toLowerCase(Locale.ROOT))) {
            return "Esa contraseña es demasiado común. Elija otra.";
        }
        return null;
    }
}
