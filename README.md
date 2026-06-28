# IBM MQ JMS Producer & Consumer Example

A simple Java project demonstrating how to send and receive JSON messages using IBM MQ and JMS.

## Features

* Java JMS Producer
* Java JMS Consumer
* JSON message serialization/deserialization using Jackson
* Start/Stop consumer from the console
* IBM MQ running locally using Docker

---

# Prerequisites

Install the following software:

* Java 8 or later
* Maven (or Gradle if preferred)
* Docker
* Git

---

# Running IBM MQ Locally

Pull the IBM MQ Developer image:

```bash
docker pull ibmcom/mq:latest
```

Start an MQ container:

```bash
docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --volume qm1data:/mnt/mqm --publish 1414:1414 --publish 9443:9443 --detach --env MQ_APP_PASSWORD=passw0rd ibmcom/mq:latest
```

Verify the container is running:

```bash
docker ps
```

---

# MQ Web Console

Open:

```
https://localhost:9443/ibmmq/console/login.html
```

Default credentials:

```
Username: admin
Password: passw0rd
```

Accept the browser security warning if prompted.

---

# Create a Queue

Create a local queue from the MQ Console.

Example:

```
DEV.QUEUE.1
```

Alternatively, use any queue name of your choice and update the configuration accordingly.

---

# Configure MQConstants.java

Update the values in `MQConstants.java` to match your local MQ setup.

Example:

```java
public class MQConstants {

    public static final String HOST = "localhost";
    public static final int PORT = 1414;

    public static final String CHANNEL = "DEV.APP.SVRCONN";
    public static final String QMGR = "QM1";
    public static final String QUEUE_NAME = "DEV.QUEUE.1";

    public static final String APP_USER = "app";
    public static final String APP_PASSWORD = "passw0rd";

}
```

If your queue manager or queue names differ, simply replace the values above.

---

# Running the Producer

Execute:

```
Run the `main()` method in `Producer.java`.
```

Expected output:

```
Sent message:
{
    "uniqueNumber":123,
    "test":"Hello MQ",
    "date":"06062026"
}
```

---

# Running the Consumer

Execute:

```
Run the `main()` method in `JmsConsumer.java`.
```

Available commands:

```
start
stop
exit
```

Example:

```
> start

========== Request Received ==========
Unique Number : 123
Test          : Hello MQ
Date          : 06062026
======================================
```

---

# Project Structure

```
src
 └── com.example
      ├── MQConstants.java
      ├── Producer.java
      ├── JmsConsumer.java
      └── Request.java
```

---

# Technologies Used

* Java JMS 2.0
* IBM MQ
* Jackson Databind
* Docker

---

# Notes

* Messages are sent as JSON using `TextMessage`.
* Jackson is used to convert between JSON and Java objects.
* The consumer polls the queue every five seconds.
* Type `stop` to pause consuming without closing the application.
* Type `exit` to shut down the consumer gracefully.

---

# Future Improvements

* Message Listener (`MessageListener`)
* Spring JMS integration
* SSL/TLS configuration
* Dead Letter Queue handling
* Request validation
* Unit and integration tests
* Docker Compose setup

---

# License

This project is provided for learning and demonstration purposes.
