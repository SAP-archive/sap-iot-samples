package com.sap.iot.kafka2hana;

import com.sap.iot.kafka2hana.listener.ProcessTimeSeriesStreamingAccess;
import com.sap.iot.kafka2hana.service.CfKafkaInfo;
import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfEnv;
import io.pivotal.cfenv.jdbc.CfJdbcEnv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@EntityScan("com.sap.iot.kafka2hana.model")
@ConfigurationPropertiesScan
public class Kafka2hanaApplication {

	private static ApplicationContext appContext;

	public static ApplicationContext getAppContext() {
		return appContext;
	}
	public static void main(String[] args) {
		appContext = SpringApplication.run(Kafka2hanaApplication.class, args);
	}


	@Bean
	public CfEnv cfEnv() {
		return new CfEnv();
	}

	@Bean
	ProcessTimeSeriesStreamingAccess processTimeSeriesStreamingAccess(){
		return new ProcessTimeSeriesStreamingAccess();
	}

	@Bean
	@Primary
	@Profile("cloud")
	public CfKafkaInfo cfKafkaInfo() {
		return new CfKafkaInfo();
	}

	@Bean
	@Primary
	@Profile("cloud")
	public DataSourceProperties dataSourceProperties() {
		CfJdbcEnv cfJdbcEnv = new CfJdbcEnv();
		DataSourceProperties properties = new DataSourceProperties();
		CfCredentials hanaCredentials = cfJdbcEnv.findCredentialsByTag("hana");
		if (hanaCredentials != null) {
			String uri = hanaCredentials.getUri("hana");
			properties.setUrl(uri);
			properties.setUsername(hanaCredentials.getUsername());
			properties.setPassword(hanaCredentials.getPassword());
			properties.setDriverClassName(hanaCredentials.getString("driver"));
		}
		return properties;
	}

}