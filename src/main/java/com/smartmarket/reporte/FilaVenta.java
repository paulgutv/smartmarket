package com.smartmarket.reporte;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class FilaVenta {

    private final int idVenta;
    private final Timestamp fecha;
    private final String vendedor;
    private final int unidades;
    private final BigDecimal total;

    public FilaVenta(int idVenta, Timestamp fecha, String vendedor, int unidades, BigDecimal total) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.vendedor = vendedor;
        this.unidades = unidades;
        this.total = total;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public String getVendedor() {
        return vendedor;
    }

    public Integer getUnidades() {
        return unidades;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
