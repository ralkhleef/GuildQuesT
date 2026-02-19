package guildquest.model;

import java.util.ArrayList;
import java.util.List;

public class Campaign {
    private final int campaignId;
    private String name;
    private boolean archived;

    private final List<QuestEvent> events = new ArrayList<>();
    private int nextEventId = 1;

    public Campaign(int campaignId, String name) {
        this.campaignId = campaignId;
        this.name = name;
    }

    public int getCampaignId() { return campaignId; }
    public String getName() { return name; }
    public boolean isArchived() { return archived; }

    public void setName(String name) { this.name = name; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public int nextEventId() { return nextEventId++; }

    public void addEvent(QuestEvent e) { events.add(e); }

    public void removeEventById(int eventId) {
        events.removeIf(e -> e.getEventId() == eventId);
    }

    public List<QuestEvent> getEvents() {
        return List.copyOf(events);
    }

    @Override
    public String toString() {
        return archived ? (name + " (archived)") : name;
    }
}
