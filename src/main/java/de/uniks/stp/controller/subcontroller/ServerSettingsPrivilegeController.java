package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Objects;

public class ServerSettingsPrivilegeController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ComboBox<Categories> categoryChoice;
    private ComboBox<Channel> channelChoice;
    private RadioButton privilegeOnButton;
    private RadioButton privilegeOffButton;
    private HBox privilegeOn;
    private ServerSubSettingsPrivilegeController serverSubSettingsPrivilegeController;
    private Button changePrivilege;
    private ToggleGroup group;
    private final RestClient restClient;
    private Categories selectedCategory;
    private Channel selectedChannel;


    public ServerSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        restClient = new RestClient();
    }

    public void init() {
        categoryChoice = (ComboBox<Categories>) view.lookup("#Category");
        channelChoice = (ComboBox<Channel>) view.lookup("#Channels");
        privilegeOnButton = (RadioButton) view.lookup("#Privilege_On_Button");
        privilegeOffButton = (RadioButton) view.lookup("#Privilege_Off_Button");
        privilegeOn = (HBox) view.lookup("#Privilege_On");
        changePrivilege = (Button) view.lookup("#Change_Privilege");

        group = new ToggleGroup();
        privilegeOnButton.setToggleGroup(group);
        privilegeOffButton.setToggleGroup(group);

        privilegeOnButton.setOnAction(this::privilegeOnButton);
        privilegeOffButton.setOnAction(this::privilegeOffButton);
        changePrivilege.setOnAction(this::changePrivilege);

        //load all categories
        for (Categories category : server.getCategories()) {
            categoryChoice.getItems().add(category);
            categoryChoice.setConverter(new StringConverter<Categories>() {
                @Override
                public String toString(Categories object) {
                    if (object == null) {
                        return "Select category...";
                    }
                    return object.getName();
                }

                @Override
                public Categories fromString(String string) {
                    return null;
                }
            });
        }
        // update channelList by category change
        categoryChoice.setOnAction((event) -> {
            selectedCategory = categoryChoice.getSelectionModel().getSelectedItem();

            //clears channel comboBox and button selection by category change
            if (selectedChannel != null) {
                group.selectToggle(null);
                Platform.runLater(() -> this.privilegeOn.getChildren().clear());
            }
            Platform.runLater(() -> channelChoice.getItems().clear());
            // load channel for this category
            for (Channel channel : selectedCategory.getChannel()) {
                Platform.runLater(() -> channelChoice.getItems().add(channel));
                channelChoice.setConverter(new StringConverter<Channel>() {
                    @Override
                    public String toString(Channel object) {
                        if (object == null) {
                            return "Select channel...";
                        }
                        return object.getName();
                    }

                    @Override
                    public Channel fromString(String string) {
                        return null;
                    }
                });
            }
        });

        // update radiobutton and load correct subview for chosen channel
        channelChoice.setOnAction((event) -> {
            selectedChannel = channelChoice.getSelectionModel().getSelectedItem();
            if (selectedChannel != null && selectedCategory != null) {
                if (selectedChannel.isPrivilege()) {
                    privilegeOnButton.setSelected(true);
                } else {
                    privilegeOffButton.setSelected(true);
                }
                privilegeOnButton((ActionEvent) event);
            }
        });
        // start property change listener
        for (Categories cat : server.getCategories()) {
            for (Channel channel : cat.getChannel()) {
                channel.addPropertyChangeListener(Channel.PROPERTY_PRIVILEGE, this::onPrivilegeChanged);
            }
        }
    }

    /**
     * PropertyChange when channel privilege changed
     */
    private void onPrivilegeChanged(PropertyChangeEvent propertyChangeEvent) {
        privilegeOnButton.setSelected(selectedChannel.isPrivilege());
        privilegeOffButton.setSelected(!selectedChannel.isPrivilege());
        if (!selectedChannel.isPrivilege()) {
            Platform.runLater(() -> this.privilegeOn.getChildren().clear());
        }
    }

    /**
     * Change the Privilege of the chosen channel
     */
    private void changePrivilege(ActionEvent actionEvent) {
        if (selectedChannel != null && selectedCategory != null) {
            String channelId = selectedChannel.getId();
            String serverId = server.getId();
            String categoryId = selectedCategory.getId();
            String channelName = selectedChannel.getName();
            boolean privilege = privilegeOnButton.isSelected();
            String userKey = builder.getPersonalUser().getUserKey();
            // set changed channel privileged
            selectedChannel.setPrivilege(privilegeOnButton.isSelected());
            // send change to server
            if (privilegeOnButton.isSelected()) {
                ArrayList<String> members = new ArrayList<>();
                members.add(server.getOwner());
                // add owner to channel privilege
                for (User user : server.getUser()) {
                    if (server.getOwner().equals(user.getId())) {
                        user.withPrivileged(selectedChannel);
                    }
                }
                String[] membersArray = members.toArray(new String[0]);
                restClient.updateChannel(serverId, categoryId, channelId, userKey, channelName, privilege, membersArray, response -> {
                });
            } else {
                ArrayList<User> privileged = new ArrayList<>(selectedChannel.getPrivilegedUsers());
                selectedChannel.withoutPrivilegedUsers(privileged);
                restClient.updateChannel(serverId, categoryId, channelId, userKey, channelName, privilege, null, response -> {
                });
            }
            privilegeOnButton(actionEvent);
        }
    }

    /**
     * clears the VBox when channel privilege off so that the fxml is not shown
     */
    private void privilegeOffButton(ActionEvent actionEvent) {
        Platform.runLater(() -> this.privilegeOn.getChildren().clear());
    }

    /**
     * load fxml when channel privilege on. load subcontroller.
     */
    private void privilegeOnButton(ActionEvent actionEvent) {
        if (serverSubSettingsPrivilegeController != null) {
            serverSubSettingsPrivilegeController.stop();
        }
        Platform.runLater(() -> this.privilegeOn.getChildren().clear());
        // only load when channel privileged
        if (selectedChannel != null && selectedCategory != null) {
            if (selectedChannel.isPrivilege()) {
                try {
                    //view
                    Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/settings/ServerSettings_Privilege_UserChange.fxml")));
                    //Controller
                    serverSubSettingsPrivilegeController = new ServerSubSettingsPrivilegeController(view, builder, server, selectedChannel);
                    serverSubSettingsPrivilegeController.init();
                    Platform.runLater(() -> this.privilegeOn.getChildren().add(view));
                } catch (Exception e) {
                    System.err.println("Error on showing ServerSettings_Privilege");
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        this.privilegeOnButton.setOnAction(null);
        this.privilegeOffButton.setOnAction(null);
        this.changePrivilege.setOnAction(null);
        this.categoryChoice.setOnAction(null);
        this.channelChoice.setOnAction(null);

        if (serverSubSettingsPrivilegeController != null) {
            serverSubSettingsPrivilegeController.stop();
            serverSubSettingsPrivilegeController = null;
        }
        for (Categories cat : server.getCategories()) {
            for (Channel channel : cat.getChannel()) {
                channel.removePropertyChangeListener(this::onPrivilegeChanged);
            }
        }
    }
}