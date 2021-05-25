package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
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

public class ServerPrivilegeSettingsController {

    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private ChoiceBox categoryChoice;
    private ChoiceBox channelChoice;
    private RadioButton Privilege_On_Button;
    private RadioButton Privilege_Off_Button;
    private HBox Privilege_On;
    private ServerPrivilegeSubSettingsController ServerPrivilegeSubSettingsController;
    private Button Change_Privilege;
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
        Privilege_On_Button = (RadioButton) view.lookup("#Privilege_On_Button");
        Privilege_Off_Button = (RadioButton) view.lookup("#Privilege_Off_Button");
        Privilege_On = (HBox) view.lookup("#Privilege_On");
        Change_Privilege = (Button) view.lookup("#Change_Privilege");

        group = new ToggleGroup();
        Privilege_On_Button.setToggleGroup(group);
        Privilege_Off_Button.setToggleGroup(group);
        Privilege_Off_Button.setSelected(true);

        Privilege_On_Button.setOnAction(this::Privilege_On_Button);
        Privilege_Off_Button.setOnAction(this::Privilege_Off_Button);
        Change_Privilege.setOnAction(this::Change_Privilege);

        categoryChoice.getItems().add("Text Channel");
        categoryChoice.getItems().add("Voice Channel");

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

    // Change the Privileg of the choosen channel
    private void Change_Privilege(ActionEvent actionEvent) {
        int channelIndex = channelChoice.getSelectionModel().getSelectedIndex();
        server.getCategories().get(selectedIndex).getChannel().get(channelIndex).setPrivilege(Privilege_On_Button.isSelected());
    }

    // clears the VBox when channel privileg off
    private void Privilege_Off_Button(ActionEvent actionEvent) {
        this.Privilege_On.getChildren().clear();
    }
    /**
     * load fxml when channel privileg on.
     */
    private void Privilege_On_Button(ActionEvent actionEvent) {
        this.Privilege_On.getChildren().clear();
        try {
            //view
            Parent view = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettings_Privilege_UserChange.fxml"));
            //Controller
            ServerPrivilegeSubSettingsController = new ServerPrivilegeSubSettingsController(view, builder, server);
            ServerPrivilegeSubSettingsController.init();
            this.Privilege_On.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error on showing ServerSettings_Privilege");
            e.printStackTrace();
        }
    }
}
