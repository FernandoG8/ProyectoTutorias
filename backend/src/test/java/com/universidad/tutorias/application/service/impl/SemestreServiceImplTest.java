package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.TestDataBuilder;
import com.universidad.tutorias.application.dto.semestre.ActualizarSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.CrearSemestreDTO;
import com.universidad.tutorias.application.dto.semestre.EstadisticasSemestreDTO;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.exception.SemestreNotFoundException;
import com.universidad.tutorias.domain.exception.SemestreValidationException;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.SemestreRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test para SemestreServiceImpl
 * Prueba operaciones CRUD, validaciones y estadísticas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SemestreServiceImpl Tests")
class SemestreServiceImplTest {

    @Mock
    private SemestreRepository semestreRepository;

    @Mock
    private AsignacionRepository asignacionRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private SemestreServiceImpl semestreService;

    private Semestre semestre;
    private CrearSemestreDTO crearDTO;
    private ActualizarSemestreDTO actualizarDTO;

    @BeforeEach
    void setUp() {
        semestre = TestDataBuilder.semestre()
                .id(1L)
                .codigo("2025-2026-F1")
                .nombre("Semestre Agosto 2025 - Enero 2026")
                .fechaInicio(LocalDate.of(2025, 8, 1))
                .fechaFin(LocalDate.of(2026, 1, 31))
                .activo(false)
                .build();

        crearDTO = new CrearSemestreDTO(
                "2025-2026-F1",
                "Semestre Agosto 2025 - Enero 2026",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2026, 1, 31)
        );

