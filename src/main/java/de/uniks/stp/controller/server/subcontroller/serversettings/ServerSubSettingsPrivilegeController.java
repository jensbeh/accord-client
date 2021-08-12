package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;

public class ServerSubSettingsPrivilegeController {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final ServerChannel channel;
    private final RestClient restClient;
    private ComboBox<String> addUserMenu;
    private ComboBox<String> removeUserMenu;
    private Button addUser;
    private Button removeUser;
    private User selectedRemoveUser;
    private User selectedAddUser;

    public ServerSubSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server, ServerChannel channel) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        this.channel = channel;
        restClient = builder.getRestClient();
    }

    @SuppressWarnings("unchecked")
    public void init() {
        addUserMenu = (ComboBox<String>) view.lookup("#Add_User_to_Privilege");
        removeUserMenu = (ComboBox<String>) view.lookup("#Remove_User_from_Privilege");
        addUser = (Button) view.lookup("#User_to_Privilege");
        removeUser = (Button) view.lookup("#User_from_Privilege");

        addUser.setOnAction(this::addPrivilegedUser);
        removeUser.setOnAction(this::removePrivilegedUser);

        for (User user : server.getUser()) {
            if (!channel.getPrivilegedUsers().contains(user)) {
                addUserMenu.getItems().add(user.getName());
            }
        }
        for (User user : channel.getPrivilegedUsers()) {
            removeUserMenu.getItems().add(user.getName());
        }
    }

    /**
     * removes a privileged user from channel
     */
    private void removePrivilegedUser(ActionEvent actionEvent) {
        if (removeUserMenu.getSelectionModel().getSelectedItem() != null) {
            for (User user : server.getUser()) {
                if (user.getName().equals(removeUserMenu.getSelectionModel().getSelectedItem())) {
                    selectedRemoveUser = user;
                }
            }
            // remove selected user from channel as privileged
            channel.withoutPrivilegedUsers(selectedRemoveUser);
            // update removeMenu
            removeUserMenu.getItems().clear();
            for (User user : server.getUser()) {
                if (user.getPrivileged().contains(channel)) {
                    removeUserMenu.getItems().add(user.getName());
                }
            }
            // update addMenu
            addUserMenu.getItems().clear();
            for (User user : server.getUser()) {
                if (!user.getPrivileged().contains(channel)) {
                    addUserMenu.getItems().add(user.getName());
                }
            }
            channelPrivilegedUserUpdate();
        }
    }

    /**
     * set a user privileged for a channel
     */
    private void addPrivilegedUser(ActionEvent actionEvent) {
        if (addUserMenu.getSelectionModel().getSelectedItem() != null) {
            for (User user : server.getUser()) {
                if (user.getName().equals(addUserMenu.getSelectionModel().getSelectedItem())) {
                    selectedAddUser = user;
                }
            }
            // set selected user to channel as privileged
            channel.withPrivilegedUsers(selectedAddUser);
            // update addMenu
            addUserMenu.getItems().clear();
            for (User user : server.getUser()) {
                if (!user.getPrivileged().contains(channel)) {
                    addUserMenu.getItems().add(user.getName());
                }
            }
            // update removeMenu
            removeUserMenu.getItems().clear();
            for (User user : server.getUser()) {
                if (user.getPrivileged().contains(channel)) {
                    removeUserMenu.getItems().add(user.getName());
                }
            }
            channelPrivilegedUserUpdate();
        }
    }

    /**
     * updates the channel privileged
     */
    private void channelPrivilegedUserUpdate() {
        String userKey = builder.getPersonalUser().getUserKey();
        if (channel.getPrivilegedUsers().size() != 0) {
            ArrayList<String> members = new ArrayList<>();
            for (User user : channel.getPrivilegedUsers()) {
                members.add(user.getId());
            }
            String[] membersArray = members.toArray(new String[0]);
            // send update to server
            restClient.updateChannel(server.getId(), channel.getCategories().getId(), channel.getId(), userKey,
                    channel.getName(), channel.isPrivilege(), membersArray, response -> {
                    });
        } else {
            channel.setPrivilege(false);
            restClient.updateChannel(server.getId(), channel.getCategories().getId(), channel.getId(), userKey,
                    channel.getName(), false, null, response -> {
                    });
        }
    }

    public void stop() {
        addUser.setOnAction(null);
        removeUser.setOnAction(null);
    }
}
