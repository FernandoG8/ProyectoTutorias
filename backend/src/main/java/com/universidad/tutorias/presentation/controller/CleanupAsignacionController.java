package com.universidad.tutorias.presentation.controller;

import com.universidad.tutorias.application.service.CleanupAsignacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para gestionar la limpieza de asignaciones corruptas.
 *
 * ENDPOINTS:
 * - GET  /api/cleanup/detectar/{semestreId}     → Detectar corrupción en semestre
 * - GET  /api/cleanup/detectar-global            → Detectar corrupción global
 * - POST /api/cleanup/limpiar/{semestreId}       → Limpiar semestre específico
 * - POST /api/cleanup/limpiar-global             → Limpiar todo el sistema
 * - POST /api/cleanup/recalcular/{semestreId}    → Recalcular cargas de tutores
 *
 * ROLES REQUERIDOS: ADMIN (requiere elevados permisos)
 */
@RestController
@RequestMapping("/api/cleanup")
@RequiredArgsConstructor
@Slf4j
public class CleanupAsignacionController {

    private final CleanupAsignacionService cleanupService;

    /**
     * Detecta asignaciones corruptas en un semestre específico.
     *
     * @param semestreId ID del semestre a analizar
     * @return Mapa con detalles de la corrupción encontrada
     */
    @GetMapping("/detectar/{semestreId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> detectarCorrupcion(@PathVariable Long semestreId) {
        log.info("Solicitado análisis de corrupción para semestre: {}", semestreId);

        try {
            Map<String, Object> resultado = cleanupService.detectarAsignacionesCorruptas(semestreId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al detectar corrupción: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Detecta asignaciones corruptas en TODO el sistema.
     *
     * @return Mapa con detalles globales de la corrupción
     */
    @GetMapping("/detectar-global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> detectarCorrupcionGlobal() {
        log.info("Solicitado análisis GLOBAL de corrupción");

        try {
            Map<String, Object> resultado = cleanupService.detectarAsignacionesCorruptasGlobal();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al detectar corrupción global: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Limpia asignaciones corruptas en un semestre específico.
     *
     * ⚠️ ACCIÓN DESTRUCTIVA - Elimina registros de la base de datos
     *
     * @param semestreId ID del semestre a limpiar
     * @param confirmar Token de confirmación (debe ser "CONFIRMAR_LIMPIEZA")
     * @return Estadísticas de la limpieza realizada
     */
    @PostMapping("/limpiar/{semestreId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> limpiarSemestre(
            @PathVariable Long semestreId,
            @RequestParam(required = false) String confirmar) {

        log.warn("Solicitada limpieza para semestre: {} (confirmación: {})", semestreId, confirmar);

        // Validar confirmación
        if (!"CONFIRMAR_LIMPIEZA".equals(confirmar)) {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("error", "Operación no confirmada. Incluir parámetro: confirmar=CONFIRMAR_LIMPIEZA");
            respuesta.put("advertencia", "Esta es una operación DESTRUCTIVA que eliminará registros");
            respuesta.put("instruccion", "Para confirmar, ejecute: POST /api/cleanup/limpiar/{semestreId}?confirmar=CONFIRMAR_LIMPIEZA");
            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            log.warn("INICIANDO LIMPIEZA CONFIRMADA de semestre: {}", semestreId);
            Map<String, Object> resultado = cleanupService.limpiarAsignacionesPorSemestre(semestreId);

            if ("EXITOSO".equals(resultado.get("estado"))) {
                log.warn("Limpieza exitosa: {} asignaciones eliminadas", resultado.get("asignaciones_eliminadas"));
                return ResponseEntity.ok(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
            }
        } catch (Exception e) {
            log.error("Error CRÍTICO durante limpieza: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Limpia asignaciones corruptas en TODO el sistema.
     *
     * ⚠️ ACCIÓN DESTRUCTIVA GLOBAL - Elimina registros de toda la base de datos
     *
     * @param confirmar Token de confirmación (debe ser "CONFIRMAR_LIMPIEZA_GLOBAL")
     * @return Estadísticas globales de la limpieza
     */
    @PostMapping("/limpiar-global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> limpiarGlobal(
            @RequestParam(required = false) String confirmar) {

        log.warn("Solicitada limpieza GLOBAL (confirmación: {})", confirmar);

        // Validar confirmación doble
        if (!"CONFIRMAR_LIMPIEZA_GLOBAL".equals(confirmar)) {
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("error", "Operación no confirmada. Incluir parámetro: confirmar=CONFIRMAR_LIMPIEZA_GLOBAL");
            respuesta.put("advertencia", "⚠️ ACCIÓN DESTRUCTIVA GLOBAL - Afectará TODO el sistema");
            respuesta.put("instruccion", "Para confirmar, ejecute: POST /api/cleanup/limpiar-global?confirmar=CONFIRMAR_LIMPIEZA_GLOBAL");
            return ResponseEntity.badRequest().body(respuesta);
        }

        try {
            log.warn("⚠️ INICIANDO LIMPIEZA GLOBAL CONFIRMADA");
            Map<String, Object> resultado = cleanupService.limpiarAsignacionesGlobal();

            if ("EXITOSO".equals(resultado.get("estado"))) {
                log.warn("Limpieza global exitosa: {} asignaciones eliminadas TOTALES",
                        resultado.get("total_asignaciones_eliminadas"));
                return ResponseEntity.ok(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
            }
        } catch (Exception e) {
            log.error("Error CRÍTICO durante limpieza global: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Recalcula la carga de tutores después de una limpieza.
     *
     * @param semestreId ID del semestre (opcional, null = todos)
     * @return Estadísticas de recalculo
     */
    @PostMapping("/recalcular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> recalcularCarga(
            @RequestParam(required = false) Long semestreId) {

        log.info("Solicitado recalculo de cargas (semestre: {})", semestreId);

        try {
            Map<String, Object> resultado = cleanupService.recalcularCargaDespuesLimpieza(semestreId);

            if ("EXITOSO".equals(resultado.get("estado"))) {
                log.info("Recalculo exitoso: {} tutores actualizados", resultado.get("tutores_actualizados"));
                return ResponseEntity.ok(resultado);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
            }
        } catch (Exception e) {
            log.error("Error durante recalculo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint de prueba para verificar acceso.
     *
     * @return Mensaje de estado
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "servicio", "Cleanup de Asignaciones",
            "mensaje", "El servicio de limpieza está activo"
        ));
    }
}
