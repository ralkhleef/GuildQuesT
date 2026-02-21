package guildquest.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestEvent {
    private final int eventId;
    private String title;
    private GlobalTime startTime;
    private GlobalTime endTime; // nullable
    private Realm realm;

    // Lightweight MVP for "participants" + "items" fields in the GUI and requirements.
    // (Strings keep it simple; you can later upgrade to Character/Inventory.)
    private final List<String> participants = new ArrayList<>();
    private final List<String> items = new ArrayList<>();

    // Event-level sharing (share a single event without sharing the entire campaign)
    private final Map<User, Permission> sharedWith = new HashMap<>();

    public QuestEvent(int eventId, String title, GlobalTime startTime, GlobalTime endTime, Realm realm) {
        this.eventId = eventId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.realm = realm;
    }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public GlobalTime getStartTime() { return startTime; }
    public GlobalTime getEndTime() { return endTime; }
    public Realm getRealm() { return realm; }

    public List<String> getParticipants() { return Collections.unmodifiableList(participants); }
    public List<String> getItems() { return Collections.unmodifiableList(items); }

    public Map<User, Permission> getSharedWith() { return Collections.unmodifiableMap(sharedWith); }

    public void setTitle(String title) { this.title = title; }
    public void setStartTime(GlobalTime startTime) { this.startTime = startTime; }
    public void setEndTime(GlobalTime endTime) { this.endTime = endTime; }
    public void setRealm(Realm realm) { this.realm = realm; }

    public void addParticipant(String name) {
        if (name == null) return;
        String n = name.trim();
        if (!n.isEmpty()) participants.add(n);
    }

    public void clearParticipants() { participants.clear(); }

    public void addItem(String item) {
        if (item == null) return;
        String it = item.trim();
        if (!it.isEmpty()) items.add(it);
    }

    public void clearItems() { items.clear(); }

    public void shareWith(User user, Permission permission) {
        if (user == null) return;
        sharedWith.put(user, permission == null ? Permission.VIEW_ONLY : permission);
    }

    public void unshare(User user) {
        if (user == null) return;
        sharedWith.remove(user);
    }

    public String formatForDisplay(boolean showWorldTime, boolean showLocalTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(eventId).append(" ").append(title);

        if (showWorldTime) {
            sb.append(" | World: ").append(startTime);
            if (endTime != null) sb.append(" - ").append(endTime);
        }
        if (showLocalTime && realm != null) {
            GlobalTime localStart = realm.toLocalTime(startTime);
            sb.append(" | ").append(realm.getName()).append(": ").append(localStart);
            if (endTime != null) {
                GlobalTime localEnd = realm.toLocalTime(endTime);
                sb.append(" - ").append(localEnd);
            }
        }
        if (realm != null) sb.append(" | Realm: ").append(realm);
        return sb.toString();
    }
}
