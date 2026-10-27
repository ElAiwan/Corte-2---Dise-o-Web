-- =====================================================================
-- Sistema de préstamo de equipos tecnológicos - TecnoUAM
-- Script para reproducir la base de datos de la demostración (MySQL 8).
--
-- La aplicación también crea las tablas sola (ddl-auto=update) y carga
-- estos mismos datos con data.sql; este script es la alternativa manual.
--
-- Cuentas de prueba (admin / usuario): NO se insertan aquí. Las crea la
-- aplicación al arrancar (DataInitializer) porque sus contraseñas se
-- guardan cifradas con BCrypt.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS prestamos_db;
USE prestamos_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(100) NOT NULL,
    rol      VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS equipos (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    codigo_patrimonial VARCHAR(30)  NOT NULL,
    nombre             VARCHAR(100) NOT NULL,
    tipo               VARCHAR(20)  NOT NULL,
    estado             VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_equipos_codigo_patrimonial UNIQUE (codigo_patrimonial)
);

CREATE TABLE IF NOT EXISTS prestamos (
    id                        BIGINT       NOT NULL AUTO_INCREMENT,
    equipo_id                 BIGINT       NOT NULL,
    nombre_solicitante        VARCHAR(100) NOT NULL,
    fecha_prestamo            DATE         NOT NULL,
    fecha_devolucion_prevista DATE         NOT NULL,
    fecha_devolucion_real     DATE         NULL,
    estado                    VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_prestamos_equipo FOREIGN KEY (equipo_id) REFERENCES equipos (id)
);

-- Datos mínimos (mismos que backend/src/main/resources/data.sql)
INSERT IGNORE INTO equipos (id, codigo_patrimonial, nombre, tipo, estado) VALUES
 (1, 'UAM-LAP-001', 'Laptop Dell Latitude 5420',     'LAPTOP',    'DISPONIBLE'),
 (2, 'UAM-LAP-002', 'Laptop HP ProBook 440 G8',      'LAPTOP',    'DISPONIBLE'),
 (3, 'UAM-PRO-001', 'Proyector Epson PowerLite X49', 'PROYECTOR', 'DISPONIBLE'),
 (4, 'UAM-PRO-002', 'Proyector BenQ MS560',          'PROYECTOR', 'PRESTADO'),
 (5, 'UAM-TAB-001', 'Tableta Samsung Galaxy Tab A8', 'TABLETA',   'DISPONIBLE'),
 (6, 'UAM-TAB-002', 'Tableta Lenovo Tab M10',        'TABLETA',   'DISPONIBLE');

INSERT IGNORE INTO prestamos (id, equipo_id, nombre_solicitante, fecha_prestamo, fecha_devolucion_prevista, fecha_devolucion_real, estado) VALUES
 (1, 4, 'Instructor Carlos Méndez', '2026-10-27', '2026-10-30', NULL, 'ACTIVO');
