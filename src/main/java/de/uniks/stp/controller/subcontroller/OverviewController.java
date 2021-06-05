package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class OverviewController {
    private final Parent view;
    private final ModelBuilder builder;
    private final RestClient restClient;
    private Label serverName;
    private Button leaveServer;

    public OverviewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = new RestClient();
    }

    public void init() {
        this.serverName = (Label) view.lookup("#serverName");
        serverName.setText(builder.getCurrentServer().getName());
        this.leaveServer = (Button) view.lookup("#leaveServer");
        //Buttons
        leaveServer.setOnAction(this::onLeaveServerClicked);
    }

    /**
     * User leaves the current server
     */
    private void onLeaveServerClicked(ActionEvent actionEvent) {
        restClient.postServerLeave(builder.getCurrentServer().getId(), builder.getPersonalUser().getUserKey(), response -> {
            builder.getPersonalUser().getServer().remove(builder.getCurrentServer());
        });
        StageManager.showHome();
        Platform.runLater(() -> {
            Stage stage = (Stage) serverName.getScene().getWindow();
            stage.close();
        });

    }
}
