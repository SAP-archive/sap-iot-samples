# Streaming Access Examples
Sample consumer code in here allows you to deploy a cloud foundry java application to start consuming the streaming data from 
SAP IoT topics (both raw sensor data and processed data with business semantics)

## Prerequisites
1. You should have signed up for Early Adopter Care Program of SAP IoT and requested for Streaming Access Feature by following the process specified [here](https://help.sap.com/viewer/6207c716025a46ac903072ecd8d71053/2102a/en-US).
2. You should have created service instance of type kafka with plan reference using the advertisement id shared by SAP.

## Steps to deploy the application
1. Run ``` mvn clean install ```
2. Login into Cloud Foundry using CLI.  For more information, please refer to the sections [Download and Install the Cloud Foundry Command Line Interface](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/4ef907afb1254e8286882a2bdef0edf4.html?version=Cloud) and [Log On to the Cloud Foundry Environment Using the Cloud Foundry Command Line Interface](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/7a37d66c2e7d401db4980db0cd74aa6b.html?version=Cloud).
3. Run ```cf push ```
