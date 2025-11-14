package com.universidad.tutorias.domain.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SemestreTest {

    @Test
    void crearSemestre_ConDatosValidos_DebeCrearCorrectamente() {
        // Arrange & Act
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Semestre Agosto 2025 - Enero 2026")
                .fechaInicio(LocalDate.of(2025, 8, 1))
                .fechaFin(LocalDate.of(2026, 1, 31))
                .activo(false)
                .build();

        // Assert
        assertEquals("2025-2026-F1", semestre.getCodigo());
        assertEquals("Semestre Agosto 2025 - Enero 2026", semestre.getNombre());
        assertFalse(semestre.getActivo());
        assertTrue(semestre.getAsignaciones().isEmpty());
    }

    @Test
    void validarFechas_ConFechaFinAnterior_DebeLanzarExcepcion() {
        // Arrange
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(LocalDate.of(2026, 1, 31))
                .fechaFin(LocalDate.of(2025, 8, 1)) // FIN antes de INICIO
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, semestre::validarFechas);
    }

    @Test
    void estaVigente_ConFechaActualDentroDelRango_DebeRetornarTrue() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(hoy.minusMonths(1))
                .fechaFin(hoy.plusMonths(1))
                .build();

        // Act & Assert
        assertTrue(semestre.estaVigente());
    }

    @Test
    void estaVigente_ConFechaActualAnteriorAlRango_DebeRetornarFalse() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(hoy.plusMonths(1))
                .fechaFin(hoy.plusMonths(2))
                .build();

        // Act & Assert
        assertFalse(semestre.estaVigente());
    }

    @Test
    void estaVigente_ConFechaActualPosteriorAlRango_DebeRetornarFalse() {
        // Arrange
        LocalDate hoy = LocalDate.now();
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(hoy.minusMonths(2))
                .fechaFin(hoy.minusMonths(1))
                .build();

        // Act & Assert
        assertFalse(semestre.estaVigente());
    }

    @Test
    void estaVigente_ConFechasNulas_DebeRetornarFalse() {
        // Arrange
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(null)
                .fechaFin(null)
                .build();

        // Act & Assert
        assertFalse(semestre.estaVigente());
    }

    @Test
    void estaActivo_ConActivoTrue_DebeRetornarTrue() {
        // Arrange & Act
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(LocalDate.of(2025, 8, 1))
                .fechaFin(LocalDate.of(2026, 1, 31))
                .activo(true)
                .build();

        // Assert
        assertTrue(semestre.estaActivo());
    }

    @Test
    void estaActivo_ConActivoFalse_DebeRetornarFalse() {
        // Arrange & Act
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(LocalDate.of(2025, 8, 1))
                .fechaFin(LocalDate.of(2026, 1, 31))
                .activo(false)
                .build();

        // Assert
        assertFalse(semestre.estaActivo());
    }

    @Test
    void getTotalAsignaciones_ConAsignacionesVacias_DebeRetornarCero() {
        // Arrange & Act
        Semestre semestre = Semestre.builder()
                .codigo("2025-2026-F1")
                .nombre("Test")
                .fechaInicio(LocalDate.of(2025, 8, 1))
                .fechaFin(LocalDate.of(2026, 1, 31))
                .build();

        // Assert
        assertEquals(0, semestre.getTotalAsignaciones());
    }

    @Test
    void codigoValidation_ConFormatoInvalido_DebeLanzarExcepcion() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            Semestre semestre = Semestre.builder()
                    .codigo("2025-INVALIDO")
                    .nombre("Test")
                    .fechaInicio(LocalDate.of(2025, 8, 1))
                    .fechaFin(LocalDate.of(2026, 1, 31))
                    .build();
            // La validación del patrón ocurre en la persistencia, pero el builder no la valida
            // Este test documenta que la validación existe a nivel de JPA
        });
    }

    @Test
    void toString_ConDatosCompletos_DebeContenerInformacionRelevante() {
        // Arrange & Act
        Semestre semestre = Semestre.builder()
                .id(1L)
                .codigo("2025-2026-F1")
                .nombre("Semestre Agosto 2025 - Enero 2026")
                .fechaInicio(LocalDate.of(2025, 8, 1))
                .fechaFin(LocalDate.of(2026, 1, 31))
                .activo(true)
                .build();

        String resultado = semestre.toString();

        // Assert
        assertTrue(resultado.contains("2025-2026-F1"));
        assertTrue(resultado.contains("Semestre"));
        assertTrue(resultado.contains("id=1"));
    }
}
