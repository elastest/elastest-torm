package io.elastest.epm.client.test.integration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.epm.client.service.K8Service;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { K8Service.class }, webEnvironment = RANDOM_PORT)
@Tag("integration")
@DisplayName("Integration tests for K8Docker Compose Service")
@EnableAutoConfiguration
@PropertySources({ @PropertySource(value = "classpath:epm-client.properties") })
public class K8ServiceIntegrationTest {
    @Autowired
    K8Service k8Service;
    
    @Test
    public void testDeployJob() {
        k8Service.deployJob();        
    }
}
