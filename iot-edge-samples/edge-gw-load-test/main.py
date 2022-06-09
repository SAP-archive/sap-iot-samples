# -*- coding: utf-8 -*-
"""
A load test for the Edge Gateway Service using the MQTT protocol.

Author: Mikhail Bessonov
"""

# Standard Python 3 libraries
import argparse
import asyncio
import datetime
import json
import logging
import logging.handlers
import math
import multiprocessing
import platform
import sys
import time
from typing import Dict

# Third party libraries
import asyncio_mqtt
import async_timeout
import tdigest  # Percentile estimation for streaming data

# Local modules
import correlator

# The maximum time in seconds to wait for the confirmation of a measurement
# message by the gateway.
MEASUREMENT_ACK_TIMEOUT = 30  # seconds; if no acks come before the timeout expires, we stop waiting.


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('-b', '--host',
                        help='MQTT broker host', default='localhost')
    parser.add_argument('-p', '--port', type=int,
                        help='MQTT broker port', default=61657)
    parser.add_argument('-d', '--devices', type=int, choices=range(1, 11),
                        help='The number of simulated end IoT devices', default=1)
    parser.add_argument('-r', '--rate', type=int, default=100,
                        help='Message rate: the number of messages sent per second from each IoT device')
    parser.add_argument('-s', '--samples', type=int,
                        help='The number of samples (measurements) per message', default=1)
    parser.add_argument('-t', '--time', type=int,
                        help='The time duration in seconds for running the test', default=60)
    parser.add_argument('-m', '--multiprocess', type=int, choices=range(1, 9), default=1,
                        help='Use N parallel processes to generate the load')
    args = parser.parse_args()

    root_logger = logging.getLogger()
    root_logger.setLevel(logging.DEBUG)

    console = logging.StreamHandler()
    console.setLevel(logging.DEBUG)
    formatter = logging.Formatter('%(levelname)-8s %(message)s')
    console.setFormatter(formatter)
    root_logger.addHandler(console)

    # Child processes log to this queue
    log_queue = multiprocessing.Queue()
    # The queue listener gets log records from the queue and prints them to the console
    queue_listener = logging.handlers.QueueListener(log_queue, console)
    queue_listener.start()

    with multiprocessing.Pool(args.multiprocess, init_worker, [log_queue, logging.INFO]) as pool:
        results = []
        start = 0
        step = math.ceil(args.devices / args.multiprocess)
        for i in range(args.multiprocess):
            upto = min(start + step, args.devices)
            results.append(pool.apply_async(simulate_device_range_wrapper, (args, start, upto)))
            start = upto

        for result in results:
            result.ready()
            result.get()

    # Wait for potential unprocessed log records in the queue
    time.sleep(1)
    queue_listener.stop()
    logging.info('Exit.')
    logging.shutdown()


def init_worker(log_queue: multiprocessing.Queue, loglevel: int) -> None:
    """Initialize the worker process."""
    root_logger = logging.getLogger()
    root_logger.setLevel(loglevel)
    queue_handler = logging.handlers.QueueHandler(log_queue)  # Non-blocking handler.
    root_logger.addHandler(queue_handler)


def simulate_device_range_wrapper(args: argparse.Namespace, start: int, upto: int) -> None:
    """A synchronous wrapper for the :py:func:`simulate_device_range` co-routine.

    This wrapper is run in its own process, so it must create a new asyncio context for the async tasks.
    """
    if platform.system() == 'Windows':
        asyncio.set_event_loop_policy(asyncio.WindowsSelectorEventLoopPolicy())
    asyncio.run(simulate_device_range(args, start, upto))


async def simulate_device_range(args: argparse.Namespace, start: int, upto: int) -> None:
    """Simulate several IoT devices.
    :param args: the parsed command line arguments of the script (see the main() function)
    :param start: the start of the range of the alternate IDs of the simulated devices.
    :param upto: the end of the range of the alternate IDs of the simulated devices.
    The start and upto are treated according to the Python range/slice rules, that is, start is included
    and upto is not.
    """
    simulators = [simulate_device(args, f"PerfTest_mb_{i}") for i in range(start, upto)]
    await asyncio.gather(*simulators)


