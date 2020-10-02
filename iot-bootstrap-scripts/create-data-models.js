/**
 * @fileoverview Create a sample data model in SAP IoT.
 *
 * The code is written in ECMAScript 8, which is supported by recent versions of Node.js.
 * Asynchronous functions (co-routines) are used intensively.
 *
 * @author Mikhail Bessonov <mikhail.bessonov@sap.com>, Marcus Behrens <marcus.behrens@sap.com>
 * @license
 * SAP Sample Code License Agreement, see the LICENSE file
 */

// Imports

// Native Node.js modules
const fs = require('fs');
const path = require('path');

// SAP IoT SDK
const SAPIoT = require('sap-leonardo-iot-sdk');

// Read the configuration file and the model file.
const model = readConfigFile('model.json');

// the model.js file needs to contain the model description.
// only one thing type per file is supported.
// the capabilities/property sets/packages and thing types listed should not exist yet.
// only the float data type is supported for measures.
// only the string data type is supported for data.
// only one master data and one measurement property set type is supported

main()
    .catch((err) => {
        console.error(err);
        process.exit(1);
    });

async function main() {
    // setup basic apis, tenants and package

    const client = new SAPIoT();
    const tenantInfo = await client.request({ // this is the cloud foundry tenant concept tenant
        url: `${client.navigator.businessPartner()}/Tenants`
    });

    const packageName = `${tenantInfo.value[0].package}.` + model.package;


    // collect all properties into payloads to be used later for both device and thing model creation
    let capabilityPayload = [];
    let measuresetPayload = [];
    let propertyMappings = [];
    for(let property of model.capability_data_sets[0].properties) {
        capabilityPayload.push({
          name:property.id,
          dataType:'float'
        });
        measuresetPayload.push({
          Name:property.id,
          Type:'NumericFlexible'
        });
        propertyMappings.push( {
          capabilityPropertyId: property.id,
          npstPropertyId: property.id
        });
    }

    let propertySetTypePayloadTimeSeriesData = {
        Name: `${packageName}:` + model.capability_data_sets[0].id,
        DataCategory: 'TimeSeriesData',
        Properties: measuresetPayload
    };

    let datasetPayload = [];
    for(let property of model.application_data_sets[0].properties) {
        datasetPayload.push({
          Name:property.id,
          Type:'String'
        });
    }
    let propertySetTypePayloadMasterData = {
            Name: `${packageName}:` + model.application_data_sets[0].id,
            DataCategory: 'MasterData',
            Properties: datasetPayload
    };


    // trigger device model generation first as the ids generated are required to complete the mapping deviceAssignmentPayload
    const capabilityName = model.package + ':' + model.capability_data_sets[0].id;
    const curl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/capabilities';
    const capability = await client.request({url:curl, method:'POST', headers:{}, body:{
        alternateId: capabilityName,
        properties: capabilityPayload,
        name: capabilityName
    }});
    //console.log(capability);
    console.log(`capability created: ${capability.id}`);
    const capabilityId = capability.id;

    const surl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/sensorTypes';
    const typeName = model.package + ':' + model.types[0].id;
    const sensorType = await client.request({url:surl, method:'POST', headers:{}, body:{
        name: typeName,
        capabilities: [{
            id: capabilityId,
            type: 'measure'
        }]
    }});
    //console.log(sensorType);
    console.log(`sensorType created: ${sensorType.id}`);
    const sensorTypeId = sensorType.id;

    let typeMappingPayload = {
      name: 'default',
      thingTypeId: `${packageName}:`+ model.types[0].id,
      mappings: [ {
          sensorTypeId: sensorTypeId,
          measures: [{
              capabilityId: capabilityId,
              namedPropertySetId: model.capability_data_sets[0].id,
              propertyMappings: propertyMappings
          }]
        }
      ]
    }


    // make sure device model is replicated for mapping to be established
    console.info('have to wait 10 seconds for kafka and spark to do their job');
    await new Promise(resolve => setTimeout(resolve, 10000));


    // define the remaining payloads for creating thing model artefacts
    let packagePayload = {
        Name: packageName,
        Scope: 'private'
    };

    let thingTypePayload = {
        Name: `${packageName}:` + model.types[0].id,
        PropertySets: []
    };

    for(let propertyset of model.types[0].data_sets) {
      thingTypePayload.PropertySets.push({Name: propertyset, PropertySetType: packageName + ':' + propertyset});
    };

    // create the thing model side of the model
    await client.createPackage(packagePayload);
    console.log(`Package created: ${packagePayload.Name}`);
    await client.createPropertySetType(packagePayload.Name, propertySetTypePayloadTimeSeriesData);
    console.log(`Property set type created: ${propertySetTypePayloadTimeSeriesData.Name}`);
    await client.createPropertySetType(packagePayload.Name, propertySetTypePayloadMasterData);
    console.log(`Property set type created: ${propertySetTypePayloadMasterData.Name}`);
    await client.createThingType(packagePayload.Name, thingTypePayload);
    console.log(`Thing type created: ${thingTypePayload.Name}`);
    const murl = client.navigator.getDestination('tm-data-mapping') + '/v1/Mappings';
    const mappings = await client.request({url:murl, method:'POST', headers:{}, body:typeMappingPayload});
    console.log(`mapping created`);

    console.info('Device and thing model successfully created.');
} // end of main

/**
 * Read the configuration file in JSON format synchronously, parse the content and return the result.
 * @param {string} filename a file name relative to the script directory
 * @return {Object}  the parsed configuration file
 */
function readConfigFile(filename) {
    const abs_path = path.join(__dirname, filename);
    const json = fs.readFileSync(abs_path, {
        'encoding': 'utf8'
    });
    return JSON.parse(json);
}
