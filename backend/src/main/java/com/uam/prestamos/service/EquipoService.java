package com.uam.prestamos.service;

import com.uam.prestamos.dto.EquipoRequest;
import com.uam.prestamos.dto.EquipoResponse;
import com.uam.prestamos.exception.ConflictoNegocioException;
import com.uam.prestamos.exception.RecursoNoEncontradoException;
import com.uam.prestamos.model.Equipo;
import com.uam.prestamos.model.EstadoEquipo;
import com.uam.prestamos.model.EstadoPrestamo;
import com.uam.prestamos.repository.EquipoRepository;
import com.uam.prestamos.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final PrestamoRepository prestamoRepository;

    public EquipoService(EquipoRepository equipoRepository, PrestamoRepository prestamoRepository) {
        this.equipoRepository = equipoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipoResponse> listar() {
        return equipoRepository.findAll().stream().map(EquipoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public EquipoResponse obtener(Long id) {
        return EquipoResponse.from(buscar(id));
    }

    @Transactional
    public EquipoResponse crear(EquipoRequest req) {
        String codigo = req.codigoPatrimonial();
        if (equipoRepository.existsByCodigoPatrimonial(codigo)) {
            throw codigoDuplicado(codigo);
        }
        Equipo equipo = new Equipo(codigo, req.nombre(), req.tipo());
        equipo.setEstado(EstadoEquipo.DISPONIBLE);
        return EquipoResponse.from(equipoRepository.save(equipo));
    }

    @Transactional
    public EquipoResponse actualizar(Long id, EquipoRequest req) {
        Equipo equipo = buscar(id);
        String codigo = req.codigoPatrimonial();
        if (equipoRepository.existsByCodigoPatrimonialAndIdNot(codigo, id)) {
            throw codigoDuplicado(codigo);
        }
        equipo.setCodigoPatrimonial(codigo);
        equipo.setNombre(req.nombre());
        equipo.setTipo(req.tipo());
        return EquipoResponse.from(equipoRepository.save(equipo));
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = buscar(id);
        if (prestamoRepository.existsByEquipoIdAndEstado(id, EstadoPrestamo.ACTIVO)) {
            throw new ConflictoNegocioException("No se puede eliminar el equipo "
                    + equipo.getCodigoPatrimonial() + " porque tiene préstamos activos");
        }
        equipoRepository.delete(equipo);
    }

    private Equipo buscar(Long id) {
        return equipoRepository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("No existe el equipo con id " + id));
    }

    private ConflictoNegocioException codigoDuplicado(String codigo) {
        return new ConflictoNegocioException("Ya existe un equipo con el código patrimonial " + codigo);
    }
}
