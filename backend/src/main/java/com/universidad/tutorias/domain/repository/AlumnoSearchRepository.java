package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.application.enums.SearchSortOption;
import com.universidad.tutorias.domain.enums.EstadoAlumno;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class AlumnoSearchRepository {

    private static final String SCORE_EXPRESSION = """
            CASE
                WHEN LOWER(a.matricula) = :normalizedQuery THEN 1000
                WHEN LOWER(a.matricula) LIKE CONCAT(:likeQuery, '%') THEN 800
                WHEN INSTR(LOWER(a.matricula), :normalizedQuery) > 0 THEN 600
                WHEN LOWER(a.nombre) = :normalizedQuery THEN 500
                WHEN LOWER(a.nombre) LIKE CONCAT(:likeQuery, '%') THEN 420
                WHEN LOWER(a.nombre) REGEXP CONCAT('(^| )', :regexQuery) THEN 400
                WHEN INSTR(LOWER(a.nombre), :normalizedQuery) > 0 THEN 200
                ELSE 0
            END
            """;

    private static final String MATCH_CONDITION = """
            (
                LOWER(a.matricula) = :normalizedQuery
                OR LOWER(a.matricula) LIKE CONCAT(:likeQuery, '%')
                OR INSTR(LOWER(a.matricula), :normalizedQuery) > 0
                OR LOWER(a.nombre) = :normalizedQuery
                OR LOWER(a.nombre) LIKE CONCAT(:likeQuery, '%')
                OR LOWER(a.nombre) REGEXP CONCAT('(^| )', :regexQuery)
                OR INSTR(LOWER(a.nombre), :normalizedQuery) > 0
            )
            """;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Object[]> searchWithRanking(String normalizedQuery,
                                            String likeQuery,
                                            String regexQuery,
                                            EstadoAlumno estado,
                                            String carrera,
                                            Integer semestre,
                                            Pageable pageable,
                                            SearchSortOption sortOption) {

        String whereClause = buildWhereClause(estado, carrera, semestre);
        String selectSql = """
                SELECT a.id,
                       a.matricula,
                       a.nombre,
                       a.carrera,
                       a.semestre,
                       a.estado,
                       %s AS score,
                       t.id AS tutor_id,
                       t.nombre AS tutor_nombre,
                       t.carrera AS tutor_carrera
                FROM alumnos a
                LEFT JOIN tutores t ON a.id_tutor_actual = t.id
                """.formatted(SCORE_EXPRESSION) + whereClause + buildOrderClause(sortOption);

        Query dataQuery = entityManager.createNativeQuery(selectSql);
        applyCommonParameters(dataQuery, normalizedQuery, likeQuery, regexQuery);
        applyFilterParameters(dataQuery, estado, carrera, semestre);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        String countSql = "SELECT COUNT(*) FROM alumnos a " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        applyCommonParameters(countQuery, normalizedQuery, likeQuery, regexQuery);
        applyFilterParameters(countQuery, estado, carrera, semestre);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        Number total = (Number) countQuery.getSingleResult();

        return new PageImpl<>(rows, pageable, total.longValue());
    }

    public Optional<Object[]> findByMatricula(String normalizedQuery,
                                              String likeQuery,
                                              String regexQuery) {

        String sql = """
                SELECT a.id,
                       a.matricula,
                       a.nombre,
                       a.carrera,
                       a.semestre,
                       a.estado,
                       %s AS score,
                       t.id AS tutor_id,
                       t.nombre AS tutor_nombre,
                       t.carrera AS tutor_carrera
                FROM alumnos a
                LEFT JOIN tutores t ON a.id_tutor_actual = t.id
                WHERE LOWER(a.matricula) = :normalizedQuery
                """.formatted(SCORE_EXPRESSION);

        Query query = entityManager.createNativeQuery(sql);
        applyCommonParameters(query, normalizedQuery, likeQuery, regexQuery);
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    private String buildWhereClause(EstadoAlumno estado, String carrera, Integer semestre) {
        StringBuilder builder = new StringBuilder("WHERE ");
        builder.append(MATCH_CONDITION);
        if (estado != null) {
            builder.append(" AND a.estado = :estado");
        }
        if (carrera != null) {
            builder.append(" AND LOWER(a.carrera) = :carrera");
        }
        if (semestre != null) {
            builder.append(" AND a.semestre = :semestre");
        }
        return builder.toString();
    }

    private void applyCommonParameters(Query query,
                                       String normalizedQuery,
                                       String likeQuery,
                                       String regexQuery) {
        query.setParameter("normalizedQuery", normalizedQuery);
        query.setParameter("likeQuery", likeQuery);
        query.setParameter("regexQuery", regexQuery);
    }

    private void applyFilterParameters(Query query,
                                       EstadoAlumno estado,
                                       String carrera,
                                       Integer semestre) {
        if (estado != null) {
            query.setParameter("estado", estado.name());
        }
        if (carrera != null) {
            query.setParameter("carrera", carrera);
        }
        if (semestre != null) {
            query.setParameter("semestre", semestre);
        }
    }

    private String buildOrderClause(SearchSortOption sortOption) {
        if (sortOption == null || sortOption == SearchSortOption.RELEVANCE) {
            return " ORDER BY score DESC, a.matricula ASC, a.nombre ASC";
        }
        if (sortOption == SearchSortOption.MATRICULA) {
            return " ORDER BY a.matricula ASC, score DESC";
        }
        return " ORDER BY a.nombre ASC, score DESC";
    }
}
