package io.elastest.etm.dao.external;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalMonitoringDB;

public interface ExternalMonitoringDBRepository
        extends JpaRepository<ExternalMonitoringDB, Long> {
}



