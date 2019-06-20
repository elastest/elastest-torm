package io.elastest.etm.dao.external;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalElasticsearch;

public interface ExternalElasticsearchRepository
        extends JpaRepository<ExternalElasticsearch, Long> {
}



