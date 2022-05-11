package kafka.stateful.streams;

import kafka.stateful.streams.model.Streamer;
import kafka.stateful.streams.serialization.json.JsonSerializer;
import kafka.tutorial1.ProducerJson;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Properties;

public class ProducerStreamer {
    public static void main(String[] args) {
        // logger
        Logger logger = LoggerFactory.getLogger(ProducerJson.class);

        // kafka server
        String bootstrapServers = "192.168.2.13:9094";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class.getName());

        // create the producer
        KafkaProducer<String, Streamer> producer = new KafkaProducer<String, Streamer>(properties);

        // initialize record
        ProducerRecord<String, Streamer> record;

        // set topic
        String topic = "streamers";

        // import json parser
        JSONParser jsonParser  = new JSONParser();

        // File path
        String path = System.getProperty("user.dir") + "\\kafka-basics\\data\\streamers.json";

        try {
            Object obj = jsonParser.parse(new FileReader(path));
            JSONArray streamers = (JSONArray) obj;

            Iterator<JSONObject> streamersIterator = streamers.iterator();

            while (streamersIterator.hasNext()){

                JSONObject jsonStreamer = streamersIterator.next();
                Streamer streamer = new Streamer();

                // skip incorrect records
                if (jsonStreamer.get("id") == null || jsonStreamer.get("name") == null){
                    continue;
                }
                streamer.setId(Long.parseLong(jsonStreamer.get("id").toString()));
                streamer.setName(jsonStreamer.get("name").toString());

                record = new ProducerRecord<>(topic,streamer.getId().toString(),streamer);

                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if(e==null){
                            logger.info("Received new metadata. \n" +
                                    "Topic: " + recordMetadata.topic() + "\n" +
                                    "Partition: " + recordMetadata.partition() + "\n" +
                                    "Offset: " + recordMetadata.offset() + "\n" +
                                    "Timestamp: " + recordMetadata.timestamp());
                        }else{
                            logger.error("Error generating new record",e);
                        }
                    }
                });

                Thread.sleep(2000);


            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
