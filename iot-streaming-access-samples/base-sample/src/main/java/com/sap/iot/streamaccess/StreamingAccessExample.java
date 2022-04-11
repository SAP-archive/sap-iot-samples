package com.sap.iot.streamaccess;

import com.sap.iot.streamaccess.constants.Constants;
import com.sap.iot.streamaccess.kafka.consumer.StreamingAccessFactory;
import com.sap.iot.streamaccess.model.KafkaServiceInfo;
import com.sap.iot.streamaccess.util.CloudUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class StreamingAccessExample {

    private static final Logger logger = LoggerFactory.getLogger(StreamingAccessExample.class);


    public static void main(String[] args) {

        String consumerGroupId = Optional.ofNullable(System.getenv(Constants.CONSUMER_GROUP_ID)).orElse("streaming-access-example");

        //get the KafkaServiceInfo from kafka binding. Note: Only one kafka service should be bound to the application
        KafkaServiceInfo kafkaServiceInfo = CloudUtils.getKafkaServiceInfo();

        //get all permitted topics for consumption
        List<String> topics = kafkaServiceInfo.getTopics();

        logger.debug("Topics: {}", topics);

        //for each 'topic' start the appropriate implementation
        topics.forEach(topic -> {
            logger.info("Start streaming access for topic: {}", topic);
            try {
                StreamingAccessFactory
                        .getStreamingAccessInstance(topic, consumerGroupId, kafkaServiceInfo)
                        .start();
            } catch (Exception e) {
                logger.info(e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
