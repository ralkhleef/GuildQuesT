package guildquest.gui;

import guildquest.model.Campaign;
import guildquest.model.GlobalTime;
import guildquest.model.QuestEvent;
import guildquest.model.Realm;
import guildquest.model.User;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Optional;

public class GuiMain extends Application {

    private final User activeUser = new User("GUI User");

    private final ObservableList<Campaign> campaigns = FXCollections.observableArrayList();
    private final ObservableList<QuestEvent> events = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // Your Realm constructor is (String name, int offsetMinutes)
        Realm defaultRealm = new Realm("Earth", 0);

        Campaign sample = new Campaign(1, "My Campaign");

        // Your QuestEvent expects endTime as GlobalTime (nullable), not Optional
        sample.addEvent(new QuestEvent(
                1,
                "First Event",
                new GlobalTime(0),
                null,               // endTime
                defaultRealm
        ));

        campaigns.add(sample);

        ListView<Campaign> campaignList = new ListView<>(campaigns);
        campaignList.setPrefWidth(260);
        campaignList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Campaign item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getCampaignId() + " - " + item.getName()));
            }
        });

        ListView<QuestEvent> eventList = new ListView<>(events);
        eventList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(QuestEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }

                // endTime is GlobalTime (nullable)
                String end = (item.getEndTime() != null) ? (" → " + item.getEndTime()) : "";

                setText(item.getEventId()
                        + " - " + item.getTitle()
                        + " @ " + item.getStartTime()
                        + end
                        + " (" + item.getRealm().getName() + ")"
                );
            }
        });

        // When campaign changes, refresh events list
        campaignList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            events.clear();
            if (newV != null) events.addAll(newV.getEvents());
        });
        campaignList.getSelectionModel().selectFirst();

        Button addCampaignBtn = new Button("Add Campaign");
        Button delCampaignBtn = new Button("Delete Campaign");
        Button addEventBtn = new Button("Add Event");
        Button delEventBtn = new Button("Delete Event");

        addCampaignBtn.setOnAction(e -> {
            String name = prompt("Campaign name:", "New Campaign");
            if (name == null) return;
            campaigns.add(new Campaign(nextCampaignId(), name));
        });

        delCampaignBtn.setOnAction(e -> {
            Campaign selected = campaignList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            campaigns.remove(selected);
            events.clear();
        });

        addEventBtn.setOnAction(e -> {
            Campaign selected = campaignList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                alert("Pick a campaign first.");
                return;
            }

            String title = prompt("Event title:", "New Event");
            if (title == null) return;

            Integer startMin = promptInt("Start time (total minutes):", "0");
            if (startMin == null) return;

            // optional end time
            Integer endMin = promptIntAllowBlank("End time (total minutes) (leave blank for none):", "");
            GlobalTime endTime = (endMin == null) ? null : new GlobalTime(endMin);

            String realmName = prompt("Realm name:", "Earth");
            if (realmName == null) return;

            Integer offset = promptInt("Realm offset minutes (e.g., -60, 0, 120):", "0");
            if (offset == null) return;

            int newEventId = nextEventId(selected);
            Realm realm = new Realm(realmName, offset);

            QuestEvent ev = new QuestEvent(
                    newEventId,
                    title,
                    new GlobalTime(startMin),
                    endTime,
                    realm
            );

            selected.addEvent(ev);
            events.add(ev);
        });

        delEventBtn.setOnAction(e -> {
            Campaign selectedCampaign = campaignList.getSelectionModel().getSelectedItem();
            QuestEvent selectedEvent = eventList.getSelectionModel().getSelectedItem();
            if (selectedCampaign == null || selectedEvent == null) return;

            // Your Campaign method is deleteEvent(int) based on your file
            selectedCampaign.deleteEvent(selectedEvent.getEventId());
            events.remove(selectedEvent);
        });

        HBox buttons = new HBox(10, addCampaignBtn, delCampaignBtn, addEventBtn, delEventBtn);
        buttons.setPadding(new Insets(10));

        VBox left = new VBox(6, new Label("Campaigns"), campaignList);
        VBox right = new VBox(6, new Label("Events"), eventList);

        SplitPane split = new SplitPane(left, right);
        split.setDividerPositions(0.33);

        BorderPane root = new BorderPane();
        root.setCenter(split);
        root.setBottom(buttons);
        root.setPadding(new Insets(10));

        stage.setTitle("GuildQuest (GUI)");
        stage.setScene(new Scene(root, 900, 520));
        stage.show();
    }

    private int nextCampaignId() {
        int max = 0;
        for (Campaign c : campaigns) max = Math.max(max, c.getCampaignId());
        return max + 1;
    }

    private int nextEventId(Campaign c) {
        int max = 0;
        for (QuestEvent e : c.getEvents()) max = Math.max(max, e.getEventId());
        return max + 1;
    }

    private String prompt(String header, String defaultValue) {
        TextInputDialog d = new TextInputDialog(defaultValue);
        d.setTitle("GuildQuest");
        d.setHeaderText(header);
        Optional<String> r = d.showAndWait();
        return r.map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
    }

    private Integer promptInt(String header, String defaultValue) {
        while (true) {
            String s = prompt(header, defaultValue);
            if (s == null) return null;
            try { return Integer.parseInt(s); }
            catch (NumberFormatException ex) { alert("Enter a valid integer."); }
        }
    }

    // returns null if user leaves blank, otherwise integer
    private Integer promptIntAllowBlank(String header, String defaultValue) {
        TextInputDialog d = new TextInputDialog(defaultValue);
        d.setTitle("GuildQuest");
        d.setHeaderText(header);
        Optional<String> r = d.showAndWait();
        if (r.isEmpty()) return null;

        String s = r.get().trim();
        if (s.isEmpty()) return null;

        try { return Integer.parseInt(s); }
        catch (NumberFormatException ex) {
            alert("Enter a valid integer (or leave blank).");
            return promptIntAllowBlank(header, defaultValue);
        }
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("GuildQuest");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
