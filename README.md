[![GitHub language count](https://img.shields.io/github/languages/count/SAP-Samples/iot-edge-samples)](https://github.com/SAP-samples/sap-iot-samples)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP-samples/sap-iot-samples)](https://api.reuse.software/info/github.com/SAP-samples/sap-iot-samples)

# Overview

With SAP IoT you can receive measurement data from any device, match it to a business process context in SAP IoT and trigger business processes in SAP ERP with rules.

Use this sample collection to learn and speed up the use of the different SAP IoT APIs.

The context of most of the included examples is to build a solution, that allows deliveries coming from the sales order processing in ERP to be tracked with sensors. This allows to find out, if a product that should have been cooled (or not exposed to too much light or any other condition), was actually cooled all the way from shipment to receiving. This is called in the IoT-World a "cold chain tracking" scenario.

# Prerequisites

1. An SAP Business Technology Platform global account with entitlements for SAP IoT or a Cloud Platform Enterprise Agreement in place at your company
2. Other prerequisites are listed with each sample under "Download, Installation, Configuration and Use".

# Download, Installation, Configuration, and Use

Clone or download the repo to your computer and then follow more detailed installation instructions below per folder in the repo:

## [iot-autoid-services-samples](iot-autoid-services-samples)
Sample codes and utilities for the SAP Internet of Things Smart Sensing.

## [iot-bootstrap-scripts](iot-bootstrap-scripts)
These scripts allow you to create device and thing/application model based on a definition language for digital twins in json files. You can use these scripts for a first prototype where you want to have the device model and the thing/application model mostly in sync and where you have only one set of measurements and one set of master data. To use these scripts please refer to the tutorials at https://developers.sap.com/tutorials/iot-model-create.html and https://developers.sap.com/tutorials/iot-onboard-device.html to learn how to apply them to your tenant/system.

## [iot-location-services-samples](iot-location-services-samples)
The postman collection and environment in here allow you to make calls to the location services APIs of SAP IoT. Instructions for how to use them are provided in the tutorials at http://developers.sap.com/tutorials/iot-locations-geofence and http://developers.sap.com/tutorials/iot-locations-geomatch.

## [iot-sac-integration-samples](iot-sac-integration-samples)
The postman collection and environment in here allow you to create a small sample application model from scratch, put some measurement data into it. Then it provides the apis in SAP IoT that allow you to hook up to and share the aggregated data with SAP Analytics Cloud. Instructions how to use the collection are provided in the tutorial at http://developers.sap.com/tutorials/iot-sac-integration-ext.

## [iot-streaming-access-samples](iot-streaming-access-samples)
Sample consumer code in here allows you to deploy a cloud foundry java application to start consuming the streaming data from 
SAP IoT topics (both raw sensor data and processed data with business semantics). Please note that the feature can only be used with Early Adopter Access.

# References
To find all developer oriented material please refer to https://community.sap.com/topics/internet-of-things.

# Limitations and Known Issues
The included samples do not cover all SAP IoT APIs and functionalities.

# Support and Contributing
These samples are provided "as-is" basis with detailed documentation on how to use them.

Please use our [community](https://answers.sap.com/tags/73554900100800002247) for questions and answers on the sample code.

If you have found a material issue with the example please create an [issue](https://github.com/SAP-samples/sap-iot-samples/issues) in this GitHub repo.

Please feel free to put in a pull request and give us some time to review and respond.

# License
Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved. This project is licensed under the Apache Software License, version 2.0 except as noted otherwise in the [LICENSE](https://github.com/SAP-samples/sap-iot-samples/LICENSES/Apache-2.0.txt) in this repository.
