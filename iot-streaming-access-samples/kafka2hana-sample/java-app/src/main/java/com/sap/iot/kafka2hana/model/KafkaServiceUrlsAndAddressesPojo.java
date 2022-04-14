package com.sap.iot.kafka2hana.model;

public class KafkaServiceUrlsAndAddressesPojo {

    private final String certUrl;
    private final String tokenUrl;
    private final String serviceCertUrl;

    private final String brokersAuthSsl;
    private final String brokerClientSsl;

    public KafkaServiceUrlsAndAddressesPojo(String certUrl, String tokenUrl, String serviceCertUrl, String brokersAuthSsl, String brokerClientSsl) {
        this.certUrl = certUrl;
        this.tokenUrl = tokenUrl;
        this.serviceCertUrl = serviceCertUrl;
        this.brokersAuthSsl = brokersAuthSsl;
        this.brokerClientSsl = brokerClientSsl;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getServiceCertUrl() {
        return serviceCertUrl;
    }

    public String getBrokersAuthSsl() {
        return brokersAuthSsl;
    }

    public String getBrokerClientSsl() {
        return brokerClientSsl;
    }
}
