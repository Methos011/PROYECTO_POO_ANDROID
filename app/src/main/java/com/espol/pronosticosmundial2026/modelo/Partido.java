package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa un partido de la Copa Mundial FIFA 2026. Contiene la
 * información básica del encuentro (fase, fecha, hora, estadio y las
 * dos selecciones que se enfrentan) junto con su estado actual, que
 * determina qué acciones puede realizar el administrador y el
 * participante sobre él en cada momento (ABIERTO, CERRADO o FINALIZADO).
 * No hereda de ninguna otra clase del modelo: es una clase independiente
 * que se relaciona con Pronostico y Resultado a través de su id.
 *
 * @author David Delgado
 */
public class Partido {
    private int id;
    private Fase fase;
    private String fecha;
    private String hora;
    private String estadio;
    private String seleccion1;
    private String seleccion2;
    private EstadoPartido estado;

    /**
     * Crea un nuevo partido con todos sus datos, incluyendo su estado
     * inicial. Estos objetos se construyen a partir de la información
     * leída línea por línea desde el archivo partidos.txt.
     *
     * @param id identificador único del partido
     * @param fase fase del torneo a la que pertenece el partido
     * @param fecha fecha en la que se disputa el partido
     * @param hora hora en la que se disputa el partido
     * @param estadio estadio donde se disputa el partido
     * @param seleccion1 nombre de la primera selección
     * @param seleccion2 nombre de la segunda selección
     * @param estado estado actual del partido (ABIERTO, CERRADO o FINALIZADO)
     */
    public Partido(int id, Fase fase, String fecha, String hora, String estadio, String seleccion1, String seleccion2, EstadoPartido estado){
        this.id = id;
        this.fase = fase;
        this.fecha = fecha;
        this.hora = hora;
        this.estadio = estadio;
        this.seleccion1 = seleccion1;
        this.seleccion2 = seleccion2;
        this.estado  = estado;
    }

    /** @return el identificador único del partido */
    public int getPartidoId(){
        return id;
    }
    /** @return la fase del torneo a la que pertenece el partido */
    public Fase getFase(){
        return fase;
    }
    /** @return la fecha del partido */
    public String getFecha(){
        return fecha;
    }
    /** @return la hora del partido */
    public String getHora(){
        return hora;
    }
    /** @return el estadio donde se disputa el partido */
    public String getEstadio(){
        return estadio;
    }
    /** @return el nombre de la primera selección */
    public String getSeleccion1(){
        return seleccion1;
    }
    /** @return el nombre de la segunda selección */
    public String getSeleccion2(){
        return seleccion2;
    }
    /** @return el estado actual del partido */
    public EstadoPartido getEstado(){
        return estado;
    }

    /**
     * Cambia el estado del partido. Se usa cuando el administrador cierra
     * los pronósticos (ABIERTO a CERRADO) o registra el resultado oficial
     * (CERRADO a FINALIZADO), y el cambio se persiste reescribiendo
     * partidos.txt para que se conserve en futuras ejecuciones de la app.
     *
     * @param estado nuevo estado a asignar al partido
     */
    public void setEstado(EstadoPartido estado){
        this.estado = estado;
    }
}