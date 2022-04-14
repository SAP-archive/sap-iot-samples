package com.sap.iot.kafka2hana.model;

public class KafkaServiceSecurityPojo {


    private final String certFormat;
    private final String clientCert;
    private final String clientKey;
    private final String keyFormat;


    public KafkaServiceSecurityPojo(String certFormat, String clientCert, String clientKey, String keyFormat) {
        this.certFormat = certFormat;
        this.clientCert = clientCert;
        this.clientKey = clientKey;
        this.keyFormat = keyFormat;
    }

    public String getCertFormat() {
        return certFormat;
    }

    public String getClientCert() {
        return clientCert;
    }

    public String getClientKey() {
        return clientKey;
    }

    public String getKeyFormat() {
        return keyFormat;
    }
}
