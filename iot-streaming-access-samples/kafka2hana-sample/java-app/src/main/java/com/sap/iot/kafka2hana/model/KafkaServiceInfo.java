package com.sap.iot.kafka2hana.model;

import java.util.List;
import java.util.stream.Collectors;

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

    private final String brokersAuthSsl;
    private final String brokerClientSsl;

    private final String certFormat;
    private final String clientCert;
    private final String clientKey;
    private final String keyFormat;

    private final List<String> topics;

    public KafkaServiceInfo(String id, String username, String password, KafkaServiceUrlsAndAddressesPojo
            kafkaServiceUrlsAndAddressesPojo, KafkaServiceSecurityPojo kafkaServiceSecurityPojo, List<String> topics) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.certUrl = kafkaServiceUrlsAndAddressesPojo.getCertUrl();
        this.tokenUrl = kafkaServiceUrlsAndAddressesPojo.getTokenUrl();
        this.serviceCertUrl = kafkaServiceUrlsAndAddressesPojo.getServiceCertUrl();
        this.certFormat = kafkaServiceSecurityPojo.getCertFormat();
        this.clientCert = kafkaServiceSecurityPojo.getClientCert();
        this.clientKey = kafkaServiceSecurityPojo.getClientKey();
        this.keyFormat = kafkaServiceSecurityPojo.getKeyFormat();
        this.brokersAuthSsl = kafkaServiceUrlsAndAddressesPojo.getBrokersAuthSsl();
        this.brokerClientSsl = kafkaServiceUrlsAndAddressesPojo.getBrokerClientSsl();
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

    public String getTopicsAsString(){
        if (topics == null)
            return "";
        return String.join(",", topics);
    }
}
