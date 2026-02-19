package guildquest.model;

public class QuestEvent {
    private final int eventId;
    private String title;
    private GlobalTime startTime;
    private GlobalTime endTime; // nullable
    private Realm realm;

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

    public void setTitle(String title) { this.title = title; }
    public void setStartTime(GlobalTime startTime) { this.startTime = startTime; }
    public void setEndTime(GlobalTime endTime) { this.endTime = endTime; }
    public void setRealm(Realm realm) { this.realm = realm; }

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