class PublishingState:
    """A namespace for keeping the state of a single IoT device.

    This simulated device publishes sensor readings and receives acknowledgements from the gateway.
    """

    def __init__(self, args: argparse.Namespace, mqtt_client: asyncio_mqtt.Client,
                 device_alternate_id: str, num_msgs: int = 10):
        """PublishingState constructor
        :param args: the parsed command line arguments of the script (see the main() function)
        :param mqtt_client: the instance of an MQTT client to use
        :param device_alternate_id: the alternate ID of the simulated device
        :param num_msgs: the number of the measurement messages to send.
        """
        self.mqtt_client = mqtt_client
        """The instance of an MQTT client to use"""
        self.device_alternate_id: str = device_alternate_id
        """The alternate ID of the simulated device"""
        self.num_msgs: int = num_msgs
        """The total number of messages to publish"""
        self.desired_message_rate: float = args.rate
        """The number of messages per second to publish"""
        self.samples_per_message: int = args.samples
        """The number of samples (measurement) per message to publish"""
        self.unconfirmed_measurements: Dict[str, datetime.datetime] = {}
        """The timestamp when the message with the given message ID--the key--was published.
        The messages referenced in the dictionary were not confirmed by the gateway (yet).
        As soon as the message is confirmed, the corresponding entry is removed from the dictionary.
        """
        self.utc_start_ts: float | None = None
        """The timestamp when the first measurement message was posted"""
        self.messages_published: int = 0
        """The number of measurement messages published"""
        self.bytes_published: int = 0
        """The cumulative size of the measurement messages published (in bytes)"""
        self.messages_acked = 0
        """The number of measurement messages successfully acknowledged by the gateway"""
        self.correlator = correlator.Correlator()
        """Measures how the latency correlates with the number of the published measurement messages.
        A positive correlation means that the gateway does not cope with the load and keeps buffering
        the incoming messages.
        """
        self.digest = tdigest.TDigest()
        """Percentile estimation for streaming data"""


async def simulate_device(args: argparse.Namespace, device_alternate_id: str) -> None:
    # An individual loglevel can be set for the MQTT client.
    logger = logging.getLogger('mqtt')
    # logger.setLevel(logging.DEBUG)
    logger.setLevel(logging.INFO)
    async with asyncio_mqtt.Client(hostname=args.host,
                                   port=args.port,
                                   client_id=device_alternate_id,
                                   logger=logger) as mqtt_client:
        logging.info("Connection to MQTT open")
        state = PublishingState(args, mqtt_client, device_alternate_id,
                                num_msgs=args.time * args.rate)
        await asyncio.gather(
            send_measurements(state),
            process_acks(state)
        )


async def send_measurements(state: PublishingState) -> None:
    """The task for posting measurement messages to the MQTT gateway.

    The task sends measurements from a single simulated device.
    """
    mqtt_client = state.mqtt_client
    device_alternate_id = state.device_alternate_id
    msgs_to_send = state.num_msgs

    utc_ts = datetime.datetime.utcnow()
    state.messages_published = 0
    state.utc_start_ts = utc_ts.timestamp()
    # The number of messages published one after the other without any sleep() in between
    messages_no_sleep = 0
    for i in range(msgs_to_send):
        utc_ts = datetime.datetime.utcnow()
        payload = {
            "measureMessageId": str(i),
            "capabilityAlternateId": "PerfTest",
            "sensorAlternateId": "PerfTest",
            "measures": [{
                "ts": utc_ts.isoformat() + 'Z',
                "sn": i
            }] * state.samples_per_message
        }
        payload_json = json.dumps(payload)
        payload_bytes = payload_json.encode('utf-8')

        measure_message_id = payload["measureMessageId"]
        state.unconfirmed_measurements[measure_message_id] = utc_ts
        await mqtt_client.publish(f"measures/{device_alternate_id}", payload_json)
        logging.debug("Published measurement for '%s': %d bytes", device_alternate_id, len(payload_bytes))
        state.messages_published += 1
        state.bytes_published += len(payload_bytes)

        utc_ts = datetime.datetime.utcnow()
        delay = state.messages_published / state.desired_message_rate - (utc_ts.timestamp() - state.utc_start_ts)
        if delay > 0.1:
            await asyncio.sleep(delay)
            messages_no_sleep = 0
        elif delay < -15.0:
            logging.error("Publisher cannot keep the sampling rate of %f samples/s, only %f achieved. Aborting.",
                          state.desired_message_rate,
                          state.messages_published / (utc_ts.timestamp() - state.utc_start_ts))
            await mqtt_client.disconnect()
            return
        else:
            messages_no_sleep += 1
            if messages_no_sleep > 10:
                await asyncio.sleep(0)
                messages_no_sleep = 0

    posting_duration = utc_ts.timestamp() - state.utc_start_ts
    logging.info("Publishing done for '%s' in %d seconds, actual/desired rate %.1f/%.1f messages per second, "
                 "%.1f bytes per message, %d samples per message, %.1f KBytes/s",
                 device_alternate_id, round(posting_duration), msgs_to_send / posting_duration,
                 state.desired_message_rate, state.bytes_published / state.messages_published,
                 state.samples_per_message, state.bytes_published / posting_duration / 1000.0)


