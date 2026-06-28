package com.example;

public class MQConstants {

    // Create variables for the connection to MQ
    static final String HOST = "localhost"; // Host name or IP address
    static final int PORT = 1414; // Listener port for your queue manager
    static final String CHANNEL = "DEV.APP.SVRCONN"; // Channel name default for local docker
    static final String QMGR = "QM1"; // Queue manager name
    static final String APP_USER = "app"; // User name that application uses to connect to MQ
    static final String APP_PASSWORD = "passw0rd"; // Password that the application uses to connect to MQ
    static final String QUEUE_NAME = "DEV.QUEUE.1"; // Queue that the application uses to put and get messages to and from
}
