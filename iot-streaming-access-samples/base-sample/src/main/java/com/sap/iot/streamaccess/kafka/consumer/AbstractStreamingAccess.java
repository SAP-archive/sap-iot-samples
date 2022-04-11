package com.sap.iot.streamaccess.kafka.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * abstract method containing the template for kafka stream access
 */
public abstract class AbstractStreamingAccess {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected KafkaConsumer<String, byte[]> consumer;

    AbstractStreamingAccess(KafkaConsumer<String, byte[]> consumer) {
        this.consumer = consumer;
    }

    /**
     * run's the consumer in a separate thread
     */
    public void start() {
        new Thread(consume()).start();
    }

    /**
     * @return runnable defining the consumption logic for this consumer
     */
    public abstract Runnable consume();
}
