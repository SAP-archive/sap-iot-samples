package com.sap.iot.streamaccess.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * raw_timeseries topic consumption example
 */
public class RawTimeSeriesStreamingAccess extends AbstractStreamingAccess {

    public RawTimeSeriesStreamingAccess(KafkaConsumer<String, byte[]> consumer) {
        super(consumer);
    }

    @Override
    public Runnable consume() {
        return () -> {
            while (true) {

                try {
                    ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, byte[]> record : consumerRecords) {
                        logger.info("Key: {}, Value: {}, Partition: {}, Offset: {}", record.key(), new String(record.value(), StandardCharsets.UTF_8), record.partition(), record.offset());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }
}
