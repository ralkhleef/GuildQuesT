package guildquest.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimelineView {
    private final Campaign campaign;

    public TimelineView(Campaign campaign) {
        this.campaign = campaign;
    }

    public List<QuestEvent> eventsAll() {
        return sortedCopy(campaign.getEvents());
    }

    public List<QuestEvent> eventsDay(GlobalTime now) {
        int day = now.toDays();
        List<QuestEvent> out = new ArrayList<>();
        for (QuestEvent e : campaign.getEvents()) {
            if (e.getStartTime().toDays() == day) out.add(e);
        }
        return sortedCopy(out);
    }

    public List<QuestEvent> eventsWeek(GlobalTime now) {
        int startDay = now.toDays();
        int endDay = startDay + 6;

        List<QuestEvent> out = new ArrayList<>();
        for (QuestEvent e : campaign.getEvents()) {
            int d = e.getStartTime().toDays();
            if (d >= startDay && d <= endDay) out.add(e);
        }
        return sortedCopy(out);
    }

    private List<QuestEvent> sortedCopy(List<QuestEvent> list) {
        List<QuestEvent> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparingInt(e -> e.getStartTime().toMinutes()));
        return copy;
    }
}
