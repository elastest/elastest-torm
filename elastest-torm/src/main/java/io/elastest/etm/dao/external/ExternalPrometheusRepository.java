package io.elastest.etm.dao.external;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalPrometheus;

public interface ExternalPrometheusRepository
        extends JpaRepository<ExternalPrometheus, Long> {
}