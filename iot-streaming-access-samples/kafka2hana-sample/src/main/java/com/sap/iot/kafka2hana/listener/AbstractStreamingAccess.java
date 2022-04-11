package com.sap.iot.kafka2hana.listener;

import org.apache.avro.generic.GenericRecord;

public interface AbstractStreamingAccess {

     void processMessage(GenericRecord deserializedMessage);

}
