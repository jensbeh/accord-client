package de.uniks.stp.util;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class Alerts {

    public static void invalidNameAlert(ModelBuilder builder) {
        try {
            ResourceBundle lang = builder.getStageManager().getLangBundle();

            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("alert/InfoBox.fxml")), builder.getStageManager().getLangBundle());
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.initOwner(builder.getStageManager().getHomeViewController().getHomeView().getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            // DropShadow of Scene
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/DropShadow/DropShadow.css")).toExternalForm());

            // create titleBar
            HBox titleBarBox = (HBox) root.lookup("#titleBarBox");
            Parent titleBarView = null;
            try {
                titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), builder.getStageManager().getLangBundle());
            } catch (IOException e) {
                e.printStackTrace();
            }
            titleBarBox.getChildren().add(titleBarView);
            TitleBarController titleBarController = new TitleBarController(stage, titleBarView, builder);
            titleBarController.init();
            titleBarController.setTheme();
            titleBarController.setMaximizable(false);
            titleBarController.setTitle(lang.getString("label.warning"));
            stage.setTitle(lang.getString("label.warning"));

            stage.setScene(scene);
            stage.show();

            Label serverDeletedLabel = (Label) root.lookup("#label_info");
            serverDeletedLabel.setText(builder.getStageManager().getLangBundle().getString("warning.invalidName"));
            Button okButton = (Button) root.lookup("#button_OK");
            okButton.setOnAction((a) -> {
                stage.close();
            });
            if (builder.getTheme().equals("Bright")) {
                root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
            } else {
                root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
