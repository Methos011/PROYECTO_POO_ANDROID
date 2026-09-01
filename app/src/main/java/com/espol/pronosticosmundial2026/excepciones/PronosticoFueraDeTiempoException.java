package com.espol.pronosticosmundial2026.excepciones;

/**
 * Excepción propia y verificada (checked) que se lanza cuando un
 * participante intenta registrar o modificar su pronóstico para un
 * partido que ya no está en estado ABIERTO (es decir, está CERRADO o
 * FINALIZADO). Se lanza y captura dentro de PronosticosActivity, al
 * validar el estado del partido antes de guardar el pronóstico, y su
 * mensaje se muestra al usuario mediante Toast.makeText().
 *
 * @author David Delgado
 */
public class PronosticoFueraDeTiempoException extends Exception {
    /**
     * Crea la excepción con un mensaje que indica que el período para
     * registrar pronósticos de ese partido ya ha finalizado.
     *
     * @param message mensaje descriptivo del error, mostrado al usuario mediante Toast
     */
    public PronosticoFueraDeTiempoException(String message) {
        super(message);
    }
}