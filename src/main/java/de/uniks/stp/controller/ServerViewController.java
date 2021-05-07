package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import kong.unirest.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerViewController {

    private final RestClient restClient;
    private final Server server;
    private final Parent view;
    private HBox root;
    private ScrollPane scrollPaneUserBox;
    private ModelBuilder builder;

    private VBox channelBox;
    private VBox textChannelBox;
    private Label serverNameText;
    private TextField sendTextField;
    private Button sendMessageButton;
    private JSONArray members;

    public ServerViewController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
        restClient = new RestClient();
    }

    public void init() {
        root = (HBox) view.lookup("#root");
        channelBox = (VBox) view.lookup("#channelBox");
        serverNameText = (Label) view.lookup("#serverName");
        serverNameText.setText(server.getName());
        textChannelBox = (VBox) view.lookup("#textChannelBox");
        sendTextField = (TextField) view.lookup("#sendTextField");
        sendMessageButton = (Button) view.lookup("#sendMessageButton");
        sendMessageButton.setOnAction(this::onSendMessage);

    }

    public void showServerChat() {
        showText();
        showChannels();
    }

    public HBox getRoot() {
        return root;
    }


    public void showOnlineUsers( String userKey) {
        ArrayList<User> users = null;
        restClient.getServerUsers(server.getId(), userKey, response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                this.members = body.getObject().getJSONObject("data").getJSONArray("members");
                Platform.runLater(() -> {
                    for(int i = 0; i < members.length(); i++) {
                        JSONObject member = members.getJSONObject(i);
                        String id = member.getString("id");
                        String name = member.getString("name");
                        boolean online = member.getBoolean("online");
                        builder.buildUser(name, id, online);
                    }
                });

            } else if (status.equals("failure")) {
                System.out.println(body.getObject().getString("message"));
            }
        });
    }

    public void showText() {

    }

    public void onSendMessage(ActionEvent event) {

    }

    public void showChannels() {

    }

}
