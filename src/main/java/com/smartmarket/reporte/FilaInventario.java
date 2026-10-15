package com.smartmarket.reporte;

import java.math.BigDecimal;

public class FilaInventario {

    private final String codigo;
    private final String nombre;
    private final BigDecimal precio;
    private final int stock;
    private final int stockMinimo;

    public FilaInventario(String codigo, String nombre, BigDecimal precio, int stock, int stockMinimo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public String getEstado() {
        return stockMinimo > 0 && stock <= stockMinimo ? "BAJO" : "OK";
    }

    public BigDecimal getValor() {
        return precio.multiply(BigDecimal.valueOf(stock));
    }
}
