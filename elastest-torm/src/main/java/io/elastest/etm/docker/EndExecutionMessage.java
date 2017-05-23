package io.elastest.etm.docker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EndExecutionMessage {
	
	private String message;
	
	public EndExecutionMessage(){
		
	}
	
	public EndExecutionMessage (String message){
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toJSON(){
		ObjectMapper mapper = new ObjectMapper();

		//Object to JSON in String
		String jsonInString;
		try {
			jsonInString = mapper.writeValueAsString(this);
			return jsonInString;

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	

}
