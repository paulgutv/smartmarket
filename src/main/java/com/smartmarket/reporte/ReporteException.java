package com.smartmarket.reporte;

public class ReporteException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReporteException(String mensaje) {
        super(mensaje);
    }

    public ReporteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
