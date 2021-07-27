package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kong.unirest.JsonNode;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class OverviewOwnerController {
    private final Parent view;
    private final ModelBuilder builder;
    private Label serverName;
    private TextField nameText;
    private final RestClient restClient;

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
        this.serverName.setText(nameText.getText());
        builder.getCurrentServer().setName(nameText.getText());
        restClient.putServer(builder.getCurrentServer().getId(), builder.getCurrentServer().getName(), builder.getPersonalUser().getUserKey(), response -> {
            builder.getCurrentServer().setName(nameText.getText());
        });
    }

    /**
     * Deletes current server and shows homeView with webSocket
     */
    private void onDeleteServerClicked(ActionEvent actionEvent) {
        ResourceBundle lang = StageManager.getLangBundle();
        ButtonType button = new ButtonType(lang.getString("button.deleteServer"));
        ButtonType button2 = new ButtonType(lang.getString("button.cancel"));
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", button, button2);
        alert.setTitle(lang.getString("window_title_serverSettings"));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().remove("alert");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        alert.setHeaderText(lang.getString("warning.deleteServer"));
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.close());

        if (builder.getTheme().equals("Bright")) {
            dialogPane.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Alert.css")).toExternalForm());
            buttonBar.setStyle("-fx-font-size: 14px;" +
                    "-fx-text-fill: BLACK;"
                    + "-fx-background-color: WHITE;");
            buttonBar.getButtons().get(0).setStyle("-fx-background-color: #7987ff;" + "-fx-text-fill: white;");
            buttonBar.getButtons().get(1).setStyle("-fx-background-color: #7987ff;" + "-fx-text-fill: white;");
        } else {
            dialogPane.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Alert.css")).toExternalForm());
            buttonBar.setStyle("-fx-font-size: 14px;" +
                    "-fx-text-fill: white;"
                    + "-fx-background-color: #2f3136;");
            buttonBar.getButtons().get(0).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");
            buttonBar.getButtons().get(1).setStyle("-fx-background-color: #727272;" + "-fx-text-fill: white;");
        }
        dialogPane.getStyleClass().add("AlertStyle");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == button) {
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
}
