package com.sap.iot.streamaccess.exceptions;

import java.io.Serializable;

public class CustomAvroException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 4596518384682340411L;

    public CustomAvroException(final String s, final Throwable e) {
        super(s, e);
    }
}
