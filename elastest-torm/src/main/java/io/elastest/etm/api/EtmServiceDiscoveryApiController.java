package io.elastest.etm.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.elastest.etm.service.EsmService;

@Controller
public class EtmServiceDiscoveryApiController implements EtmServiceDiscoveryApi {
	
	@Autowired
	EsmService esmService;

	@Override
	public ResponseEntity<Map<String,String>> getTSSInstanceContext(@PathVariable("tSSInstanceId") String tSSInstanceId) {
		Map<String, String> tSSInstanceContextMap = esmService.getTSSInstanceContext(tSSInstanceId);
		if (tSSInstanceContextMap != null){
			return new ResponseEntity<Map<String, String>>(esmService.getTSSInstanceContext(tSSInstanceId), HttpStatus.OK);
		}else{
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
	}

}
