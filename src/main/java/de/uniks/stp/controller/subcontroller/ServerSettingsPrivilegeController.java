package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

public class ServerSettingsPrivilegeController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ChoiceBox categoryChoice;
    private ChoiceBox channelChoice;
    private RadioButton privilegeOnButton;
    private RadioButton privilegeOffButton;
    private HBox privilegeOn;
    private ServerSubSettingsPrivilegeController serverSubSettingsPrivilegeController;
    private Button changePrivilege;
    private int selectedIndex;
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
        categoryChoice = (ChoiceBox) view.lookup("#Category");
        channelChoice = (ChoiceBox) view.lookup("#Channels");
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

        /*Channel channel1 = new Channel().setName("test");
        Categories cat = new Categories().setName("test");
        server.getCategories().get(0).withChannel(channel1);
        server.withCategories(cat);
*/
        for (Categories category : server.getCategories()) {
            categoryChoice.getItems().add(category.getName());
        }

        categoryChoice.setOnAction((event) -> {
            selectedIndex = categoryChoice.getSelectionModel().getSelectedIndex();
            channelChoice.getItems().clear();
            for (Channel channel : server.getCategories().get(selectedIndex).getChannel()) {
                channelChoice.getItems().add(channel.getName());
            }
            if (server.getCategories().get(selectedIndex).getChannel().get(0).isPrivilege()) {
                privilegeOnButton.setSelected(true);
            } else {
                privilegeOffButton.setSelected(true);
            }
            channelChoice.getSelectionModel().select(0);
        });

        channelChoice.setOnAction((event) -> {
            //fxml nur laden wenn channel privilege true
            if (privilegeOnButton.isSelected()) {
                channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
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
        String channelId = server.getCategories().get(selectedIndex).getChannel().get(channelIndex).getId();
        String serverId = server.getId();
        String categoryId = server.getCategories().get(selectedIndex).getId();
        String serverName = server.getName();
        boolean privilege = privilegeOnButton.isSelected();
        String userKey = builder.getPersonalUser().getUserKey();
        ArrayList<String> members = new ArrayList<>();
        members.add(server.getOwner());
        String[] membersArray = members.toArray(new String[0]);
        restClient.updateChannel(serverId, categoryId, channelId, userKey, serverName, privilege, membersArray, response -> {

        });
        server.getCategories().get(selectedIndex).getChannel().get(channelIndex).setPrivilege(privilegeOnButton.isSelected());
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
        try {
            //view
            Parent view = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettings_Privilege_UserChange.fxml"));
            Channel channel = server.getCategories().get(selectedIndex).getChannel().get(channelIndex);
            //Controller
            serverSubSettingsPrivilegeController = new ServerSubSettingsPrivilegeController(view, builder, server, channel);
            serverSubSettingsPrivilegeController.init();
            this.privilegeOn.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error on showing ServerSettings_Privilege");
            e.printStackTrace();
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
