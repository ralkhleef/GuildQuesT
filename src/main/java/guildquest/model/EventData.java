package guildquest.model;

/**
 * Parameter object for creating or updating a QuestEvent.
 *
 * Motivation (A3): avoids long/duplicated parameter lists across GUI + model code,
 * and centralizes validation in one place.
 */
public class EventData {
    private final String title;
    private final GlobalTime startTime;
    private final GlobalTime endTime; // nullable
    private final Realm realm;

    public EventData(String title, GlobalTime startTime, GlobalTime endTime, Realm realm) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.realm = realm;
    }

    /** Convenience constructor for GUI dialogs that collect minutes. */
    public EventData(String title, int startMinutes, Integer endMinutes, Realm realm) {
        this(title,
                new GlobalTime(startMinutes),
                (endMinutes == null) ? null : new GlobalTime(endMinutes),
                realm);
    }

    public String getTitle() { return title; }
    public GlobalTime getStartTime() { return startTime; }
    public GlobalTime getEndTime() { return endTime; }
    public Realm getRealm() { return realm; }

    public boolean isValid() {
        return getValidationError() == null;
    }

    public String getValidationError() {
        if (title == null || title.trim().isEmpty()) {
            return "Title cannot be empty";
        }
        if (startTime == null) {
            return "Start time is required";
        }
        if (endTime != null && startTime.compareTo(endTime) >= 0) {
            return "End time must be after start time";
        }
        if (realm == null) {
            return "Realm is required";
        }
        return null;
    }
}
