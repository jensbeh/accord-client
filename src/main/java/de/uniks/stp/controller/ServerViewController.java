package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private JSONArray members;
    private ListView<User> onlineUsersList;
    private VBox chatViewContainer;



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
        chatViewContainer = (VBox) view.lookup("#chatBox");
        this.loadCurrentUserMessageFxml();
    }

    public void showServerChat() {
        showText();
        showChannels();
    }

    public HBox getRoot() {
        return root;
    }


    public void showOnlineUsers(String userKey) {
        restClient.getServerUsers(server.getId(), userKey, response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                members = body.getObject().getJSONObject("data").getJSONArray("members");
                for (User user : builder.getCurrentServer().getUser()) {
                    builder.getCurrentServer().withoutUser(user);
                }
                for (int i = 0; i < members.length(); i++) {
                    JSONObject member = members.getJSONObject(i);
                    String id = member.getString("id");
                    String name = member.getString("name");
                    boolean online = member.getBoolean("online");
                    User user = new User().setCurrentUser(builder.getPersonalUser()).setId(id).setName(name).setStatus(online);
                    builder.getCurrentServer().withUser(user);
                }


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

    private void loadCurrentUserMessageFxml(){
        this.chatViewContainer.getChildren().clear();
        try {
            Parent view = FXMLLoader.load(StageManager.class.getResource("ChatView.fxml"));
            ChatViewController messageController = new ChatViewController(view, builder);
            messageController.init();

            this.chatViewContainer.getChildren().add(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
