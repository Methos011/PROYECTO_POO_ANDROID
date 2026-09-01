package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa a un administrador del sistema, un tipo de usuario encargado
 * de gestionar los partidos del torneo: cerrar pronósticos, registrar
 * resultados oficiales y actualizar la tabla de posiciones. Hereda de
 * Usuario los atributos comunes y agrega un atributo propio: cargo,
 * cuyo valor se lee desde el archivo administradores.txt.
 *
 * @author Jair Cárdenas
 */
public class Administrador extends Usuario{
    private String cargo;

    /**
     * Crea un nuevo administrador. Invoca al constructor de Usuario mediante
     * super() para inicializar los atributos comunes, y luego inicializa
     * su atributo propio (cargo).
     *
     * @param id identificador único del administrador
     * @param username nombre de usuario para iniciar sesión
     * @param password contraseña del administrador
     * @param nombreCompleto nombre completo del administrador
     * @param cargo cargo que ocupa dentro de la organización (ej. Administrador General)
     */
    public Administrador(int id, String username, String password, String nombreCompleto, String cargo){
        super(id, username, password, nombreCompleto);
        this.cargo = cargo;
    }

    /** @return el cargo que ocupa el administrador */
    public String getCargo(){
        return cargo;
    }

    /**
     * Genera una representación en texto legible del administrador,
     * mostrando su nombre de usuario y su cargo. Sobrescribe el
     * toString() heredado de Usuario para dar un mensaje más específico
     * a este tipo de usuario, distinto al de Participante — esta
     * diferencia de comportamiento entre subclases que comparten el
     * mismo método heredado es un ejemplo de polimorfismo.
     *
     * @return el texto formateado con el username y el cargo del administrador
     */
    @Override
    public String toString(){
        return "Estimado "+getUsername()+", se lo ha reconocido como Administrador, su cargo es: "+cargo;
    }
}