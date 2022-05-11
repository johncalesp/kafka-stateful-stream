package sandbox.stream.dev.model.join;

import sandbox.stream.dev.model.Game;

public class Enriched implements Comparable<Enriched>{

    private Long streamerId;
    private String streamerName;
    private Long gameId;
    private String gameName;

    public void setStreamerId(Long streamerId) {
        this.streamerId = streamerId;
    }

    public void setStreamerName(String streamerName) {
        this.streamerName = streamerName;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    private Double donation;

    public Enriched(DonationWithStreamer donationWithStreamer, Game game){
        this.streamerId = donationWithStreamer.getStreamer().getId();
        this.streamerName = donationWithStreamer.getStreamer().getName();
        this.gameId = game.getId();
        this.gameName = game.getName();
        this.donation = donationWithStreamer.getDonationRecord().getDonation();
    }

    public Enriched(){}

    @Override
    public int compareTo(Enriched enriched) {
        return Double.compare(enriched.donation, donation);
    }

    public Long getStreamerId() {
        return streamerId;
    }

    public String getStreamerName() {
        return streamerName;
    }

    public Long getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public Double getDonation() {
        return donation;
    }

    public void setDonation(Double donation){
        this.donation = donation;
    }

    @Override
    public String toString() {
        return "{"
                + " streamerId='"
                + getStreamerId()
                + "'"
                + ", streamerName='"
                + getStreamerName()
                + "'"
                + ", gameName='"
                + getGameName()
                + "'"
                + ", donation='"
                + getDonation()
                + "'"
                + "}";
    }
}
