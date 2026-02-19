package guildquest.gui.strategy;

import guildquest.model.QuestEvent;

public interface EventDisplayStrategy {
    String getName();
    String format(QuestEvent e);
}