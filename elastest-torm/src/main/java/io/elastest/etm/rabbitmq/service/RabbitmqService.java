package io.elastest.etm.rabbitmq.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

@Service
public class RabbitmqService {
	@Autowired
	private CachingConnectionFactory rabbitConnectionFactory;
	private Connection connection;
	private Channel channel;

	public Connection createRabbitmqConnection(String host, String user, String pass) {
		rabbitConnectionFactory = new CachingConnectionFactory(host);
		rabbitConnectionFactory.setUsername(user);
		rabbitConnectionFactory.setPassword(pass);
		connection = rabbitConnectionFactory.createConnection();
		createChannel();
		return connection;
	}

	public void closeConnection() {
		connection.close();
		rabbitConnectionFactory.destroy();
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
}