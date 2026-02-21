package guildquest;

import java.util.*;

import guildquest.gui.strategy.BothTimeStrategy;
import guildquest.gui.strategy.EventDisplayStrategy;
import guildquest.gui.strategy.LocalTimeStrategy;
import guildquest.gui.strategy.WorldTimeStrategy;
import guildquest.model.*;

/**
 * Simple text-based GuildQuest app for Assignment 2.
 * Focus: Campaigns + QuestEvents + WorldClock + Realms (fixed offset).
 */
public class ConsoleApp {

    private final Scanner in = new Scanner(System.in);
    private final WorldClock clock = new WorldClock(new GlobalTime(0));
    private final User user = new User("Player1");

    private int nextCampaignId = 1;

    // Simple realm registry
    private final Map<String, Realm> realms = new LinkedHashMap<>();

    // Strategy pattern: swap event formatting without conditionals.
    private EventDisplayStrategy displayStrategy = new BothTimeStrategy();

    // Refactoring (introduce enum): avoid stringly-typed views.
    private enum TimelineRange { ALL, DAY, WEEK }

    // Command pattern: eliminate large switch statement in the main loop.
    private interface Command {
        String name();
        void execute();
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();

    public static void run() {
        new ConsoleApp().loop();
    }

    private ConsoleApp() {
        // default realms
        realms.put("Earth", new Realm("Earth", 0));
        realms.put("Mars", new Realm("Mars", 39));     // example offset
        realms.put("Luna", new Realm("Luna", 0));

        // Observer example: print whenever time changes.
        clock.addListener(newTime -> System.out.println("[clock] now " + newTime));

        registerCommands();
    }

