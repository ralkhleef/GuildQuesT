package guildquest.model;

public class WorldClock {
    private GlobalTime currentTime;

    public WorldClock(GlobalTime start) {
        this.currentTime = start;
    }

    public GlobalTime now() { return currentTime; }

    public void advance(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("minutes must be >= 0");
        currentTime = currentTime.plus(minutes);
    }
}
