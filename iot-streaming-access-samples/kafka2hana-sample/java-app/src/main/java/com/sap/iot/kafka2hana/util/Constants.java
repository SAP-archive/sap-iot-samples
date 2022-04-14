package com.sap.iot.kafka2hana.util;

public class Constants {

    private Constants(){}

    public static final String KAFKA_ROOT_CA = "KafkaRootCA";

    public static final String KAFKA_NEXT_ROOT_CA = "KafkaNextRootCA";

    public static final String KAFKA_ROOT_CA_CERTS = "kafkaRootCACerts";

    public static final String KAFKA_TRUST_STORE = "kafkaTrustStore";

    public static final String CERT_CURRENT = "current";

    public static final String CERT_NEXT = "next";

    public static final String CERT_FACTORY_INSTANCE = "X.509";

    public static final String NAME = "name";

    public static final String CREDENTIALS = "credentials";

    public static final String RESOURCES = "resources";

    public static final String RESOURCE_TYPE = "type";

    public static final String RESOURCE_NAME = "name";

    public static final String CLUSTER = "cluster";

    public static final String URLS = "urls";


    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String CA_CERT = "ca_cert";

    public static final String CERTS = "certs";

    public static final String TOKEN = "token";

    public static final String CERT_FORMAT = "certformat";

    public static final String CLIENT_CERT = "clientcert";

    public static final String CLIENT_KEY = "clientkey";

    public static final String KEY_FORMAT = "keyformat";

    public static final String SERVICE_CLIENT_CERT_URL = "service_client_cert";

    public static final String BROKER_CLIENT = "brokers.client_ssl";

    public static final String BROKER_AUTH = "brokers.auth_ssl";

    public static final String PROCESSED_TS_TOPIC = ".sap.v1.iot.processed_timeseries";
    public static final String RAW_TS_TOPIC = ".sap.v1.iot.raw_timeseries";
    public static final String ABSTRACT_TS_TOPIC = ".sap.iot.abstract.processed_timeseries.v1";

    public static final String MESSAGE_PROPERTY_TIME = "_time";
    public static final String MESSAGE_PROPERTY_THINGID = "thingId";
    public static final String MESSAGE_PROPERTY_PST = "propertySetType";
    public static final String MESSAGE_PROPERTY_MEASUREMENTS = "measurements";

}
