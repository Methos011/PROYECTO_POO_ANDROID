package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa las distintas fases del torneo de la Copa Mundial FIFA 2026,
 * en el orden en que se disputan. Se utiliza tanto para clasificar cada
 * Partido según la fase a la que pertenece, como para armar el nombre de
 * los archivos de pronósticos serializados (pronostico_idusuario_fase.dat),
 * y para poblar el Spinner de selección de fase en PronosticosActivity y
 * AdministrarPartidosActivity.
 *
 * @author David Delgado
 */
public enum Fase {
    FASE_DE_GRUPOS,
    DIECISEISAVOS_DE_FINAL,
    OCTAVOS_DE_FINAL,
    CUARTOS_DE_FINAL,
    SEMIFINALES,
    TERCER_LUGAR,
    FINAL
}