package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa el resultado oficial de un partido, una vez que este ha
 * finalizado. Es una clase independiente que se relaciona con Partido
 * a través de su idPartido, y se compara contra cada Pronostico para
 * calcular los puntos obtenidos por los participantes. Los objetos de
 * esta clase se guardan en el archivo resultados.txt como texto plano
 * (a diferencia de Pronostico, que se guarda por serialización).
 *
 * @author David Delgado
 */
public class Resultado {
    private int idResultado;
    private int idPartido;
    private int golesSeleccion1;
    private int golesSeleccion2;

    /**
     * Crea un nuevo resultado oficial. Se construye cuando el administrador
     * registra el marcador final de un partido en AdministrarPartidosActivity,
     * pasando este a estado FINALIZADO.
     *
     * @param idResultado identificador del resultado
     * @param idPartido id del partido al que pertenece el resultado
     * @param golesSeleccion1 goles anotados por la primera selección
     * @param golesSeleccion2 goles anotados por la segunda selección
     */
    public Resultado(int idResultado, int idPartido, int golesSeleccion1, int golesSeleccion2){
        this.idResultado = idResultado;
        this.idPartido = idPartido;
        this.golesSeleccion1 = golesSeleccion1;
        this.golesSeleccion2 = golesSeleccion2;
    }

    /** @return el identificador del resultado */
    public int getIdResultado(){
        return idResultado;
    }
    /** @return el id del partido al que pertenece el resultado */
    public int getIdPartido(){
        return idPartido;
    }
    /** @return los goles anotados por la primera selección */
    public int getGolesSeleccion1R(){
        return golesSeleccion1;
    }
    /** @return los goles anotados por la segunda selección */
    public int getGolesSeleccion2R(){
        return golesSeleccion2;
    }
}