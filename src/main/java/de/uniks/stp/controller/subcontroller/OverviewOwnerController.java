package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class OverviewOwnerController {
    private final Parent view;
    private final ModelBuilder builder;
    private Label serverName;
    private Button deleteServer;
    private Button changeName;
    private TextField nameText;

    public OverviewOwnerController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        this.serverName = (Label) view.lookup("#serverName");
        this.deleteServer = (Button) view.lookup("#deleteServer");
        this.changeName = (Button) view.lookup("#changeName");
        this.nameText = (TextField) view.lookup("#nameText");

        deleteServer.setOnAction(this::onDeleteServerClicked);
        changeName.setOnAction(this::onChangeNameClicked);
    }

    private void onChangeNameClicked(ActionEvent actionEvent) {
        builder.getCurrentServer().setName(nameText.getText());
    }

    private void onDeleteServerClicked(ActionEvent actionEvent) {
        //Platform.runLater(() -> {
        ButtonType button = new ButtonType("Delete Server");
        ButtonType button2 = new ButtonType("Cancel");
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", button, button2);
        alert.setTitle("Settings");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().remove("alert");
        ButtonBar buttonBar = (ButtonBar) alert.getDialogPane().lookup(".button-bar");
        GridPane grid = (GridPane) dialogPane.lookup(".header-panel");
        alert.setHeaderText("Warning!\n" +
                "Do you want to delete this server\n" +
                "all Data information of the Server will be lost ");
        buttonBar.setStyle("-fx-font-size: 14px;" +
                "-fx-text-fill: white;"
                + "-fx-background-color: indianred;");
        buttonBar.getButtons().get(0).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");
        buttonBar.getButtons().get(1).setStyle("-fx-background-color: red;" + "-fx-text-fill: white;");

        //alert.getDialogPane().getScene().getStylesheets().add("styles/AlertStyle.css");
        dialogPane.getStylesheets().add(
                StageManager.class.getResource("styles/AlertStyle.css").toExternalForm());
        dialogPane.getStyleClass().add("AlertStyle");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            //delete server
        }
    }


}
