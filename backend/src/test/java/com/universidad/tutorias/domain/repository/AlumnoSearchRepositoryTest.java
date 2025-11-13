package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.domain.entity.Alumno;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AlumnoSearchRepository.class)
@ActiveProfiles("test")
class AlumnoSearchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AlumnoSearchRepository alumnoSearchRepository;

    @BeforeEach
    void clean() {
        entityManager.getEntityManager().createQuery("DELETE FROM Alumno").executeUpdate();
    }

    @Test
    void shouldOrderByRankingForMatriculaMatches() {
        persistAlumno("2021", "Exact Match", "INGENIERIA_SISTEMAS", 5, EstadoAlumno.ACTIVO);
        persistAlumno("20210", "Prefix Match", "INGENIERIA_SISTEMAS", 5, EstadoAlumno.ACTIVO);
        persistAlumno("9999", "Contiene 2021", "INGENIERIA_SISTEMAS", 5, EstadoAlumno.ACTIVO);

        entityManager.flush();

        Page<Object[]> page = alumnoSearchRepository.searchWithRanking(
                "2021",
                "2021",
                "2021",
                null,
                null,
                null,
                PageRequest.of(0, 10),
                SearchSortOption.RELEVANCE
        );

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent())
                .extracting(row -> ((Number) row[6]).intValue())
                .containsExactly(1000, 800, 200);
    }

    @Test
    void shouldApplyFiltersIndependently() {
        persistAlumno("3000", "Activo Sistemas", "INGENIERIA_SISTEMAS", 6, EstadoAlumno.ACTIVO);
        persistAlumno("3001", "Inactivo Sistemas", "INGENIERIA_SISTEMAS", 6, EstadoAlumno.INACTIVO);
        persistAlumno("3002", "Activo Civil", "INGENIERIA_CIVIL", 4, EstadoAlumno.ACTIVO);

        entityManager.flush();

        Page<Object[]> page = alumnoSearchRepository.searchWithRanking(
                "activo",
                "activo",
                "activo",
                EstadoAlumno.ACTIVO,
                "ingenieria_sistemas",
                6,
                PageRequest.of(0, 10),
                SearchSortOption.RELEVANCE
        );

        assertThat(page.getContent()).hasSize(1);
        Object[] row = page.getContent().getFirst();
        assertThat(row[1]).isEqualTo("3000");
    }

    @Test
    void shouldPaginateResults() {
        for (int i = 0; i < 3; i++) {
            persistAlumno("400" + i, "Alumno 400" + i, "INGENIERIA_SISTEMAS", 3, EstadoAlumno.ACTIVO);
        }
        entityManager.flush();

        Page<Object[]> firstPage = alumnoSearchRepository.searchWithRanking(
                "400",
                "400",
                "400",
                null,
                null,
                null,
                PageRequest.of(0, 2),
                SearchSortOption.RELEVANCE
        );

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }

    @Test
    void shouldSearchCaseInsensitive() {
        persistAlumno("5000", "María López", "INGENIERIA_SISTEMAS", 7, EstadoAlumno.ACTIVO);
        entityManager.flush();

        Page<Object[]> page = alumnoSearchRepository.searchWithRanking(
                "maría",
                "maría",
                "maría",
                null,
                null,
                null,
                PageRequest.of(0, 5),
                SearchSortOption.RELEVANCE
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(((String) page.getContent().getFirst()[2])).isEqualTo("María López");
    }

    private void persistAlumno(String matricula,
                               String nombre,
                               String carrera,
                               int semestre,
                               EstadoAlumno estado) {
        Alumno alumno = new Alumno();
        alumno.setMatricula(matricula);
        alumno.setNombre(nombre);
        alumno.setCarrera(carrera);
        alumno.setSemestre(semestre);
        alumno.setEstado(estado);
        alumno.setFechaRegistro(LocalDateTime.now());
        entityManager.persist(alumno);
    }
}
