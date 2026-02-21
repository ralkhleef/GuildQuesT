package guildquest.gui;

import guildquest.gui.commands.*;
import guildquest.gui.strategy.*;
import guildquest.model.Campaign;
import guildquest.model.CampaignObserver;
import guildquest.model.EventData;
import guildquest.model.GlobalTime;
import guildquest.model.Permission;
import guildquest.model.QuestEvent;
import guildquest.model.Realm;
import guildquest.model.Theme;
import guildquest.model.TimeDisplayPreference;
import guildquest.model.User;
import guildquest.model.Visibility;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.application.Platform;

import java.util.Optional;

public class GuiMain extends Application implements CampaignObserver {

    private final User activeUser = new User("GUI User");

    // Demo users for sharing (small + realistic)
    private final ObservableList<User> allUsers = FXCollections.observableArrayList(
            activeUser,
            new User("Jane Doe"),
            new User("John Doe"),
            new User("Alex Smith")
    );

    private final ObservableList<Campaign> campaigns = FXCollections.observableArrayList();
    private final ObservableList<QuestEvent> events = FXCollections.observableArrayList();

    private final ObjectProperty<EventDisplayStrategy> eventDisplay = new SimpleObjectProperty<>();

    // Keep references so observer callbacks can refresh UI safely.
    private ListView<Campaign> campaignList;
    private ListView<QuestEvent> eventList;

    // Stored so Commands can operate without capturing many locals.
    private Stage stageRef;
    private Realm defaultRealmRef;

    @Override
    public void start(Stage stage) {
        this.stageRef = stage;
        // Your Realm constructor is (String name, int offsetMinutes)
        this.defaultRealmRef = new Realm("Earth", 0);

        Campaign sample = new Campaign(1, "My Campaign", activeUser);

        // Your QuestEvent expects endTime as GlobalTime (nullable), not Optional
        sample.addEvent(new QuestEvent(
                1,
                "First Event",
                new GlobalTime(0),
                null,               // endTime
                defaultRealmRef
        ));

        campaigns.add(sample);
        // Observer registration (A3)
        sample.addObserver(this);

        // Default settings wiring (strategy + theme)
        syncDisplayStrategyFromSettings();

        campaignList = new ListView<>(campaigns);
        campaignList.setPrefWidth(260);
        campaignList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Campaign item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }

                String v = item.getVisibility() == null ? "PRIVATE" : item.getVisibility().name();
                int shared = item.getSharedWith() == null ? 0 : item.getSharedWith().size();
                String sharedText = (shared == 0) ? "" : (" | shared: " + shared);

