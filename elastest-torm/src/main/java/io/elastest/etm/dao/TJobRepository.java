package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.ElasEtmTjob;

@Repository(value="io.elastest.etm.dao.TJobRepository")
public interface TJobRepository extends JpaRepository<ElasEtmTjob, Long> {

}
