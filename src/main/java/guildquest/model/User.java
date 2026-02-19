package guildquest.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String name;
    private final List<Campaign> campaigns = new ArrayList<>();

    public User(String name) { this.name = name; }

    public String getName() { return name; }
    public List<Campaign> getCampaigns() { return campaigns; }

    public void addCampaign(Campaign c) { campaigns.add(c); }
    public void removeCampaignById(int campaignId) {
        campaigns.removeIf(c -> c.getCampaignId() == campaignId);
    }

    @Override public String toString() { return name; }
}
