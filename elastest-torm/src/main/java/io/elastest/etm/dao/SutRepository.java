package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.api.model.SutSpecification;

public interface SutRepository extends JpaRepository<SutSpecification, Long> {

}
