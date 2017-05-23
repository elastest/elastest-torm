package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.ElasEtmTjob;

public interface TJobRepository extends JpaRepository<ElasEtmTjob, Long> {

}
