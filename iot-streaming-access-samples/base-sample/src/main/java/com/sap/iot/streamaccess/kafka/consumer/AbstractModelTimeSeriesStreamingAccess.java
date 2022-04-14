package com.sap.iot.streamaccess.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sap.iot.streamaccess.constants.Constants;
import com.sap.iot.streamaccess.constants.DataModelType;
import com.sap.iot.streamaccess.service.MessageParser;
import com.sap.iot.streamaccess.service.MessageParserImpl;
import com.sap.iot.streamaccess.util.AvroUtils;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;

/**
 * abstractmodel_timeseries topic consumption example
 * uses avro deserializer
 */
public class AbstractModelTimeSeriesStreamingAccess extends AbstractStreamingAccess {

    private final ObjectMapper objectMapper;
    private final MessageParser messageParser;

    public AbstractModelTimeSeriesStreamingAccess(KafkaConsumer<String, byte[]> consumer) {
        super(consumer);
        this.objectMapper = new ObjectMapper();
        this.messageParser = new MessageParserImpl();
    }

    @Override
    public Runnable consume() {
        return () -> {
            while (true) {

                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, byte[]> record : records) {
                        ObjectNode msgIdJson = objectMapper.createObjectNode();
                        msgIdJson.put("topic", record.topic());
                        msgIdJson.put("partition", record.partition());
                        msgIdJson.put("offset", record.offset());
                        msgIdJson.put("key", record.key());

                        String kafkaMsgIdentifier = msgIdJson.toString();

                        if (record.value() != null) {
                            GenericRecord deserializedMessage = AvroUtils.deserialize(record.value());

                            String message = messageParser.parseMessage(kafkaMsgIdentifier, deserializedMessage.toString(), DataModelType.ABSTRACT_MODEL);

                            logger.info("Processed Message: {}", message);

                        } else {

                            logger.warn("{}unable to process record: {}", Constants.LOG_PREFIX, kafkaMsgIdentifier);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
