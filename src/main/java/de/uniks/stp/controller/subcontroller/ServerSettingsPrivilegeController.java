package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
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

        group = new ToggleGroup();
        privilegeOnButton.setToggleGroup(group);
        privilegeOffButton.setToggleGroup(group);

        privilegeOnButton.setOnAction(this::privilegeOnButton);
        privilegeOffButton.setOnAction(this::privilegeOffButton);
        changePrivilege.setOnAction(this::changePrivilege);

        Channel channel1 = new Channel().setName("test");
        Categories cat = new Categories().setName("test");
        server.getCategories().get(0).withChannel(channel1);
        server.withCategories(cat);


        for (Categories category : builder.getCurrentServer().getCategories()) {
            this.categoryChoice.getItems().add(category.getName());
            categoryChoice.setConverter(new StringConverter<String>() {
                @Override
                public String toString(String name) {
                    if (name == null) {
                        return "Select category...";
                    }
                    return name;
                }

                @Override
                public String fromString(String string) {
                    return null;
                }
            });
        }
        Platform.runLater(() -> {
            categoryChoice.getSelectionModel().clearSelection();
        });

        categoryChoice.setOnAction((event) -> {
            categoryIndex = categoryChoice.getSelectionModel().getSelectedIndex();
            channelChoice.getItems().clear();
            for (Channel channel : server.getCategories().get(categoryIndex).getChannel()) {
                channelChoice.getItems().add(channel.getName());
            }
            if (server.getCategories().get(categoryIndex).getChannel().get(0).isPrivilege()) {
                privilegeOnButton.setSelected(true);
            } else {
                privilegeOffButton.setSelected(true);
            }
            channelChoice.getSelectionModel().select(0);
        });

        channelChoice.setOnAction((event) -> {
            channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
            if (server.getCategories().get(categoryIndex).getChannel().get(channelIndex).isPrivilege()) {
                privilegeOnButton.setSelected(true);
            } else {
                privilegeOffButton.setSelected(true);
            }
            if (privilegeOnButton.isSelected()) {
                privilegeOnButton((ActionEvent) event);
            }
        });
        categoryChoice.getSelectionModel().select(0);
    }

    /**
     * Change the Privileg of the choosen channel
     */
    private void changePrivilege(ActionEvent actionEvent) {

        channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
        String channelId = server.getCategories().get(categoryIndex).getChannel().get(channelIndex).getId();
        String serverId = server.getId();
        String categoryId = server.getCategories().get(categoryIndex).getId();
        String serverName = server.getName();
        boolean privilege = privilegeOnButton.isSelected();
        String userKey = builder.getPersonalUser().getUserKey();

        server.getCategories().get(categoryIndex).getChannel().get(channelIndex).setPrivilege(privilegeOnButton.isSelected());
        if (privilegeOnButton.isSelected()) {
            ArrayList<String> members = new ArrayList<>();
            //members.add(server.getOwner());
            String[] membersArray = members.toArray(new String[0]);
            restClient.updateChannel(serverId, categoryId, channelId, userKey, serverName, privilege, membersArray, response -> {
            });
        } else {
            restClient.updateChannel(serverId, categoryId, channelId, userKey, serverName, privilege, null, response -> {
            });
        }
    }

    /**
     * clears the VBox when channel privileg off so that the fxml is not shown
     */
    private void privilegeOffButton(ActionEvent actionEvent) {
        this.privilegeOn.getChildren().clear();
    }

    /**
     * load fxml when channel privileg on. load subcontroller.
     */
    private void privilegeOnButton(ActionEvent actionEvent) {
        this.privilegeOn.getChildren().clear();
        //if (server.getCategories().get(categoryIndex).getChannel().get(channelIndex).isPrivilege()) {
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
        // }
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
