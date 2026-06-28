package com.example;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class JmsProducer {

    // System exit status value (assume unset value to be 1)
     static int status = 1;

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {

        // Variables
        JMSContext context;
        Destination destination;
        JMSProducer producer;

        try {
            // Create a connection factory
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, MQConstants.HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, MQConstants.PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, MQConstants.CHANNEL);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, MQConstants.QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            cf.setStringProperty(WMQConstants.USERID, MQConstants.APP_USER);
            cf.setStringProperty(WMQConstants.PASSWORD, MQConstants.APP_PASSWORD);
            //cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, "*TLS12ORHIGHER");
            //cf.setIntProperty(MQConstants.CERTIFICATE_VALIDATION_POLICY, MQConstants.MQ_CERT_VAL_POLICY_NONE);

            // Create JMS objects
            context = cf.createContext();
            destination = context.createQueue("queue:///" + MQConstants.QUEUE_NAME);

            ObjectMapper mapper = new ObjectMapper();

            Request request = new Request();
            long uniqueNumber = System.currentTimeMillis() % 1000;
            request.setUniqueNumber(uniqueNumber);
            request.setTest("Hello MQ");
            request.setDate("06062026");

            String json = mapper.writeValueAsString(request);
            TextMessage message = context.createTextMessage(json);

            producer = context.createProducer();
            producer.send(destination, message);
            System.out.println("Sent message:\n" + message);

            context.close();

            recordSuccess();
        } catch (JMSException e) {
            recordFailure(e);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

        System.exit(status);

    } // end main()

    /**
     * Record this run as successful.
     */
     static void recordSuccess() {
        System.out.println("SUCCESS");
        status = 0;
     }

    /**
     * Record this run as failure.
     *
     * @param ex
     */
     static void recordFailure(Exception ex) {
        if (ex != null) {
            if (ex instanceof JMSException) {
                processJMSException((JMSException) ex);
            } else {
                System.out.println(ex);
            }
        }
        System.out.println("FAILURE");
        status = -1;
     }

    /**
     * Process a JMSException and any associated inner exceptions.
     *
     * @param e
     */
     static void processJMSException(JMSException e) {
        System.out.println(e);
        Throwable innerException = e.getLinkedException();
        if (innerException != null) {
            System.out.println("Inner exception(s):");
        }
        while (innerException != null) {
            System.out.println(innerException);
            innerException = innerException.getCause();
        }
     }
}
