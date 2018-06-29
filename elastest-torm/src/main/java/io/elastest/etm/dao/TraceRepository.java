package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.Trace;

@Repository
public interface TraceRepository extends JpaRepository<Trace, Long> {
    public List<Trace> findByExecInAndStreamAndComponent(List<String> execs,
            String stream, String component);
}
