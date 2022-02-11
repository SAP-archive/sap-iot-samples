package com.sap.iot.streamaccess.service;

import com.sap.iot.streamaccess.constants.DataModelType;


public interface MessageParser {
    String parseMessage(String kafkaMsgIdentifier, String avroDeserializedMessage, DataModelType dataModelType);
}
