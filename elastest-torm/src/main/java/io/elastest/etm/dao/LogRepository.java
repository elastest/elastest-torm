package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import io.elastest.etm.api.model.Log;

public interface LogRepository extends JpaRepository<Log, Long> {	

}
