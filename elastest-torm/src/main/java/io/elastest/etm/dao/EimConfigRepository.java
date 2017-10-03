package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.EimConfig;

public interface EimConfigRepository extends JpaRepository<EimConfig, Long> {

}
