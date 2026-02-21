package guildquest.gui.strategy;

import guildquest.model.QuestEvent;

public class WorldTimeStrategy implements EventDisplayStrategy {

    @Override
    public String getName() { return "World Time"; }

    @Override
    public String format(QuestEvent e) {
        String end = (e.getEndTime() != null) ? (" -> " + e.getEndTime()) : "";
        String realmName = (e.getRealm() != null) ? e.getRealm().getName() : "No Realm";

        return e.getEventId()
                + " - " + e.getTitle()
                + " | World: " + e.getStartTime() + end
                + " (" + realmName + ")";
    }
}