package kafka.stateful.streams;

import kafka.stateful.streams.model.Donation;
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

public class ProducerDonations {
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
        KafkaProducer<String, Donation> producer = new KafkaProducer<String, Donation>(properties);

        // initialize record
        ProducerRecord<String, Donation> record;

        // set topic
        String topic = "donation-events";

        // import json parser
        JSONParser jsonParser  = new JSONParser();

        // File path
        String path = System.getProperty("user.dir") + "\\kafka-basics\\data\\donations.json";

        try {
            Object obj = jsonParser.parse(new FileReader(path));
            JSONArray donations = (JSONArray) obj;

            Iterator<JSONObject> donationsIterator = donations.iterator();
            while (donationsIterator.hasNext()){

                JSONObject jsonDonation = donationsIterator.next();
                Donation donation = new Donation();
                // skip incorrect records
                if (jsonDonation.get("donation") == null || jsonDonation.get("game_id") == null || jsonDonation.get("streamer_id") == null){
                    continue;
                } else {
                    donation.setDonation(Double.parseDouble(jsonDonation.get("donation").toString()));
                    donation.setGameId(Long.parseLong(jsonDonation.get("game_id").toString()));
                    donation.setStreamerId(Long.parseLong(jsonDonation.get("streamer_id").toString()));

                    record = new ProducerRecord<>(topic,donation);

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
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
