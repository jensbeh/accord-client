package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.SubSetting;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
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
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ServerSettingsPrivilegeController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private final RestClient restClient;
    private ComboBox<Categories> categoryChoice;
    private ComboBox<ServerChannel> channelChoice;
    private RadioButton privilegeOnButton;
    private RadioButton privilegeOffButton;
    private HBox privilegeOn;
    private ServerSubSettingsPrivilegeController serverSubSettingsPrivilegeController;
    private Button changePrivilege;
    private ToggleGroup group;
    private Categories selectedCategory;
    private ServerChannel selectedChannel;
    private VBox root;


    public ServerSettingsPrivilegeController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
        restClient = builder.getRestClient();
    }

    @SuppressWarnings("unchecked")
    public void init() {
        root = (VBox) view.lookup("#rootPrivilege");
        categoryChoice = (ComboBox<Categories>) view.lookup("#Category");
        channelChoice = (ComboBox<ServerChannel>) view.lookup("#Channels");
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
            categoryChoice.setConverter(new StringConverter<>() {
                @Override
                public String toString(Categories object) {
                    if (object == null) {
                        ResourceBundle lang = StageManager.getLangBundle();
                        return lang.getString("comboBox.selectCategory");
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
            for (ServerChannel channel : selectedCategory.getChannel()) {
                Platform.runLater(() -> channelChoice.getItems().add(channel));
                channelChoice.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(ServerChannel object) {
                        if (object == null) {
                            ResourceBundle lang = StageManager.getLangBundle();
                            return lang.getString("comboBox.selectChannel");
                        }
                        return object.getName();
                    }

                    @Override
                    public ServerChannel fromString(String string) {
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
                privilegeOnButton(event);
            }
        });
        // start property change listener
        for (Categories cat : server.getCategories()) {
            for (ServerChannel channel : cat.getChannel()) {
                channel.addPropertyChangeListener(ServerChannel.PROPERTY_PRIVILEGE, this::onPrivilegeChanged);
            }
        }
    }

    /**
     * PropertyChange when channel privilege changed
     */
    private void onPrivilegeChanged(PropertyChangeEvent propertyChangeEvent) {
        if (selectedChannel != null)
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
     * load fxml when channel privilege on. load SubController.
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
                    Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/serversettings/ServerSettings_Privilege_UserChange.fxml")), StageManager.getLangBundle());
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
            for (ServerChannel channel : cat.getChannel()) {
                channel.removePropertyChangeListener(this::onPrivilegeChanged);
            }
        }
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/ServerSettings.css")).toExternalForm());
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ServerSettings.css")).toExternalForm());
    }
}
