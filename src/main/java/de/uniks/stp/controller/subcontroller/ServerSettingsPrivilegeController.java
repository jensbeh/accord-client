package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

public class ServerSettingsPrivilegeController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ComboBox<String> categoryChoice;
    private ComboBox channelChoice;
    private RadioButton privilegeOnButton;
    private RadioButton privilegeOffButton;
    private HBox privilegeOn;
    private ServerSubSettingsPrivilegeController serverSubSettingsPrivilegeController;
    private Button changePrivilege;
    private int categoryIndex;
    private ToggleGroup group;
    private RestClient restClient;
    private int channelIndex;

    public ServerSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        restClient = new RestClient();
    }

    public void init() {
        categoryChoice = (ComboBox<String>) view.lookup("#Category");
        channelChoice = (ComboBox) view.lookup("#Channels");
        privilegeOnButton = (RadioButton) view.lookup("#Privilege_On_Button");
        privilegeOffButton = (RadioButton) view.lookup("#Privilege_Off_Button");
        privilegeOn = (HBox) view.lookup("#Privilege_On");
        changePrivilege = (Button) view.lookup("#Change_Privilege");

        // set RadioButton in group
        group = new ToggleGroup();
        privilegeOnButton.setToggleGroup(group);
        privilegeOffButton.setToggleGroup(group);

        privilegeOnButton.setOnAction(this::privilegeOnButton);
        privilegeOffButton.setOnAction(this::privilegeOffButton);
        changePrivilege.setOnAction(this::changePrivilege);

        //load all categories
        for (Categories category : server.getCategories()) {
            categoryChoice.getItems().add(category.getName());
        }

        // update channellist by category change
        categoryChoice.setOnAction((event) -> {
            categoryIndex = categoryChoice.getSelectionModel().getSelectedIndex();
            //clears channel combobox and button selection by category change
            if (channelChoice.getSelectionModel().getSelectedItem() != null) {
                group.selectToggle(null);
                privilegeOn.getChildren().clear();
            }
            channelChoice.getItems().clear();
            // load channel for this category
            for (Channel channel : server.getCategories().get(categoryIndex).getChannel()) {
                channelChoice.getItems().add(channel.getName());
            }
        });

        // update radiobutton and load correct subview for chosen channel
        channelChoice.setOnAction((event) -> {
            if (channelChoice.getSelectionModel().getSelectedItem() != null && categoryChoice.getSelectionModel().getSelectedItem() != null) {
                channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
                if (server.getCategories().get(categoryIndex).getChannel().get(channelIndex).isPrivilege()) {
                    privilegeOnButton.setSelected(true);
                } else {
                    privilegeOffButton.setSelected(true);
                }
                privilegeOnButton((ActionEvent) event);
            }
        });
    }

    /**
     * Change the Privilege of the chosen channel
     */
    private void changePrivilege(ActionEvent actionEvent) {
        if (channelChoice.getSelectionModel().getSelectedItem() != null && categoryChoice.getSelectionModel().getSelectedItem() != null) {
            channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
            String channelId = server.getCategories().get(categoryIndex).getChannel().get(channelIndex).getId();
            String serverId = server.getId();
            String categoryId = server.getCategories().get(categoryIndex).getId();
            String serverName = server.getName();
            boolean privilege = privilegeOnButton.isSelected();
            String userKey = builder.getPersonalUser().getUserKey();
            Channel channel = server.getCategories().get(categoryIndex).getChannel().get(channelIndex);
            // set changed channel privileged
            channel.setPrivilege(privilegeOnButton.isSelected());
            // send change to server
            if (privilegeOnButton.isSelected()) {
                ArrayList<String> members = new ArrayList<>();
                members.add(server.getOwner());
                // add owner to channel privilege
                for (User user : server.getUser()) {
                    if (server.getOwner().equals(user.getId())) {
                        user.withPrivileged(channel);
                    }
                }
                String[] membersArray = members.toArray(new String[0]);
                restClient.updateChannel(serverId, categoryId, channelId, userKey, serverName, privilege, membersArray, response -> {
                });
            } else {
                restClient.updateChannel(serverId, categoryId, channelId, userKey, serverName, privilege, null, response -> {
                    if (response.getBody().getObject().getJSONArray("members") == null) {
                        for (User user : channel.getPrivilegedUsers()) {
                            user.withoutPrivileged(channel);
                        }
                    }
                });
            }
            privilegeOnButton(actionEvent);
        }
    }

    /**
     * clears the VBox when channel privilege off so that the fxml is not shown
     */
    private void privilegeOffButton(ActionEvent actionEvent) {
        this.privilegeOn.getChildren().clear();
    }

    /**
     * load fxml when channel privilege on. load subcontroller.
     */
    private void privilegeOnButton(ActionEvent actionEvent) {
        this.privilegeOn.getChildren().clear();
        // only load when channel privileged
        if (channelChoice.getSelectionModel().getSelectedItem() != null && categoryChoice.getSelectionModel().getSelectedItem() != null) {
            if (server.getCategories().get(categoryIndex).getChannel().get(channelIndex).isPrivilege()) {
                try {
                    //view
                    Parent view = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettings_Privilege_UserChange.fxml"));
                    Channel channel = server.getCategories().get(categoryIndex).getChannel().get(channelIndex);
                    //Controller
                    serverSubSettingsPrivilegeController = new ServerSubSettingsPrivilegeController(view, builder, server, channel);
                    serverSubSettingsPrivilegeController.init();
                    this.privilegeOn.getChildren().add(view);
                } catch (Exception e) {
                    System.err.println("Error on showing ServerSettings_Privilege");
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        privilegeOnButton.setOnAction(null);
        privilegeOffButton.setOnAction(null);
        changePrivilege.setOnAction(null);

        if (serverSubSettingsPrivilegeController != null) {
            serverSubSettingsPrivilegeController.stop();
            serverSubSettingsPrivilegeController = null;
        }
    }
}
