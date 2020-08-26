/**
 * @fileoverview Code sample for demonstrating SAP IoT device connectivity.
 *
 * creates devices and things and assigns them to each other for later ingestion.
 * devices should be listed with alternate id and other master data in a devices.json file.
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

// Constants
const LeonardoIoT = require('sap-leonardo-iot-sdk');
const client = new LeonardoIoT();

// Read the configuration files
const devices = readFile('devices.json');
const model = readFile('model.json');

/**
 * The ID of the SAP IoT Services Cloud MQTT Gateway.
 * @type {string}
 */
let gatewayId;

/**
 * The ID of the sample sensor type.
 * @type {string}
 */
let sensorTypeId;


main()
    .catch((err) => {
        console.error(err);
        process.exit(1);
    });

// Only function declarations after this point.

/**
 * The main function.
 */
async function main() {
    const tenantInfo = await client.request({
        url: `${client.navigator.businessPartner()}/Tenants`
    });

    const packageName = `${tenantInfo.value[0].package}.`+ model.package;

    sensorTypeId = await findSensorTypeId(model.package + ':'+model.types[0].id);
    gatewayId = await findCloudGatewayMQTT();

    const rootObjectGroup = await client.getRootObjectGroup();
    const thingPayload = {
        _thingType: [packageName + ':' + model.types[0].id],
        "_description": {
          "en": "default description",
        },
        _objectGroup: rootObjectGroup.objectGroupID,
        "data": {}
    };

    const murl = client.navigator.getDestination('tm-data-mapping') + '/v1/Mappings/mappingIds?sensorTypeId=' + sensorTypeId;
    const mappings = await client.request({url:murl});
    //console.log(mappings);
    mappingId = mappings[0];

    for (let device of devices) {
      //console.log(device);
      let deviceAlternateId = device.id;
      const deviceId = await createDevice(gatewayId, deviceAlternateId);
      const sensorId = await createSensor(deviceId, deviceAlternateId, sensorTypeId);
      const absPathPem = path.join(__dirname + '/certificates', deviceAlternateId + '-device_certificate.pem');
      const absPathPass = path.join(__dirname + '/certificates', deviceAlternateId + '-device_passphrase.txt');

      const curl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/devices/' + deviceId + '/authentications/clientCertificate/pem';
      const resp = await client.request({url:curl, method:'GET', headers:{}, body:{deviceId:deviceId}});

      fs.writeFileSync(absPathPem, resp.pem);
      fs.writeFileSync(absPathPass, resp.secret);
      console.info('Saved the PEM and the passphrase for device ' + deviceAlternateId);

      let propertySetName = model.application_data_sets[0].id;
      thingPayload._name = deviceAlternateId;
      thingPayload._alternateId = deviceAlternateId;
      thingPayload._description.en = deviceAlternateId;
      thingPayload.data[propertySetName] = {};
      for(let property of model.application_data_sets[0].properties) {
        thingPayload.data[propertySetName][property.id] = device[property.id];
      }

      // make sure device model is replicated for mapping to be established
      console.info('have to wait 2 seconds for kafka and spark to do their job');
      await new Promise(resolve => setTimeout(resolve, 2000));

      const createThingResponse = await client.createThing(thingPayload, {resolveWithFullResponse: true});
      const thingId = createThingResponse.headers.location.split('\'')[1];
      console.log(`Thing created: ${thingId}`);
      const aurl = client.navigator.getDestination('tm-data-mapping') + '/v1/Assignments';
      let deviceAssignmentPayload =
      {
        "thingId": thingId,
        "sensorIds": [sensorId],
        "mappingId": mappingId,
      };
      //console.log(deviceAssignmentPayload);
      const assignments = await client.request({url:aurl, method:'POST', headers:{}, body:deviceAssignmentPayload});
      //console.log(assignments);
    }

    console.info('Devices and a sensor created successfully.');
}

/**
 * Read the configuration file in JSON format synchronously, parse the content and return the result.
 * @param {string} filename a file name relative to the script directory
 * @return {Object}  the parsed configuration file
 */
function readFile(filename) {
    const abs_path = path.join(__dirname, filename);
    const json = fs.readFileSync(abs_path, {
        'encoding': 'utf8'
    });
    return JSON.parse(json);
}

/**
 * Find the ID of the IoT Service sensor type by its name.
 *
 * The name is assumed to be unique, even though the IoT Service does not guarantee it.
 */
async function findSensorTypeId(typeName) {
  const surl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/sensorTypes?filter=name eq \'' + typeName + '\'';
  const existingTypes = await client.request({url:surl, method:'GET', headers:{}, body:{}});
  //console.log(existingTypes);
  if (!(Array.isArray(existingTypes) && existingTypes.length == 1)) {
    throw new Error(`Lookup of the sensor type with the name '${typeName}' failed!`);
  }
  return existingTypes[0].id;
}

/**
 * Find the ID of the SAP IoT Service MQTT gateway
 * @return {string}  the ID of the gateway used by SAP IoT Services
 * @throws {Error} Raises an exception when SAP IoT Service returns an error.
 */
async function findCloudGatewayMQTT() {
  const gurl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/gateways?filter=alternateId eq \'GATEWAY_CLOUD_MQTT\'';
  const gateways = await client.request({url:gurl, method:'GET', headers:{}, body:{}});
  //console.log(gateways);

  if (!(Array.isArray(gateways) && gateways.length > 0)) {
      throw new Error('Lookup of Cloud MQTT gateway failed!');
  }
  console.info(`Cloud MQTT gateway ID is '${gateways[0].id}'`);
  return gateways[0].id;
}

/**
 * Create the device in SAP IoT Services if it does not exist yet.
 * @param {string} gatewayId  the ID of the gateway to which the device sends the data
 * @param {string} alternateId  the alternate ID of the created device
 * @param {boolean} isRouter  true if the created device can post measurements on behalf of other devices
 * @return {string} the ID of the device used by SAP IoT Services
 * @throws {Error} Raises an exception when SAP IoT Service returns an error.
 */
async function createDevice(gatewayId, alternateId, isRouter = false) {
    // The lookup was OK, but the capability with this alternate ID was not found.
    console.info(`Creating a new device '${alternateId}'`);
    const request = {
        gatewayId: gatewayId,
        alternateId: alternateId,
        name: alternateId
    }
    if (isRouter) {
        request.authorizations = [{
            type: 'router'
        }];
    }
    const durl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/devices';
    console.log(durl);
    const res = await client.request({url:durl, method:'POST', headers:{}, body:request});

    console.info(`Device '${alternateId}' created with ID='${res.id}'.`);
    return res.id;
}

/**
 * Create the sensor in SAP IoT Services if it does not exist yet.
 * @param {string} deviceId  the ID of the device to which the sensor is attached
 * @param {string} alternateId  the alternate ID of the created sensor
 * @param {string} sensorTypeId  the ID of the type of the created sensor
 * @return {string}  the ID of the sensor used by SAP IoT Services
 * @throws {Error} Raises an exception when SAP IoT Service returns an error.
 */
async function createSensor(deviceId, alternateId, sensorTypeId) {
    console.info(`Creating a new sensor '${alternateId}'`);
    const surl = client.navigator.getDestination('iot-device-connectivity') + '/api/v1/sensors';
    const res = await client.request({url:surl, method:'POST', headers:{}, body:{
            deviceId,
            alternateId,
            sensorTypeId,
            name: alternateId
    }});

    console.info(`Sensor '${alternateId}' for device '${deviceId}' created with ID='${res.id}'.`);
    return res.id;
}
