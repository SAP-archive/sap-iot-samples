package com.sap.iot.kafka2hana.exceptions;

public class InvalidTopicException extends RuntimeException{

    private static final long serialVersionUID = 163276072755631230L;

    public InvalidTopicException(String message) {
        super(message);
    }
}
