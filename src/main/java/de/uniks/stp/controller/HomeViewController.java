package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;


import java.io.IOException;

public class HomeViewController {
    private BorderPane root;
    private ScrollPane scrollPaneUserBox;
    private ScrollPane scrollPaneServerBox;
    private VBox userBox;
    private VBox serverBox;
    private ModelBuilder builder;
    private Parent view;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        root = (BorderPane) view.lookup("#root");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");
        serverBox = (VBox) scrollPaneServerBox.getContent().lookup("#serverBox");
    }


    public void login() throws IOException {
        String userKey = RestClient.login("Peter Lustig", "1234");
        builder.buildPersonalUser("Peter Lustig", userKey);
    }

    public void showServers() {
        try {
            JSONArray jsonResponse = RestClient.getServers(builder.getPersonalUser().getUserKey());
            for (int i = 0; i < jsonResponse.length(); i++) {
                Parent root  = FXMLLoader.load(StageManager.class.getResource("ServerProfileView.fxml"));
                ServerProfileController serverProfileController = new ServerProfileController(root, builder);
                serverProfileController.init();

                String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                builder.buildServer(serverName, serverId);
                serverProfileController.setServerName(serverName);
                this.serverBox.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showUser() {
        try {
            JSONArray jsonResponse = RestClient.getUsers(builder.getPersonalUser().getUserKey());
            for (int i = 0; i < jsonResponse.length(); i++) {
                Parent root  = FXMLLoader.load(StageManager.class.getResource("UserProfileView.fxml"));
                UserProfileController userProfileController = new UserProfileController(root, builder);
                userProfileController.init();

                String userName = jsonResponse.getJSONObject(i).get("name").toString();
                builder.buildUser(userName);

                userProfileController.setUser(userName);
                userProfileController.setOnline();
                this.userBox.getChildren().add(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }
}