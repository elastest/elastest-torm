package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.api.model.TOJob;

public interface TOJobRepository extends JpaRepository<TOJob, Long> {

}
