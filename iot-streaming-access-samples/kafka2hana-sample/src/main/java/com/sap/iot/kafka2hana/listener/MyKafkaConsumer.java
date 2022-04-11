package com.sap.iot.kafka2hana.listener;


import com.sap.iot.kafka2hana.Kafka2hanaApplication;
import com.sap.iot.kafka2hana.util.Constants;
import com.sap.iot.kafka2hana.util.AvroUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MyKafkaConsumer {

    private static int numberOfListeners = 0;


    protected MethodKafkaListenerEndpoint<String, String> createDefaultMethodKafkaListenerEndpoint(String name,
                                                                                                   String group,
                                                                                                   String ... topics) {

        MethodKafkaListenerEndpoint<String, String> kafkaListenerEndpoint = new MethodKafkaListenerEndpoint<>();
        String consumerId = getConsumerId(name);
        log.info("Consumer Id {}", consumerId);
        kafkaListenerEndpoint.setId(consumerId);
        kafkaListenerEndpoint.setGroupId(group);
        kafkaListenerEndpoint.setAutoStartup(true);
        kafkaListenerEndpoint.setTopics(topics);
        kafkaListenerEndpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());
        return kafkaListenerEndpoint;
    }

    private static String getConsumerId(String name) {
        if (isBlank(name)) {
            return MyKafkaConsumer.class.getCanonicalName() + "#" + numberOfListeners++;
        } else {
            return name;
        }
    }

    private static boolean isBlank(String string) {
        return Optional.ofNullable(string)
                .map(String::isBlank)
                .orElse(true);
    }

    @SneakyThrows
    public KafkaListenerEndpoint createKafkaListenerEndpoint(String name, String group, String ... topics) {
        MethodKafkaListenerEndpoint<String, String> kafkaListenerEndpoint =
                createDefaultMethodKafkaListenerEndpoint(name, group, topics);
        kafkaListenerEndpoint.setBean(new MyMessageListener());
        kafkaListenerEndpoint.setMethod(MyMessageListener.class.getMethod("onMessage", ConsumerRecord.class));
        return kafkaListenerEndpoint;
    }

    @Slf4j
    private static class MyMessageListener implements MessageListener<String, byte[]> {


        @Override
        public void onMessage(ConsumerRecord<String, byte[]> rec) {
            if (rec.topic().contains(Constants.PROCESSED_TS_TOPIC)){
            CompletableFuture.runAsync(()->processMessage(rec))
                    .join();
                log.debug("My message listener done processing record: " + rec);
            }
        }

        @SneakyThrows
        private void processMessage(ConsumerRecord<String, byte[]> rec) {
            GenericRecord gRecord = AvroUtils.deserialize(rec.value());
            Kafka2hanaApplication.getAppContext()
                    .getBean(ProcessTimeSeriesStreamingAccess.class).processMessage(gRecord);
        }
    }
}