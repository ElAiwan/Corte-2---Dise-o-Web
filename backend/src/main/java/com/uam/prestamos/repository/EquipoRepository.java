package com.uam.prestamos.repository;

import com.uam.prestamos.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    /** Para validar el código patrimonial único al crear un equipo. */
    boolean existsByCodigoPatrimonial(String codigoPatrimonial);

    /** Para validar el código único al actualizar, excluyendo al propio equipo. */
    boolean existsByCodigoPatrimonialAndIdNot(String codigoPatrimonial, Long id);
}
