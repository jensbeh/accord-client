package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.Alerts;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kong.unirest.JsonNode;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OverviewOwnerController {
    private final Parent view;
    private final ModelBuilder builder;
    private final RestClient restClient;
    private Label serverName;
    private TextField nameText;
    private Stage stage;

    public OverviewOwnerController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = modelBuilder.getRestClient();
    }

    public void init() {
        this.serverName = (Label) view.lookup("#serverName");
        serverName.setText(builder.getCurrentServer().getName());
        Button deleteServer = (Button) view.lookup("#deleteServer");
        Button changeName = (Button) view.lookup("#serverChangeButton");
        this.nameText = (TextField) view.lookup("#nameText");
        //Buttons
        deleteServer.setOnAction(this::onDeleteServerClicked);
        changeName.setOnAction(this::onChangeNameClicked);
    }

    /**
     * Changes name of current server
     */
    private void onChangeNameClicked(ActionEvent actionEvent) {
        Matcher whiteSpaceMatcher = Pattern.compile("^( )*$").matcher(nameText.getText());
        if (!whiteSpaceMatcher.find()) {
            this.serverName.setText(nameText.getText());
            builder.getCurrentServer().setName(nameText.getText());
            restClient.putServer(builder.getCurrentServer().getId(), builder.getCurrentServer().getName(), builder.getPersonalUser().getUserKey(),
                    response -> builder.getCurrentServer().setName(nameText.getText()));
        } else {
            Alerts.invalidNameAlert(builder);
        }
    }

    /**
     * Deletes current server and shows homeView with webSocket
     */
    private void onDeleteServerClicked(ActionEvent actionEvent) {
        try {
            ResourceBundle lang = builder.getStageManager().getLangBundle();

            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("alert/DeleteServer.fxml")), builder.getStageManager().getLangBundle());
            stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setResizable(false);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.initOwner(nameText.getScene().getWindow());
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

            Label deleteLabel = (Label) root.lookup("#label_deleteServer");
            deleteLabel.setText(lang.getString("warning.deleteServer"));
            Button deleteButton = (Button) root.lookup("#button_delete");
            Button cancelButton = (Button) root.lookup("#button_cancel");
            deleteButton.setText(lang.getString("button.deleteServer"));
            cancelButton.setText(lang.getString("button.cancelDeleteServer"));
            deleteButton.setOnAction(this::deleteServer);
            cancelButton.setOnAction((a) -> stage.close());
            if (builder.getTheme().equals("Bright")) {
                root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
            } else {
                root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteServer(ActionEvent actionEvent) {
        // disconnect from audioChannel
        if (builder.getAudioStreamClient() != null && builder.getCurrentServer() == builder.getCurrentAudioChannel().getCategories().getServer()) {
            builder.getServerSystemWebSocket().getServerViewController().onAudioDisconnectClicked(new ActionEvent());
        }
        //delete server
        restClient.deleteServer(builder.getCurrentServer().getId(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            System.out.println("Overview controller: " + body.toString());
            String status = body.getObject().getString("status");
            System.out.println("status: " + status);
        });
        Platform.runLater(() -> {
            Stage stage = (Stage) serverName.getScene().getWindow();
            stage.close();
        });
    }
}
