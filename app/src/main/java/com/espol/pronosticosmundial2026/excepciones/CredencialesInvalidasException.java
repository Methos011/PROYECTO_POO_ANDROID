package com.espol.pronosticosmundial2026.excepciones;

/**
 * Excepción propia y verificada (checked) que se lanza cuando el usuario
 * ingresa un nombre de usuario o contraseña que no coincide con ningún
 * registro de usuarios.txt. Al extender de Exception (y no de
 * RuntimeException), el compilador obliga a manejarla explícitamente con
 * try-catch, lo cual se hace en LoginActivity al validar las credenciales,
 * mostrando el mensaje al usuario mediante Toast.makeText() en vez de
 * dejar que la aplicación se cierre.
 *
 * @author Jair Cárdenas
 */
public class CredencialesInvalidasException extends Exception {
    /**
     * Crea la excepción con un mensaje que indica que el usuario o la
     * contraseña son incorrectos.
     *
     * @param message mensaje descriptivo del error, mostrado al usuario mediante Toast
     */
    public CredencialesInvalidasException(String message){
        super(message);
    }
}