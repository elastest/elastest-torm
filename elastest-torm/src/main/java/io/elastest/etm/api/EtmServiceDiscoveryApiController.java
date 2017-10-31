package io.elastest.etm.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.elastest.etm.service.EsmService;

@Controller
public class EtmServiceDiscoveryApiController implements EtmServiceDiscoveryApi {

	@Autowired
	EsmService esmService;

	@Value("${et.edm.elasticsearch.api}")
	public String elasticsearchApi;

	@Value("${et.public.host}")
	public String publicHost;

	@Override
	public ResponseEntity<Map<String, String>> getTSSInstanceContext(
			@PathVariable("tSSInstanceId") String tSSInstanceId) {
		Map<String, String> tSSInstanceContextMap = esmService.getTSSInstanceContext(tSSInstanceId);
		if (tSSInstanceContextMap != null) {
			return new ResponseEntity<Map<String, String>>(esmService.getTSSInstanceContext(tSSInstanceId),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@Override
	public ResponseEntity<String> getElasticsearchApiUrl() {
		return new ResponseEntity<String>("http://" + publicHost + ":37000/elasticsearch", HttpStatus.OK);
		// return new ResponseEntity<String>(elasticsearchApi, HttpStatus.OK);
		// TODO
	}

	@Override
	public ResponseEntity<String> getLogstashIp() {
		return new ResponseEntity<String>(publicHost, HttpStatus.OK);
		// TODO real logstash ip with proxy
	}

	@Override
	public ResponseEntity<Map<String, String>> getLogstashInfo() {
		Map<String, String> logstashInfo = new HashMap<>();
		// TODO real logstash ip with proxy
		logstashInfo.put("logstashIp", publicHost);
		logstashInfo.put("logstashBeatsPort", "5044");
		logstashInfo.put("logstashHttpPort", "5003");

		return new ResponseEntity<Map<String, String>>(logstashInfo, HttpStatus.OK);
	}
}
