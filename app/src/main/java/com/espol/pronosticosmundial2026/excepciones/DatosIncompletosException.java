package com.espol.pronosticosmundial2026.excepciones;

/**
 * Excepción propia y verificada (checked) que se lanza cuando faltan
 * datos o estos son inválidos al registrar un pronóstico o un resultado
 * oficial (por ejemplo, campos de goles vacíos o con valores negativos).
 * Se usa tanto en PronosticosActivity como en AdministrarPartidosActivity,
 * y en ambos casos se captura para mostrar el mensaje al usuario mediante
 * Toast.makeText() sin exponer la excepción cruda ni cerrar la aplicación.
 *
 * @author Jair Cárdenas
 */
public class DatosIncompletosException extends Exception {
    /**
     * Crea la excepción con un mensaje que indica qué dato falta o es inválido.
     *
     * @param message mensaje descriptivo del error, mostrado al usuario mediante Toast
     */
    public DatosIncompletosException(String message) {
        super(message);
    }
}