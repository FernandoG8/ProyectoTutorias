// ============================================
// ALUMNO INACTIVO CONTROLLER
// ============================================

package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.service.ResolucionMotivoService;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alumnos-inactivos")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AlumnoInactivoController {

    private final ResolucionMotivoService resolucionService;

    @GetMapping("/pendientes")
    public ResponseEntity<List<AlumnoInactivo>> obtenerPendientes() {
        log.info("Obteniendo alumnos inactivos pendientes de resolución");
        List<AlumnoInactivo> pendientes = resolucionService.obtenerPendientesDeResolucion();
        return ResponseEntity.ok(pendientes);
    }

    @PutMapping("/{alumnoInactivoId}/motivo")
    public ResponseEntity<Map<String, String>> asignarMotivo(
            @PathVariable Long alumnoInactivoId,
            @RequestParam MotivoInactividad motivo,
            @RequestParam String usuario) {

        log.info("Asignando motivo {} a alumno inactivo {} por usuario {}",
                motivo, alumnoInactivoId, usuario);

        resolucionService.asignarMotivo(alumnoInactivoId, motivo, usuario);

        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Motivo asignado correctamente");
        response.put("motivo", motivo.toString());
        response.put("alumno_inactivo_id", alumnoInactivoId.toString());

        return ResponseEntity.ok(response);
    }
}