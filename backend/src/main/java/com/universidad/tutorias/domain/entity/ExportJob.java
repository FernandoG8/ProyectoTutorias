package com.universidad.tutorias.domain.entity;

import com.universidad.tutorias.domain.enums.ExportJobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_jobs", uniqueConstraints = {
        @UniqueConstraint(name = "uq_export_jobs_idempo", columnNames = {"created_by", "type", "idempotency_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJob {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExportJobStatus status;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "created_by", nullable = false, length = 150)
    private String createdBy;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    private Integer total;
    private Integer processed;
    @Column(name = "success_count")
    private Integer successCount;
    @Column(name = "fail_count")
    private Integer failCount;

    @Column(name = "progress_pct")
    private Integer progressPct;

    @Column(length = 255)
    private String message;

    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "error_json", columnDefinition = "TEXT")
    private String errorJson;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ExportJobStatus.QUEUED;
        }
        if (processed == null) processed = 0;
        if (successCount == null) successCount = 0;
        if (failCount == null) failCount = 0;
    }

    public void markRunning() {
        status = ExportJobStatus.RUNNING;
        startedAt = LocalDateTime.now();
    }

    public void markFinished(ExportJobStatus finalStatus) {
        status = finalStatus;
        finishedAt = LocalDateTime.now();
    }
}
