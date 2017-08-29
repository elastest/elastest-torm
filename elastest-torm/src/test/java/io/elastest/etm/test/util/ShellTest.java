package io.elastest.etm.test.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.elastest.etm.utils.Shell;

@RunWith(JUnitPlatform.class)
public class ShellTest {
	Process p;
	

	
	@Test
	public void testRunAndWait(){
		String result = Shell.runAndWaitString("java -version");
		assertTrue(result.length() > 0);
	}
}
