package com.vetsalud.util;

import java.util.regex.Pattern;

/** Validaciones manuales (el proyecto no permite Hibernate Validator). */
public final class ValidationUtil {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private ValidationUtil() {}

    /** Obligatorio: no nulo, no vacío y con longitud máxima. Devuelve el texto sin espacios sobrantes. */
    public static String requerido(String valor, String campo, int max) {
        if (valor == null || valor.trim().isEmpty()) {
            throw ApiException.badRequest("El campo '" + campo + "' es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw ApiException.badRequest("El campo '" + campo + "' no puede superar " + max + " caracteres");
        }
        return limpio;
    }

    /** Opcional: devuelve null si viene vacío. */
    public static String opcional(String valor, String campo, int max) {
        if (valor == null || valor.trim().isEmpty()) return null;
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw ApiException.badRequest("El campo '" + campo + "' no puede superar " + max + " caracteres");
        }
        return limpio;
    }

    public static String emailOpcional(String valor) {
        String email = opcional(valor, "email", 255);
        if (email != null && !EMAIL.matcher(email).matches()) {
            throw ApiException.badRequest("El email no tiene un formato válido");
        }
        return email;
    }
}
