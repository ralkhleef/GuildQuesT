package guildquest.model;

import java.util.ArrayList;
import java.util.List;

public class WorldClock {
    private GlobalTime currentTime;

    /**
     * Observer pattern: listeners can react whenever the clock changes.
     */
    public interface ClockListener {
        void onTimeChanged(GlobalTime newTime);
    }

    private final List<ClockListener> listeners = new ArrayList<>();

    public WorldClock(GlobalTime start) {
        this.currentTime = start;
    }

    public GlobalTime now() { return currentTime; }

    public void addListener(ClockListener l) {
        if (l != null) listeners.add(l);
    }

    public void removeListener(ClockListener l) {
        listeners.remove(l);
    }

    public void advance(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("minutes must be >= 0");
        currentTime = currentTime.plus(minutes);
        notifyListeners();
    }

    private void notifyListeners() {
        for (ClockListener l : List.copyOf(listeners)) {
            l.onTimeChanged(currentTime);
        }
    }
}
