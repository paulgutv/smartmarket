package com.smartmarket.modelo;

import java.math.BigDecimal;

public class DetalleVenta {

    private final int idProducto;
    private final String codigo;
    private final String nombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleVenta(int idProducto, String codigo, String nombre, int cantidad, BigDecimal precioUnitario) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    private void recalcularSubtotal() {
        subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        recalcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
