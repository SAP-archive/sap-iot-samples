package com.sap.iot.streamaccess.util;

import com.sap.iot.streamaccess.constants.Constants;
import com.sap.iot.streamaccess.model.KafkaServiceInfo;
import io.pivotal.cfenv.core.CfEnv;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * utility to fetch service bindings
 */
public class CloudUtils {
    CloudUtils() {
        throw new IllegalArgumentException("Utility class");
    }

    private static CfEnv cfEnv = new CfEnv();

    /**
     * @return returns kafka service binding
     */
    public static KafkaServiceInfo getKafkaServiceInfo() {

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
        List<String> kafkaTopics = null;

        if (null != resources && !resources.isEmpty()) {
            kafkaTopics =
                    resources.stream()
                            .filter(resource -> resource.get(Constants.RESOURCE_TYPE).equals("topic"))
                            .map(resource -> (String) resource.get(Constants.RESOURCE_NAME))
                            .collect(Collectors.toList());
        }

        return new KafkaServiceInfo(id, username, password, certUrl, tokenUrl, serviceCertUrl, certFormat, clientCert, clientKey, keyFormat, brokersAuthSsl, brokersClientSsl, kafkaTopics);
    }

    /**
     *
     * @return CF Instance Index for the current CF Instance
     */
    public static String getCFInstanceIndex(){
        return cfEnv.getApp().getInstanceId();
    }

    public static String getCFOrgName(){
        Map<String,Object> cfAppParameters = cfEnv.getApp().getMap();
        if(cfAppParameters.containsKey("organization_name")){
            return cfAppParameters.get("organization_name").toString();
        }
        return null;
    }
}
