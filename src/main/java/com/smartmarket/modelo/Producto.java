package com.smartmarket.modelo;

import java.math.BigDecimal;

public class Producto {

    private int idProducto;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private int stock;
    private int stockMinimo;
    private boolean activo = true;

    public Producto() {
    }

    public Producto(int idProducto, String codigo, String nombre, BigDecimal precio,
                    int stock, int stockMinimo, boolean activo) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
        this.activo = activo;
    }

    public boolean tieneStockBajo() {
        return stockMinimo > 0 && stock <= stockMinimo;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
