package io.elastest.etm.dao.external;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.elastest.etm.model.external.ExternalPrometheus;

public interface ExternalPrometheusRepository
        extends JpaRepository<ExternalPrometheus, Long> {
    @Query("SELECT p FROM ExternalPrometheus p JOIN FETCH p.fieldFilters WHERE p.id = (:id)")
    public Optional<ExternalPrometheus> findById(Long id);
}