package com.uam.prestamos.controller;

import com.uam.prestamos.exception.GlobalExceptionHandler;
import com.uam.prestamos.model.Equipo;
import com.uam.prestamos.model.EstadoEquipo;
import com.uam.prestamos.model.EstadoPrestamo;
import com.uam.prestamos.model.Prestamo;
import com.uam.prestamos.model.TipoEquipo;
import com.uam.prestamos.repository.EquipoRepository;
import com.uam.prestamos.repository.PrestamoRepository;
import com.uam.prestamos.security.JwtAuthenticationFilter;
import com.uam.prestamos.service.EquipoService;
import com.uam.prestamos.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP y servicios reales; solo la persistencia está sustituida con Mockito. */
/* La seguridad se prueba aparte: aquí se excluye el filtro JWT para probar solo HTTP + servicios. */
@WebMvcTest(controllers = {EquipoController.class, PrestamoController.class},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@Import({EquipoService.class, PrestamoService.class, GlobalExceptionHandler.class})
class CrudApiTest {

    private static final String EQUIPO_JSON = """
            {"codigoPatrimonial":"UAM-LAP-010","nombre":"Laptop Lenovo ThinkPad E14","tipo":"LAPTOP"}
            """;
    private static final String PRESTAMO_JSON = """
            {"equipoId":7,"nombreSolicitante":"Instructora Ana Ruiz",
             "fechaPrestamo":"2026-10-28","fechaDevolucionPrevista":"2026-10-31"}
            """;

    @Autowired
    private MockMvc mvc;
    @MockBean
    private EquipoRepository equipos;
    @MockBean
    private PrestamoRepository prestamos;

    private Equipo equipo;
    private Prestamo prestamo;

    @BeforeEach
    void preparar() {
        equipo = new Equipo("UAM-LAP-010", "Laptop Lenovo ThinkPad E14", TipoEquipo.LAPTOP);
        equipo.setId(7L);
        prestamo = new Prestamo(equipo, "Instructora Ana Ruiz",
                LocalDate.of(2026, 10, 28), LocalDate.of(2026, 10, 31));
        prestamo.setId(2L);
        when(equipos.findById(7L)).thenReturn(Optional.of(equipo));
        when(prestamos.findById(2L)).thenReturn(Optional.of(prestamo));
        when(equipos.save(any(Equipo.class))).thenAnswer(invocation -> {
            Equipo guardado = invocation.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId(7L);
            }
            return guardado;
        });
        when(prestamos.save(any(Prestamo.class))).thenAnswer(invocation -> {
            Prestamo guardado = invocation.getArgument(0);
            if (guardado.getId() == null) {
                guardado.setId(2L);
            }
            return guardado;
        });
    }

    @Test
    void listarEquiposDevuelve200YSeisElementos() throws Exception {
        when(equipos.findAll()).thenReturn(LongStream.rangeClosed(1, 6).mapToObj(id -> {
            Equipo e = new Equipo("UAM-LAP-00" + id, "Equipo " + id, TipoEquipo.LAPTOP);
            e.setId(id);
            return e;
        }).toList());
        mvc.perform(get("/api/equipos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].prestamos").doesNotExist());
    }

    @Test
    void equipoInexistenteDevuelve404ConFormatoComun() throws Exception {
        mvc.perform(get("/api/equipos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No existe el equipo con id 999"))
                .andExpect(jsonPath("$.path").value("/api/equipos/999"))
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    void crearEquipoDevuelve201LocationYDisponible() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(EQUIPO_JSON))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/equipos/7"))
                .andExpect(jsonPath("$.codigoPatrimonial").value("UAM-LAP-010"))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void codigoDuplicadoSeNormalizaAntesDeConsultarYDevuelve409() throws Exception {
        when(equipos.existsByCodigoPatrimonial("UAM-LAP-010")).thenReturn(true);
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content(EQUIPO_JSON.replace("UAM-LAP-010", "  uam-lap-010  ")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Ya existe un equipo con el código patrimonial UAM-LAP-010"));
        verify(equipos, never()).save(any());
    }

    @Test
    void nombreVacioDevuelve400ConErrorDeCampo() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content(EQUIPO_JSON.replace("Laptop Lenovo ThinkPad E14", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Datos inválidos"))
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"));
        verify(equipos, never()).save(any());
    }

    @Test
    void tipoDesconocidoDevuelve400() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content(EQUIPO_JSON.replace("LAPTOP", "CELULAR")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    void actualizarDevuelve200YConservaEstadoPrestado() throws Exception {
        equipo.setEstado(EstadoEquipo.PRESTADO);
        mvc.perform(put("/api/equipos/7").contentType(MediaType.APPLICATION_JSON)
                        .content(EQUIPO_JSON.replace("Laptop Lenovo ThinkPad E14", "Equipo actualizado")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Equipo actualizado"))
                .andExpect(jsonPath("$.estado").value("PRESTADO"));
    }

    @Test
    void registrarPrestamoDevuelve201YCambiaEquipoAPrestado() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO_JSON))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/prestamos/2"))
                .andExpect(jsonPath("$.equipoId").value(7))
                .andExpect(jsonPath("$.equipoCodigo").value("UAM-LAP-010"))
                .andExpect(jsonPath("$.equipoNombre").value("Laptop Lenovo ThinkPad E14"))
                .andExpect(jsonPath("$.fechaPrestamo").value("2026-10-28"))
                .andExpect(jsonPath("$.fechaDevolucionReal").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
        mvc.perform(get("/api/equipos/7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PRESTADO"));
        verify(equipos).save(equipo);
    }

    @Test
    void repetirPrestamoDevuelve409() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO_JSON))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El equipo UAM-LAP-010 ya se encuentra PRESTADO"));
    }

    @Test
    void fechasInvertidasDevuelven400SinGuardar() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(PRESTAMO_JSON.replace("2026-10-31", "2026-10-27")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "La fecha prevista de devolución no puede ser anterior a la fecha del préstamo"));
        verify(equipos, never()).findById(any());
        verify(prestamos, never()).save(any());
    }

    @Test
    void eliminarEquipoConPrestamoActivoDevuelve409() throws Exception {
        equipo.setId(4L);
        equipo.setCodigoPatrimonial("UAM-PRO-002");
        when(equipos.findById(4L)).thenReturn(Optional.of(equipo));
        when(prestamos.existsByEquipoIdAndEstado(4L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        mvc.perform(delete("/api/equipos/4")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "No se puede eliminar el equipo UAM-PRO-002 porque tiene préstamos activos"));
        verify(equipos, never()).delete(any());
    }

    @Test
    void devolverPrestamoDevuelve200YCambiaEquipoADisponible() throws Exception {
        equipo.setEstado(EstadoEquipo.PRESTADO);
        mvc.perform(put("/api/prestamos/2/devolver"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("DEVUELTO"))
                .andExpect(jsonPath("$.fechaDevolucionReal").value(LocalDate.now().toString()));
        mvc.perform(get("/api/equipos/7"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("DISPONIBLE"));
        verify(equipos).save(equipo);
        verify(prestamos).save(prestamo);
    }

    @Test
    void repetirDevolucionDevuelve409() throws Exception {
        mvc.perform(put("/api/prestamos/2/devolver")).andExpect(status().isOk());
        mvc.perform(put("/api/prestamos/2/devolver"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El préstamo 2 ya fue devuelto"));
    }

    @Test
    void eliminarSinPrestamosActivosDevuelve204SinCuerpo() throws Exception {
        mvc.perform(delete("/api/equipos/7"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(equipos).delete(equipo);
    }

    @Test
    void filtrarPrestamosActivosDevuelve200() throws Exception {
        when(prestamos.findByEstado(EstadoPrestamo.ACTIVO)).thenReturn(List.of(prestamo));
        mvc.perform(get("/api/prestamos").param("estado", "ACTIVO"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
        verify(prestamos, never()).findAll();
    }

    @Test
    void prestamoActivoBloqueaRegistroInclusoSiEquipoFiguraDisponible() throws Exception {
        when(prestamos.existsByEquipoIdAndEstado(7L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content(PRESTAMO_JSON))
                .andExpect(status().isConflict());
        verify(prestamos, never()).save(any());
        assertThat(equipo.getEstado()).isEqualTo(EstadoEquipo.DISPONIBLE);
    }

    @Test
    void actualizarConCodigoDeOtroEquipoDevuelve409() throws Exception {
        when(equipos.existsByCodigoPatrimonialAndIdNot("UAM-LAP-010", 7L)).thenReturn(true);
        mvc.perform(put("/api/equipos/7").contentType(MediaType.APPLICATION_JSON).content(EQUIPO_JSON))
                .andExpect(status().isConflict());
        verify(equipos, never()).save(any());
    }

    @Test
    void registrarConEquipoInexistenteDevuelve404() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(PRESTAMO_JSON.replace("\"equipoId\":7", "\"equipoId\":999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el equipo con id 999"));
    }

    @Test
    void prestamoInexistenteDevuelve404AlConsultarYDevolver() throws Exception {
        mvc.perform(get("/api/prestamos/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe el préstamo con id 999"));
        mvc.perform(put("/api/prestamos/999/devolver")).andExpect(status().isNotFound());
    }

    @Test
    void parametrosInvalidosDevuelven400() throws Exception {
        mvc.perform(get("/api/equipos/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parámetro inválido: id"));
        mvc.perform(get("/api/prestamos").param("estado", "XYZ")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parámetro inválido: estado"));
    }

    @Test
    void camposObligatoriosYEquipoPositivoSeValidan() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.equipoId").exists())
                .andExpect(jsonPath("$.errores.nombreSolicitante").exists())
                .andExpect(jsonPath("$.errores.fechaPrestamo").exists())
                .andExpect(jsonPath("$.errores.fechaDevolucionPrevista").exists());
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(PRESTAMO_JSON.replace("\"equipoId\":7", "\"equipoId\":0")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.equipoId").exists());
    }

    @Test
    void longitudesMaximasSeValidan() throws Exception {
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON)
                        .content(EQUIPO_JSON.replace("UAM-LAP-010", "X".repeat(31))
                                .replace("Laptop Lenovo ThinkPad E14", "N".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.codigoPatrimonial").exists())
                .andExpect(jsonPath("$.errores.nombre").exists());
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(PRESTAMO_JSON.replace("Instructora Ana Ruiz", "N".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreSolicitante").exists());
    }

    @Test
    void fechaMalFormadaYJsonIncompletoDevuelven400() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(PRESTAMO_JSON.replace("2026-10-28", "28/10/2026")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fechasIgualesSonValidas() throws Exception {
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .content(PRESTAMO_JSON.replace("2026-10-31", "2026-10-28")))
                .andExpect(status().isCreated());
    }

    @Test
    void listarSinFiltroYObtenerPrestamoDevuelvenDtos() throws Exception {
        when(prestamos.findAll()).thenReturn(List.of(prestamo));
        mvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].equipoId").value(7))
                .andExpect(jsonPath("$[0].equipo").doesNotExist());
        mvc.perform(get("/api/prestamos/2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/ruta-inexistente")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ruta no encontrada"));
    }

    @Test
    void restriccionDeBaseDeDatosDevuelve409SinFiltrarDetalle() throws Exception {
        when(equipos.save(any(Equipo.class)))
                .thenThrow(new DataIntegrityViolationException("detalle SQL privado"));
        mvc.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content(EQUIPO_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("La operación viola una restricción de la base de datos"))
                .andExpect(jsonPath("$.errores").doesNotExist());
    }

    @Test
    void errorInesperadoDevuelve500SinFiltrarDetalle() throws Exception {
        when(equipos.findAll()).thenThrow(new IllegalStateException("detalle privado"));
        mvc.perform(get("/api/equipos")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Error interno del servidor"))
                .andExpect(jsonPath("$.errores").doesNotExist());
    }
}
