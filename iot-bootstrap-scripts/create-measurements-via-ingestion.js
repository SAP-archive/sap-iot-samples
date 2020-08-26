/**
 * @fileoverview Code sample for demonstrating SAP IoT Service device management.
 *
 *
 * The code is written in ECMAScript 8, which is supported by recent versions of Node.js.
 * Asynchronous functions (co-routines) are used intensively.
 *
 * @author Mikhail Bessonov <mikhail.bessonov@sap.com>, Marcus Behrens <marcus.behrens@sap.com>
 * @license
 * SAP Sample Code License Agreement, see the LICENSE file
 */

const mqtt = require('mqtt')
const fs = require('fs');
const path = require('path');

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

const config = readFile('default-env.json');
const deviceconnectivity = config.VCAP_SERVICES.iotae[0].credentials['iot-device-connectivity'];
//console.log(deviceconnectivity);
const devices = readFile('devices.json');
const model = readFile('model.json');

var initialData = {}

for(let property of model.capability_data_sets[0].properties)
  initialData[property.id] = property.min;

var lastData = {};
var mqttClient = {};
var deviceFactors = {};

connectToMQTT();

setInterval(() => {
    sendDataViaMQTT()
}, 60000);

function generateData(time, devicenumber) {
  for(let property of model.capability_data_sets[0].properties) {
    lastData[property.id] =  initialData[property.id] + (time + deviceFactors[devicenumber]) * (property.max - property.min) / 9;
  }
}

function sendDataViaMQTT() {
  let devicenumber = 0;
  let d = new Date();
  let time = d.getMinutes() % 10;
  for (let device of devices) {
    generateData(time, devicenumber);
    devicenumber++;
    let deviceAlternateId = device.id;
    let capabilityId = model.package + ':' + model.capability_data_sets[0].id;
    var payload = {
        sensorAlternateId: deviceAlternateId,
        capabilityAlternateId: capabilityId,
        measures: [lastData]
    }

//    console.log(payload);

    var topicName = 'measures/' + deviceAlternateId;
    mqttClient[device.id].publish(topicName, JSON.stringify(payload), [], error => {
        if(!error) {
            console.log("Data successfully sent for " + deviceAlternateId);
        } else {
            console.log("An unexpected error occurred:", error);
        }
    });
  }
}

function connectToMQTT() {
  let host = deviceconnectivity.mqtt.substr(8,deviceconnectivity.mqtt.length-13);
  //console.log(host);
  let devicenumber = 0;
  for (let device of devices) {
    let deviceAlternateId = device.id;
    let certificateFile = 'certificates/' + deviceAlternateId + '-device_certificate.pem';
    let passphraseFile = 'certificates/' + deviceAlternateId + '-device_passphrase.txt';
    var options = {
        keepalive: 10,
        clientId: deviceAlternateId,
        clean: true,
        reconnectPeriod: 2000,
        connectTimeout: 2000,
        cert: fs.readFileSync(certificateFile),
        key: fs.readFileSync(certificateFile),
        passphrase: fs.readFileSync(passphraseFile).toString(),
        rejectUnauthorized: false
    };

    deviceFactors[devicenumber] = Math.random() * devicenumber; // higher device numbers are more off then the low numbers
    devicenumber++;

    mqttClient[device.id] = mqtt.connect(`mqtts://${host}:8883`, options);

    mqttClient[device.id].subscribe('ack/' + deviceAlternateId);
    mqttClient[device.id].on('connect', () => console.log("Connection established for " + deviceAlternateId));
    mqttClient[device.id].on("error", err => console.log("Unexpected error occurred:", err.toString()));
    mqttClient[device.id].on('reconnect', () => console.log("Reconnected!"));
    mqttClient[device.id].on('close', () => console.log("Disconnected!"));
    mqttClient[device.id].on('message', (topic, msg) => console.log("Received acknowledgement for message:", msg.toString()));
  }
}
