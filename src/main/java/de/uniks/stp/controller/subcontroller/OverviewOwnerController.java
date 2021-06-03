package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;

import java.util.Optional;

public class OverviewOwnerController {
    private final Parent view;
    private final ModelBuilder builder;
    private Label serverName;
    private Button deleteServer;
    private Button changeName;
    private TextField nameText;
    private RestClient restClient;

    public OverviewOwnerController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        restClient = new RestClient();
    }

    public void init() {
        this.serverName = (Label) view.lookup("#serverName");
        serverName.setText(builder.getCurrentServer().getName());
        this.deleteServer = (Button) view.lookup("#deleteServer");
        this.changeName = (Button) view.lookup("#changeName");
        this.nameText = (TextField) view.lookup("#nameText");
        nameText.setStyle("-fx-text-fill: white;" + "-fx-background-color:  #333333;");
        //Buttons
        deleteServer.setOnAction(this::onDeleteServerClicked);
        changeName.setOnAction(this::onChangeNameClicked);
    }

    /**
     * Changes name of current server
     */
    private void onChangeNameClicked(ActionEvent actionEvent) {
        builder.getCurrentServer().setName(nameText.getText());
        restClient.putServer(builder.getCurrentServer().getId(), builder.getCurrentServer().getName(), builder.getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
        });
    }

    /**
     * Deletes current server and shows homeview
     */
    private void onDeleteServerClicked(ActionEvent actionEvent) {
        ButtonType button = new ButtonType("Delete Server");
        ButtonType button2 = new ButtonType("Cancel");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", button, button2);
        alert.setTitle("Settings");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().remove("alert");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        alert.setHeaderText("Warning!\n" +
                "Do you want to delete this server\n" +
                "all Data information of the Server will be lost ");
        buttonBar.setStyle("-fx-font-size: 14px;" +
                "-fx-text-fill: white;"
                + "-fx-background-color: indianred;");
        buttonBar.getButtons().get(0).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");
        buttonBar.getButtons().get(1).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");
        dialogPane.getStylesheets().add(
                StageManager.class.getResource("styles/AlertStyle.css").toExternalForm());
        dialogPane.getStyleClass().add("AlertStyle");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == button) {
            //delete server
            restClient.deleteServer(builder.getCurrentServer().getId(), builder.getPersonalUser().getUserKey(), response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                System.out.println("status: " + status);
                builder.getPersonalUser().getServer().remove(builder.getCurrentServer());
            });
            StageManager.showHome();
        }
    }


}
