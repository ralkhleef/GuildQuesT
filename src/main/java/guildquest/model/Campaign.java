package guildquest.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Campaign {
    private final int campaignId;
    private String name;
    private boolean archived;

    private Visibility visibility = Visibility.PRIVATE;
    private User owner; // nullable for legacy constructors
    private final Map<User, Permission> sharedWith = new HashMap<>();

    // ---- Observer (A3) ----
    private final List<CampaignObserver> observers = new ArrayList<>();

    private final List<QuestEvent> events = new ArrayList<>();
    private int nextEventId = 1;

    public Campaign(int campaignId, String name) {
        this.campaignId = campaignId;
        this.name = name;
    }

    public Campaign(int campaignId, String name, User owner) {
        this(campaignId, name);
        this.owner = owner;
    }

    public int getCampaignId() { return campaignId; }
    public String getName() { return name; }
    public boolean isArchived() { return archived; }

    public Visibility getVisibility() { return visibility; }
    public User getOwner() { return owner; }
    public Map<User, Permission> getSharedWith() { return Collections.unmodifiableMap(sharedWith); }

    public void addObserver(CampaignObserver observer) {
        if (observer == null) return;
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeObserver(CampaignObserver observer) {
        observers.remove(observer);
    }

    private void notifyCampaignChanged() {
        for (CampaignObserver o : List.copyOf(observers)) {
            o.onCampaignChanged(this);
        }
    }

    private void notifyEventChanged(QuestEvent event) {
        for (CampaignObserver o : List.copyOf(observers)) {
            o.onEventChanged(this, event);
        }
    }

    public void setName(String name) {
        this.name = name;
        notifyCampaignChanged();
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
        notifyCampaignChanged();
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = (visibility == null) ? Visibility.PRIVATE : visibility;
        notifyCampaignChanged();
    }

    public void setOwner(User owner) {
        this.owner = owner;
        notifyCampaignChanged();
    }

    public void shareWith(User user, Permission permission) {
        if (user == null) return;
        sharedWith.put(user, permission == null ? Permission.VIEW_ONLY : permission);
        notifyCampaignChanged();
    }

    public void unshare(User user) {
        if (user == null) return;
        sharedWith.remove(user);
        notifyCampaignChanged();
    }

    public boolean canView(User user) {
        if (visibility == Visibility.PUBLIC) return true;
        if (user == null) return false;
        if (owner != null && owner.equals(user)) return true;
        return sharedWith.containsKey(user);
    }

    public boolean canEdit(User user) {
        if (user == null) return false;
        if (owner != null && owner.equals(user)) return true;
        Permission p = sharedWith.get(user);
        return p == Permission.COLLABORATIVE;
    }

    public int nextEventId() { return nextEventId++; }

    public void addEvent(QuestEvent e) {
        events.add(e);
        notifyEventChanged(e);
    }

    /** Parameter Object overload (A3 refactoring): create + add an event from a single data object. */
    public QuestEvent addEvent(EventData data) {
        if (data == null) throw new IllegalArgumentException("EventData is required");
        if (!data.isValid()) {
            throw new IllegalArgumentException("Invalid event data: " + data.getValidationError());
        }

        QuestEvent event = new QuestEvent(
                nextEventId(),
                data.getTitle(),
                data.getStartTime(),
                data.getEndTime(),
                data.getRealm()
        );
        addEvent(event); // uses existing method + notifies observers
        return event;
    }

    // Original method (keep it)
    public void removeEventById(int eventId) {
        QuestEvent removed = null;
        for (QuestEvent e : events) {
            if (e.getEventId() == eventId) {
                removed = e;
                break;
            }
        }
        if (removed != null) {
            events.remove(removed);
            notifyEventChanged(removed);
        }
    }

    // Alias used by the GUI (so GuiMain can call deleteEvent)
    public void deleteEvent(int eventId) {
        removeEventById(eventId);
    }

    public List<QuestEvent> getEvents() {
        return List.copyOf(events);
    }

    @Override
    public String toString() {
        return archived ? (name + " (archived)") : name;
    }
}
