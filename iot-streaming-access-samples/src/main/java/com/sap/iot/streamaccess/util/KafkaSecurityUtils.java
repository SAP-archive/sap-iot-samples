package com.sap.iot.streamaccess.util;

import com.sap.iot.streamaccess.constants.Constants;
import com.sap.iot.streamaccess.model.KafkaServiceInfo;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaSecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSecurityUtils.class);

    private KafkaSecurityUtils() {

        throw new IllegalStateException("Utility class");

    }

    /**
     * returns X509 cert based security configs for the kafka consumer/producer
     */
    public static Map<String, Object> getSecurityProperties(KafkaServiceInfo kafkaServiceInfo) {

        Map<String, Object> sslProps = new ConcurrentHashMap<>();

        if (kafkaServiceInfo.getClientCert() == null || kafkaServiceInfo.getClientKey() == null || kafkaServiceInfo.getCertUrl() == null || kafkaServiceInfo.getTokenUrl() == null) {
            logger.info("{}skipping sasl - no sasl information provided", Constants.LOG_PREFIX);
            return sslProps;
        }

        try {

            String trustStorePass = kafkaServiceInfo.getPassword(); //re-use as truststore & keystore pass

            sslProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            sslProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, createTruststoreWithRootCert(trustStorePass, kafkaServiceInfo.getCertUrl()));
            sslProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, trustStorePass);
            sslProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, getKeyStoreLocation(kafkaServiceInfo, trustStorePass));
            sslProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, trustStorePass);
            sslProps.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");


        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | InvalidNameException e) {
            logger.error("{}error reading SSL properties - {0}", Constants.LOG_PREFIX, e);

        }

        return sslProps;
    }

    private static String getKeyStoreLocation(KafkaServiceInfo kafkaService, String password) throws IOException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, InvalidNameException {

        KeyStore keyStore = createKeyStore(kafkaService.getClientCert(), kafkaService.getClientKey(), kafkaService.getPassword());

        if (keyStore == null || password == null) {
            return null;
        }

        File keystoreFile = File.createTempFile("client.kafkaKeyStore.jks", null);

        try (FileOutputStream fileOutputStream = new FileOutputStream(keystoreFile)) {
            keyStore.store(fileOutputStream, password.toCharArray());
        }

        return keystoreFile.getAbsolutePath();

    }

    public static KeyStore createKeyStore(String clientCert, String clientKey, String password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException, InvalidNameException {

        if (clientCert == null || clientKey == null || password == null) {
            return null;
        }

        PrivateKey privateKey = readPrivateKey(clientKey);
        List<X509Certificate> certificates = readCertificateChain(clientCert);

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setKeyEntry(getCN(certificates.get(0)), privateKey, toCharArray(password), certificates.toArray(new Certificate[0]));

        return keyStore;
    }

    private static PrivateKey readPrivateKey(String clientKey) throws IOException {

        try (StringReader reader = new StringReader(clientKey)) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PEMParser pemParser = new PEMParser(reader);
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());

            return converter.getPrivateKey(privateKeyInfo);
        }

    }

    private static List<X509Certificate> readCertificateChain(String clientCert) throws CertificateException, IOException {

        List<X509Certificate> certificates = new ArrayList<>();

        try (StringReader reader = new StringReader(clientCert)) {
            PEMParser pemParser = new PEMParser(reader);
            Object object;

            while ((object = pemParser.readObject()) != null) {

                certificates.add(new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) object));
            }
        }

        return certificates;
    }

    private static String getCN(X509Certificate x509Certificate) throws InvalidNameException {

        return LdapUtils.getStringValue(new LdapName(x509Certificate.getSubjectX500Principal().getName()), "cn");
    }

    private static char[] toCharArray(String s) {

        return s != null ? s.toCharArray() : "".toCharArray();
    }


    private static String createTruststoreWithRootCert(String password, String certUrl) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        File certFile = File.createTempFile(Constants.KAFKA_ROOT_CA_CERTS, null);
        downloadFile(certUrl, certFile);
        return createTruststoreWithRootCerts(password, certFile.getAbsolutePath());
    }

    private static void downloadFile(String url, File output) throws IOException {

        try (ReadableByteChannel in = Channels.newChannel(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(output);
             FileChannel out = fileOutputStream.getChannel()) {
            out.transferFrom(in, 0, Long.MAX_VALUE);

        }
    }

    /**
     * Loads the current and if available also the next Kafka CA Root certificate from
     * the provided file.
     *
     * @param certFile The path of a file containing certificates in PEM format.
     * @return A map with a "current" and if available also a "next" {@link java.security.cert.Certificate} entry. For a
     * seamless certificate rotation your Kafka client needs both, so both certificates should be imported into
     * the client's truststore (see {@link #createTruststoreWithRootCerts(String, String)}).
     * @throws CertificateException if the provided certificates could not be parsed.
     * @throws IOException          if an i/o error occurred.
     */
    public static Map<String, Certificate> loadCerts(String certFile) throws CertificateException, IOException {

        Map<String, Certificate> certs = new HashMap<>();
        CertificateFactory cf = CertificateFactory.getInstance(Constants.CERT_FACTORY_INSTANCE);
        try (InputStream is = new BufferedInputStream(new FileInputStream(certFile))) {
            certs.put(Constants.CERT_CURRENT, cf.generateCertificate(is));

            if (is.available() > 0) {
                certs.put(Constants.CERT_NEXT, cf.generateCertificate(is));
            }
        }

        return certs;
    }

    /*
     * The response of the /certs/rootCA endpoint must be saved to a file and can directly be fed into
     * this method to create a truststore in JKS format for use with a Kafka Java client.
     *
     * @param trustStorePass The password for the truststore to create.
     *
     * @param certFile The path of a file containing certificates in PEM format.
     *
     * @return The path of the created truststore file. This truststore contains an entry with alias
     * "KafkaRootCA" containing the currently active Kafka CA Root certificate, and if available also
     * an entry with alias "KafkaNextRootCA" containing the Kafka CA Root certificate to be activated
     * in the future.
     *
     * @throws KeyStoreException if the certificates could not be stored properly.
     *
     * @throws NoSuchAlgorithmException if a cryptographic algorithm is not available.
     *
     * @throws CertificateException if the provided certificates could not be parsed.
     *
     * @throws IOException if an i/o error occurred.
     */
    public static String createTruststoreWithRootCerts(String trustStorePass, String certFile) throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException {

        // Create the truststore
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(null, trustStorePass.toCharArray());

        // Add certificates
        Map<String, Certificate> certs = loadCerts(certFile);
        keystore.setCertificateEntry(Constants.KAFKA_ROOT_CA, certs.get(Constants.CERT_CURRENT));

        if (certs.containsKey(Constants.CERT_NEXT)) {
            keystore.setCertificateEntry(Constants.KAFKA_NEXT_ROOT_CA, certs.get(Constants.CERT_NEXT));
        }

        // Save the new truststore
        File keystoreFile = File.createTempFile(Constants.KAFKA_TRUST_STORE, null);

        try (FileOutputStream os = new FileOutputStream(keystoreFile)) {
            keystore.store(os, trustStorePass.toCharArray());

        }

        return keystoreFile.getAbsolutePath();
    }
}
