package sandbox.stream.dev.serialization.json;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import sandbox.stream.dev.HighDonations;
import sandbox.stream.dev.model.DonationRecord;
import sandbox.stream.dev.model.Game;
import sandbox.stream.dev.model.Streamer;
import sandbox.stream.dev.model.join.Enriched;

public class JsonSerdes {

    public static Serde<HighDonations> HighDonations(){
        JsonSerializer<HighDonations> serializer = new JsonSerializer<>();
        JsonDeserializer<HighDonations> deserializer = new JsonDeserializer<>(HighDonations.class);
        return Serdes.serdeFrom(serializer,deserializer);
    }

    public static Serde<Enriched> Enriched(){
        JsonSerializer<Enriched> serializer = new JsonSerializer<>();
        JsonDeserializer<Enriched> deserializer = new JsonDeserializer<>(Enriched.class);
        return Serdes.serdeFrom(serializer,deserializer);
    }
    public static Serde<DonationRecord> DonationRecord() {
        JsonSerializer<DonationRecord> serializer = new JsonSerializer<>();
        JsonDeserializer<DonationRecord> deserializer = new JsonDeserializer<>(DonationRecord.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<Streamer> Streamer() {
        JsonSerializer<Streamer> serializer = new JsonSerializer<>();
        JsonDeserializer<Streamer> deserializer = new JsonDeserializer<>(Streamer.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<Game> Game() {
        JsonSerializer<Game> serializer = new JsonSerializer<>();
        JsonDeserializer<Game> deserializer = new JsonDeserializer<>(Game.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

}
