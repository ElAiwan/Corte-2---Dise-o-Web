package com.uam.prestamos.service;

import com.uam.prestamos.dto.PrestamoRequest;
import com.uam.prestamos.dto.PrestamoResponse;
import com.uam.prestamos.exception.ConflictoNegocioException;
import com.uam.prestamos.exception.RecursoNoEncontradoException;
import com.uam.prestamos.exception.SolicitudInvalidaException;
import com.uam.prestamos.model.Equipo;
import com.uam.prestamos.model.EstadoEquipo;
import com.uam.prestamos.model.EstadoPrestamo;
import com.uam.prestamos.model.Prestamo;
import com.uam.prestamos.repository.EquipoRepository;
import com.uam.prestamos.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final EquipoRepository equipoRepository;

    public PrestamoService(PrestamoRepository prestamoRepository, EquipoRepository equipoRepository) {
        this.prestamoRepository = prestamoRepository;
        this.equipoRepository = equipoRepository;
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar(EstadoPrestamo estado) {
        List<Prestamo> prestamos = estado == null
                ? prestamoRepository.findAll() : prestamoRepository.findByEstado(estado);
        return prestamos.stream().map(PrestamoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id) {
        return PrestamoResponse.from(buscar(id));
    }

    @Transactional
    public PrestamoResponse registrar(PrestamoRequest req) {
        if (req.fechaDevolucionPrevista().isBefore(req.fechaPrestamo())) {
            throw new SolicitudInvalidaException(
                    "La fecha prevista de devolución no puede ser anterior a la fecha del préstamo");
        }
        Equipo equipo = equipoRepository.findById(req.equipoId()).orElseThrow(() ->
                new RecursoNoEncontradoException("No existe el equipo con id " + req.equipoId()));
        if (equipo.getEstado() == EstadoEquipo.PRESTADO
                || prestamoRepository.existsByEquipoIdAndEstado(req.equipoId(), EstadoPrestamo.ACTIVO)) {
            throw new ConflictoNegocioException("El equipo "
                    + equipo.getCodigoPatrimonial() + " ya se encuentra PRESTADO");
        }
        Prestamo prestamo = new Prestamo(equipo, req.nombreSolicitante(),
                req.fechaPrestamo(), req.fechaDevolucionPrevista());
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        prestamo.setFechaDevolucionReal(null);
        equipo.setEstado(EstadoEquipo.PRESTADO);
        equipoRepository.save(equipo);
        return PrestamoResponse.from(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponse devolver(Long id) {
        Prestamo prestamo = buscar(id);
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ConflictoNegocioException("El préstamo " + id + " ya fue devuelto");
        }
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamo.getEquipo().setEstado(EstadoEquipo.DISPONIBLE);
        equipoRepository.save(prestamo.getEquipo());
        return PrestamoResponse.from(prestamoRepository.save(prestamo));
    }

    private Prestamo buscar(Long id) {
        return prestamoRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("No existe el préstamo con id " + id));
    }
}
