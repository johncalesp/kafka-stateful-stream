package sandbox.stream.dev;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;

import java.util.Properties;

public class App {

    public static void main(String[] args) {
        Topology topology = RankingTopology.build();

        // set the required properties for running Kafka Streams

        String host = System.getProperty("host");
        Integer port = Integer.parseInt(System.getProperty("port"));
        String stateDir = System.getProperty("stateDir");
        String endpoint = String.format("%s:%s", host, port);

        // set the required properties for running Kafka Streams
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "development");
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:29092");
        properties.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        properties.setProperty(StreamsConfig.APPLICATION_SERVER_CONFIG, endpoint);
        properties.setProperty(StreamsConfig.STATE_DIR_CONFIG, stateDir);


        // build the topology and start streaming!
        KafkaStreams streams = new KafkaStreams(topology, properties);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        System.out.println("Starting Donation Ranking streams");
        streams.start();

        // start the REST service
        HostInfo hostInfo = new HostInfo(host, port);
        RankingService rankingService = new RankingService(hostInfo,streams);
        rankingService.start();
    }
}