        actualizarDTO = new ActualizarSemestreDTO(
                "Semestre Actualizado",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2026, 2, 28)
        );
    }

    // ==================== CREATE TESTS ====================

    @Test
    @DisplayName("Crear semestre con datos válidos")
    void crearSemestre_conDatosValidos_debeCrearCorrectamente() {
        // Arrange
        when(semestreRepository.existsByCodigo(crearDTO.getCodigo().toUpperCase())).thenReturn(false);
        when(semestreRepository.save(any(Semestre.class))).thenReturn(semestre);

        // Act
        Semestre resultado = semestreService.crearSemestre(crearDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("2025-2026-F1", resultado.getCodigo());
        assertEquals("Semestre Agosto 2025 - Enero 2026", resultado.getNombre());
        assertFalse(resultado.getActivo());

        verify(semestreRepository, times(1)).existsByCodigo(anyString());
        verify(semestreRepository, times(1)).save(any(Semestre.class));
    }

    @Test
    @DisplayName("Crear semestre con código duplicado lanza excepción")
    void crearSemestre_conCodigoDuplicado_debeLanzarExcepcion() {
        // Arrange
        when(semestreRepository.existsByCodigo(crearDTO.getCodigo().toUpperCase())).thenReturn(true);

        // Act & Assert
        assertThrows(SemestreValidationException.class, () -> semestreService.crearSemestre(crearDTO));
        verify(semestreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear semestre con fecha fin anterior a inicio lanza excepción")
    void crearSemestre_conFechasInvalidas_debeLanzarExcepcion() {
        // Arrange
        CrearSemestreDTO dtoInvalido = new CrearSemestreDTO(
                "2025-2026-F1",
                "Test",
                LocalDate.of(2026, 1, 31),
                LocalDate.of(2025, 8, 1) // FIN antes de INICIO
        );

        when(semestreRepository.existsByCodigo(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(SemestreValidationException.class, () -> semestreService.crearSemestre(dtoInvalido));
        verify(semestreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear semestre valida formato de código")
    void crearSemestre_conFormatoCodigoInvalido_debeLanzarExcepcion() {
        // Arrange
        CrearSemestreDTO dtoInvalido = new CrearSemestreDTO(
                "FORMATO-INVALIDO",
                "Test",
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2026, 1, 31)
        );

        // Act & Assert
        assertThrows(SemestreValidationException.class, () -> semestreService.crearSemestre(dtoInvalido));
        verify(semestreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear semestre siempre inicia inactivo")
    void crearSemestre_debeCrearInactivo() {
        // Arrange
        when(semestreRepository.existsByCodigo(anyString())).thenReturn(false);
        when(semestreRepository.save(any(Semestre.class))).thenAnswer(invocation -> {
            Semestre s = invocation.getArgument(0);
            assertFalse(s.getActivo(), "El semestre debe crearse inactivo");
            return s;
        });

        // Act
        semestreService.crearSemestre(crearDTO);

        // Assert
        verify(semestreRepository, times(1)).save(any(Semestre.class));
    }

    // ==================== READ TESTS ====================

    @Test
    @DisplayName("Obtener semestre por ID exitosamente")
    void obtenerPorId_conIdValido_debeRetornarSemestre() {
        // Arrange
        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestre));

        // Act
        Semestre resultado = semestreService.obtenerPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("2025-2026-F1", resultado.getCodigo());

        verify(semestreRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener semestre por ID inexistente lanza excepción")
    void obtenerPorId_conIdInexistente_debeLanzarExcepcion() {
        // Arrange
        when(semestreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(SemestreNotFoundException.class, () -> semestreService.obtenerPorId(999L));
    }

    @Test
    @DisplayName("Obtener semestre por código exitosamente")
    void obtenerPorCodigo_conCodigoValido_debeRetornarSemestre() {
        // Arrange
        when(semestreRepository.findByCodigo("2025-2026-F1")).thenReturn(Optional.of(semestre));

        // Act
        Semestre resultado = semestreService.obtenerPorCodigo("2025-2026-F1");

        // Assert
        assertNotNull(resultado);
        assertEquals("2025-2026-F1", resultado.getCodigo());

        verify(semestreRepository, times(1)).findByCodigo("2025-2026-F1");
    }

    @Test
    @DisplayName("Obtener semestre activo exitosamente")
    void obtenerSemestreActivo_conSemestreActivo_debeRetornarSemestre() {
        // Arrange
        Semestre semestreActivo = TestDataBuilder.semestre()
                .id(1L)
                .activo(true)
                .build();

        when(semestreRepository.findByActivoTrue()).thenReturn(Optional.of(semestreActivo));

        // Act
        Optional<Semestre> resultado = semestreService.obtenerSemestreActivo();

        // Assert
        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getActivo());

        verify(semestreRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener semestre activo cuando no existe")
    void obtenerSemestreActivo_sinSemestreActivo_debeRetornarEmpty() {
        // Arrange
        when(semestreRepository.findByActivoTrue()).thenReturn(Optional.empty());

        // Act
        Optional<Semestre> resultado = semestreService.obtenerSemestreActivo();

        // Assert
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Listar todos los semestres")
    void listarTodos_debeRetornarListaOrdenada() {
        // Arrange
        Semestre semestre2 = TestDataBuilder.semestre()
                .id(2L)
                .codigo("2024-2025-F2")
                .fechaInicio(LocalDate.of(2024, 8, 1))
                .build();

        List<Semestre> semestres = List.of(semestre, semestre2);

        when(semestreRepository.findAllByOrderByFechaInicioDesc()).thenReturn(semestres);

        // Act
        List<Semestre> resultado = semestreService.listarTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        verify(semestreRepository, times(1)).findAllByOrderByFechaInicioDesc();
    }

    @Test
    @DisplayName("Listar últimos N semestres")
    void listarUltimos_debeRetornarLimitada() {
        // Arrange
        List<Semestre> semestres = List.of(semestre);

        when(semestreRepository.findTop5ByOrderByFechaInicioDesc()).thenReturn(semestres);

        // Act
        List<Semestre> resultado = semestreService.listarUltimos(1);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        verify(semestreRepository, times(1)).findTop5ByOrderByFechaInicioDesc();
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @DisplayName("Actualizar semestre con datos válidos")
    void actualizarSemestre_conDatosValidos_debeActualizarCorrectamente() {
        // Arrange
        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestre));
        when(semestreRepository.save(any(Semestre.class))).thenReturn(semestre);

        // Act
        Semestre resultado = semestreService.actualizarSemestre(1L, actualizarDTO);

        // Assert
        assertNotNull(resultado);
        verify(semestreRepository, times(1)).findById(1L);
        verify(semestreRepository, times(1)).save(any(Semestre.class));
    }

    @Test
    @DisplayName("Actualizar semestre con fechas inválidas lanza excepción")
    void actualizarSemestre_conFechasInvalidas_debeLanzarExcepcion() {
        // Arrange
        ActualizarSemestreDTO dtoInvalido = new ActualizarSemestreDTO(
                "Test",
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2025, 8, 1)
        );

        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestre));

        // Act & Assert
        assertThrows(SemestreValidationException.class, () -> semestreService.actualizarSemestre(1L, dtoInvalido));
        verify(semestreRepository, never()).save(any(Semestre.class));
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Eliminar semestre inactivo exitosamente")
    void eliminarSemestre_conSemestreInactivo_debeEliminarCorrectamente() {
        // Arrange
        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestre));
        when(asignacionRepository.countBySemestreId(1L)).thenReturn(0L);

        // Act
        semestreService.eliminarSemestre(1L);

        // Assert
        verify(semestreRepository, times(2)).findById(1L); // Una para validar, otra para eliminar
        verify(semestreRepository, times(1)).delete(semestre);
    }

    @Test
    @DisplayName("Eliminar semestre activo lanza excepción")
    void eliminarSemestre_conSemestreActivo_debeLanzarExcepcion() {
        // Arrange
        Semestre semestreActivo = TestDataBuilder.semestre()
                .id(1L)
                .activo(true)
                .build();

        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestreActivo));

        // Act & Assert
        assertThrows(SemestreValidationException.class, () -> semestreService.eliminarSemestre(1L));
        verify(semestreRepository, never()).delete(any());
    }

    // ==================== ACTIVATION TESTS ====================

    @Test
    @DisplayName("Activar semestre inactivo")
    void activarSemestre_conSemestreInactivo_debeActivar() {
        // Arrange
        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestre));
        when(semestreRepository.save(any(Semestre.class))).thenReturn(semestre);

        // Act
        semestreService.activarSemestre(1L);

        // Assert
        verify(semestreRepository, times(1)).desactivarTodos();
        verify(semestreRepository, times(1)).save(any(Semestre.class));
    }

    @Test
    @DisplayName("Activar semestre ya activo no hace cambios")
    void activarSemestre_conSemestreYaActivo_noHaceNada() {
        // Arrange
        Semestre semestreActivo = TestDataBuilder.semestre()
                .id(1L)
                .activo(true)
                .build();

        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestreActivo));

        // Act
        semestreService.activarSemestre(1L);

        // Assert
        verify(semestreRepository, never()).desactivarTodos();
        verify(semestreRepository, never()).save(any());
    }

    @Test
    @DisplayName("Desactivar semestre")
    void desactivarSemestre_debeDesactivarCorrectamente() {
        // Arrange
        Semestre semestreActivo = TestDataBuilder.semestre()
                .id(1L)
                .activo(true)
                .build();

        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestreActivo));
        when(semestreRepository.save(any(Semestre.class))).thenReturn(semestreActivo);

        // Act
        semestreService.desactivarSemestre(1L);

        // Assert
        verify(semestreRepository, times(1)).save(any(Semestre.class));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Validar formato de código válido")
    void validarFormatoCodigo_conFormatoValido_noLanzaExcepcion() {
        // Act & Assert
        assertDoesNotThrow(() -> semestreService.validarFormatoCodigo("2025-2026-F1"));
        assertDoesNotThrow(() -> semestreService.validarFormatoCodigo("2024-2025-F2"));
    }

    @Test
    @DisplayName("Validar formato de código inválido lanza excepción")
    void validarFormatoCodigo_conFormatoInvalido_debeLanzarExcepcion() {
        // Act & Assert
        assertThrows(SemestreValidationException.class, () -> semestreService.validarFormatoCodigo("INVALIDO"));
        assertThrows(SemestreValidationException.class, () -> semestreService.validarFormatoCodigo("2025-F1"));
        assertThrows(SemestreValidationException.class, () -> semestreService.validarFormatoCodigo(""));
    }

    @Test
    @DisplayName("Verificar existencia de código")
    void existeCodigo_conCodigoExistente_debeRetornarTrue() {
        // Arrange
        when(semestreRepository.existsByCodigo("2025-2026-F1")).thenReturn(true);

        // Act
        boolean resultado = semestreService.existeCodigo("2025-2026-F1");

        // Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Verificar existencia de código inexistente")
    void existeCodigo_conCodigoInexistente_debeRetornarFalse() {
        // Arrange
        when(semestreRepository.existsByCodigo("9999-9999-F9")).thenReturn(false);

        // Act
        boolean resultado = semestreService.existeCodigo("9999-9999-F9");

        // Assert
        assertFalse(resultado);
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    @DisplayName("Obtener estadísticas del semestre")
    void obtenerEstadisticas_debeRetornarEstadisticas() {
        // Arrange
        when(semestreRepository.findById(1L)).thenReturn(Optional.of(semestre));
        when(asignacionRepository.countBySemestreId(1L)).thenReturn(10L);
        when(asignacionRepository.countActivosBySemestreId(1L)).thenReturn(8L);
        when(tutorRepository.findAllActivos()).thenReturn(List.of());
        when(asignacionRepository.findBySemestreId(1L)).thenReturn(List.of());

        // Act
        EstadisticasSemestreDTO resultado = semestreService.obtenerEstadisticas(1L);

        // Assert
        assertNotNull(resultado);
        verify(semestreRepository, times(1)).findById(1L);
        verify(asignacionRepository, times(1)).countBySemestreId(1L);
    }
}
