-- Datos mínimos para la demostración.
-- PostgreSQL: ON CONFLICT DO NOTHING evita duplicados al reiniciar.
-- Los usuarios NO van aquí: los crea DataInitializer con la contraseña cifrada en BCrypt.

INSERT INTO equipos (id, codigo_patrimonial, nombre, tipo, estado) VALUES
 (1, 'UAM-LAP-001', 'Laptop Dell Latitude 5420',     'LAPTOP',    'DISPONIBLE'),
 (2, 'UAM-LAP-002', 'Laptop HP ProBook 440 G8',      'LAPTOP',    'DISPONIBLE'),
 (3, 'UAM-PRO-001', 'Proyector Epson PowerLite X49', 'PROYECTOR', 'DISPONIBLE'),
 (4, 'UAM-PRO-002', 'Proyector BenQ MS560',          'PROYECTOR', 'PRESTADO'),
 (5, 'UAM-TAB-001', 'Tableta Samsung Galaxy Tab A8', 'TABLETA',   'DISPONIBLE'),
 (6, 'UAM-TAB-002', 'Tableta Lenovo Tab M10',        'TABLETA',   'DISPONIBLE')
ON CONFLICT DO NOTHING;

INSERT INTO prestamos (id, equipo_id, nombre_solicitante, fecha_prestamo, fecha_devolucion_prevista, fecha_devolucion_real, estado) VALUES
 (1, 4, 'Instructor Carlos Méndez', '2026-10-27', '2026-10-30', NULL, 'ACTIVO')
ON CONFLICT DO NOTHING;

-- Los ids explícitos de la semilla no adelantan las secuencias IDENTITY.
-- Se conserva también su valor previo para no reutilizar ids eliminados.
SELECT setval(pg_get_serial_sequence('equipos', 'id'),
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM equipos),
             COALESCE(pg_sequence_last_value(pg_get_serial_sequence('equipos', 'id')::regclass), 1)), true);
SELECT setval(pg_get_serial_sequence('prestamos', 'id'),
    GREATEST((SELECT COALESCE(MAX(id), 1) FROM prestamos),
             COALESCE(pg_sequence_last_value(pg_get_serial_sequence('prestamos', 'id')::regclass), 1)), true);