    private void loop() {
        System.out.println("GuildQuest (A2) - Text UI");
        help();

        while (true) {
            System.out.print("\n> ");
            String cmd = in.nextLine().trim();
            if (cmd.isEmpty()) continue;

            try {
                String key = cmd.toLowerCase(Locale.ROOT);
                if (key.equals("quit") || key.equals("exit")) {
                    System.out.println("Bye!");
                    return;
                }

                Command c = commands.get(key);
                if (c == null) {
                    System.out.println("Unknown command. Type 'help'.");
                } else {
                    c.execute();
                }
            } catch (RuntimeException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void registerCommands() {
        commands.put("help", cmd("help", this::help));
        commands.put("time", cmd("time", this::showTime));
        commands.put("advance", cmd("advance", this::advanceTime));
        commands.put("realms", cmd("realms", this::listRealms));
        commands.put("addrealm", cmd("addrealm", this::addRealm));
        commands.put("settings", cmd("settings", this::settings));

        commands.put("campaigns", cmd("campaigns", this::listCampaigns));
        commands.put("addcampaign", cmd("addcampaign", this::addCampaign));
        commands.put("renamecampaign", cmd("renamecampaign", this::renameCampaign));
        commands.put("archivecampaign", cmd("archivecampaign", this::archiveCampaign));
        commands.put("deletecampaign", cmd("deletecampaign", this::deleteCampaign));

        commands.put("events", cmd("events", this::listEvents));
        commands.put("addevent", cmd("addevent", this::addEvent));
        commands.put("updateevent", cmd("updateevent", this::updateEvent));
        commands.put("deleteevent", cmd("deleteevent", this::deleteEvent));
    }

    private Command cmd(String name, Runnable r) {
        return new Command() {
            @Override public String name() { return name; }
            @Override public void execute() { r.run(); }
        };
    }

    private void help() {
        System.out.println(
            "Commands:\n" +
            "  help\n" +
            "  time              (show world clock)\n" +
            "  advance           (advance world time by minutes)\n" +
            "  realms            (list realms)\n" +
            "  addrealm          (add realm with fixed offset)\n" +
            "  settings          (toggle time display: world/local)\n\n" +
            "  campaigns         (list campaigns)\n" +
            "  addcampaign\n" +
            "  renamecampaign\n" +
            "  archivecampaign\n" +
            "  deletecampaign\n\n" +
            "  events            (list events in a campaign with view filter)\n" +
            "  addevent\n" +
            "  updateevent\n" +
            "  deleteevent\n\n" +
            "  quit\n"
        );
    }

    private void showTime() {
        System.out.println("WorldClock: " + clock.now());
    }

    private void advanceTime() {
        int mins = askInt("Advance minutes: ");
        clock.advance(mins);
        System.out.println("WorldClock is now: " + clock.now());
    }

    private void listRealms() {
        System.out.println("Realms:");
        for (Realm r : realms.values()) {
            System.out.println("  - " + r);
        }
    }

    private void addRealm() {
        String name = ask("Realm name: ");
        int offset = askInt("Offset minutes from WorldClock (e.g., -60, 90): ");
        realms.put(name, new Realm(name, offset));
        System.out.println("Added realm: " + realms.get(name));
    }

    private void settings() {
        System.out.println("Current display mode: " + displayStrategy.getName());
        System.out.println("Pick display mode: 1) World  2) Local  3) Both");
        int choice = askInt("Choice (1-3): ");
        displayStrategy = switch (choice) {
            case 1 -> new WorldTimeStrategy();
            case 2 -> new LocalTimeStrategy();
            default -> new BothTimeStrategy();
        };
        System.out.println("Updated display mode: " + displayStrategy.getName());
    }

    private void listCampaigns() {
        if (user.getCampaigns().isEmpty()) {
            System.out.println("(no campaigns)");
            return;
        }
        for (Campaign c : user.getCampaigns()) {
            System.out.println("  #" + c.getCampaignId() + " " + c);
        }
    }

    private void addCampaign() {
        String name = ask("Campaign name: ");
        Campaign c = new Campaign(nextCampaignId++, name);
        user.addCampaign(c);
        System.out.println("Added campaign #" + c.getCampaignId());
    }

    private Campaign pickCampaign() {
        listCampaigns();
        int id = askInt("Campaign id: ");
        Campaign c = user.getCampaignById(id);
        if (c == null) throw new RuntimeException("Campaign not found: " + id);
        return c;
    }

    private void renameCampaign() {
        Campaign c = pickCampaign();
        String name = ask("New name: ");
        c.setName(name);
        System.out.println("Renamed.");
    }

    private void archiveCampaign() {
        Campaign c = pickCampaign();
        boolean arch = askYesNo("Archive it? (y/n): ");
        c.setArchived(arch);
        System.out.println("Updated.");
    }

    private void deleteCampaign() {
        Campaign c = pickCampaign();
        user.removeCampaignById(c.getCampaignId());
        System.out.println("Deleted.");
    }

    private void listEvents() {
        Campaign c = pickCampaign();
        TimelineView tv = new TimelineView(c);

        TimelineRange view = askTimelineRange();

        List<QuestEvent> events = switch (view) {
            case DAY -> tv.eventsDay(clock.now());
            case WEEK -> tv.eventsWeek(clock.now());
            default -> tv.eventsAll();
        };

        if (events.isEmpty()) {
            System.out.println("(no events)");
            return;
        }
        for (QuestEvent e : events) {
            System.out.println("  " + displayStrategy.format(e));
        }
    }

    private TimelineRange askTimelineRange() {
        String raw = ask("View (all/day/week): ").trim().toLowerCase(Locale.ROOT);
        return switch (raw) {
            case "day" -> TimelineRange.DAY;
            case "week" -> TimelineRange.WEEK;
            default -> TimelineRange.ALL;
        };
    }

    private void addEvent() {
        Campaign c = pickCampaign();
        String title = ask("Title: ");
        GlobalTime start = askTime("Start time (D H M): ");
        boolean hasEnd = askYesNo("Has end time? (y/n): ");
        GlobalTime end = hasEnd ? askTime("End time (D H M): ") : null;

        Realm realm = pickRealm();
        QuestEvent e = new QuestEvent(c.nextEventId(), title, start, end, realm);
        c.addEvent(e);
        System.out.println("Added event #" + e.getEventId());
    }

    private void updateEvent() {
        Campaign c = pickCampaign();
        int id = askInt("Event id: ");
        QuestEvent target = null;
        for (QuestEvent e : c.getEvents()) if (e.getEventId() == id) target = e;
        if (target == null) throw new RuntimeException("Event not found: " + id);

        // Refactoring: Extract Method (break up a long method).
        System.out.println("Leave blank to keep current value.");
        applyOptionalTitle(target);
        applyOptionalStartTime(target);
        applyOptionalEndTime(target);
        applyOptionalRealm(target);

        System.out.println("Updated.");
    }

    private void applyOptionalTitle(QuestEvent target) {
        String title = ask("New title: ");
        if (!title.isBlank()) target.setTitle(title);
    }

    private void applyOptionalStartTime(QuestEvent target) {
        String startRaw = ask("New start (D H M): ");
        if (!startRaw.isBlank()) target.setStartTime(parseTime(startRaw));
    }

    private void applyOptionalEndTime(QuestEvent target) {
        String endRaw = ask("New end (D H M) or 'none': ");
        if (endRaw.isBlank()) return;
        if (endRaw.equalsIgnoreCase("none")) target.setEndTime(null);
        else target.setEndTime(parseTime(endRaw));
    }

    private void applyOptionalRealm(QuestEvent target) {
        if (askYesNo("Change realm? (y/n): ")) target.setRealm(pickRealm());
    }

    private void deleteEvent() {
        Campaign c = pickCampaign();
        int id = askInt("Event id: ");
        c.removeEventById(id);
        System.out.println("Deleted (if existed).");
    }

    private Realm pickRealm() {
        listRealms();
        String name = ask("Realm name: ");
        Realm r = realms.get(name);
        if (r == null) throw new RuntimeException("Unknown realm: " + name);
        return r;
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    private int askInt(String prompt) {
        while (true) {
            String s = ask(prompt);
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("Enter a number."); }
        }
    }

    private boolean askYesNo(String prompt) {
        while (true) {
            String s = ask(prompt).toLowerCase(Locale.ROOT);
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Type y/n.");
        }
    }

    private GlobalTime askTime(String prompt) {
        String raw = ask(prompt);
        return parseTime(raw);
    }

    private GlobalTime parseTime(String raw) {
        String[] parts = raw.trim().split("\\s+");
        if (parts.length != 3) throw new RuntimeException("Format must be: D H M (e.g., 2 13 45)");
        int d = Integer.parseInt(parts[0]);
        int h = Integer.parseInt(parts[1]);
        int m = Integer.parseInt(parts[2]);
        return new GlobalTime(d, h, m);
    }
}
