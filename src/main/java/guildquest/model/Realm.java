package guildquest.model;

/**
 * Realm represents a physical location with a simple fixed offset (in minutes)
 * from WorldClock time. (LocalTime = WorldTime + offsetMinutes)
 */
public class Realm {
    private final int realmId;
    private final String name;
    private final String description; // nullable
    private final int offsetMinutes;

    /**
     * Backward-compatible constructor.
     * Realm identity is derived from the name (good enough for the assignment MVP).
     */
    public Realm(String name, int offsetMinutes) {
        this(Math.abs(name == null ? 0 : name.hashCode()), name, null, offsetMinutes);
    }

    public Realm(int realmId, String name, String description, int offsetMinutes) {
        this.realmId = realmId;
        this.name = name;
        this.description = (description == null || description.isBlank()) ? null : description;
        this.offsetMinutes = offsetMinutes;
    }

    /** Unique map identity (minimum requirement). */
    public int getRealmId() { return realmId; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getOffsetMinutes() { return offsetMinutes; }

    public GlobalTime toLocalTime(GlobalTime worldTime) {
        return worldTime.plusMinutes(offsetMinutes);
    }

    @Override
    public String toString() {
        String sign = offsetMinutes >= 0 ? "+" : "-";
        int abs = Math.abs(offsetMinutes);
        int h = abs / 60;
        int m = abs % 60;
        return name + " (#" + realmId + ") (UTC" + sign + String.format("%02d:%02d", h, m) + ")";
    }
}
