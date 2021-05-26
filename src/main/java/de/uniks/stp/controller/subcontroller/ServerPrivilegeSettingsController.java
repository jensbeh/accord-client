package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class ServerPrivilegeSettingsController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ChoiceBox categoryChoice;
    private ChoiceBox channelChoice;
    private RadioButton privilegeOnButton;
    private RadioButton privilegeOffButton;
    private HBox privilegeOn;
    private ServerPrivilegeSubSettingsController ServerPrivilegeSubSettingsController;
    private Button changePrivilege;
    private int selectedIndex;
    private ToggleGroup group;

    public ServerPrivilegeSettingsController(Parent view, ModelBuilder builder, Server server) {
        this.view = view;
        this.builder = builder;
        this.server = server;
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
        privilegeOffButton.setSelected(true);

        privilegeOnButton.setOnAction(this::Privilege_On_Button);
        privilegeOffButton.setOnAction(this::Privilege_Off_Button);
        changePrivilege.setOnAction(this::Change_Privilege);

        for (Categories category : server.getCategories()) {
            categoryChoice.getItems().add(category.getName());
        }

        categoryChoice.setOnAction((event) -> {
            selectedIndex = categoryChoice.getSelectionModel().getSelectedIndex();
            channelChoice.getItems().clear();
            for (Channel channel : server.getCategories().get(selectedIndex).getChannel()) {
                channelChoice.getItems().add(channel.getName());
            }
            channelChoice.getSelectionModel().select(0);
        });
        categoryChoice.getSelectionModel().select(0);
    }

    /**
     * Change the Privileg of the choosen channel
     */
    private void Change_Privilege(ActionEvent actionEvent) {
        int channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
        server.getCategories().get(selectedIndex).getChannel().get(channelIndex).setPrivilege(privilegeOnButton.isSelected());
    }

    /**
     * clears the VBox when channel privileg off so that the fxml is not shown
     */
    private void Privilege_Off_Button(ActionEvent actionEvent) {
        this.privilegeOn.getChildren().clear();
    }

    /**
     * load fxml when channel privileg on. load subcontroller.
     */
    private void Privilege_On_Button(ActionEvent actionEvent) {
        this.privilegeOn.getChildren().clear();
        try {
            //view
            Parent view = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettings_Privilege_UserChange.fxml"));
            //Controller
            ServerPrivilegeSubSettingsController = new ServerPrivilegeSubSettingsController(view, builder, server);
            ServerPrivilegeSubSettingsController.init();
            this.privilegeOn.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error on showing ServerSettings_Privilege");
            e.printStackTrace();
        }
    }
}
