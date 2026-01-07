// ============================================
// COMPARADOR ALUMNOS SERVICE IMPLEMENTATION
// ============================================

package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoExcelDTO;
import com.universidad.tutorias.application.service.ComparadorAlumnosService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComparadorAlumnosServiceImpl implements ComparadorAlumnosService {

    private final AlumnoRepository alumnoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Alumno> identificarInactivos(List<AlumnoExcelDTO> alumnosExcel, Long semestreId) {
        log.info("Identificando alumnos inactivos para semestre ID: {}", semestreId);

        // Obtener alumnos activos actuales en BD
        List<Alumno> alumnosActivosEnBD = alumnoRepository.findByEstadoWithTutor(EstadoAlumno.ACTIVO);

        // Crear set de matrículas del Excel para búsqueda rápida
        Set<String> matriculasEnExcel = alumnosExcel.stream()
                .map(a -> a.getMatricula() != null ? a.getMatricula().toUpperCase().trim() : "")
                .filter(m -> !m.isEmpty())
                .collect(Collectors.toSet());

        // Identificar alumnos que están en BD pero NO en Excel
        // Estos alumnos serán marcados como inactivos para este semestre
        List<Alumno> alumnosAInactivar = alumnosActivosEnBD.stream()
                .filter(alumno -> !matriculasEnExcel.contains(alumno.getMatricula().toUpperCase()))
                .collect(Collectors.toList());

        log.info("Identificados {} alumnos para marcar como inactivos de un total de {} activos en BD, semestre: {}",
                alumnosAInactivar.size(), alumnosActivosEnBD.size(), semestreId);

        return alumnosAInactivar;
    }
}