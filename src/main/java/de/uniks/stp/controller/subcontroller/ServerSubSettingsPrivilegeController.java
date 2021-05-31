package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;

public class ServerSubSettingsPrivilegeController {

    private static ComboBox<String> addUserMenu;
    private static ComboBox<String> removeUserMenu;
    private static Button addUser;
    private static Button removeUser;
    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final Channel channel;
    private final RestClient restClient;

    public ServerSubSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server, Channel channel) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        this.channel = channel;
        restClient = new RestClient();
    }

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
            User selectedUser = server.getUser().get(removeUserMenu.getSelectionModel().getSelectedIndex());
            // remove selected user from channel as privileged
            channel.withoutPrivilegedUsers(selectedUser);
            // update removeMenu
            removeUserMenu.getItems().remove(selectedUser.getName());
            // update addMenu
            addUserMenu.getItems().add(selectedUser.getName());
            channelPrivilegedUserUpdate();
        }
    }

    /**
     * set a user privileged for a channel
     */
    private void addPrivilegedUser(ActionEvent actionEvent) {
        if (addUserMenu.getSelectionModel().getSelectedItem() != null) {
            User selectedUser = server.getUser().get(addUserMenu.getSelectionModel().getSelectedIndex());
            // set selected user to channel as privileged
            channel.withPrivilegedUsers(selectedUser);
            // update addMenu
            addUserMenu.getItems().remove(selectedUser.getName());
            // update removeMenu
            removeUserMenu.getItems().add(selectedUser.getName());
            channelPrivilegedUserUpdate();
        }
    }

    /**
     * updates the channel privileged
     */
    private void channelPrivilegedUserUpdate() {
        String userKey = builder.getPersonalUser().getUserKey();
        System.out.println(channel.getPrivilegedUsers().size());
        if (channel.getPrivilegedUsers().size() != 0) {
            ArrayList<String> members = new ArrayList<>();
            for (User user : channel.getPrivilegedUsers()) {
                members.add(user.getId());
            }
            String[] membersArray = members.toArray(new String[0]);
            // send update to server
            restClient.updateChannel(server.getId(), channel.getCategories().getId(), channel.getId(), userKey,
                    channel.getName(), channel.isPrivilege(), membersArray, response -> {
                        System.out.println(response.getBody() + "1");
                    });
        } else {
            channel.setPrivilege(false);
            restClient.updateChannel(server.getId(), channel.getCategories().getId(), channel.getId(), userKey,
                    channel.getName(), false, null, response -> {
                        System.out.println(response.getBody());
                    });
        }
    }

    public void stop() {
        addUser.setOnAction(null);
        removeUser.setOnAction(null);
    }
}
