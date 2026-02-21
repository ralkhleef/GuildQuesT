package guildquest.gui.strategy;

import guildquest.model.GlobalTime;
import guildquest.model.QuestEvent;
import guildquest.model.Realm;

public class BothTimeStrategy implements EventDisplayStrategy {

    @Override
    public String getName() { return "World + Local"; }

    @Override
    public String format(QuestEvent e) {
        String endWorld = (e.getEndTime() != null) ? (" -> " + e.getEndTime()) : "";

        Realm r = e.getRealm();
        if (r == null) {
            return e.getEventId()
                    + " - " + e.getTitle()
                    + " | World: " + e.getStartTime() + endWorld
                    + " | (No Realm)";
        }

        GlobalTime localStart = r.toLocalTime(e.getStartTime());
        String endLocal = "";
        if (e.getEndTime() != null) {
            endLocal = " -> " + r.toLocalTime(e.getEndTime());
        }

        return e.getEventId()
                + " - " + e.getTitle()
                + " | World: " + e.getStartTime() + endWorld
                + " | " + r.getName() + ": " + localStart + endLocal;
    }
}