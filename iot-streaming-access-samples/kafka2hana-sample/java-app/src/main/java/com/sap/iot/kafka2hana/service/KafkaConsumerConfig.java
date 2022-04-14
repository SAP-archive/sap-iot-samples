package com.sap.iot.kafka2hana.service;

import com.sap.iot.kafka2hana.model.KafkaServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Autowired
    private CfKafkaInfo cfKafkaInfo;

    public ConsumerFactory<String, String> consumerFactory(String groupId, String clientId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.putAll(getSecurityProperties(cfKafkaInfo.getKafkaServiceInfo()));
        return new DefaultKafkaConsumerFactory<>(props);
    }

    private static Map<? extends String, ?> getSecurityProperties(KafkaServiceInfo kafkaServiceInfo) {
        //add security properties
        Map<String, Object> securityProperties = KafkaSecurityUtils.getSecurityProperties(kafkaServiceInfo);
        securityProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServiceInfo.getBrokerClientSsl());
        return securityProperties;
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(String groupId) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(groupId,"springbootkafkahana"));
        return factory;
    }
}