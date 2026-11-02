package com.vetsalud.util;

import org.mindrot.jbcrypt.BCrypt;

/** Encriptación de contraseñas con BCrypt (dos métodos: cifrar y verificar). */
public final class PasswordUtil {

    private PasswordUtil() {}

    public static String cifrar(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt());
    }

    public static boolean verificar(String passwordPlano, String hash) {
        if (passwordPlano == null || hash == null) return false;
        try {
            return BCrypt.checkpw(passwordPlano, hash);
        } catch (IllegalArgumentException e) {
            // El valor guardado en la BD no es un hash BCrypt válido (p. ej. texto plano)
            return false;
        }
    }
    
    public static void main(String[] args) {
    String hash = cifrar("admin123");
    System.out.println("COPIA ESTE HASH EN PHPMYADMIN:");
    System.out.println(hash);
}
}
