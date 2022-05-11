package sandbox.stream.dev;

import io.javalin.Javalin;
import io.javalin.http.Context;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyQueryMetadata;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sandbox.stream.dev.model.join.Enriched;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingService {

    private  HostInfo hostInfo;
    private  KafkaStreams streams;

    private static final Logger log = LoggerFactory.getLogger(RankingService.class);


    public RankingService(HostInfo hostInfo, KafkaStreams streams){
        this.hostInfo = hostInfo;
        this.streams = streams;
    }

    ReadOnlyKeyValueStore<String, HighDonations> getStore(){
        return streams.store(
                StoreQueryParameters.fromNameAndType(
                        "donation-board",
                        QueryableStoreTypes.keyValueStore()));
    }

    void start(){
        Javalin app = Javalin.create().start(hostInfo.port());

        /** Local key-value store query: all entries */
        app.get("/leaderboard", this::getAll);

        /** Local key-value store query: point-lookup / single-key lookup */
        app.get("/leaderboard/{key}", this::getKey);
    }

    void getAll(Context ctx){
        Map<String, List<Enriched>> leaderboard = new HashMap<>();

        KeyValueIterator<String, HighDonations> range = getStore().all();
        while(range.hasNext()){
            KeyValue<String,HighDonations> next = range.next();
            String game = next.key;
            HighDonations highDonations = next.value;
            leaderboard.put(game,highDonations.toList());
        }

        range.close();

        ctx.json(leaderboard);
    }

    void getKey(Context ctx){

        String gameId = ctx.pathParam("key");
        // find out which host has the key
        KeyQueryMetadata metadata =
                streams.queryMetadataForKey("donation-board",gameId, Serdes.String().serializer());

        // IF the local instance has this key
        if(hostInfo.equals(metadata.activeHost())){
            log.info("Querying local host for key");

            HighDonations highDonations = getStore().get(gameId);
            if (highDonations == null) {
                // game was not found
                ctx.status(404);
                return;
            }

            // game was found, so return the high scores
            ctx.json(highDonations.toList());
            return;
        }

        // IF a remote instance has the key
        String remoteHost = metadata.activeHost().host();
        int remotePort = metadata.activeHost().port();
        // Prepare the request
        String url =
                String.format(
                        "http://%s:%d/leaderboard/%s",
                        // params
                        remoteHost, remotePort, gameId);

        // issue the request
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            log.info("Querying remote store for key");
            ctx.result(response.body().string());
        } catch (Exception e) {
            ctx.status(500);
        }
    }
}
