package com.universidad.tutorias.infrastructure.controller;

import com.universidad.tutorias.application.dto.AlumnoCreateDTO;
import com.universidad.tutorias.application.dto.AlumnoPatchDTO;
import com.universidad.tutorias.application.dto.AlumnoResponseDTO;
import com.universidad.tutorias.application.dto.AlumnoUpdateDTO;
import com.universidad.tutorias.application.service.AlumnoCrudService;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.infrastructure.controller.response.ApiResponse;
import com.universidad.tutorias.infrastructure.controller.response.PagedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AlumnoController {

    private final AlumnoCrudService alumnoCrudService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AlumnoResponseDTO>>> listarAlumnos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) EstadoAlumno estado,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) Integer semestre) {

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, limit);

        Page<AlumnoResponseDTO> resultado = alumnoCrudService.listarAlumnos(estado, carrera, semestre, pageable);

        PagedResponse<AlumnoResponseDTO> data = new PagedResponse<>(
                resultado.getContent(),
                page,
                limit,
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> obtenerAlumno(@PathVariable Long id) {
        AlumnoResponseDTO alumno = alumnoCrudService.obtenerAlumno(id);
        return ResponseEntity.ok(ApiResponse.success(alumno));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> crearAlumno(@Valid @RequestBody AlumnoCreateDTO request) {
        log.info("Creando alumno con matrícula {}", request.getMatricula());
        AlumnoResponseDTO alumno = alumnoCrudService.crearAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(alumno, "Alumno creado correctamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> reemplazarAlumno(
            @PathVariable Long id,
            @Valid @RequestBody AlumnoUpdateDTO request) {
        log.info("Actualizando alumno {} mediante PUT", id);
        AlumnoResponseDTO alumno = alumnoCrudService.reemplazarAlumno(id, request);
        return ResponseEntity.ok(ApiResponse.success(alumno, "Alumno actualizado correctamente"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AlumnoResponseDTO>> actualizarParcialmente(
            @PathVariable Long id,
            @Valid @RequestBody AlumnoPatchDTO request) {
        log.info("Actualizando parcialmente alumno {}", id);
        AlumnoResponseDTO alumno = alumnoCrudService.actualizarAlumnoParcial(id, request);
        return ResponseEntity.ok(ApiResponse.success(alumno, "Alumno actualizado correctamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarAlumno(@PathVariable Long id) {
        log.info("Eliminando alumno {}", id);
        alumnoCrudService.eliminarAlumno(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Alumno eliminado correctamente"));
    }
}
