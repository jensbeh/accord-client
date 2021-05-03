package de.uniks.stp.controller;

import com.sun.javafx.scene.control.LabeledText;
import de.uniks.stp.AlternateChannelListCellFactory;
import de.uniks.stp.AlternateUserListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.json.JSONArray;


import java.io.IOException;
import java.security.cert.PolicyNode;

public class HomeViewController {
    private BorderPane root;
    private ScrollPane scrollPaneUserBox;
    private ScrollPane scrollPaneServerBox;
    private ScrollPane privateChatScrollpane;
    private VBox userBox;
    private VBox currentUserBox;
    private VBox serverBox;
    private ModelBuilder builder;
    private Parent view;
    private ListView<Channel> privateChatList;
    private ObservableList<Channel> privateChats;
    private Channel selectedChat;
    private VBox messages;
    private ListView<User> onlineUsersList;
    private ObservableList<User> onlineUsers;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        // Load all view references
        root = (BorderPane) view.lookup("#root");

        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");
        scrollPaneUserBox = (ScrollPane) view.lookup("#scrollPaneUserBox");
        privateChatScrollpane = (ScrollPane) view.lookup("#privateChatScrollpane");

        currentUserBox = (VBox) scrollPaneUserBox.getContent().lookup("#currentUserBox");
        userBox = (VBox) scrollPaneUserBox.getContent().lookup("#userBox");
        serverBox = (VBox) scrollPaneServerBox.getContent().lookup("#serverBox");
        messages = (VBox) view.lookup("#messages");

        privateChatList = (ListView<Channel>) privateChatScrollpane.getContent().lookup("#privateChatList");
        privateChatList.setCellFactory(new AlternateChannelListCellFactory());
        this.privateChatList.setOnMouseReleased(this::onprivateChatListClicked);

        privateChats = FXCollections.observableArrayList();
        this.privateChatList.setItems(privateChats);

        onlineUsersList = (ListView<User>) scrollPaneUserBox.getContent().lookup("#onlineUsers");
        onlineUsersList.setCellFactory(new AlternateUserListCellFactory());
        this.onlineUsersList.setOnMouseReleased(this::ononlineUsersListClicked);

        onlineUsers = FXCollections.observableArrayList();
        this.onlineUsersList.setItems(onlineUsers);

        login();
        showServers();
        showCurrentUser();
        showUser();
    }


    private void login() {
        try {
            String userKey = RestClient.login("Peter Lustig", "1234");
            builder.buildPersonalUser("Peter Lustig", userKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showServers() {
        try {
            JSONArray jsonResponse = RestClient.getServers(builder.getPersonalUser().getUserKey());
            for (int i = 0; i < jsonResponse.length(); i++) {
                Parent root = FXMLLoader.load(StageManager.class.getResource("ServerProfileView.fxml"));
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

    private void showCurrentUser() {
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("UserProfileView.fxml"));
            UserProfileController userProfileController = new UserProfileController(root, builder);
            userProfileController.init();
            User currentUser = builder.getPersonalUser();
            userProfileController.setUserName(currentUser.getName());
            userProfileController.setOnline();
            this.currentUserBox.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showUser() {
        onlineUsers.clear();
        JSONArray jsonResponse = RestClient.getUsers(builder.getPersonalUser().getUserKey());
        for (int i = 0; i < jsonResponse.length(); i++) {
            String userName = jsonResponse.getJSONObject(i).get("name").toString();
            if (!userName.equals(builder.getPersonalUser().getName())) {
                builder.buildUser(userName);
                onlineUsers.add(new User().setName(userName).setStatus(true));
            }
        }
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    private void onprivateChatListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            selectedChat = this.privateChatList.getSelectionModel().getSelectedItem();
            MessageViews();
        }
    }

    private void MessageViews() {
        this.messages.getChildren().clear();
        for (Message msg : this.selectedChat.getMessage()) {
            try {
                Parent view = FXMLLoader.load(StageManager.class.getResource("controller/Message.fxml"));
                MessageController messageController = new MessageController(msg, view, builder);
                messageController.init();

                this.messages.getChildren().add(view);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void ononlineUsersListClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            boolean flag = true;
            String selectedUserName = this.onlineUsersList.getSelectionModel().getSelectedItem().getName();
            for (Channel channel : privateChats) {
                if (channel.getName().equals(selectedUserName)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                privateChats.add(new Channel().setName(selectedUserName));
            }
        }
    }
}