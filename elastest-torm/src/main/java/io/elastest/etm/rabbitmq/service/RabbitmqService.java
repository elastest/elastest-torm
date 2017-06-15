package io.elastest.etm.rabbitmq.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
	// private CachingConnectionFactory rabbitConnectionFactory;
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

	public void closeChannel() {
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

	public void deleteFanoutExchange(String name) {
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

	public void deleteQueue(String name) {
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

	public Map<String, String> createTJobExecQueues(String execId, boolean withSut) {
		String queuePrefix = "q-" + execId;
		Map<String, String> rabbitMap = new HashMap<String, String>();
		rabbitMap.put(queuePrefix + "-test-log", "test." + execId + ".log");
		rabbitMap.put(queuePrefix + "-test-metrics", "test." + execId + ".metrics");
		if (withSut) {
			rabbitMap.put(queuePrefix + "-sut-log", "sut." + execId + ".log");
			rabbitMap.put(queuePrefix + "-sut-metrics", "sut." + execId + ".metrics");
		}

		return rabbitMap;
	}

	public Map<String, String> startRabbitmq(String execId, boolean withSut) throws Exception {
		Map<String, String> rabbitMap = createTJobExecQueues(execId, withSut);
		try {
			System.out.println("Starting Rabbitmq queues " + execId);
			createRabbitmqConnection();
			for (Map.Entry<String, String> rabbitLine : rabbitMap.entrySet()) {
				createQueue(rabbitLine.getKey());
				bindQueueToExchange(rabbitLine.getKey(), "amq.topic", rabbitLine.getValue());
			}

			System.out.println("Successfully started Rabbitmq " + execId);
		} catch (Exception e) {
			e.printStackTrace();
			purgeRabbitmq(rabbitMap, execId);
			throw e;
		}
		return rabbitMap;
	}

	public void purgeRabbitmq(Map<String, String> rabbitMap, String execId) {

		try {
			System.out.println("Purging Rabbitmq " + execId);
			queueIsEmpty("q-" + execId + "-test-log");

			for (Map.Entry<String, String> rabbitLine : rabbitMap.entrySet()) {
				deleteQueue(rabbitLine.getKey());
			}
			closeChannel();
			closeConnection();
		} catch (Exception e) {
			System.out.println("Error on purging Rabbitmq " + execId);
		}
	}

	public void queueIsEmpty(String queue) {
		try {
			long n1 = channel.messageCount(queue);
			if (n1 > 0) {
				System.out.println("Queue is not empty, waiting for consuming messages");
				Thread.sleep(2000);
				long n2 = channel.messageCount(queue);

				if (n1 <= n2) {
					int counter = 2;
					while (n1 <= n2 && counter > 0) {
						System.out.println("Not consumming messages. Retrying, rabbitmq counter " + counter);
						Thread.sleep(2000);
						n2 = channel.messageCount(queue);
						counter--;
					}
					System.out.println("Not consumming messages. End retry. Purging queue");

				} else {
					System.out.println("Consumming messages. Rechecking queue");
					queueIsEmpty(queue);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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