package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.LogAnalyzerConfig;

@Repository
public interface LogAnalyzerRepository extends JpaRepository<LogAnalyzerConfig, Long> {

}
