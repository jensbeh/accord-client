package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class OverviewController {
    private final Parent view;
    private final ModelBuilder builder;
    private final RestClient restClient;
    private Label serverName;
    private VBox root;

    public OverviewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = modelBuilder.getRestClient();
    }

    public void init() {
        root = (VBox) view.lookup("#rootOverview");
        this.serverName = (Label) view.lookup("#serverName");
        serverName.setText(builder.getCurrentServer().getName());
        Button leaveServer = (Button) view.lookup("#leaveServer");
        //Buttons
        leaveServer.setOnAction(this::onLeaveServerClicked);
    }

    /**
     * User leaves the current server with webSocket
     */
    private void onLeaveServerClicked(ActionEvent actionEvent) {
        restClient.postServerLeave(builder.getCurrentServer().getId(), builder.getPersonalUser().getUserKey(), response -> builder.getPersonalUser().getServer().remove(builder.getCurrentServer()));
        Platform.runLater(() -> {
            Stage stage = (Stage) serverName.getScene().getWindow();
            stage.close();
        });
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/bright/ServerSettings.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/de/uniks/stp/themes/dark/ServerSettings.css")).toExternalForm());
    }
}
