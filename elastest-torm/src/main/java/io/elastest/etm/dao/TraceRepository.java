package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.Trace;

@Repository
public interface TraceRepository extends JpaRepository<Trace, Long> {

}
