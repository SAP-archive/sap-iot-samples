package com.sap.iot.kafka2hana.service;

import com.sap.iot.kafka2hana.model.KafkaServiceInfo;
import com.sap.iot.kafka2hana.model.KafkaServiceSecurityPojo;
import com.sap.iot.kafka2hana.model.KafkaServiceUrlsAndAddressesPojo;
import com.sap.iot.kafka2hana.util.Constants;
import io.pivotal.cfenv.core.CfEnv;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CfKafkaInfo {

    @Autowired
    private CfEnv cfEnv;

    public String getCFInstanceIndex(){
        return cfEnv.getApp().getInstanceId();
    }

    public String getCFOrgName(){
        Map<String,Object> cfAppParameters = cfEnv.getApp().getMap();
        if(cfAppParameters.containsKey("organization_name")){
            return cfAppParameters.get("organization_name").toString();
        }
        return null;
    }

    @Bean
    @SuppressWarnings("unchecked")
    private KafkaServiceInfo kafkaServiceInfo() {

        log.info("Invoking cf apis to get kafkaServiceInfo");
        Map<String, Object> serviceData = cfEnv.findServiceByTag("kafka").getMap();

        String id = (String) serviceData.get(Constants.NAME);

        Map<String, Object> credentials = (Map<String, Object>) serviceData.get(Constants.CREDENTIALS);
        Map<String, Object> cluster = (Map<String, Object>) credentials.get(Constants.CLUSTER);
        Map<String, Object> urls = (Map<String, Object>) credentials.get(Constants.URLS);
        List<Map<String, Object>> resources = (List<Map<String, Object>>) credentials.get(Constants.RESOURCES);

        String username = credentials.get(Constants.USERNAME).toString();
        String password = credentials.get(Constants.PASSWORD).toString();
        String certFormat = credentials.get(Constants.CERT_FORMAT).toString();
        String clientCert = credentials.get(Constants.CLIENT_CERT).toString();
        String keyFormat = credentials.get(Constants.KEY_FORMAT).toString();
        String clientKey = credentials.get(Constants.CLIENT_KEY).toString();

        String certUrl = null;

        if (urls.get(Constants.CERTS) != null) {
            certUrl = urls.get(Constants.CERTS).toString();
        } else if (urls.get(Constants.CA_CERT) != null) {
            certUrl = urls.get(Constants.CA_CERT).toString();
        }

        String serviceCertUrl = urls.get(Constants.SERVICE_CLIENT_CERT_URL).toString();
        String tokenUrl = urls.get(Constants.TOKEN).toString();
        String brokersAuthSsl = cluster.get(Constants.BROKER_AUTH).toString();
        String brokersClientSsl = cluster.get(Constants.BROKER_CLIENT).toString();

        //fetch topics
        List<String> kafkaTopics = new ArrayList<>();

        if (null != resources && !resources.isEmpty()) {
            kafkaTopics =
                    resources.stream()
                            .filter(resource -> resource.get(Constants.RESOURCE_TYPE).equals("topic"))
                            .map(resource -> (String) resource.get(Constants.RESOURCE_NAME))
                            .collect(Collectors.toList());
        }

        return new KafkaServiceInfo(id, username, password,
                new KafkaServiceUrlsAndAddressesPojo(certUrl, tokenUrl, serviceCertUrl, brokersAuthSsl, brokersClientSsl),
                new KafkaServiceSecurityPojo(certFormat, clientCert, clientKey, keyFormat), kafkaTopics);
    }

    public KafkaServiceInfo getKafkaServiceInfo(){
        return kafkaServiceInfo();
    }
}
