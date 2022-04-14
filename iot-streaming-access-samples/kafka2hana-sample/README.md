# Streaming Access forward data to SAP HANA cloud
This sample consumer allows you to deploy a cloud foundry java application to start consuming the streaming data from
SAP IoT topics and forward it to SAP HANA Cloud (in this sample only data from timeseries-processed data with business semantics topics is implemented).

## Prerequisites
1. You should have signed up for Early Adopter Care Program of SAP IoT and requested for Streaming Access Feature by following the process specified [here](https://help.sap.com/viewer/6207c716025a46ac903072ecd8d71053/2102a/en-US).
2. You should have created service instance of type kafka with plan reference using the advertisement id shared by SAP.
3. You are licensed to SAP HANA Cloud, and created a DB instance in the same Cloud Foundry subaccount and space

## Steps to deploy the application
1. Create your ``` kafka2hana ``` hdi-container and define the table ``` MEASURES ``` and the Calculation View (required if you would like to consume data from SAP Analytics Cloud).

   A prebuilt HANA project is available in the folder [```./hdi```](hdi). Issue ```cf push``` to deploy it.


   **HANA table definition:**

   ```
   COLUMN TABLE "MEASURES" (
    "id" BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) NOT NULL,
    "thingId" NVARCHAR(32),
    "pst" NVARCHAR(125),
    "measurement" NVARCHAR(125),
    "value" FLOAT,
    "time" TIMESTAMP,
    PRIMARY KEY ("id")
   )
   ```

  **Note:** If you are changing the columns of the table, it will impact in the *Entity* definition in the path [```./src/main/java/com/sap/iot/kafka2hana/model/Measures.java```](src/main/java/com/sap/iot/kafka2hana/model/Measures.java)

2. Go to the home folder and run ``` mvn clean install ```
3. Login into Cloud Foundry using CLI.  For more information, please refer to the sections [Download and Install the Cloud Foundry Command Line Interface](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/4ef907afb1254e8286882a2bdef0edf4.html?version=Cloud) and [Log On to the Cloud Foundry Environment Using the Cloud Foundry Command Line Interface](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/7a37d66c2e7d401db4980db0cd74aa6b.html?version=Cloud).
4. Run ```cf push ```


### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.6.4/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.6.4/maven-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.6.4/reference/htmlsingle/#boot-features-developing-web-applications)
* [Rest Repositories](https://docs.spring.io/spring-boot/docs/2.6.4/reference/htmlsingle/#howto-use-exposing-spring-data-repositories-rest-endpoint)
* [Spring LDAP](https://docs.spring.io/spring-boot/docs/2.6.4/reference/htmlsingle/#boot-features-ldap)
* [JDBC API](https://docs.spring.io/spring-boot/docs/2.6.4/reference/htmlsingle/#boot-features-sql)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.6.4/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/2.6.4/reference/htmlsingle/#boot-features-kafka)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)
* [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)
* [Managing Transactions](https://spring.io/guides/gs/managing-transactions/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
