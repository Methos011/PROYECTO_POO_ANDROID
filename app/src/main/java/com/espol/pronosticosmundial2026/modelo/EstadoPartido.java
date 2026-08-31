package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa los posibles estados en los que puede encontrarse un
 * partido durante el torneo. El estado determina qué acciones están
 * disponibles tanto para el participante (registrar/modificar su
 * pronóstico) como para el administrador (cerrar pronósticos, registrar
 * el resultado oficial). El flujo siempre avanza en una sola dirección:
 * ABIERTO → CERRADO → FINALIZADO.
 *
 * @author David Delgado
 */
public enum EstadoPartido {
    /** El partido aún no se ha jugado; los participantes pueden registrar o modificar su pronóstico. */
    ABIERTO,
    /** Los pronósticos ya no pueden modificarse; el partido puede estar en desarrollo o por comenzar. */
    CERRADO,
    /** El partido concluyó y su resultado oficial ya fue registrado por el administrador. */
    FINALIZADO
}