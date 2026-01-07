package com.universidad.tutorias.domain.repository;

import com.universidad.tutorias.domain.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExportJobRepository extends JpaRepository<ExportJob, String> {

    Optional<ExportJob> findByCreatedByAndTypeAndIdempotencyKey(String createdBy, String type, String idempotencyKey);
}
