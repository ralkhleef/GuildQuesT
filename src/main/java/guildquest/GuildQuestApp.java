package guildquest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import guildquest.model.Campaign;
import guildquest.model.GlobalTime;
import guildquest.model.QuestEvent;
import guildquest.model.TimelineView;
import guildquest.model.User;
import guildquest.model.Visibility;
import guildquest.model.WorldClock;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuildQuestApp extends Application {

    // ===== MULTI-USER SUPPORT (visible in GUI) =====
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private User activeUser;
    private ComboBox<User> userBox;

    // These lists are what the ListViews display (reloaded from active user)
    private final ObservableList<Campaign> campaigns = FXCollections.observableArrayList();
    private final ObservableList<QuestEvent> eventsForSelected = FXCollections.observableArrayList();

    // Start at Day 0, 06:00 (360 minutes)
    private final WorldClock clock = new WorldClock(new GlobalTime(360));

    private Label timeLabel;

    private ListView<Campaign> campaignList;
    private ListView<QuestEvent> eventList;
    private ComboBox<String> filterBox;
    private CheckBox hideArchivedBox;

    private Label detailsTitle;
    private Label detailsTime;
    private Label detailsRealm;
    private TextArea detailsParticipants;
    private TextArea detailsItems;

    private enum EventAction { SAVE, DELETE }

    private static class EventDialogResult {
        final EventAction action;
        final QuestEvent event;
        EventDialogResult(EventAction action, QuestEvent event) {
            this.action = action;
            this.event = event;
        }
    }

    private static class CampaignDraft {
        final String name;
        final Visibility visibility;
        CampaignDraft(String name, Visibility visibility) {
            this.name = name;
            this.visibility = visibility;
        }
    }

    private static class CampaignEditDraft {
        final String name;
        final Visibility visibility;
        final boolean archived;
        CampaignEditDraft(String name, Visibility visibility, boolean archived) {
            this.name = name;
            this.visibility = visibility;
            this.archived = archived;
        }
    }

    @Override
    public void start(Stage stage) {
        timeLabel = new Label();
        updateTimeLabel();

        campaignList = new ListView<>(campaigns);
        eventList = new ListView<>(eventsForSelected);

        // ===== User dropdown =====
        userBox = new ComboBox<>(users);
        userBox.setMaxWidth(200);

        // Show username in dropdown
        userBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getUsername());
            }
        });
        userBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getUsername());
            }
        });

        // Campaign list cell formatting
        campaignList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Campaign c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                } else {
                    String vis = (c.getVisibility() == null) ? "PRIVATE" : c.getVisibility().name();
                    String arch = c.isArchived() ? " • archived" : "";
                    setText(c.getName() + "  [" + vis + "]" + arch);
                }
            }
        });

        // Event list cell formatting (two lines)
        eventList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(QuestEvent e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) {
                    setText(null);
                } else {
                    String end = (e.getEndTime() == null) ? "(none)" : e.getEndTime().toString();
                    setText("#" + e.getEventId() + "  " + e.getTitle() + "\n" +
                            e.getStartTime() + " → " + end);
                }
            }
        });

        // ===== Buttons =====
        Button newCampaignBtn = new Button("New Campaign");
        Button editCampaignBtn = new Button("Edit Campaign");

        hideArchivedBox = new CheckBox("Hide archived");
        hideArchivedBox.setSelected(true);

        Button addEventBtn = new Button("Add Event");
        Button editEventBtn = new Button("Edit Event");

        Button advanceTimeBtn = new Button("Advance +60 min");
        Button resetTimeBtn = new Button("Reset Time");

        makeFullWidth(newCampaignBtn, editCampaignBtn, addEventBtn, editEventBtn);
        setButtonHeights(34, newCampaignBtn, editCampaignBtn, addEventBtn, editEventBtn, advanceTimeBtn, resetTimeBtn);

        // ===== Filter =====
        filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Today", "This Week");
        filterBox.setValue("All");
        filterBox.setMaxWidth(Double.MAX_VALUE);

        // ===== Details panel =====
        detailsTitle = new Label("Select an event");
        detailsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        detailsTime = new Label("");
        detailsRealm = new Label("");
        detailsTime.setWrapText(true);
        detailsRealm.setWrapText(true);

        detailsParticipants = new TextArea();
        detailsParticipants.setEditable(false);
        detailsParticipants.setWrapText(true);
        detailsParticipants.setPrefRowCount(2);
        detailsParticipants.setMaxHeight(72);

        detailsItems = new TextArea();
        detailsItems.setEditable(false);
        detailsItems.setWrapText(true);
        detailsItems.setPrefRowCount(2);
        detailsItems.setMaxHeight(72);

        VBox detailsBox = new VBox(10,
                sectionTitle("Event Details"),
                detailsTitle,
                detailsTime,
                detailsRealm,
                new Separator(),
                smallLabel("Participants"),
                detailsParticipants,
                smallLabel("Items"),
                detailsItems
        );
        detailsBox.setPadding(new Insets(12));
        detailsBox.setStyle("""
                -fx-background-color: #f7f7f8;
                -fx-border-color: #e6e6ea;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);

        // ===== Left (Campaigns) =====
        VBox campaignButtons = new VBox(8, newCampaignBtn, editCampaignBtn);
        campaignButtons.setFillWidth(true);

        VBox left = new VBox(10,
                sectionTitle("Campaigns"),
                campaignList,
                campaignButtons,
                hideArchivedBox
        );
        left.setPadding(new Insets(12));
        VBox.setVgrow(campaignList, Priority.ALWAYS);
        left.setStyle("""
                -fx-background-color: #fbfbfc;
                -fx-border-color: #e6e6ea;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);

        // ===== Middle (Events) =====
        VBox eventButtons = new VBox(8, addEventBtn, editEventBtn);
        eventButtons.setFillWidth(true);

        VBox middle = new VBox(10,
                sectionTitle("Events"),
                filterBox,
                eventList,
                eventButtons
        );
        middle.setPadding(new Insets(12));
        VBox.setVgrow(eventList, Priority.ALWAYS);
        middle.setStyle("""
                -fx-background-color: #ffffff;
                -fx-border-color: #e6e6ea;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                """);

        // ===== SplitPane =====
        SplitPane split = new SplitPane(left, middle, detailsBox);
        split.setPadding(new Insets(12, 0, 0, 0));
        split.setDividerPositions(0.26, 0.66);

        left.setMinWidth(260);
        middle.setMinWidth(380);
        detailsBox.setMinWidth(320);

        // ===== Top bar =====
        Label userLabel = new Label("User:");
        Label worldLabel = new Label("World Time:");

        HBox top = new HBox(12,
                userLabel, userBox,
                new Separator(),
                worldLabel, timeLabel,
                spacer(),
                advanceTimeBtn, resetTimeBtn
        );
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        top.setStyle("""
                -fx-background-color: #111827;
                -fx-background-radius: 10;
                """);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        worldLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        advanceTimeBtn.setStyle("-fx-background-color: white;");
        resetTimeBtn.setStyle("-fx-background-color: white;");

        // ===== Root =====
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(top);
        root.setCenter(split);

        // ===== Behavior =====
        userBox.setOnAction(e -> {
            User u = userBox.getSelectionModel().getSelectedItem();
            if (u == null) return;
            activeUser = u;
            refreshCampaignList();
            updateDetails(null);
        });

        campaignList.getSelectionModel().selectedItemProperty().addListener((obs, oldC, newC) -> {
            refreshEventList();
            updateDetails(null);
        });

        eventList.getSelectionModel().selectedItemProperty().addListener((obs, oldE, newE) -> updateDetails(newE));

        filterBox.setOnAction(e -> refreshEventList());
        hideArchivedBox.setOnAction(e -> refreshCampaignList());

        newCampaignBtn.setOnAction(e -> onNewCampaign());
        editCampaignBtn.setOnAction(e -> onEditCampaign());

        addEventBtn.setOnAction(e -> onAddEvent());
        editEventBtn.setOnAction(e -> onEditEvent());

        advanceTimeBtn.setOnAction(e -> {
            clock.advance(60);
            updateTimeLabel();
            refreshEventList();
        });

        resetTimeBtn.setOnAction(e -> {
            clock.resetTime();
            updateTimeLabel();
            refreshEventList();
        });

        // ===== Starter data: MULTIPLE USERS in GUI =====
        seedUsersAndCampaigns();

        userBox.getSelectionModel().select(activeUser);
        refreshCampaignList();
        campaignList.getSelectionModel().selectFirst();

        // ===== Stage sizing =====
        stage.setTitle("GuildQuest (JavaFX)");
        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.show();
    }

    private void seedUsersAndCampaigns() {
        // Users
        User u1 = new User(1, "John");
        User u2 = new User(2, "Jane");
        User u3 = new User(3, "Alex");
        users.addAll(u1, u2, u3);

        // --- John’s campaigns ---
        Campaign c1 = u1.createCampaign(1, "ICS 46 Study Sprint");
        u1.setCampaignVisibility(1, Visibility.PRIVATE);

        QuestEvent e1 = new QuestEvent(c1.nextEventId(), "Office Hours: Linked Lists", new GlobalTime(90), null);
        e1.changeRealm("UCI - ICS Building");
        e1.addParticipant("John");
        e1.addParticipant("TA");
        e1.addItem("Notes PDF");

        QuestEvent e2 = new QuestEvent(c1.nextEventId(), "Quiz Prep Session", new GlobalTime(420), new GlobalTime(480));
        e2.changeRealm("UCI - Science Library");
        e2.addParticipant("John");
        e2.addItem("Practice Problems");

        c1.addEvent(e1);
        c1.addEvent(e2);

        // --- Jane’s campaigns ---
        Campaign c2 = u2.createCampaign(1, "INF 122 GuildQuest Demo");
        u2.setCampaignVisibility(1, Visibility.PUBLIC);

        QuestEvent e3 = new QuestEvent(c2.nextEventId(), "Milestone: GUI Checklist", new GlobalTime(300), null);
        e3.changeRealm("UCI - Student Center");
        e3.addParticipant("Jane");
        e3.addParticipant("Partner");
        e3.addItem("Rubric");
        e3.addItem("Revised UML");

        c2.addEvent(e3);

        // --- Alex’s campaigns ---
        Campaign c3 = u3.createCampaign(1, "Project Meeting Tracker");
        u3.setCampaignVisibility(1, Visibility.PRIVATE);
        c3.archive(); // show archived example

        QuestEvent e4 = new QuestEvent(c3.nextEventId(), "Team Sync", new GlobalTime(600), new GlobalTime(660));
        e4.changeRealm("UCI - Engineering Hall");
        e4.addParticipant("Alex");
        e4.addItem("Agenda");
        c3.addEvent(e4);

        activeUser = u1;
    }

    // ===================== Actions =====================

    private void onNewCampaign() {
        if (activeUser == null) {
            alert("Select a user first.");
            return;
        }

        Optional<CampaignDraft> draft = showNewCampaignDialog();
        if (draft.isEmpty()) return;

        CampaignDraft d = draft.get();
        if (d == null) {
            alert("Please enter a campaign name.");
            return;
        }

        int newId = nextCampaignIdForActiveUser();
        Campaign c = activeUser.createCampaign(newId, d.name);
        activeUser.setCampaignVisibility(newId, d.visibility == null ? Visibility.PRIVATE : d.visibility);

        refreshCampaignList();
        campaignList.getSelectionModel().select(c);
    }

    private void onEditCampaign() {
        if (activeUser == null) {
            alert("Select a user first.");
            return;
        }

        Campaign selected = campaignList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Select a campaign first.");
            return;
        }

        Optional<CampaignEditDraft> draft = showEditCampaignDialog(selected);
        if (draft.isEmpty()) return;

        CampaignEditDraft d = draft.get();
        if (d == null) {
            alert("Please enter a campaign name.");
            return;
        }

        activeUser.renameCampaign(selected.getCampaignId(), d.name);
        activeUser.setCampaignVisibility(selected.getCampaignId(), d.visibility == null ? Visibility.PRIVATE : d.visibility);

        if (d.archived) activeUser.archiveCampaign(selected.getCampaignId());
        else selected.unarchive();

        refreshCampaignList();
        campaignList.refresh();
    }

    private void onAddEvent() {
        Campaign selected = campaignList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Select a campaign first.");
            return;
        }

        Optional<EventDialogResult> resultOpt = showEventDialog("Add Event", null, selected.nextEventId(), false);
        if (resultOpt.isEmpty()) return;

        EventDialogResult result = resultOpt.get();
        if (result == null || result.action != EventAction.SAVE || result.event == null) {
            alert("Please fill out Title and a valid Start time.");
            return;
        }

        selected.addEvent(result.event);
        refreshEventList();
    }

    private void onEditEvent() {
        Campaign selectedCampaign = campaignList.getSelectionModel().getSelectedItem();
        QuestEvent selectedEvent = eventList.getSelectionModel().getSelectedItem();

        if (selectedCampaign == null || selectedEvent == null) {
            alert("Select a campaign and an event first.");
            return;
        }

        Optional<EventDialogResult> resultOpt =
                showEventDialog("Edit Event", selectedEvent, selectedEvent.getEventId(), true);

        if (resultOpt.isEmpty()) return;

        EventDialogResult result = resultOpt.get();
        if (result == null) {
            alert("Please fill out Title and a valid Start time.");
            return;
        }

        if (result.action == EventAction.DELETE) {
            if (confirm("Delete this event?")) {
                selectedCampaign.removeEventById(selectedEvent.getEventId());
                refreshEventList();
                updateDetails(null);
            }
            return;
        }

        selectedCampaign.updateEvent(result.event);
        refreshEventList();
    }

    // ===================== Campaign dialogs =====================

    private Optional<CampaignDraft> showNewCampaignDialog() {
        Dialog<CampaignDraft> dialog = new Dialog<>();
        dialog.setTitle("New Campaign");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Campaign name");

        ComboBox<Visibility> visBox = new ComboBox<>();
        visBox.getItems().addAll(Visibility.PRIVATE, Visibility.PUBLIC);
        visBox.setValue(Visibility.PRIVATE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(150);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Visibility:"), visBox);

        nameField.setMaxWidth(Double.MAX_VALUE);
        visBox.setMaxWidth(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String name = nameField.getText().trim();
            if (name.isEmpty()) return null;
            return new CampaignDraft(name, visBox.getValue());
        });

        Optional<CampaignDraft> res = dialog.showAndWait();
        if (res.isEmpty()) return Optional.empty();
        return Optional.ofNullable(res.get());
    }

    private Optional<CampaignEditDraft> showEditCampaignDialog(Campaign c) {
        Dialog<CampaignEditDraft> dialog = new Dialog<>();
        dialog.setTitle("Edit Campaign");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(c.getName());
        nameField.setPromptText("Campaign name");

        ComboBox<Visibility> visBox = new ComboBox<>();
        visBox.getItems().addAll(Visibility.PRIVATE, Visibility.PUBLIC);
        visBox.setValue(c.getVisibility() == null ? Visibility.PRIVATE : c.getVisibility());

        CheckBox archivedBox = new CheckBox("Archived");
        archivedBox.setSelected(c.isArchived());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(150);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Visibility:"), visBox);
        grid.addRow(2, new Label(""), archivedBox);

        nameField.setMaxWidth(Double.MAX_VALUE);
        visBox.setMaxWidth(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String name = nameField.getText().trim();
            if (name.isEmpty()) return null;
            return new CampaignEditDraft(name, visBox.getValue(), archivedBox.isSelected());
        });

        Optional<CampaignEditDraft> res = dialog.showAndWait();
        if (res.isEmpty()) return Optional.empty();
        return Optional.ofNullable(res.get());
    }

    // ===================== Event dialog =====================

    private Optional<EventDialogResult> showEventDialog(String title, QuestEvent existing, int eventId, boolean allowDelete) {
        Dialog<EventDialogResult> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType okType = ButtonType.OK;
        ButtonType cancelType = ButtonType.CANCEL;

        ButtonType deleteType = null;
        if (allowDelete) {
            deleteType = new ButtonType("Delete", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(deleteType, okType, cancelType);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(okType, cancelType);
        }

        TextField titleField = new TextField(existing == null ? "" : existing.getTitle());
        titleField.setPromptText("Event title (e.g., Quiz Review)");

        List<Integer> days = new ArrayList<>();
        for (int i = 0; i <= 365; i++) days.add(i);

        List<Integer> hours = new ArrayList<>();
        for (int i = 0; i <= 23; i++) hours.add(i);

        List<Integer> minutes5 = new ArrayList<>();
        for (int i = 0; i <= 55; i += 5) minutes5.add(i);

        ComboBox<Integer> sDay = new ComboBox<>(FXCollections.observableArrayList(days));
        ComboBox<Integer> sHour = new ComboBox<>(FXCollections.observableArrayList(hours));
        ComboBox<Integer> sMin = new ComboBox<>(FXCollections.observableArrayList(minutes5));

        ComboBox<Integer> eDay = new ComboBox<>(FXCollections.observableArrayList(days));
        ComboBox<Integer> eHour = new ComboBox<>(FXCollections.observableArrayList(hours));
        ComboBox<Integer> eMin = new ComboBox<>(FXCollections.observableArrayList(minutes5));

        sDay.setMaxWidth(Double.MAX_VALUE);
        sHour.setMaxWidth(Double.MAX_VALUE);
        sMin.setMaxWidth(Double.MAX_VALUE);
        eDay.setMaxWidth(Double.MAX_VALUE);
        eHour.setMaxWidth(Double.MAX_VALUE);
        eMin.setMaxWidth(Double.MAX_VALUE);

        setCombosFromMinutes(sDay, sHour, sMin, roundTo5(clock.now().toMinutes()));

        CheckBox noEndBox = new CheckBox("No end time");
        noEndBox.setSelected(true);

        if (existing != null) {
            setCombosFromMinutes(sDay, sHour, sMin, roundTo5(existing.getStartTime().toMinutes()));
            if (existing.getEndTime() != null) {
                setCombosFromMinutes(eDay, eHour, eMin, roundTo5(existing.getEndTime().toMinutes()));
                noEndBox.setSelected(false);
            }
        }

        Button startPlus15Btn = new Button("+15 min");
        Button startPlus60Btn = new Button("+60 min");
        Button startPlusDayBtn = new Button("+1 day");

        startPlus15Btn.setOnAction(e -> setStartWithOffset(sDay, sHour, sMin, 15));
        startPlus60Btn.setOnAction(e -> setStartWithOffset(sDay, sHour, sMin, 60));
        startPlusDayBtn.setOnAction(e -> setStartWithOffset(sDay, sHour, sMin, 1440));

        Button endFromStart15Btn = new Button("Start +15");
        Button endFromStart60Btn = new Button("Start +60");
        Button endFromStartDayBtn = new Button("Start +1 day");

        endFromStart15Btn.setOnAction(e -> { noEndBox.setSelected(false); setEndFromStart(sDay, sHour, sMin, eDay, eHour, eMin, 15); });
        endFromStart60Btn.setOnAction(e -> { noEndBox.setSelected(false); setEndFromStart(sDay, sHour, sMin, eDay, eHour, eMin, 60); });
        endFromStartDayBtn.setOnAction(e -> { noEndBox.setSelected(false); setEndFromStart(sDay, sHour, sMin, eDay, eHour, eMin, 1440); });

        Runnable syncEndEnabled = () -> {
            boolean disabled = noEndBox.isSelected();
            eDay.setDisable(disabled);
            eHour.setDisable(disabled);
            eMin.setDisable(disabled);
            endFromStart15Btn.setDisable(disabled);
            endFromStart60Btn.setDisable(disabled);
            endFromStartDayBtn.setDisable(disabled);
        };
        noEndBox.setOnAction(e -> syncEndEnabled.run());
        syncEndEnabled.run();

        TextField realmField = new TextField(existing == null ? "" : safe(existing.getRealm()));
        realmField.setPromptText("Realm (optional)");

        TextArea participantsArea = new TextArea();
        participantsArea.setPromptText("Participants (one per line)\nExample:\nJohn\nJane");
        participantsArea.setPrefRowCount(5);

        TextArea itemsArea = new TextArea();
        itemsArea.setPromptText("Items (one per line)\nExample:\nNotes\nRubric");
        itemsArea.setPrefRowCount(5);

        if (existing != null) {
            participantsArea.setText(String.join("\n", existing.getParticipants()));
            itemsArea.setText(String.join("\n", existing.getItems()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(170);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        HBox startRow = new HBox(8, label("Day"), sDay, label("Hr"), sHour, label("Min"), sMin);
        startRow.setAlignment(Pos.CENTER_LEFT);

        HBox startPresets = new HBox(8, startPlus15Btn, startPlus60Btn, startPlusDayBtn);
        startPresets.setAlignment(Pos.CENTER_LEFT);

        HBox endRow = new HBox(8, label("Day"), eDay, label("Hr"), eHour, label("Min"), eMin);
        endRow.setAlignment(Pos.CENTER_LEFT);

        HBox endPresets = new HBox(8, endFromStart15Btn, endFromStart60Btn, endFromStartDayBtn);
        endPresets.setAlignment(Pos.CENTER_LEFT);

        grid.addRow(0, new Label("Title:"), titleField);
        grid.addRow(1, new Label("Start time:"), startRow);
        grid.addRow(2, new Label("Quick start:"), startPresets);

        grid.addRow(3, new Label("End time:"), endRow);
        grid.addRow(4, new Label(""), noEndBox);
        grid.addRow(5, new Label("Quick end:"), endPresets);

        grid.addRow(6, new Label("Realm:"), realmField);
        grid.addRow(7, new Label("Participants:"), participantsArea);
        grid.addRow(8, new Label("Items:"), itemsArea);

        titleField.setMaxWidth(Double.MAX_VALUE);
        realmField.setMaxWidth(Double.MAX_VALUE);

        dialog.getDialogPane().setContent(grid);

        ButtonType finalDeleteType = deleteType;
        dialog.setResultConverter(btn -> {
            if (finalDeleteType != null && btn == finalDeleteType) {
                return new EventDialogResult(EventAction.DELETE, null);
            }
            if (btn != okType) return null;

            String t = titleField.getText().trim();
            if (t.isEmpty()) return null;

            Integer sd = sDay.getValue(), sh = sHour.getValue(), sm = sMin.getValue();
            if (sd == null || sh == null || sm == null) return null;

            GlobalTime start = new GlobalTime(sd * 1440 + sh * 60 + sm);

            GlobalTime end = null;
            if (!noEndBox.isSelected()) {
                Integer ed = eDay.getValue(), eh = eHour.getValue(), em = eMin.getValue();
                if (ed == null || eh == null || em == null) return null;
                end = new GlobalTime(ed * 1440 + eh * 60 + em);
            }

            QuestEvent ev = new QuestEvent(eventId, t, start, end);

            String realm = realmField.getText().trim();
            if (!realm.isEmpty()) ev.changeRealm(realm);

            for (String p : splitLines(participantsArea.getText())) ev.addParticipant(p);
            for (String it : splitLines(itemsArea.getText())) ev.addItem(it);

            return new EventDialogResult(EventAction.SAVE, ev);
        });

        Optional<EventDialogResult> result = dialog.showAndWait();
        if (result.isEmpty()) return Optional.empty();
        return Optional.ofNullable(result.get());
    }

    // ===================== Refresh + Details =====================

    private void refreshCampaignList() {
        Campaign selected = campaignList.getSelectionModel().getSelectedItem();

        campaigns.clear();
        if (activeUser == null) return;

        for (Campaign c : activeUser.getCampaigns()) {
            if (hideArchivedBox != null && hideArchivedBox.isSelected() && c.isArchived()) continue;
            campaigns.add(c);
        }

        if (selected != null) {
            for (Campaign c : campaigns) {
                if (c.getCampaignId() == selected.getCampaignId()) {
                    campaignList.getSelectionModel().select(c);
                    break;
                }
            }
        }

        campaignList.refresh();
        refreshEventList();
    }

    private void refreshEventList() {
        Campaign selected = campaignList.getSelectionModel().getSelectedItem();
        eventsForSelected.clear();
        if (selected == null) return;

        TimelineView view = new TimelineView(selected);
        String mode = filterBox.getValue();

        if ("Today".equals(mode)) eventsForSelected.addAll(view.eventsDay(clock.now()));
        else if ("This Week".equals(mode)) eventsForSelected.addAll(view.eventsWeek(clock.now()));
        else eventsForSelected.addAll(view.eventsAll());
    }

    private void updateDetails(QuestEvent e) {
        if (e == null) {
            detailsTitle.setText("Select an event");
            detailsTime.setText("");
            detailsRealm.setText("");
            detailsParticipants.setText("");
            detailsItems.setText("");
            return;
        }

        detailsTitle.setText("#" + e.getEventId() + " " + e.getTitle());

        String timeStr = "Start: " + e.getStartTime()
                + (e.getEndTime() != null ? (" | End: " + e.getEndTime()) : " | End: (none)");
        detailsTime.setText(timeStr);

        detailsRealm.setText("Realm: " + safe(e.getRealm()));
        detailsParticipants.setText(String.join(", ", e.getParticipants()));
        detailsItems.setText(String.join(", ", e.getItems()));
    }

    private void updateTimeLabel() {
        timeLabel.setText(clock.now().toString());
    }

    // ===================== Small utilities =====================

    private int nextCampaignIdForActiveUser() {
        int max = 0;
        if (activeUser == null) return 1;
        for (Campaign c : activeUser.getCampaigns()) max = Math.max(max, c.getCampaignId());
        return max + 1;
    }

    private List<String> splitLines(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) return out;
        for (String line : text.split("\\R")) {
            String t = line.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private Label label(String s) {
        Label l = new Label(s);
        l.setStyle("-fx-opacity: 0.85;");
        return l;
    }

    private int roundTo5(int minutes) {
        int mod = minutes % 5;
        if (mod == 0) return minutes;
        return minutes + (5 - mod);
    }

    private void setCombosFromMinutes(ComboBox<Integer> day, ComboBox<Integer> hour, ComboBox<Integer> min, int total) {
        if (total < 0) total = 0;
        int d = total / 1440;
        int h = (total % 1440) / 60;
        int m = total % 60;
        m = (m / 5) * 5;
        day.setValue(Math.min(d, 365));
        hour.setValue(h);
        min.setValue(m);
    }

    private int getMinutesFromCombos(ComboBox<Integer> day, ComboBox<Integer> hour, ComboBox<Integer> min) {
        Integer d = day.getValue(), h = hour.getValue(), m = min.getValue();
        if (d == null || h == null || m == null) return -1;
        return d * 1440 + h * 60 + m;
    }

    private void setStartWithOffset(ComboBox<Integer> sDay, ComboBox<Integer> sHour, ComboBox<Integer> sMin, int offsetMinutes) {
        int base = roundTo5(clock.now().toMinutes());
        setCombosFromMinutes(sDay, sHour, sMin, base + offsetMinutes);
    }

    private void setEndFromStart(
            ComboBox<Integer> sDay, ComboBox<Integer> sHour, ComboBox<Integer> sMin,
            ComboBox<Integer> eDay, ComboBox<Integer> eHour, ComboBox<Integer> eMin,
            int offsetMinutes) {

        int start = getMinutesFromCombos(sDay, sHour, sMin);
        if (start < 0) return;
        setCombosFromMinutes(eDay, eHour, eMin, start + offsetMinutes);
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    // ===================== UI helpers =====================

    private void makeFullWidth(Button... buttons) {
        for (Button b : buttons) b.setMaxWidth(Double.MAX_VALUE);
    }

    private void setButtonHeights(double h, Button... buttons) {
        for (Button b : buttons) b.setPrefHeight(h);
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        return l;
    }

    private Label smallLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-opacity: 0.85;");
        return l;
    }

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
}