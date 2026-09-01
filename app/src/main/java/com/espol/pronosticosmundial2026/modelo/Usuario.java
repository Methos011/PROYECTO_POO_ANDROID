package com.espol.pronosticosmundial2026.modelo;

/**
 * Representa a un usuario del sistema de Pronósticos Mundial 2026. Es una
 * clase abstracta que agrupa los atributos comunes a todo usuario de la
 * aplicación (id, credenciales de acceso y nombre completo), sin importar
 * si es Participante o Administrador. Nunca se crea un objeto Usuario
 * directamente: siempre se instancia a través de una de sus subclases,
 * que son las que definen el comportamiento específico de cada rol
 * mediante encapsulamiento y herencia.
 *
 * @author Jair Cárdenas
 */
public abstract class Usuario {
    private int id;
    private String username;
    private String password;
    private String nombreCompleto;

    /**
     * Crea un nuevo usuario con sus datos básicos. Este constructor es
     * invocado desde las subclases (Participante, Administrador) mediante
     * super(), para inicializar los atributos comunes antes de que cada
     * subclase agregue los suyos propios.
     *
     * @param id identificador único del usuario, tomado de usuarios.txt
     * @param username nombre de usuario para iniciar sesión
     * @param password contraseña del usuario, usada para validar el login
     * @param nombreCompleto nombre completo del usuario, mostrado en pantalla
     */
    public Usuario(int id, String username, String password, String nombreCompleto){
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
    }

    /** @return el identificador único del usuario */
    public int getId(){
        return id;
    }
    /** @return el nombre de usuario usado para iniciar sesión */
    public String getUsername(){
        return username;
    }
    /** @return la contraseña del usuario */
    public String getPassword(){
        return password;
    }
    /** @return el nombre completo del usuario */
    public String getNombreCompleto(){
        return nombreCompleto;
    }

    /**
     * Genera una representación en texto legible del usuario, mostrando
     * su nombre de usuario y su nombre completo. Sobrescribe el toString()
     * genérico de Object para que sea útil al momento de depurar o mostrar
     * información básica del usuario.
     *
     * @return el texto formateado con el username y el nombre completo
     */
    @Override
    public String toString(){
        return "Usuario: "+username+" - "+nombreCompleto;
    }
}