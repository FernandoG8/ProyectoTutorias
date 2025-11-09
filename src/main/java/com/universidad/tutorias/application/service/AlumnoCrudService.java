package com.universidad.tutorias.application.service;

import com.universidad.tutorias.application.dto.AlumnoCreateDTO;
import com.universidad.tutorias.application.dto.AlumnoPatchDTO;
import com.universidad.tutorias.application.dto.AlumnoResponseDTO;
import com.universidad.tutorias.application.dto.AlumnoUpdateDTO;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlumnoCrudService {
    Page<AlumnoResponseDTO> listarAlumnos(EstadoAlumno estado, String carrera, Integer semestre, Pageable pageable);

    AlumnoResponseDTO obtenerAlumno(Long id);

    AlumnoResponseDTO crearAlumno(AlumnoCreateDTO request);

    AlumnoResponseDTO reemplazarAlumno(Long id, AlumnoUpdateDTO request);

    AlumnoResponseDTO actualizarAlumnoParcial(Long id, AlumnoPatchDTO request);

    void eliminarAlumno(Long id);
}
