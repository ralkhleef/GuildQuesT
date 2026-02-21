package guildquest.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private final String name;
    private final List<Campaign> campaigns = new ArrayList<>();
    private final UserSettings settings;

    public User(String name) {
        this.name = name;
        // Reasonable defaults (can be changed from a Settings screen later)
        this.settings = new UserSettings(new Realm("Earth", 0), Theme.CLASSIC, TimeDisplayPreference.BOTH);
    }

    public String getName() { return name; }

    // Alias used by GuildQuestApp
    public String getUsername() { return name; }

    public UserSettings getSettings() { return settings; }

    /**
     * Encapsulate internal collection: callers can iterate, but cannot mutate.
     */
    public List<Campaign> getCampaigns() {
        return Collections.unmodifiableList(campaigns);
    }

    public Campaign getCampaignById(int campaignId) {
        for (Campaign c : campaigns) {
            if (c.getCampaignId() == campaignId) return c;
        }
        return null;
    }

    public void addCampaign(Campaign c) {
        if (c != null && c.getOwner() == null) {
            c.setOwner(this);
        }
        campaigns.add(c);
    }
    public void removeCampaignById(int campaignId) {
        campaigns.removeIf(c -> c.getCampaignId() == campaignId);
    }

    @Override public String toString() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return name != null ? name.equals(other.name) : other.name == null;
    }

    @Override
    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }
}
