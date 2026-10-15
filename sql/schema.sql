CREATE DATABASE IF NOT EXISTS smartmarket
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smartmarket;

CREATE TABLE usuario (
    idUsuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombreUsuario   VARCHAR(50)  NOT NULL UNIQUE,
    passwordHash    VARCHAR(60)  NOT NULL,           
    nombreCompleto  VARCHAR(100) NOT NULL,
    rol             ENUM('ADMINISTRADOR','VENTAS','INVENTARIO') NOT NULL,
    activo          TINYINT(1)   NOT NULL DEFAULT 1,
    fechaCreacion   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


CREATE TABLE proveedor (
    idProveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    telefono    VARCHAR(20),
    activo      TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE producto (
    idProducto  INT AUTO_INCREMENT PRIMARY KEY,
    codigo      VARCHAR(30)   NOT NULL UNIQUE,       
    nombre      VARCHAR(100)  NOT NULL,
    precio      DECIMAL(10,2) NOT NULL,
    stock       INT           NOT NULL DEFAULT 0,
    stockMinimo INT           NOT NULL DEFAULT 0,
    activo      TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock  CHECK (stock  >= 0)   
) ENGINE=InnoDB;

CREATE TABLE venta (
    idVenta     INT AUTO_INCREMENT PRIMARY KEY,
    fecha       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total       DECIMAL(10,2) NOT NULL,
    idUsuario   INT           NOT NULL,             
    CONSTRAINT chk_venta_total   CHECK (total >= 0),
    CONSTRAINT fk_venta_usuario  FOREIGN KEY (idUsuario) REFERENCES usuario(idUsuario)
) ENGINE=InnoDB;

CREATE TABLE detalleventa (
    idDetalle       INT AUTO_INCREMENT PRIMARY KEY,
    idVenta         INT           NOT NULL,
    idProducto      INT           NOT NULL,
    cantidad        INT           NOT NULL,
    precioUnitario  DECIMAL(10,2) NOT NULL,          
    subtotal        DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_detalleventa_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_detalleventa_venta     FOREIGN KEY (idVenta)    REFERENCES venta(idVenta),
    CONSTRAINT fk_detalleventa_producto  FOREIGN KEY (idProducto) REFERENCES producto(idProducto)
) ENGINE=InnoDB;

CREATE TABLE ingreso (
    idIngreso   INT AUTO_INCREMENT PRIMARY KEY,
    fecha       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    idProveedor INT          NOT NULL,
    idUsuario   INT          NOT NULL,               
    observacion VARCHAR(200),
    CONSTRAINT fk_ingreso_proveedor FOREIGN KEY (idProveedor) REFERENCES proveedor(idProveedor),
    CONSTRAINT fk_ingreso_usuario   FOREIGN KEY (idUsuario)   REFERENCES usuario(idUsuario)
) ENGINE=InnoDB;

CREATE TABLE detalleingreso (
    idDetalleIngreso INT AUTO_INCREMENT PRIMARY KEY,
    idIngreso        INT NOT NULL,
    idProducto       INT NOT NULL,
    cantidad         INT NOT NULL,
    CONSTRAINT chk_detalleingreso_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_detalleingreso_ingreso   FOREIGN KEY (idIngreso)  REFERENCES ingreso(idIngreso),
    CONSTRAINT fk_detalleingreso_producto  FOREIGN KEY (idProducto) REFERENCES producto(idProducto)
) ENGINE=InnoDB;

CREATE TABLE ajusteinventario (
    idAjuste    INT AUTO_INCREMENT PRIMARY KEY,
    fecha       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    idProducto  INT          NOT NULL,
    idUsuario   INT          NOT NULL,              
    tipo        ENUM('PERDIDA','DANIADO','VENCIDO','CORRECCION') NOT NULL,
    cantidad    INT          NOT NULL,
    motivo      VARCHAR(200),
    CONSTRAINT chk_ajuste_cantidad CHECK (cantidad <> 0),
    CONSTRAINT fk_ajuste_producto  FOREIGN KEY (idProducto) REFERENCES producto(idProducto),
    CONSTRAINT fk_ajuste_usuario   FOREIGN KEY (idUsuario)  REFERENCES usuario(idUsuario)
) ENGINE=InnoDB;


CREATE INDEX idx_venta_fecha   ON venta(fecha);
CREATE INDEX idx_ingreso_fecha ON ingreso(fecha);
CREATE INDEX idx_ajuste_fecha  ON ajusteinventario(fecha);

-- datos de ejemplo
INSERT INTO proveedor (nombre, telefono) VALUES
    ('Distribuidora Andina S.A.C.', '987654321'),
    ('Comercial Lima Norte',        '912345678');

INSERT INTO producto (codigo, nombre, precio, stock, stockMinimo) VALUES
    ('P001', 'Arroz extra 5 kg',      24.50, 40, 10),
    ('P002', 'Aceite vegetal 1 L',    10.90, 30, 10),
    ('P003', 'Leche evaporada 400 g',  4.20, 60, 20),
    ('P004', 'Azúcar rubia 1 kg',      4.80, 50, 15),
    ('P005', 'Detergente 800 g',      12.00, 25,  8);
    
    
    
    