async def process_acks(state: PublishingState) -> None:
    """The task for processing acknowledgements from the MQTT gateway."""
    mqtt_client = state.mqtt_client
    device_alternate_id = state.device_alternate_id
    msgs_expected = state.num_msgs
    loop = asyncio.get_event_loop()

    ack_topic = f"ack/{device_alternate_id}"
    async with mqtt_client.unfiltered_messages() as messages:
        await mqtt_subscribe(mqtt_client, ack_topic)
        logging.info("Subscribed to the acknowledgement topic '%s'.", ack_topic)
        try:
            # Raise an exception if no ask is received before the timeout
            async with async_timeout.timeout(MEASUREMENT_ACK_TIMEOUT) as context_manager:
                async for msg in messages:
                    # Reschedule the timeout
                    context_manager.update(loop.time() + MEASUREMENT_ACK_TIMEOUT)
                    logging.debug("Message on topic '%s': %s", msg.topic, msg.payload.decode())
                    utc_now = datetime.datetime.utcnow()

                    status = json.loads(msg.payload.decode('utf-8'))[0]
                    logging.debug("Got ACK for '%s': %s", device_alternate_id, status)
                    assert status['code'] == 202
                    measure_message_id = status['id']
                    utc_publish_ts = state.unconfirmed_measurements[measure_message_id]
                    del state.unconfirmed_measurements[measure_message_id]
                    latency = round((utc_now.timestamp() - utc_publish_ts.timestamp()) * 1000)  # in ms
                    assert latency >= 0
                    logging.debug("Measurement %s for %s latency: %s ms", measure_message_id,
                                  device_alternate_id, latency)
                    state.correlator.add_point(latency)
                    state.digest.update(latency)

                    msgs_expected -= 1
                    if msgs_expected <= 0:
                        break
                    elif (msgs_expected % 1000) == 0:
                        state.digest.compress()
        except asyncio.exceptions.TimeoutError:
            logging.error("Timeout waiting for acknowledgement of a message published over MQTT, "
                          "%d messages remain unacknowledged. The test results are invalid.",
                          len(state.unconfirmed_measurements))
            return

    logging.info("All acknowledgements received for '%s', time correlation %.2f, "
                 "latency percentiles (50%%, 95%%, 99%%): %d %d %d ms",
                 device_alternate_id, state.correlator.approx_correlation(),
                 round(state.digest.percentile(50)),
                 round(state.digest.percentile(95)),
                 round(state.digest.percentile(99)))


async def mqtt_subscribe(client: asyncio_mqtt.Client, topic: str) -> None:
    """Subscribe to the topic and raise an exception in case of failure."""
    res = await client.subscribe(topic)
    if not 0 <= res[0] <= 2:
        raise asyncio_mqtt.MqttError(f'Subscription to topic "{topic}" refused.')


if __name__ == '__main__':
    main()
