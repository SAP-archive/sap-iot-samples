package com.sap.iot.kafka2hana.listener;

import com.sap.iot.kafka2hana.service.CfKafkaInfo;
import com.sap.iot.kafka2hana.service.KafkaConsumerConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomKafkaListenerRegistrar implements InitializingBean {


    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaConsumerConfig kafkaConsumerConfig;

    @Autowired
    private CfKafkaInfo cfKafkaInfo;

    @Override
    public void afterPropertiesSet() {
        log.info("Registering consumer for topics {}", cfKafkaInfo.getKafkaServiceInfo().getTopicsAsString());
        this.registerCustomKafkaListener(true, cfKafkaInfo.getKafkaServiceInfo().getTopics().toArray(new String[0]));
    }

    @SneakyThrows
    public void registerCustomKafkaListener(boolean startImmediately, String ... topics) {
        kafkaListenerEndpointRegistry.registerListenerContainer(
                myKafkaConsumer.createKafkaListenerEndpoint(null, "springbootkafkahana", topics ),
                kafkaConsumerConfig.kafkaListenerContainerFactory("springbootkafkahana"), startImmediately);
    }

    @Autowired
    private MyKafkaConsumer myKafkaConsumer;

}