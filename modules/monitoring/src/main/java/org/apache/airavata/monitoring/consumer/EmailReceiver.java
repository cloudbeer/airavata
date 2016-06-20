package org.apache.airavata.monitoring.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class EmailReceiver extends Thread {
    private static volatile Connection connection;
    private static volatile Channel channel;
    private static final String EXCHANGE_TYPE = "fanout";

    private String exchangeName;
    private String queueName;
    private String brokerURI;
    private Thread recieverThread;

    public EmailReceiver(String exchangeName, String queueName, String brokerURI) {
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.brokerURI = brokerURI;
    }

    public void startThread() throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setUri(brokerURI);
        connection = factory.newConnection();
        recieverThread = new Thread(this);
        recieverThread.start();
    }

    public void shutdown() throws IOException, TimeoutException {
        channel.close();
        connection.close();
        System.out.println("Email receiver thread succesfully shutdown");
    }

    @Override
    public void run() {
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE);
            channel.queueDeclare(queueName, true, false, false, null).getQueue();
            channel.queueBind(queueName, exchangeName, "");
            System.out.println(" [*] Waiting for messages.");
            Consumer consumer = new EmailConsumer(channel);
            channel.basicConsume(queueName, true, consumer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}