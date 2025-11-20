package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.domain.entity.Tutor;
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
@Import(TutorSearchRepository.class)
@ActiveProfiles("test")
class TutorSearchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TutorSearchRepository tutorSearchRepository;

    @BeforeEach
    void clean() {
        entityManager.getEntityManager().createQuery("DELETE FROM Tutor").executeUpdate();
    }

    @Test
    void shouldRankByName() {
        persistTutor("María López", "INGENIERIA_SISTEMAS");
        persistTutor("María Fernanda", "INGENIERIA_SISTEMAS");
        persistTutor("Ana María", "INGENIERIA_SISTEMAS");

        entityManager.flush();

        Page<Object[]> page = tutorSearchRepository.searchWithRanking(
                "maría",
                "maría",
                "maría",
                null,
                PageRequest.of(0, 5),
                SearchSortOption.RELEVANCE
        );

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getContent())
                .extracting(row -> ((Number) row[3]).intValue())
                .containsExactly(420, 420, 400);
    }

    @Test
    void shouldFilterByCarreraCaseInsensitive() {
        persistTutor("Carlos Ruiz", "INGENIERIA_SISTEMAS");
        persistTutor("Carlos Méndez", "INGENIERIA_CIVIL");

        entityManager.flush();

        Page<Object[]> page = tutorSearchRepository.searchWithRanking(
                "carlos",
                "carlos",
                "carlos",
                "ingenieria_sistemas",
                PageRequest.of(0, 5),
                SearchSortOption.RELEVANCE
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst()[2]).isEqualTo("INGENIERIA_SISTEMAS");
    }

    private void persistTutor(String nombre, String carrera) {
        Tutor tutor = new Tutor();
        tutor.setNombre(nombre);
        tutor.setCarrera(carrera);
        tutor.setCapacidadMax(10);
        tutor.setCargaActual(0);
        tutor.setActivo(true);
        tutor.setFechaRegistro(LocalDateTime.now());
        entityManager.persist(tutor);
    }
}
