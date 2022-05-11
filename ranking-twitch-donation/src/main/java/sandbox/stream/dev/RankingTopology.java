package sandbox.stream.dev;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import sandbox.stream.dev.model.DonationRecord;
import sandbox.stream.dev.model.Game;
import sandbox.stream.dev.model.Streamer;
import sandbox.stream.dev.model.join.DonationWithStreamer;
import sandbox.stream.dev.model.join.Enriched;
import sandbox.stream.dev.serialization.json.JsonSerdes;

public class RankingTopology {

   public static Topology build(){
       StreamsBuilder builder = new StreamsBuilder();

       // register the donations events stream
       KStream<String, DonationRecord> donationEvent =
               builder.stream("donation-events", Consumed.with(Serdes.ByteArray(), JsonSerdes.DonationRecord()))
                       // now marked for re-partitioning
                       .selectKey((k,v)->v.getStreamerId().toString());
       //donationEvent.print(Printed.<String, DonationRecord>toSysOut().withLabel("donation-stream"));

       // create the sharded streamers table
       KTable<String, Streamer> streamers =
               builder.table("streamers", Consumed.with(Serdes.String(), JsonSerdes.Streamer()));
       //streamers.toStream().print(Printed.<String, Streamer>toSysOut().withLabel("streamers-stream"));

       // create the global games table
       GlobalKTable<String, Game> games =
               builder.globalTable("games", Consumed.with(Serdes.String(),JsonSerdes.Game()));
       //System.out.println(games.toString());

       // join params for donationRecord -> streamer join
       Joined<String, DonationRecord, Streamer> streamerJoinParams =
               Joined.with(Serdes.String(), JsonSerdes.DonationRecord(),JsonSerdes.Streamer());

       // join donationRecord -> streamer
       ValueJoiner<DonationRecord,Streamer, DonationWithStreamer> donationStreamerJoiner =
               (donation,streamer) -> new DonationWithStreamer(donation,streamer);

       KStream<String,DonationWithStreamer> withStreamers =
               donationEvent.join(streamers,donationStreamerJoiner,streamerJoinParams);

       KeyValueMapper<String, DonationWithStreamer, String> keyMapper =
               (leftKey, donationWithPlayer) ->
                    String.valueOf(donationWithPlayer.getDonationRecord().getGameId());

       // join the withStreamers stream to the game global ktable
       ValueJoiner<DonationWithStreamer,Game, Enriched> gameJoiner =
               (donationWithStreamer, game) -> new Enriched(donationWithStreamer,game);

       KStream<String,Enriched> withGames = withStreamers.join(games,keyMapper,gameJoiner);
       withGames.print(Printed.<String, Enriched>toSysOut().withLabel("with-games"));

       /** Group the enriched game stream */
       KGroupedStream<String,Enriched> grouped =
               withGames.groupBy(
                       (key,value) -> value.getGameId().toString(),
                       Grouped.with(Serdes.String(),JsonSerdes.Enriched())
               );

       // Initializer
       Initializer<HighDonations> highDonationsInitializer = HighDonations::new;

       // Accumulator
       Aggregator<String, Enriched, HighDonations> highDonationsAgg =
               (key,value,adder) -> adder.add(value);

       // Perform the aggregation
       KTable<String, HighDonations> highDonations =
               grouped.aggregate(
                       highDonationsInitializer,
                       highDonationsAgg,
                       Materialized.<String, HighDonations, KeyValueStore<Bytes, byte[]>>
                               // give the state store an explicit name to make it available for interactive
                               // queries
                                       as("donation-board")
                               .withKeySerde(Serdes.String())
                               .withValueSerde(JsonSerdes.HighDonations()));

       highDonations.toStream().to("high-donations");


       return builder.build();
   }


}
