![](https://img.shields.io/badge/STATUS-NOT%20CURRENTLY%20MAINTAINED-red.svg?longCache=true&style=flat)

# Important Notice
This public repository is read-only and no longer maintained. For the latest sample code repositories, visit the [SAP Samples](https://github.com/SAP-samples) organization.

[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/SAP-Samples/sap-iot-samples)](https://github.com/SAP-samples/sap-iot-samples/pulls?q=is%3Aopen+is%3Apr)
[![Pull Requests](https://img.shields.io/github/issues-pr-closed-raw/SAP-Samples/sap-iot-samples)](https://github.com/SAP-samples/sap-iot-samples/pulls?q=is%3Apr+is%3Aclosed)
[![GitHub tag](https://img.shields.io/github/issues-closed-raw/SAP-Samples/sap-iot-samples)](https://github.com/SAP-Samples/sap-iot-samples/issues?q=is%3Aopen+is%3Apr)
[![GitHub tag](https://img.shields.io/github/issues-raw/SAP-Samples/sap-iot-samples)](https://github.com/SAP-Samples/sap-iot-samples/issues?q=is%3Apr+is%3Aclosed)
[![GitHub release](https://img.shields.io/github/release/SAP-Samples/sap-iot-samples)](https://github.com/SAP-Samples/sap-iot-samples/releases/)
[![GitHub tag](https://img.shields.io/github/tag/SAP-Samples/sap-iot-samples)](https://github.com/SAP-Samples/sap-iot-samples/tags/)

[![SAP IoT](https://img.shields.io/badge/SAP%20Internet%20of%20Things-Samples-blue)](https://github.com/SAP-samples/sap-iot-samples)
[![GitHub contributors](https://img.shields.io/github/contributors-anon/SAP-Samples/sap-iot-samples)](https://github.com/SAP-samples/sap-iot-samples/graphs/contributors)
[![GitHub language count](https://img.shields.io/github/languages/count/SAP-Samples/sap-iot-samples)](https://github.com/SAP-samples/sap-iot-samples)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/sap-iot-samples)](https://api.reuse.software/info/github.com/SAP-samples/sap-iot-samples)

# Overview 

With **[SAP Internet of Things](https://www.sap.com/products/iot-data-services.html)** you can receive measurement data from any device, match it to a business process context in SAP IoT and trigger and automate business processes in SAP ERP with **Smart Sensing**, rules and actions.

Integrate and extend the computing capabilities with the **IoT Edge**, the Intelligent Edge Computing software that provides "microservices at the edge" for Internet of Things (IoT).

Use this sample collection to learn and speed up the use of the different SAP IoT APIs and services.

The context of most of the included examples is to build a solution, that allows deliveries coming from the sales order processing in ERP to be tracked with sensors. This allows to find out, if a product that should have been cooled (or not exposed to too much light or any other condition), was actually cooled all the way from shipment to receiving. This is called in the IoT-World a "cold chain tracking" scenario.

# Prerequisites

1. An **SAP Business Technology Platform** global account with entitlements for **SAP IoT**, see the [Discovery Center](https://discovery-center.cloud.sap/#/serviceCatalog/sap-iot?tab=service_plan&region=all&service_plan=one-product) for different pricing solutions.

2. Other prerequisites are listed in each sample.

# Download, Installation, Configuration, and Use

Clone or download the repo to your computer and then follow more detailed installation instructions below per folder in the repo:

| Feature      | Description      | Link          |
| :------------- | :------------- | :-------------: |
| SAP IoT APIs | These scripts allow you to create device and thing/application model based on a definition language for digital twins in json files. You can use these scripts for a first prototype where you want to have the device model and the thing/application model mostly in sync and where you have only one set of measurements and one set of master data. To use these scripts please refer to the tutorials at https://developers.sap.com/tutorials/iot-model-create.html and https://developers.sap.com/tutorials/iot-onboard-device.html to learn how to apply them to your tenant/system. | [SAP IoT APIs](iot-bootstrap-scripts)  |
| SAP IoT Edge | Extension Service samples for the IoT Edge. | [IoT Edge Extensions](iot-edge-samples)  |
| Smart Sensing | This folder contains Sample codes and utilities for the SAP Internet of Things Smart Sensing. |  [Smart Sensing Folder](iot-smart-sensing-samples)  |
| Location Services | The postman collection and environment in here allow you to make calls to the location services APIs of SAP IoT. Instructions for how to use them are provided in the tutorials at http://developers.sap.com/tutorials/iot-locations-geofence.html. |  [Location Services Samples](iot-location-services-samples)  |
| Analytics | The postman collection and environment in here allow you to create a small sample application model from scratch, put some measurement data into it. Then it provides the apis in SAP IoT that allow you to hook up to and share the aggregated data with SAP Analytics Cloud. Instructions how to use the collection are provided in the tutorial at http://developers.sap.com/tutorials/iot-sac-integration-ext.html. |  [SAP Analytics Cloud Integration](iot-sac-integration-samples) |
| Streaming Access |  Samples to deploy a Cloud Foundry java application to start consuming the streaming data from SAP IoT topics (both raw sensor data and processed data with business semantics). Please note that the feature can only be used with Early Adopter Access. The provided samples implement a simple pure Java consumer and a Spring Boot application to consume from the streaming access Kafka and forward the data to a specific _hdi-container_ of your **SAP HANA Cloud** instance.  |    [Streaming Access Folder](iot-streaming-access-samples) |
| Various and tools | A folder collecting various tools. | [Tools](tools)  |


# References

To find all developer oriented material please refer to [Internet of Things community topic](https://community.sap.com/topics/internet-of-things).

# Product documentation

For any further information please refer to the product documentation of [SAP IoT](https://help.sap.com/viewer/p/SAP_IoT)

# Limitations and Known Issues

The included samples do not cover all SAP IoT APIs and functionalities.

# Support and Contributing

These samples are provided "as-is" basis with detailed documentation on how to use them.

Please use our [community](https://answers.sap.com/tags/73554900100800002247) for questions and answers on the sample code.

If you have found a material issue with the example please create an [issue](https://github.com/SAP-samples/sap-iot-samples/issues) in this GitHub repo.

Please feel free to put in a pull request and give us some time to review and respond.

# License

Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](LICENSES/Apache-2.0.txt) in this repository.
