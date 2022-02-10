package com.sap.iot.streamaccess.kafka.consumer;

import com.sap.iot.streamaccess.constants.TopicPattern;
import com.sap.iot.streamaccess.exceptions.InvalidTopicException;
import com.sap.iot.streamaccess.model.KafkaServiceInfo;
import com.sap.iot.streamaccess.util.CloudUtils;
import com.sap.iot.streamaccess.util.KafkaConsumerUtils;

import java.util.Collections;
import java.util.Optional;

/**
 * Factory Pattern
 */
public class StreamingAccessFactory {

    private StreamingAccessFactory() {
    }

    /**
     * @param topic            name of the topic
     * @param consumerGroupId  consumer group id
     * @param kafkaServiceInfo contains kafka binding info
     * @return returns the appropriate Streaming Access implementation based on the topic
     * @throws Exception
     */
    public static AbstractStreamingAccess getStreamingAccessInstance(String topic, String consumerGroupId, KafkaServiceInfo kafkaServiceInfo) throws InvalidTopicException {

        String cfInstanceIndex = Optional.ofNullable(CloudUtils.getCFInstanceIndex()).orElse("0");
        String cfOrgName = Optional.ofNullable(CloudUtils.getCFOrgName()).orElse("unknown_org_name");
        if (topic.endsWith(TopicPattern.RAW_TS_TOPIC)) {
            String clientId = "streaming_access_raw_consumer-" + cfOrgName +"-" + cfInstanceIndex;
            return new RawTimeSeriesStreamingAccess(
                    KafkaConsumerUtils.createConsumer(kafkaServiceInfo, Collections.singletonList(topic), consumerGroupId,clientId)
            );
        } else if (topic.endsWith(TopicPattern.PROCESSED_TS_TOPIC)) {
            String clientId = "streaming_access_processed_consumer-" + cfOrgName +"-" + cfInstanceIndex;
            return new ProcessTimeSeriesStreamingAccess(
                    KafkaConsumerUtils.createConsumer(kafkaServiceInfo, Collections.singletonList(topic), consumerGroupId,clientId)
            );
        } else if (topic.endsWith(TopicPattern.ABSTRACT_TS_TOPIC)) {
            String clientId = "streaming_access_abstract_processed_consumer-" + cfOrgName +"-" + cfInstanceIndex;
            return new AbstractModelTimeSeriesStreamingAccess(
                    KafkaConsumerUtils.createConsumer(kafkaServiceInfo, Collections.singletonList(topic), consumerGroupId,clientId)
            );
        } else {
            throw new InvalidTopicException("Invalid Topic. No Sample consumer implementation found for topic: " + topic);
        }
    }
}
