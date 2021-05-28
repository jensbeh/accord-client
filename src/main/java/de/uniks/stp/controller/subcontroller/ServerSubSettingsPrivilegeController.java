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

    private static ComboBox addUserMenu;
    private static ComboBox removeUserMenu;
    private static Button addUser;
    private static Button removeUser;
    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final Channel channel;
    private RestClient restClient;

    public ServerSubSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server, Channel channel) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        this.channel = channel;
        restClient = new RestClient();
    }

    public void init() {
        addUserMenu = (ComboBox) view.lookup("#Add_User_to_Privilege");
        removeUserMenu = (ComboBox) view.lookup("#Remove_User_from_Privilege");
        addUser = (Button) view.lookup("#User_to_Privilege");
        removeUser = (Button) view.lookup("#User_from_Privilege");

        addUser.setOnAction(this::addPrivilegedUser);

        for (User user : server.getUser()) {
            if (!channel.getPrivilegedUsers().contains(user)) {
                addUserMenu.getItems().add(user.getName());
            }
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

            addUserMenu.getItems().clear();
            // load only useres in addmenu who is not privileged yet
            for (User user : server.getUser()) {
                if (!channel.getPrivilegedUsers().contains(user)) {
                    addUserMenu.getItems().add(user.getName());
                }
            }
            String userKey = builder.getPersonalUser().getUserKey();

            ArrayList<String> members = new ArrayList<>();
            for (User user : channel.getPrivilegedUsers()) {
                members.add(user.getId());
            }
            String[] membersArray = members.toArray(new String[0]);
            // send update to server
            restClient.updateChannel(server.getId(), channel.getCategories().getId(), channel.getId(), userKey,
                    server.getName(), channel.isPrivilege(), membersArray, response -> {
                    });
        }
    }

    public void stop() {
        addUser.setOnAction(null);
    }
}
