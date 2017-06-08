package io.elastest.etm.rabbitmq.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

@Service
public class RabbitmqService {
	@Autowired
	//private CachingConnectionFactory rabbitConnectionFactory;
	private SimpleMessageListenerContainer container;	
	private Connection connection;
	private Channel channel;
	
	@Value("${spring.rabbitmq.username}")
	private String user;
	
	@Value("${spring.rabbitmq.password}")
	private String pass;

	public Connection createRabbitmqConnection() {
		connection = container.getConnectionFactory().createConnection();
		createChannel();
		return connection;
	}

	public void closeConnection() {
		connection.close();
		container.getConnectionFactory();
	}

	public void createChannel() {
		channel = connection.createChannel(false);
	}
	
	public void closeChannel(){
		try {
			channel.close();
			channel.abort();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}

	public void createFanoutExchange(String name) {
		try {
			channel.exchangeDeclare(name, BuiltinExchangeType.FANOUT, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteFanoutExchange(String name){
		try {
			channel.exchangeDelete(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createQueue(String name) {
		try {
			channel.queueDeclare(name, true, false, false, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteQueue(String name){
		try {
			channel.queueDelete(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void bindQueueToExchange(String queue, String exchange, String routingKey) {
		try {
			channel.queueBind(queue, exchange, routingKey);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	/* Getters Setters */

	public SimpleMessageListenerContainer getContainer() {
		return container;
	}

	public void setContainer(SimpleMessageListenerContainer container) {
		this.container = container;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}
}