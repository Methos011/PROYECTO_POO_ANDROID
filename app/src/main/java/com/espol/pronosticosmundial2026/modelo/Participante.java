package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa a un participante del sistema, un tipo de usuario que puede
 * registrar pronósticos para los partidos del Mundial y competir por
 * puntos con los demás participantes. Hereda de Usuario los atributos
 * comunes (id, username, password, nombreCompleto) y agrega un atributo
 * propio: puntajeAcumulado. Además, implementa la interfaz Comparable
 * para permitir que la aplicación ordene automáticamente una lista de
 * participantes según las reglas de la tabla de posiciones.
 *
 * @author Sebastian Espinoza
 */
public class Participante extends Usuario implements Comparable<Participante> {
    private int puntajeAcumulado;

    /**
     * Crea un nuevo participante. Invoca al constructor de Usuario mediante
     * super() para inicializar los atributos comunes, y luego inicializa
     * su atributo propio (puntajeAcumulado).
     *
     * @param id identificador único del participante
     * @param username nombre de usuario para iniciar sesión
     * @param password contraseña del participante
     * @param nombreCompleto nombre completo del participante
     * @param puntajeAcumulado puntos totales que lleva acumulados el participante
     */
    public Participante(int id, String username, String password, String nombreCompleto, int puntajeAcumulado){
        super(id, username, password, nombreCompleto);
        this.puntajeAcumulado = puntajeAcumulado;
    }

    /** @return el puntaje acumulado por el participante */
    public int getPuntajeAcumulado(){
        return puntajeAcumulado;
    }

    /** @param puntajeAcumulado nuevo puntaje a asignar, usado al actualizar los puntajes */
    public void setPuntajeAcumulado(int puntajeAcumulado){
        this.puntajeAcumulado = puntajeAcumulado;
    }

    /**
     * Genera una representación en texto legible del participante,
     * mostrando su nombre de usuario y su puntaje. Sobrescribe el
     * toString() heredado de Usuario para dar un mensaje más específico
     * a este tipo de usuario.
     *
     * @return el texto formateado con el username y el puntaje acumulado
     */
    @Override
    public String toString(){
        return "Estimado "+getUsername()+", su puntaje es: "+puntajeAcumulado;
    }

    /**
     * Compara este participante con otro según las reglas de la tabla de
     * posiciones: primero por puntaje, de mayor a menor, y si ambos tienen
     * el mismo puntaje, alfabéticamente por nombre de usuario. Al implementar
     * este método de Comparable, la aplicación puede usar Collections.sort()
     * sobre una lista de participantes y obtener automáticamente el orden
     * correcto, sin necesidad de programar un algoritmo de ordenamiento propio.
     *
     * @param otro el participante contra el que se compara
     * @return un número negativo si this va antes que otro, positivo si va
     *         después, o el resultado de comparar los usernames si empatan en puntaje
     */
    @Override
    public int compareTo(Participante otro){
        if(this.puntajeAcumulado > otro.puntajeAcumulado){
            return -1;
        }
        else if(this.puntajeAcumulado < otro.puntajeAcumulado){
            return 1;
        }
        else{
            return this.getUsername().compareTo(otro.getUsername());
        }
    }
}