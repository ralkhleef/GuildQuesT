package guildquest.model;

public class GlobalTime implements Comparable<GlobalTime> {
    private final int totalMinutes;

    public GlobalTime(int totalMinutes) {
        if (totalMinutes < 0) throw new IllegalArgumentException("totalMinutes must be >= 0");
        this.totalMinutes = totalMinutes;
    }

    public GlobalTime(int days, int hours, int minutes) {
        this(days * 24 * 60 + hours * 60 + minutes);
    }


    public int toMinutes() { return totalMinutes; }
    public int toDays() { return totalMinutes / (60 * 24); }

    public GlobalTime plus(int minutes) {
        return new GlobalTime(this.totalMinutes + minutes);
    }

    @Override
    public int compareTo(GlobalTime other) {
        return Integer.compare(this.totalMinutes, other.totalMinutes);
    }

    @Override
    public String toString() {
        int day = toDays();
        int minsInDay = totalMinutes % (60 * 24);
        int hr = minsInDay / 60;
        int min = minsInDay % 60;
        return "Day " + day + " " + String.format("%02d:%02d", hr, min);
    }

    public GlobalTime plusMinutes(int deltaMinutes) {
        return new GlobalTime(this.totalMinutes + deltaMinutes);
    }
}
