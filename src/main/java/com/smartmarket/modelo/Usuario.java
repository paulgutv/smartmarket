package com.smartmarket.modelo;

public class Usuario {

    private final int id;
    private final String nombreUsuario;
    private final String nombreCompleto;
    private final Rol rol;
    private final boolean activo;

    public Usuario(int id, String nombreUsuario, String nombreCompleto, Rol rol) {
        this(id, nombreUsuario, nombreCompleto, rol, true);
    }

    public Usuario(int id, String nombreUsuario, String nombreCompleto, Rol rol, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
