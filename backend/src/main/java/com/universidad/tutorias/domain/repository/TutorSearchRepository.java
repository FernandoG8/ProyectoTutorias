package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.application.enums.SearchSortOption;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class TutorSearchRepository {

    private static final String SCORE_EXPRESSION = """
            CASE
                WHEN LOWER(t.nombre) = :normalizedQuery THEN 500
                WHEN LOWER(t.nombre) LIKE CONCAT(:likeQuery, '%') THEN 420
                WHEN LOWER(t.nombre) REGEXP CONCAT('(^| )', :regexQuery) THEN 400
                WHEN INSTR(LOWER(t.nombre), :normalizedQuery) > 0 THEN 200
                ELSE 0
            END
            """;

    private static final String MATCH_CONDITION = """
            (
                LOWER(t.nombre) = :normalizedQuery
                OR LOWER(t.nombre) LIKE CONCAT(:likeQuery, '%')
                OR LOWER(t.nombre) REGEXP CONCAT('(^| )', :regexQuery)
                OR INSTR(LOWER(t.nombre), :normalizedQuery) > 0
            )
            """;

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Object[]> searchWithRanking(String normalizedQuery,
                                            String likeQuery,
                                            String regexQuery,
                                            String carrera,
                                            Pageable pageable,
                                            SearchSortOption sortOption) {

        String whereClause = buildWhereClause(carrera);
        String selectSql = """
                SELECT t.id,
                       t.nombre,
                       t.carrera,
                       %s AS score
                FROM tutores t
                """.formatted(SCORE_EXPRESSION) + whereClause + buildOrderClause(sortOption);

        Query dataQuery = entityManager.createNativeQuery(selectSql);
        applyCommonParameters(dataQuery, normalizedQuery, likeQuery, regexQuery);
        applyFilterParameters(dataQuery, carrera);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        String countSql = "SELECT COUNT(*) FROM tutores t " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        applyCommonParameters(countQuery, normalizedQuery, likeQuery, regexQuery);
        applyFilterParameters(countQuery, carrera);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQuery.getResultList();
        Number total = (Number) countQuery.getSingleResult();

        return new PageImpl<>(rows, pageable, total.longValue());
    }

    private String buildWhereClause(String carrera) {
        StringBuilder builder = new StringBuilder("WHERE ");
        builder.append(MATCH_CONDITION);
        if (carrera != null) {
            builder.append(" AND LOWER(t.carrera) = :carrera");
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

    private void applyFilterParameters(Query query, String carrera) {
        if (carrera != null) {
            query.setParameter("carrera", carrera);
        }
    }

    private String buildOrderClause(SearchSortOption sortOption) {
        if (sortOption == null || sortOption == SearchSortOption.RELEVANCE) {
            return " ORDER BY score DESC, t.nombre ASC";
        }
        return " ORDER BY t.nombre ASC, score DESC";
    }
}
