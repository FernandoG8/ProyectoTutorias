package com.universidad.tutorias.application.service.impl;

import com.universidad.tutorias.application.dto.AlumnoValidadoDTO;
import com.universidad.tutorias.application.dto.EjecutarAsignacionRequest;
import com.universidad.tutorias.application.dto.EjecucionAsignacionResponse;
import com.universidad.tutorias.application.service.EjecucionAsignacionService;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.entity.AlumnoInactivo;
import com.universidad.tutorias.domain.entity.Asignacion;
import com.universidad.tutorias.domain.entity.Semestre;
import com.universidad.tutorias.domain.entity.Tutor;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import com.universidad.tutorias.domain.enums.TipoAsignacion;
import com.universidad.tutorias.domain.repository.AlumnoInactivoRepository;
import com.universidad.tutorias.domain.repository.AlumnoRepository;
import com.universidad.tutorias.domain.repository.AsignacionRepository;
import com.universidad.tutorias.domain.repository.SemestreRepository;
import com.universidad.tutorias.domain.repository.TutorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class EjecucionAsignacionServiceImplTest {

    @Autowired
    private EjecucionAsignacionService ejecucionAsignacionService;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private AsignacionRepository asignacionRepository;

    @Autowired
    private SemestreRepository semestreRepository;

    @Autowired
    private AlumnoInactivoRepository alumnoInactivoRepository;

    private Tutor tutor;
    private Semestre semestreAnterior;
    private Semestre semestreActual;

    @BeforeEach
    void setUp() {
        alumnoInactivoRepository.deleteAll();
        asignacionRepository.deleteAll();
        alumnoRepository.deleteAll();
        tutorRepository.deleteAll();
        semestreRepository.deleteAll();

        semestreAnterior = crearSemestre("2023-2024-F2", false);
        semestreActual = crearSemestre("2024-2025-F1", true);

        tutor = new Tutor();
        tutor.setNombre("Tutor Prueba");
        tutor.setCarrera("ISC");
        tutor.setCapacidadMax(5);
        tutor.setCargaActual(0);
        tutor.setActivo(true);
        tutorRepository.save(tutor);
    }

    @Test
    void asignacionMantieneTutorPrevioSinDuplicarAlumno() {
        Alumno alumno = crearAlumnoConAsignacionPrevio("A0001", "Alumno Uno", tutor, semestreAnterior, 3);
        int cargaInicial = tutor.getCargaActual();

        EjecutarAsignacionRequest request = EjecutarAsignacionRequest.builder()
                .semestreId(semestreActual.getId())
                .alumnosValidados(List.of(alumnoValidado(alumno, semestreActual)))
                .build();

        EjecucionAsignacionResponse respuesta = ejecucionAsignacionService.ejecutar(request);

        Alumno alumnoRefrescado = alumnoRepository.findByMatricula(alumno.getMatricula()).orElseThrow();
        List<Asignacion> asignaciones = asignacionRepository.findByAlumnoId(alumnoRefrescado.getId());
        Asignacion asignacionActual = asignacionRepository.findByAlumnoAndSemestreId(alumnoRefrescado.getId(), semestreActual.getId()).orElseThrow();

        assertThat(respuesta.getStatus()).isEqualTo("OK");
        assertThat(respuesta.getAlumnosAsignados()).isEqualTo(1);
        assertThat(asignaciones).hasSize(2); // semestre previo + semestre actual
        assertThat(asignacionActual.getTutor().getId()).isEqualTo(tutor.getId());
        assertThat(alumnoRefrescado.getTutorActual().getId()).isEqualTo(tutor.getId());
        assertThat(tutorRepository.findById(tutor.getId()).orElseThrow().getCargaActual())
                .isEqualTo(cargaInicial); // no incrementa carga por mantener tutor previo
    }

    @Test
    void alumnosAusentesSeMarcanInactivosYLiberanCupo() {
        Alumno alumnoPersistente = crearAlumnoConAsignacionPrevio("A0002", "Alumno Dos", tutor, semestreAnterior, 3);
        Alumno alumnoQueFalta = crearAlumnoConAsignacionPrevio("A0003", "Alumno Tres", tutor, semestreAnterior, 4);
        tutor.sincronizarCarga(2);
        tutorRepository.save(tutor);

        EjecutarAsignacionRequest request = EjecutarAsignacionRequest.builder()
                .semestreId(semestreActual.getId())
                .alumnosValidados(List.of(alumnoValidado(alumnoPersistente, semestreActual)))
                .build();

        EjecucionAsignacionResponse respuesta = ejecucionAsignacionService.ejecutar(request);

        Alumno inactivo = alumnoRepository.findById(alumnoQueFalta.getId()).orElseThrow();
        List<AlumnoInactivo> inactivos = alumnoInactivoRepository.findAll();

        assertThat(respuesta.getStatus()).isEqualTo("OK");
        assertThat(inactivo.getEstado()).isEqualTo(EstadoAlumno.INACTIVO);
        assertThat(inactivos).hasSize(1);
        assertThat(inactivos.get(0).getAlumno().getMatricula()).isEqualTo(alumnoQueFalta.getMatricula());
        assertThat(tutorRepository.findById(tutor.getId()).orElseThrow().getCargaActual())
                .isLessThan(2); // se libera al menos un cupo
    }

    private Semestre crearSemestre(String codigo, boolean activo) {
        Semestre semestre = new Semestre();
        semestre.setCodigo(codigo);
        semestre.setNombre("Semestre " + codigo);
        semestre.setFechaInicio(LocalDate.now());
        semestre.setFechaFin(LocalDate.now().plusMonths(6));
        semestre.setActivo(activo);
        return semestreRepository.save(semestre);
    }

    private Alumno crearAlumnoConAsignacionPrevio(String matricula,
                                                  String nombre,
                                                  Tutor tutorAsignado,
                                                  Semestre semestre,
                                                  int semestreNumerico) {
        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula);
        alumno.setNombre(nombre);
        alumno.setCarrera("ISC");
        alumno.setSemestre(semestreNumerico);
        alumno.setEstado(EstadoAlumno.ACTIVO);
        alumno.setTutorActual(tutorAsignado);
        alumno = alumnoRepository.save(alumno);

        Asignacion asignacion = new Asignacion();
        asignacion.setAlumno(alumno);
        asignacion.setTutor(tutorAsignado);
        asignacion.setSemestre(semestre);
        asignacion.setSemestreAcademico(semestre.getCodigo());
        asignacion.setTipoAsignacion(TipoAsignacion.NUEVO_INGRESO);
        asignacionRepository.save(asignacion);

        tutorAsignado.incrementarCarga();
        tutorRepository.save(tutorAsignado);
        return alumno;
    }

    private AlumnoValidadoDTO alumnoValidado(Alumno alumno, Semestre semestreDestino) {
        return AlumnoValidadoDTO.builder()
                .alumnoId(alumno.getId())
                .matricula(alumno.getMatricula())
                .nombre(alumno.getNombre())
                .carrera(alumno.getCarrera())
                .semestreId(semestreDestino.getId())
                .semestreCodigo(semestreDestino.getCodigo())
                .semestreNumerico(alumno.getSemestre())
                .ordenPriority(alumno.getSemestre())
                .validado(true)
                .tipoAsignacionPrevisto(null)
                .build();
    }
}
