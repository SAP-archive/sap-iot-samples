package com.sap.iot.streamaccess.model;

import java.util.List;

/**
 * models kafka service info.
 */
public class KafkaServiceInfo {

    private final String id;
    private final String username;
    private final String password;
    private final String certUrl;
    private final String tokenUrl;
    private final String serviceCertUrl;

    private final String certFormat;
    private final String clientCert;
    private final String clientKey;
    private final String keyFormat;

    private final List<String> topics;

    private final String brokersAuthSsl;
    private final String brokerClientSsl;

    public KafkaServiceInfo(String id, String username, String password, String certUrl, String tokenUrl, String serviceCertUrl, String certFormat, String clientCert, String clientKey, String keyFormat, String brokersAuthSsl, String brokerClientSsl, List<String> topics) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.certUrl = certUrl;
        this.tokenUrl = tokenUrl;
        this.serviceCertUrl = serviceCertUrl;
        this.certFormat = certFormat;
        this.clientCert = clientCert;
        this.clientKey = clientKey;
        this.keyFormat = keyFormat;
        this.brokersAuthSsl = brokersAuthSsl;
        this.brokerClientSsl = brokerClientSsl;
        this.topics = topics;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public String getBrokersAuthSsl() {
        return brokersAuthSsl;
    }

    public String getBrokerClientSsl() {
        return brokerClientSsl;
    }

    public List<String> getTopics() {
        return topics;
    }
}
