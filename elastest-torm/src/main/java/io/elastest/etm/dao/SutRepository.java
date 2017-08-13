package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.SutSpecification;

public interface SutRepository extends JpaRepository<SutSpecification, Long> {

}
