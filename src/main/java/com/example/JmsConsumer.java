package com.example;

import java.util.Scanner;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import tools.jackson.databind.ObjectMapper;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class JmsConsumer {

    static int status = 1;

    private static volatile boolean running = false;
    private static volatile boolean exit = false;

    public static void main(String[] args) {

        try {
            // Create connection factory
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();

            // Set MQ properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, MQConstants.HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, MQConstants.PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, MQConstants.CHANNEL);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, MQConstants.QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsConsumer");
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            cf.setStringProperty(WMQConstants.USERID, MQConstants.APP_USER);
            cf.setStringProperty(WMQConstants.PASSWORD, MQConstants.APP_PASSWORD);

            // Create JMS objects
            JMSContext context = cf.createContext();
            Destination destination = context.createQueue("queue:///" + MQConstants.QUEUE_NAME);
            JMSConsumer consumer = context.createConsumer(destination);

            Thread mqThread = getMqThread(consumer);

            // Console commands
            Scanner scanner = new Scanner(System.in);

            System.out.println("Commands:");
            System.out.println(" start - Start listening");
            System.out.println(" stop  - Stop listening");
            System.out.println(" exit  - Exit application");

            while (true) {

                System.out.print("> ");
                String input = scanner.nextLine().trim().toLowerCase();

                switch (input) {

                    case "start":
                        if (!running) {
                            running = true;
                            System.out.println("MQ Consumer Started.");
                        } else {
                            System.out.println("Consumer is already running.");
                        }
                        break;

                    case "stop":
                        if (running) {
                            running = false;
                            System.out.println("MQ Consumer Stopped.");
                        } else {
                            System.out.println("Consumer is already stopped.");
                        }
                        break;

                    case "exit":
                        running = false;
                        exit = true;

                        System.out.println("Shutting down...");

                        mqThread.join();

                        consumer.close();
                        context.close();
                        scanner.close();

                        System.exit(status);
                        break;

                    default:
                        System.out.println("Unknown command.");
                        System.out.println("Available commands: start | stop | exit");
                }
            }

        } catch (Exception e) {
            recordFailure(e);
        }

        System.exit(status);
    }

    private static Thread getMqThread(JMSConsumer consumer) {
        ObjectMapper mapper = new ObjectMapper();

        // MQ Listener Thread
        Thread mqThread = new Thread(() -> {

            while (!exit) {

                if (!running) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                try {

                    Message message = consumer.receive(5000);

                    if (message == null) {
                        System.out.println("Waiting for messages...");
                        continue;
                    }

                    if (message instanceof TextMessage) {

                        TextMessage textMessage = (TextMessage) message;
                        String json = textMessage.getText();

                        Request request = mapper.readValue(json, Request.class);

                        System.out.println("\n========== Request Received ==========");
                        System.out.println("Unique Number : " + request.getUniqueNumber());
                        System.out.println("Test          : " + request.getTest());
                        System.out.println("Date          : " + request.getDate());
                        System.out.println("======================================");

                    } else {
                        System.out.println("Unsupported message type: "
                                + message.getClass().getSimpleName());
                    }

                } catch (Exception e) {
                    recordFailure(e);
                }
            }

            System.out.println("MQ listener thread exited.");

        });

        mqThread.start();
        return mqThread;
    }

    /**
     * Record failure.
     */
    static void recordFailure(Exception ex) {

        if (ex != null) {
            if (ex instanceof JMSException) {
                processJMSException((JMSException) ex);
            } else {
                ex.printStackTrace();
            }
        }

        status = -1;
    }

    /**
     * Print JMS exception details.
     */
    static void processJMSException(JMSException e) {

        e.printStackTrace();

        Throwable inner = e.getLinkedException();

        while (inner != null) {
            inner.printStackTrace();
            inner = inner.getCause();
        }
    }
}