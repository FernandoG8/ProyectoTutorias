package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.service.InactivacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.MotivoInactividad;
import com.universidad.tutorias.domain.repository.AlumnoInactivoRepository;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InactivacionServiceImplTest {

    @Autowired
    private InactivacionService inactivacionService;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AlumnoInactivoRepository alumnoInactivoRepository;

    private Tutor tutor;

    @BeforeEach
    void setUp() {
        alumnoInactivoRepository.deleteAll();
        alumnoRepository.deleteAll();
        tutorRepository.deleteAll();

        tutor = new Tutor();
        tutor.setNombre("Tutor de Prueba");
        tutor.setCarrera("ISC");
        tutor.setCapacidadMax(10);
        tutor.setCargaActual(4);
        tutor.setAreaAtencion("Pruebas");
        tutor.setLetraEdificio("A");
        tutor.setActivo(true);
        tutor = tutorRepository.save(tutor);

        crearAlumnoInactivo("A0001");
        crearAlumnoInactivo("A0002");
    }

    @Test
    void liberarCuposDebeProcesarCadaInactivoSoloUnaVez() {
        int cargaInicial = tutor.getCargaActual();

        inactivacionService.liberarCupos(1L);
        Tutor despuesPrimerLiberacion = tutorRepository.findById(tutor.getId()).orElseThrow();

        assertThat(despuesPrimerLiberacion.getCargaActual()).isEqualTo(cargaInicial - 2);

        inactivacionService.liberarCupos(1L);
        Tutor despuesSegundaLiberacion = tutorRepository.findById(tutor.getId()).orElseThrow();

        assertThat(despuesSegundaLiberacion.getCargaActual()).isEqualTo(despuesPrimerLiberacion.getCargaActual());

        List<AlumnoInactivo> inactivos = alumnoInactivoRepository.findAll();
        assertThat(inactivos)
                .isNotEmpty()
                .allSatisfy(ai -> assertThat(ai.getCupoLiberado()).isFalse());
    }

    private void crearAlumnoInactivo(String matricula) {
        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula);
        alumno.setNombre("Alumno " + matricula);
        alumno.setCarrera("ISC");
        alumno.setSemestre(3);
        alumno.setEstado(EstadoAlumno.ACTIVO);
        alumno.setTutorActual(tutor);
        alumno = alumnoRepository.save(alumno);

        AlumnoInactivo inactivo = AlumnoInactivo.builder()
                .alumno(alumno)
                .motivoInactividad(MotivoInactividad.SIN_DEFINIR)
                .tutorPreservado(tutor)
                .cupoLiberado(true)
                .build();

        alumnoInactivoRepository.save(inactivo);
    }
}
