package io.elastest.etm.test.api;

import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElastestETMSpringBoot;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElastestETMSpringBoot.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ProjectApiItTest {
	
	final Logger log = LoggerFactory.getLogger(ProjectApiItTest.class);
	
//	@Test
//	public void test
	

}
