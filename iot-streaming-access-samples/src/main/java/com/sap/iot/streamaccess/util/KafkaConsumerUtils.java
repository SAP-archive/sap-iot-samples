package com.sap.iot.streamaccess.util;

import com.sap.iot.streamaccess.model.KafkaServiceInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * utility to create Kafka Consumers
 */
public class KafkaConsumerUtils {


    public static KafkaConsumer<String, byte[]> createConsumer(KafkaServiceInfo kafkaServiceInfo, List<String> topics, String consumerGroupId,String clientId) {
        //create consumer properties
        Properties properties = getConsumerProperties(kafkaServiceInfo, consumerGroupId,clientId);
        //create consumer
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(properties);
        //subscribe to topics
        kafkaConsumer.subscribe(topics);

        return kafkaConsumer;
    }

    private static Properties getConsumerProperties(KafkaServiceInfo kafkaServiceInfo, String groupId,String clientId) {
        Properties kafkaConsumerProps = new Properties();

        kafkaConsumerProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        kafkaConsumerProps.setProperty(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        kafkaConsumerProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumerProps.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        kafkaConsumerProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaConsumerProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        kafkaConsumerProps.putAll(getSecurityProperties(kafkaServiceInfo));

        return kafkaConsumerProps;
    }

    private static Properties getSecurityProperties(KafkaServiceInfo kafkaServiceInfo) {
        //add security properties
        Map<String, Object> securityProperties = KafkaSecurityUtils.getSecurityProperties(kafkaServiceInfo);
        securityProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServiceInfo.getBrokerClientSsl());

        Properties kafkaConsumerProps = new Properties();
        kafkaConsumerProps.putAll(securityProperties);
        return kafkaConsumerProps;

    }
}
