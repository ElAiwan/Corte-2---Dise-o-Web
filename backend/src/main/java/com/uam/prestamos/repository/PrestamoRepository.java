package com.uam.prestamos.repository;

import com.uam.prestamos.model.EstadoPrestamo;
import com.uam.prestamos.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /** ¿El equipo tiene un préstamo en ese estado? Se usa para bloquear préstamos y eliminaciones (409). */
    boolean existsByEquipoIdAndEstado(Long equipoId, EstadoPrestamo estado);

    /** Filtro GET /api/prestamos?estado=ACTIVO */
    List<Prestamo> findByEstado(EstadoPrestamo estado);
}
