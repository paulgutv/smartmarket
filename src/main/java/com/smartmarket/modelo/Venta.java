package com.smartmarket.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Venta {

    private int idVenta;
    private LocalDateTime fecha;
    private final int idUsuario;
    private BigDecimal total = BigDecimal.ZERO;
    private final List<DetalleVenta> detalles = new ArrayList<>();

    public Venta(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    // si el producto ya estaba en la venta suma la cantidad en vez de duplicar la línea
    public void agregar(DetalleVenta nuevo) {
        for (DetalleVenta d : detalles) {
            if (d.getIdProducto() == nuevo.getIdProducto()) {
                d.setCantidad(d.getCantidad() + nuevo.getCantidad());
                recalcularTotal();
                return;
            }
        }
        detalles.add(nuevo);
        recalcularTotal();
    }

    public void quitar(int indice) {
        detalles.remove(indice);
        recalcularTotal();
    }

    public int cantidadDe(int idProducto) {
        for (DetalleVenta d : detalles) {
            if (d.getIdProducto() == idProducto) {
                return d.getCantidad();
            }
        }
        return 0;
    }

    private void recalcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (DetalleVenta d : detalles) {
            suma = suma.add(d.getSubtotal());
        }
        total = suma;
    }

    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public int getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
