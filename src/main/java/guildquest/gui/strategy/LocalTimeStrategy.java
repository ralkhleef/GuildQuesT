package guildquest.gui.strategy;

import guildquest.model.GlobalTime;
import guildquest.model.QuestEvent;
import guildquest.model.Realm;

public class LocalTimeStrategy implements EventDisplayStrategy {

    @Override
    public String getName() { return "Realm Local Time"; }

    @Override
    public String format(QuestEvent e) {
        Realm r = e.getRealm();
        if (r == null) {
            return e.getEventId() + " - " + e.getTitle() + " | (No Realm)";
        }

        GlobalTime localStart = r.toLocalTime(e.getStartTime());
        String end = "";
        if (e.getEndTime() != null) {
            GlobalTime localEnd = r.toLocalTime(e.getEndTime());
            end = " → " + localEnd;
        }

        return e.getEventId()
                + " - " + e.getTitle()
                + " | " + r.getName() + ": " + localStart + end;
    }
}