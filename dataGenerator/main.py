import json
import logging
import os
import time
from math import radians, sin, cos, sqrt, atan2

from kafka import KafkaProducer, KafkaAdminClient
from kafka.admin import NewTopic

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger('producer.main')

topic = "shipping_updates"
bootstrap_servers = os.getenv("BOOTSTRAP_SERVERS")


def haversine(lat1, lon1, lat2, lon2):
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])

    dlon = lon2 - lon1
    dlat = lat2 - lat1
    a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    r = 6371
    return c * r


def generate_coordinates(locations):
    coordinates = []

    for i in range(len(locations) - 1):
        lat1, lon1 = locations[i]
        lat2, lon2 = locations[i + 1]

        distance = haversine(lat1, lon1, lat2, lon2)
        remaining_distance = 0

        while remaining_distance < distance:
            lat = lat1 + (remaining_distance / distance) * (lat2 - lat1)
            lon = lon1 + (remaining_distance / distance) * (lon2 - lon1)
            coordinates.append({
                'lon': f'{lon}',
                'lat': f'{lat}'
            })
            remaining_distance += 1.628

    # Add the last location
    lat, lon = locations[-1]
    coordinates.append({
        'lon': f'{lon}',
        'lat': f'{lat}'
    })

    with open("coordinates.json", "w") as f:
        json.dump(coordinates, f, indent=4)


def topic_exists(topic_name, admin_client):
    topics = admin_client.list_topics()
    return topic_name in topics


def create_topic(topic_name):
    admin_client = KafkaAdminClient(bootstrap_servers=bootstrap_servers)
    if not topic_exists(topic_name, admin_client):
        admin_client.create_topics([NewTopic(name=topic_name, num_partitions=5, replication_factor=1)])
        logger.info(f"Topic '{topic_name}' created.")
    else:
        logger.info(f"Topic '{topic_name}' exists.")


def main():
    locations = [
        (-33.865143, 151.209900),  # Sydney
        (1.3521, 103.8198),  # Singapore
        (46.0569, 14.5058),  # Ljubljana
        (45.5167, 13.5667)  # Portorož
    ]
    generate_coordinates(locations)
    with open('coordinates.json', 'r') as file:
         coordinates = json.load(file)
    producer = KafkaProducer(bootstrap_servers=bootstrap_servers,
                             value_serializer=lambda v: json.dumps(v).encode('utf-8'))
    create_topic(topic)

    with open('coordinates.json', 'r') as file:
        coordinates = json.load(file)

    index = 0
    logger.info(len(coordinates))
    coordinates = coordinates
    starting_timestamp = 1701388800 * 1000  # Fri Dec 01 2023 00:00:00 GMT+0000
    arrival_timestamp = 1703440800 * 1000  # Sun Dec 24 2023 18:00:00 GMT+0000
    timestamp_step = (arrival_timestamp - starting_timestamp) / len(coordinates)

    try:
        while True:
            record = {
                'timestamp': int(round(starting_timestamp + timestamp_step * index)),
                'coordinates': {
                    'lat': coordinates[index]['lat'],
                    'lon': coordinates[index]['lon'],
                },
                'deliveryId': 'id-0001'
            }
            producer.send(topic, value=record)
            time.sleep(0.3)
            # logger.info(f"Message sent: {record}")
            index += 1

    except KeyboardInterrupt:
        producer.close()
    except IndexError:
        logger.info("All records sent!")
        producer.close()


if __name__ == "__main__":
    main()