                setText(item.getCampaignId() + " - " + item.getName() + " [" + v + "]" + sharedText);
            }
        });

        eventList = new ListView<>(events);
        eventList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(QuestEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    return;
                }

                EventDisplayStrategy s = eventDisplay.get();
                setText(s == null ? (item.getEventId() + " - " + item.getTitle()) : s.format(item));
            }
        });

        // If settings change, refresh event list formatting
        eventDisplay.addListener((obs, oldV, newV) -> eventList.refresh());

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

        Button shareBtn = new Button("Share / Visibility");
        Button settingsBtn = new Button("Settings");

        // ---- AI-assisted Design Pattern (A3): Command ----
        // UI event handlers now execute command objects rather than embedding logic
        // directly in lambdas.
        GuiCommand addCampaignCmd = new AddCampaignCommand(this);
        GuiCommand delCampaignCmd = new DeleteCampaignCommand(this);
        GuiCommand shareCmd = new ShareVisibilityCommand(this);
        GuiCommand addEventCmd = new AddEventCommand(this);
        GuiCommand delEventCmd = new DeleteEventCommand(this);
        GuiCommand settingsCmd = new OpenSettingsCommand(this);

        addCampaignBtn.setOnAction(e -> addCampaignCmd.execute());
        delCampaignBtn.setOnAction(e -> delCampaignCmd.execute());
        shareBtn.setOnAction(e -> shareCmd.execute());
        addEventBtn.setOnAction(e -> addEventCmd.execute());
        delEventBtn.setOnAction(e -> delEventCmd.execute());
        settingsBtn.setOnAction(e -> settingsCmd.execute());

        HBox buttons = new HBox(10, addCampaignBtn, delCampaignBtn, shareBtn, addEventBtn, delEventBtn, settingsBtn);
        buttons.setPadding(new Insets(10));

        VBox left = new VBox(6, new Label("Campaigns"), campaignList);
        VBox right = new VBox(6, new Label("Events"), eventList);

        SplitPane split = new SplitPane(left, right);
        split.setDividerPositions(0.33);

        BorderPane root = new BorderPane();
        root.setCenter(split);
        root.setBottom(buttons);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 900, 520));
        // Apply theme once at startup
        applyTheme(stage);

        stage.setTitle("GuildQuest (GUI)");
        stage.show();
    }

    // ---- CampaignObserver (A3) ----

    @Override
    public void onCampaignChanged(Campaign campaign) {
        Platform.runLater(() -> {
            if (campaignList != null) campaignList.refresh();
        });
    }

    @Override
    public void onEventChanged(Campaign campaign, QuestEvent event) {
        Platform.runLater(() -> {
            if (campaignList == null) return;
            Campaign selected = campaignList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            if (selected.getCampaignId() != campaign.getCampaignId()) return;

            events.setAll(campaign.getEvents());
            if (eventList != null) eventList.refresh();
        });
    }

    // ---- Command targets (AI-assisted pattern) ----

    public void handleAddCampaign() {
        String name = prompt("Campaign name:", "New Campaign");
        if (name == null) return;
        Campaign c = new Campaign(nextCampaignId(), name, activeUser);
        c.addObserver(this);
        campaigns.add(c);
    }

    public void handleDeleteCampaign() {
        Campaign selected = campaignList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        selected.removeObserver(this);
        campaigns.remove(selected);
        events.clear();
    }

    public void handleShareVisibility() {
        Campaign selected = campaignList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Pick a campaign first.");
            return;
        }
        openShareDialog(selected);
        // no manual refresh: observer notifications handle it
    }

    public void handleOpenSettings() {
        if (openSettingsDialog(defaultRealmRef)) {
            syncDisplayStrategyFromSettings();
            applyTheme(stageRef);
            if (eventList != null) eventList.refresh();
        }
    }

    public void handleAddEvent() {
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

        Realm realm = new Realm(realmName, offset);

        // Parameter Object refactoring (A3)
        EventData data = new EventData(title, new GlobalTime(startMin), endTime, realm);
        if (!data.isValid()) {
            alert("Invalid event: " + data.getValidationError());
            return;
        }

        QuestEvent ev = selected.addEvent(data);
        // no manual events.add: observer callback refreshes the list
        eventList.getSelectionModel().select(ev);
    }

    public void handleDeleteEvent() {
        Campaign selectedCampaign = campaignList.getSelectionModel().getSelectedItem();
        QuestEvent selectedEvent = eventList.getSelectionModel().getSelectedItem();
        if (selectedCampaign == null || selectedEvent == null) return;

        selectedCampaign.deleteEvent(selectedEvent.getEventId());
        // no manual events.remove: observer callback refreshes the list
    }

    private void syncDisplayStrategyFromSettings() {
        TimeDisplayPreference pref = activeUser.getSettings().getTimeDisplayPreference();
        if (pref == null) pref = TimeDisplayPreference.BOTH;

        switch (pref) {
            case WORLD -> eventDisplay.set(new WorldTimeStrategy());
            case LOCAL -> eventDisplay.set(new LocalTimeStrategy());
            case BOTH -> eventDisplay.set(new BothTimeStrategy());
        }
    }

    private void applyTheme(Stage stage) {
        if (stage.getScene() == null) return;
        Theme theme = activeUser.getSettings().getTheme();
        if (theme == null) theme = Theme.CLASSIC;

        // Lightweight styling (no external CSS) — “classic vs modern” per spec
        if (theme == Theme.MODERN) {
            stage.getScene().getRoot().setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-font-family: 'System';" +
                            "-fx-background-color: white;"
            );
        } else {
            stage.getScene().getRoot().setStyle(
                    "-fx-font-size: 12px;" +
                            "-fx-font-family: 'System';" +
                            "-fx-background-color: #f7f7f7;"
            );
        }
    }

    /**
     * Minimal Settings UI (subset):
     * - current realm
     * - theme
     * - time display preference
     */
    private boolean openSettingsDialog(Realm defaultRealm) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText("User Settings");

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Theme
        ComboBox<Theme> themeBox = new ComboBox<>(FXCollections.observableArrayList(Theme.values()));
        themeBox.getSelectionModel().select(activeUser.getSettings().getTheme());

        // Time preference
        ComboBox<TimeDisplayPreference> timeBox = new ComboBox<>(FXCollections.observableArrayList(TimeDisplayPreference.values()));
        timeBox.getSelectionModel().select(activeUser.getSettings().getTimeDisplayPreference());

        // Realm (simple: name + offset)
        Realm currentRealm = activeUser.getSettings().getCurrentRealm();
        if (currentRealm == null) currentRealm = defaultRealm;

        TextField realmName = new TextField(currentRealm.getName());
        TextField realmOffset = new TextField(String.valueOf(currentRealm.getOffsetMinutes()));

        grid.addRow(0, new Label("Theme:"), themeBox);
        grid.addRow(1, new Label("Time display:"), timeBox);
        grid.addRow(2, new Label("Current realm name:"), realmName);
        grid.addRow(3, new Label("Realm offset (minutes):"), realmOffset);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != save) return false;

        // Apply
        Theme chosenTheme = themeBox.getSelectionModel().getSelectedItem();
        TimeDisplayPreference chosenPref = timeBox.getSelectionModel().getSelectedItem();

        String rn = realmName.getText() == null ? "" : realmName.getText().trim();
        if (rn.isEmpty()) rn = "Earth";

        int off;
        try {
            off = Integer.parseInt(realmOffset.getText().trim());
        } catch (Exception ex) {
            alert("Realm offset must be an integer.");
            return false;
        }

        activeUser.getSettings().setTheme(chosenTheme == null ? Theme.CLASSIC : chosenTheme);
        activeUser.getSettings().setTimeDisplayPreference(chosenPref == null ? TimeDisplayPreference.BOTH : chosenPref);
        activeUser.getSettings().setCurrentRealm(new Realm(rn, off));
        return true;
    }

    /**
     * Minimal sharing/visibility UI (subset):
     * - toggle public/private
     * - share with a user (view-only or collaborative)
     */
    private void openShareDialog(Campaign campaign) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Campaign Sharing");
        dialog.setHeaderText("Visibility + Sharing for: " + campaign.getName());

        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(close);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // Visibility
        ComboBox<Visibility> visBox = new ComboBox<>(FXCollections.observableArrayList(Visibility.values()));
        visBox.getSelectionModel().select(campaign.getVisibility());
        visBox.setOnAction(e -> campaign.setVisibility(visBox.getSelectionModel().getSelectedItem()));

        HBox visRow = new HBox(10, new Label("Visibility:"), visBox);

        // Current shares
        ListView<String> shareList = new ListView<>();
        shareList.setPrefHeight(140);

        Runnable refreshShares = () -> {
            shareList.getItems().clear();
            campaign.getSharedWith().forEach((u, p) -> shareList.getItems().add(u.getName() + " — " + p.name()));
        };
        refreshShares.run();

        // Add share controls
        ObservableList<User> shareTargets = FXCollections.observableArrayList(allUsers);
        shareTargets.remove(activeUser);

        ComboBox<User> userBox = new ComboBox<>(shareTargets);
        userBox.setPromptText("Select user");

        ComboBox<Permission> permBox = new ComboBox<>(FXCollections.observableArrayList(Permission.values()));
        permBox.getSelectionModel().select(Permission.VIEW_ONLY);

        Button addShare = new Button("Share");
        Button removeShare = new Button("Unshare");

        addShare.setOnAction(e -> {
            User u = userBox.getSelectionModel().getSelectedItem();
            if (u == null) return;
            Permission p = permBox.getSelectionModel().getSelectedItem();
            campaign.shareWith(u, p);
            refreshShares.run();
        });

        removeShare.setOnAction(e -> {
            String selected = shareList.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            String userName = selected.split(" — ")[0].trim();

            User target = null;
            for (User u : allUsers) {
                if (u.getName().equals(userName)) {
                    target = u;
                    break;
                }
            }
            if (target != null) {
                campaign.unshare(target);
                refreshShares.run();
            }
        });

        HBox addRow = new HBox(10, userBox, permBox, addShare, removeShare);

        root.getChildren().addAll(
                visRow,
                new Label("Shared with:"),
                shareList,
                new Separator(),
                new Label("Add / update share:"),
                addRow
        );

        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
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
