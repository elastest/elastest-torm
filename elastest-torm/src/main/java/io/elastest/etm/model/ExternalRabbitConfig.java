package io.elastest.etm.model;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalRabbitConfig {

//TODO Test if this values are stored as this class is not a bean
//	#RabbitMQ
//	spring.rabbitmq.host= 192.168.99.100
//	spring.rabbitmq.port= 5672
//	spring.rabbitmq.username = elastest-etm
//	spring.rabbitmq.password = elastest-etm
//	spring.rabbitmq.virtual-host= /elastest-etm
	
	@JsonProperty("host")
	@Value("$spring.rabbitmq.host}")
	private String host;
	
	@JsonProperty("port")
	@Value("$spring.rabbitmq.port}")
	private String port;
	
	@JsonProperty("username")
	@Value("$spring.rabbitmq.username}")
	private String username;
	
	@JsonProperty("password")
	@Value("$spring.rabbitmq.password}")
	private String password;
	
	@JsonProperty("virtualHost")
	@Value("$spring.rabbitmq.virtal-host}")
	private String virtualHost;
	
	public ExternalRabbitConfig(){}
	
	public ExternalRabbitConfig(String host, String port, String username, String password, String virtualHost){
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.virtualHost = virtualHost;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}
	
	

}
