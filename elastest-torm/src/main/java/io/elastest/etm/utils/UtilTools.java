package io.elastest.etm.utils;

import javax.xml.ws.http.HTTPException;

public class UtilTools {

	public int getHttpExceptionCode(Exception e){
		if(e instanceof HTTPException){
			return ((HTTPException) e).getStatusCode();
		}
		return 500;
	}
	

}
