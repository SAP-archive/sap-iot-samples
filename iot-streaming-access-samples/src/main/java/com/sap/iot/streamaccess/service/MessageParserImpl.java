package com.sap.iot.streamaccess.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sap.iot.streamaccess.constants.DataModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * parses avro deserialized message
 * custom parsing can be applied based on the data model of the message.
 */
public class MessageParserImpl implements MessageParser {

    private static final Logger logger = LoggerFactory.getLogger(MessageParserImpl.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String parseMessage(String kafkaMsgIdentifier, String avroDeserializedMessage, DataModelType dataModelType) {

        //....process message based on data model type here....
        switch (dataModelType) {
            case THING_MODEL:
                logger.info("Parsing Thing Model Time-series Message");
                break;
            case ABSTRACT_MODEL:
                logger.info("Parsing Abstract Model Time-series Message");
                break;
            default:
                logger.error("invalid model type");
                return null;
        }

        SimpleModule simpleModule = new SimpleModule();

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        objectMapper.registerModule(simpleModule);

        try {

            String jsonMessage = objectMapper.writeValueAsString(objectMapper.readTree(avroDeserializedMessage));

            return jsonMessage;

        } catch (JsonProcessingException e) {
            logger.error("error processing message of type: {}; {} - {}", e, kafkaMsgIdentifier, avroDeserializedMessage);
        }
        return null;
    }

}
