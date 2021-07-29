package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;

public class OverviewController {
    private final Parent view;
    private final ModelBuilder builder;
    private final RestClient restClient;
    private Label serverName;

    public OverviewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = modelBuilder.getRestClient();
    }

    public void init() {
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
        userExitedNotification(builder.getCurrentServer());
        restClient.postServerLeave(builder.getCurrentServer().getId(), builder.getPersonalUser().getUserKey(), response -> builder.getPersonalUser().getServer().remove(builder.getCurrentServer()));
        Platform.runLater(() -> {
            Stage stage = (Stage) serverName.getScene().getWindow();
            stage.close();
        });
    }

    private void userExitedNotification(Server server) {
        if (builder.getServerChatWebSocketClient() != null) {
            JSONObject obj = new JSONObject().put("channel", server.getCategories().get(0).getChannel().get(0).getId()).put("message", builder.getPersonalUser().getName() + " left the Server");
            try {
                builder.getServerChatWebSocketClient().sendMessage(obj.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
