package guildquest.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuiMain extends Application {
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(12);
        root.setStyle("-fx-padding: 16;");
        root.getChildren().addAll(
                new Label("GuildQuest (GUI)"),
                new Label("GUI booted successfully.")
        );

        stage.setTitle("GuildQuest");
        stage.setScene(new Scene(root, 520, 220));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
