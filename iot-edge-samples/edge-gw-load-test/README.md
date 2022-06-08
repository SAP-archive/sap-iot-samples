# Testing utility for SAP Edge Gateway Service performance

## Overview

This script sends simulated sensor readings to an SAP Edge Gateway Service running in a Kubernetes cluster and
measures the throughput and latency. The communication protocol used between this script and the gateway is plain
MQTT without TLS encryption.

We expect this script to be used for benchmarking the performance of different clusters, both hardware and software,
running the Edge Gateway Service. The hardware resources and the Kubernetes distribution and settings should be
the main differentiators.

THIS SOFTWARE IS NOT INTENDED FOR PRODUCTION USE.

## Product Documentation

Product Documentation for SAP IoT is available as follows:

>[SAP IoT HELP Portal](https://help.sap.com/viewer/p/SAP_IoT)

## Description

The script uses an undocumented feature of the Edge Gateway Service (MQTT). The JSON messages sent by
an end IoT device to the MQTT gateway can have a property called `"measureMessageId"`. The value of
this property, a string, is supposed to uniquely identify the message containing sensor readings
(a.k.a. measurements or "measures"). When the gateway is done processing the message, it sends an acknowledgement
back to the IoT device on a separate MQTT topic. If the original message contains `"measureMessageId"`,
the acknowledgement also contains the same property with the same value.

The script sends a flow of measurement messages to the gateway. The flow has a number of configurable properties
defining its intensity and described later in this README. The script records the round-trip-time for each
measurement message and performs simple statistical analysis on the fly. Each IoT device is simulated
by a separate task (Python co-routine), and the results are also reported for each device separately. A typical
printout looks like this:

```text
(venv) > python main.py --host 192.168.1.236 -r 1000 --devices 2 --multiprocess 2
INFO     Connection to MQTT open
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_0'.
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_1'.
INFO     Publishing done for 'PerfTest_mb_0' in 60 seconds, actual/desired rate 1000.3/1000.0 messages per second, 164.6 bytes per message, 1 samples per message, 164.7 KBytes/s
INFO     Publishing done for 'PerfTest_mb_1' in 60 seconds, actual/desired rate 1000.0/1000.0 messages per second, 164.6 bytes per message, 1 samples per message, 164.6 KBytes/s
INFO     All acknowledgements received for 'PerfTest_0', time correlation 0.98, latency percentiles (50%, 95%, 99%): 38785 58922 61243 ms
INFO     All acknowledgements received for 'PerfTest_1', time correlation 0.98, latency percentiles (50%, 95%, 99%): 39592 59496 62456 ms
INFO     Exit.
```

Such an output shows that the Edge Gateway Service under test (including the hardware and the cluster setup)
did *not* cope with the load. Two devices were simulated (`--devices 2`) sending messages with sensor readings
at the rate of 1000 messages per second from each device (`-r 1000`). Note the `time correlation 0.98` part.
It means a strong correlation between the amount of time the test runs and the latency: the longer the test
runs, the slower is the response of the system under test.

Compare the above results with the results of another run with a lower message rate, just 100 messages per second.
```text
(venv) > python main.py -b 192.168.1.236 -d 2 --multiprocess 2
INFO     Connection to MQTT open
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_1'.
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_0'.
INFO     All acknowledgements received for 'PerfTest_1', time correlation -0.14, latency percentiles (50%, 95%, 99%): 2 53 177 ms
INFO     Publishing done for 'PerfTest_0' in 60 seconds, actual/desired rate 100.1/100.0 messages per second, 162.6 bytes per message, 1 samples per message, 16.3 KBytes/s
INFO     Publishing done for 'PerfTest_1' in 60 seconds, actual/desired rate 100.2/100.0 messages per second, 162.6 bytes per message, 1 samples per message, 16.3 KBytes/s
INFO     All acknowledgements received for 'PerfTest_0', time correlation -0.13, latency percentiles (50%, 95%, 99%): 3 53 175 ms
INFO     Exit.
```

In this case we observe a small (and even negative) time correlation. The response time of the system under test
does not grow with time, which means that it can cope with such a load. The reported latencies are valid benchmark
results:

* The *median* latency, 50% percentile, is 2 ms
* Only 5% of the messages (95% percentile) took longer than 53 ms to process
* Only 1% of the messages (99% percentile) took longer than 175 ms to process

Reporting the minimum, maximum and mean latency makes little sense because these values not statistically stable,
and the median latency alone is not sufficient.

The requested MQTT QoS is always 0. If even a single measurement message is not acknowledged by the Edge Gateway
Service over the `/ack/<deviceAlternateId>` topic, an error is reported. Such a test run should be considered
a failure, and its benchmark results are invalid.

Multiple samples (measurements) per message can be sent: the more samples the higher the load. The sustained
throughput should be lower and the latency higher with a higher number of samples per second (when specified
with the `-s` command line argument).

If the system under test has lots of computing power, the test script itself could be a bottleneck. To avoid
a potential slowdown due to the Python Global Interpreter Lock (GIL), the script can run several worker
processes if requested via the `--multiprocess N` command line argument.

## Requirements

This script requires Python 3.8+ for running. While it can execute on the same computer that runs the Edge
Gateway under test, it would "steal" some computational resources from it. Running the script on a separate
computer with good network connectivity should provide higher benchmarks.

### SAP IoT Device Model and Configuration

Since the goal is to measure the local performance at the edge and not the SAP IoT cloud, the Edge Gateway Service
should be configured *not* to forward the measurements to the cloud by default. The corresponding setting
is called `MQTT Bus Measure Flow` and it should be set to `OnlyBus`.

The script does not create the Device Model in the SAP IoT cloud that it needs for operation. This way, it does
not need any administrative permissions for the SAP IoT instance. However, it means that the Device Model
must be created "by hand" using the Device Connectivity API of SAP IoT. The following entities must be created:

1. Capability
    ```json
    {
      "alternateId": "PerfTest",
      "name": "PerfTest",
      "properties": [
        {
          "name": "sn",
          "dataType": "integer"
        },
        {
          "name": "ts",
          "dataType": "string"
        }
      ]
    }
    ```
2. Sensor type
    ```json
    {
      "name": "PerfTest",
      "capabilities": [
        {
          "id": "<the ID of the above Capability that was automatically assigned to it>",
          "type": "measure"
        }
      ]
    }
    ```
3. The needed number (we recommend 10 at least) of Devices with Sensors. The numbering of Devices must start with 0.
    Please make sure that you are creating under the correct gateway: the `gatewayId` must match the one of the
    Edge Gateway Service instance under test. (You can change the pattern of the `alternateId` of the devices
    if it conflicts with some devices in your SAP IoT instance, e.g., to 
    `"PerfTest_whatever_0", "PerfTest_whatever_1", ...`. You'd need to change the
    function `simulate_device_range` in the `main.py` accordingly too.)
    ```json
    {
      "alternateId": "PerfTest_0",
      "name": "PerfTest0",
      "gatewayId": "<the ID of the Edge Gateway Service instance under test>",
      "sensors": [
        {
          "alternateId": "PerfTest",
          "name": "PerfTest",
          "sensorTypeId": "<the ID of the above sensor type>"
        }
      ]
    }
    ```

## Download and Installation

### Download the sample app

```
git clone https://github.com/SAP-Samples/iot-edge-samples.git
cd iot-edge-samples
```

### Set up the Python virtual environment

As usual, installing it in a virtual Python environment is desirable. If the venv Python package is not installed,
you can install it under Ubuntu with the command `sudo apt install python3-venv`. It should be pre-installed with
Python under Windows and Mac.

Create the virtual environment with the command `python3 -m venv venv`.

Active the virtual environment with the command `source venv/bin/activate` under Linux and Mac or
`venv\Scripts\activate` under Windows. You command prompt should start with `(venv)` now.

Install the dependencies into the virtual environment with the command `pip install -r requirements.txt`.

## Running the performance test

The virtual Python environment should be activated as described in the section above.

Run `python main.py -h` for the full list of options.

The default settings of the Edge Gateway Service are very conservative. In particular, it is limited to using
just one available virtual CPU independently of the actual computational resources available on the platform.
There is an easy way to overcome this restriction after the Edge Gateway Service is already deployed
to the Kubernetes cluster. Use the
```
kubectl -n sap-iot-gateway edit statefulset gw-mqtt
```
command (possibly with additional options like `--kubeconfig`) to edit the service configuration. Replace
```
Limits:
    cpu:     "1"
```
with
```
Limits:
    cpu:     "32"
```
or something like this. You can also adjust other limits.

### Examples of results

The results of some runs on an notebook with an 8-thread Intel Core 7 CPU are provided for comparison.

2300 * 6 = 13800 messages per second works; 6 devices sending 2300 message per second each. The latencies
are decent.

```
(venv) PS C:\Users\I342905\src\edge-gw-load-test> python .\main.py -b 192.168.1.236 -r 2300 -d 6 --multiprocess 6 -t 180

INFO     Connection to MQTT open
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_0'.
INFO     Connection to MQTT open
INFO     Connection to MQTT open
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_2'.
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_1'.
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_5'.
INFO     Publishing done for 'PerfTest_mb_0' in 180 seconds, actual/desired rate 2301.2/2300.0 messages per second, 166.5 bytes per message, 1 samples per message, 383.1 KBytes/s
INFO     Publishing done for 'PerfTest_mb_5' in 180 seconds, actual/desired rate 2301.0/2300.0 messages per second, 166.5 bytes per message, 1 samples per message, 383.0 KBytes/s
INFO     Publishing done for 'PerfTest_mb_1' in 180 seconds, actual/desired rate 2300.3/2300.0 messages per second, 166.5 bytes per message, 1 samples per message, 382.9 KBytes/s
INFO     Publishing done for 'PerfTest_mb_2' in 180 seconds, actual/desired rate 2300.4/2300.0 messages per second, 166.5 bytes per message, 1 samples per message, 382.9 KBytes/s
INFO     Publishing done for 'PerfTest_mb_3' in 180 seconds, actual/desired rate 2300.2/2300.0 messages per second, 166.5 bytes per message, 1 samples per message, 382.9 KBytes/s
INFO     Publishing done for 'PerfTest_mb_4' in 180 seconds, actual/desired rate 2300.2/2300.0 messages per second, 166.5 bytes per message, 1 samples per message, 382.9 KBytes/s
INFO     All acknowledgements received for 'PerfTest_mb_0', time correlation -0.11, latency percentiles (50%, 95%, 99%): 78 171 223 ms
INFO     All acknowledgements received for 'PerfTest_mb_5', time correlation -0.10, latency percentiles (50%, 95%, 99%): 77 173 225 ms
INFO     All acknowledgements received for 'PerfTest_mb_3', time correlation -0.11, latency percentiles (50%, 95%, 99%): 78 173 220 ms
INFO     All acknowledgements received for 'PerfTest_mb_1', time correlation -0.10, latency percentiles (50%, 95%, 99%): 77 170 224 ms
INFO     All acknowledgements received for 'PerfTest_mb_2', time correlation -0.10, latency percentiles (50%, 95%, 99%): 77 172 233 ms
INFO     All acknowledgements received for 'PerfTest_mb_4', time correlation -0.12, latency percentiles (50%, 95%, 99%): 77 172 225 ms
INFO     Exit.
```

2400 * 6 = 14400 messages per second *DOES NOT* work, the time correlation is 0.89, that is, latency grows with time.
The meadian latency exceeds 11s in a 3-minute test run, and can increase indefinitely in longer runs.
```
(venv) PS C:\Users\I342905\src\edge-gw-load-test> python .\main.py -b 192.168.1.236 -r 2400 -d 6 --multiprocess 6 -t 180
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_0'.
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_1'.
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_2'.
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_3'.
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_4'.
INFO     Connection to MQTT open
INFO     Subscribed to the acknowledgement topic 'ack/PerfTest_mb_5'.
INFO     Publishing done for 'PerfTest_mb_0' in 180 seconds, actual/desired rate 2400.8/2400.0 messages per second, 166.5 bytes per message, 1 samples per message, 399.7 KBytes/s
INFO     Publishing done for 'PerfTest_mb_1' in 180 seconds, actual/desired rate 2401.1/2400.0 messages per second, 166.5 bytes per message, 1 samples per message, 399.8 KBytes/s
INFO     Publishing done for 'PerfTest_mb_2' in 180 seconds, actual/desired rate 2400.4/2400.0 messages per second, 166.5 bytes per message, 1 samples per message, 399.6 KBytes/s
INFO     Publishing done for 'PerfTest_mb_3' in 180 seconds, actual/desired rate 2400.7/2400.0 messages per second, 166.5 bytes per message, 1 samples per message, 399.7 KBytes/s
INFO     Publishing done for 'PerfTest_mb_4' in 180 seconds, actual/desired rate 2401.3/2400.0 messages per second, 166.5 bytes per message, 1 samples per message, 399.8 KBytes/s
INFO     Publishing done for 'PerfTest_mb_5' in 180 seconds, actual/desired rate 2400.4/2400.0 messages per second, 166.5 bytes per message, 1 samples per message, 399.6 KBytes/s
INFO     All acknowledgements received for 'PerfTest_mb_0', time correlation 0.89, latency percentiles (50%, 95%, 99%): 11181 13347 13488 ms
INFO     All acknowledgements received for 'PerfTest_mb_1', time correlation 0.89, latency percentiles (50%, 95%, 99%): 11180 13353 13485 ms
INFO     All acknowledgements received for 'PerfTest_mb_2', time correlation 0.89, latency percentiles (50%, 95%, 99%): 11176 13346 13487 ms
INFO     All acknowledgements received for 'PerfTest_mb_3', time correlation 0.89, latency percentiles (50%, 95%, 99%): 11176 13353 13486 ms
INFO     All acknowledgements received for 'PerfTest_mb_4', time correlation 0.89, latency percentiles (50%, 95%, 99%): 11177 13351 13486 ms
INFO     All acknowledgements received for 'PerfTest_mb_5', time correlation 0.89, latency percentiles (50%, 95%, 99%): 11188 13347 13490 ms
INFO     Exit.
```


