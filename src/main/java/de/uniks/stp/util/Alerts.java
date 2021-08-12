package de.uniks.stp.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ResourceBundle;

public class Alerts {

    public static void invalidNameAlert(ModelBuilder builder) {
        ResourceBundle lang = StageManager.getLangBundle();
        ButtonType button = new ButtonType("Ok");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", button);
        alert.setTitle(lang.getString("window_title_serverSettings"));

        Stage stageIcon = (Stage) alert.getDialogPane().getScene().getWindow();
        stageIcon.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().remove("alert");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        alert.setHeaderText(lang.getString("warning.invalidName"));
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.close());

        if (builder.getTheme().equals("Bright")) {
            dialogPane.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
            buttonBar.setStyle("-fx-font-size: 14px;" +
                    "-fx-text-fill: BLACK;"
                    + "-fx-background-color: WHITE;");
            buttonBar.getButtons().get(0).setStyle("-fx-background-color: #7da6df;" + "-fx-text-fill: white;");
        } else {
            dialogPane.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
            buttonBar.setStyle("-fx-font-size: 14px;" +
                    "-fx-text-fill: white;"
                    + "-fx-background-color: #2f3136;");
            buttonBar.getButtons().get(0).setStyle("-fx-background-color: #727272;" + "-fx-text-fill: white;");
        }
        dialogPane.getStyleClass().add("AlertStyle");
        alert.showAndWait();
    }
}
