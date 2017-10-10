package io.elastest.etm.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import io.elastest.etm.model.SupportServiceInstance;

public class ETEnvironmentVariables {
	@Value("${et.public.host}")
	public String ET_SERVICES_IP;

	@Value("${elastest.docker.network}")
	private String etDockerNetwork;

	@Value("ET_EDM_ALLUXIO_API")
	public String ET_EDM_ALLUXIO_API;
	@Value("${et.edm.mysql.host}")
	public String ET_EDM_MYSQL_HOST;
	@Value("${et.edm.mysql.port}")
	public String ET_EDM_MYSQL_PORT;
	@Value("${et.edm.elasticsearch.api}")
	public String ET_EDM_ELASTICSEARCH_API;
	@Value("${et.edm.api}")
	public String ET_EDM_API;
	@Value("${et.epm.api}")
	public String ET_EPM_API;
	@Value("${et.etm.api}")
	public String ET_ETM_API;
	@Value("${et.esm.api}")
	public String ET_ESM_API;
	@Value("${et.eim.api}")
	public String ET_EIM_API;
	@Value("${et.etm.lsbeats.host}")
	public String ET_ETM_LSBEATS_HOST;
	@Value("${et.etm.lsbeats.port}")
	public String ET_ETM_LSBEATS_PORT;
	@Value("${et.etm.lshttp.api}")
	public String ET_ETM_LSHTTP_API;
	@Value("${et.etm.rabbit.host}")
	public String ET_ETM_RABBIT_HOST;
	@Value("${et.etm.rabbit.port}")
	public String ET_ETM_RABBIT_PORT;
	@Value("${et.emp.api}")
	public String ET_EMP_API;
	@Value("${et.emp.influxdb.api}")
	public String ET_EMP_INFLUXDB_API;
	@Value("${et.emp.influxdb.host}")
	public String ET_EMP_INFLUXDB_HOST;
	@Value("${et.emp.influxdb.graphite.port}")
	public String ET_EMP_INFLUXDB_GRAPHITE_PORT;
	
	private Map<String, String> fillEnvVariablesToTSS(SupportServiceInstance supportServiceInstance) {
		Map<String, String> envVariables = new HashMap<>();
		envVariables.put("ET_SERVICES_IP", ET_SERVICES_IP);
		envVariables.put("ET_EDM_ALLUXIO_API", ET_EDM_ALLUXIO_API);
		envVariables.put("ET_EDM_MYSQL_HOST", ET_EDM_MYSQL_HOST);
		envVariables.put("ET_EDM_MYSQL_PORT", ET_EDM_MYSQL_PORT);
		envVariables.put("ET_EDM_ELASTICSEARCH_API", ET_EDM_ELASTICSEARCH_API);
		envVariables.put("ET_EDM_API", ET_EDM_API);
		envVariables.put("ET_EPM_API", ET_EPM_API);
		envVariables.put("ET_ETM_API", ET_ETM_API);
		envVariables.put("ET_ESM_API", ET_ESM_API);
		envVariables.put("ET_EIM_API", ET_EIM_API);
		envVariables.put("ET_ETM_LSBEATS_HOST", ET_ETM_LSBEATS_HOST);
		envVariables.put("ET_ETM_LSBEATS_PORT", ET_ETM_LSBEATS_PORT);
		envVariables.put("ET_ETM_LSHTTP_API", ET_ETM_LSHTTP_API);
		envVariables.put("ET_ETM_RABBIT_HOST", ET_ETM_RABBIT_HOST);
		envVariables.put("ET_ETM_RABBIT_PORT", ET_ETM_RABBIT_PORT);
		envVariables.put("ET_EMP_API", ET_EMP_API);
		envVariables.put("ET_EMP_INFLUXDB_API", ET_EMP_INFLUXDB_API);
		envVariables.put("ET_EMP_INFLUXDB_HOST", ET_EMP_INFLUXDB_HOST);
		envVariables.put("ET_EMP_INFLUXDB_GRAPHITE_PORT", ET_EMP_INFLUXDB_GRAPHITE_PORT);
		return envVariables;
	}

}
