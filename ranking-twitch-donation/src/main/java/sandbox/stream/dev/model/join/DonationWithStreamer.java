package sandbox.stream.dev.model.join;

import sandbox.stream.dev.model.DonationRecord;
import sandbox.stream.dev.model.Streamer;

public class DonationWithStreamer {

    private DonationRecord donationRecord;
    private Streamer streamer;

    public DonationWithStreamer(DonationRecord donationRecord, Streamer streamer){
        this.donationRecord = donationRecord;
        this.streamer = streamer;
    }

    public DonationRecord getDonationRecord(){
        return donationRecord;
    }

    public Streamer getStreamer(){
        return streamer;
    }

    @Override
    public String toString() {
        return "{" + " scoreEvent='" + getDonationRecord() + "'" + ", player='" + getStreamer() + "'" + "}";
    }
}
