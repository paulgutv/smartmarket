package com.smartmarket.util;

import org.mindrot.jbcrypt.BCrypt;

// para crear los hash que se pondrán como password
public class GeneradorHash {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Indique la contraseña como argumento del programa.");
            return;
        }
        System.out.println(BCrypt.hashpw(args[0], BCrypt.gensalt(10)));
    }
}
