package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import kong.unirest.JsonNode;

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
        this.leaveServer = (Button) view.lookup("#leaveServer");

        leaveServer.setOnAction(this::onLeaveServerClicked);
    }

    private void onLeaveServerClicked(ActionEvent actionEvent) {
        leaveServer();
    }

    public void leaveServer() {
        restClient.postServerLeave(builder.getPersonalUser().getUserKey(), builder.getCurrentServer().getId(), response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            System.out.println("status: " + status);
            builder.getPersonalUser().getServer().remove(builder.getCurrentServer());
            //remove currentUser aus serverUserList???
        });
        StageManager.showHome();
    }
}
