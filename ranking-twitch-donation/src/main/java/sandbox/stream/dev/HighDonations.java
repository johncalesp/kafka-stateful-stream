package sandbox.stream.dev;

import sandbox.stream.dev.model.join.Enriched;

import java.util.*;

public class HighDonations {

    private HashMap<Long,Enriched> streamerDonationMap = new HashMap<>();

    public HighDonations add(final Enriched enriched){
        Long streamerID = enriched.getStreamerId();
        Enriched tempEnriched;
        if(streamerDonationMap.get(streamerID)!=null){
            tempEnriched = streamerDonationMap.get(streamerID);
            tempEnriched.setDonation(tempEnriched.getDonation() + enriched.getDonation());
        } else {
            tempEnriched = new Enriched();
            tempEnriched.setDonation(enriched.getDonation());
            tempEnriched.setGameId(enriched.getGameId());
            tempEnriched.setStreamerId(enriched.getStreamerId());
            tempEnriched.setStreamerName(enriched.getStreamerName());
            tempEnriched.setGameName(enriched.getGameName());

            streamerDonationMap.put(streamerID,tempEnriched);
        }


        System.out.println(tempEnriched.getStreamerId() + " " + tempEnriched.getDonation());

        return this;
    }

    public List<Enriched> toList() {

        List<Enriched> enrichedValues = new ArrayList<>(streamerDonationMap.values());
        Collections.sort(enrichedValues);
        for (Enriched en: enrichedValues){
            System.out.println(en.getStreamerId() + " " + en.getDonation());
        }

        List<Enriched> streamersDonations = new ArrayList<>();
        // retrieving top 3 streamers with most donations
        for(int i=0;i<Math.min(3,enrichedValues.size());i++){
            streamersDonations.add(enrichedValues.get(i));
        }

        return streamersDonations;
    }
}
