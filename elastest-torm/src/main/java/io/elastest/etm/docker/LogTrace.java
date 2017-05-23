package io.elastest.etm.docker;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogTrace {
	private static final AtomicLong count = new AtomicLong(0);
	private String trace;
	private long id;

	public LogTrace() {

	}

	public LogTrace(String trace) {
		this.id = count.incrementAndGet();
		this.trace = trace;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	public long getId() {
		return id;
	}
	
	@Override
	public String toString(){
		return trace;
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
