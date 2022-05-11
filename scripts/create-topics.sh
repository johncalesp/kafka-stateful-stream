echo "Waiting for Kafka to come online..."

cub kafka-ready -b kafka:9092 1 20

# create the donation events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic donation-events \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the players topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic streamers \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the games topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic games \
  --replication-factor 1 \
  --partitions 4 \
  --create

# create the high-scores topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic high-donations \
  --replication-factor 1 \
  --partitions 4 \
  --create

sleep infinity
