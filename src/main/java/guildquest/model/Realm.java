package guildquest.model;

/**
 * Realm represents a physical location with a simple fixed offset (in minutes)
 * from WorldClock time. (LocalTime = WorldTime + offsetMinutes)
 */
public class Realm {
    private final String name;
    private final int offsetMinutes;

    public Realm(String name, int offsetMinutes) {
        this.name = name;
        this.offsetMinutes = offsetMinutes;
    }

    public String getName() { return name; }
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
        return name + " (UTC" + sign + String.format("%02d:%02d", h, m) + ")";
    }
}
