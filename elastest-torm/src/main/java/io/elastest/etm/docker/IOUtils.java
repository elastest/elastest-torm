package io.elastest.etm.docker;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class IOUtils {
	
	private List<String> logLines = new ArrayList<>();
	
	public List<LogTrace> getLogFragment(Integer fromLine){
		
		System.out.println("Start Line: " + fromLine);
				
		List<LogTrace> logLinesToReturn = new ArrayList<>();
		//ObjectMapper mapper = new ObjectMapper();
		int actualSize = logLines.size();
		
		for (int i = fromLine; i < actualSize; i++){
			logLinesToReturn.add(new LogTrace(logLines.get(i)));
			System.out.println("Log fragments to send"+"-"+i+"-"+actualSize+":"+logLines.get(i));
			if(logLines.get(i).equals("END")){
				logLines =  new ArrayList<>();
			}
		}
				
		return logLinesToReturn;

	}

	public List<String> getLogLines() {
		return logLines;
	}

	public void setLogLines(List<String> logLines) {
		this.logLines = logLines;
	}

}
