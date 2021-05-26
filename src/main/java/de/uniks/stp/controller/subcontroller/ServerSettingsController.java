package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ServerSettingsController {
    private Parent view;
    private ModelBuilder builder;
    private Server server;
    private Button selectedButton;
    private Button overview;
    private Button channel;
    private Button category;
    private Button privilege;
    private VBox serverSettingsContainer;
    private SubSetting subController;

    public ServerSettingsController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
    }

    public void init() {
        //init of sideButtons
        overview = (Button) view.lookup("#overview");
        channel = (Button) view.lookup("#channel");
        category = (Button) view.lookup("#category");
        privilege = (Button) view.lookup("#privilege");

        //Highlight the OverviewButton
        newSelectedButton(overview);

        //container
        this.serverSettingsContainer = (VBox) view.lookup("#serverSettingsContainer");
        this.serverSettingsContainer.getChildren().clear();

        //ActionHandler
        overview.setOnAction(this::onOverViewClicked);
        channel.setOnAction(this::onChannelClicked);
        category.setOnAction(this::onCategoryClicked);
        privilege.setOnAction(this::onPrivilegeClicked);
    }


    private void onOverViewClicked(ActionEvent actionEvent) {
        if (selectedButton != overview) {
            newSelectedButton(overview);
        }
    }

    private void onChannelClicked(ActionEvent actionEvent) {
        if (selectedButton != channel) {
            newSelectedButton(channel);
            openSettings("Channel");
        }
    }

    private void onCategoryClicked(ActionEvent actionEvent) {
        if (selectedButton != category) {
            newSelectedButton(category);
        }
    }
    /**
     * shows the privilege settings from the server
     */
    private void onPrivilegeClicked(ActionEvent actionEvent) {
        if (selectedButton != privilege) {
            newSelectedButton(privilege);
            openSettings("Privilege");
        }
    }


    public void stop() {
        overview.setOnAction(null);
        channel.setOnAction(null);
        category.setOnAction(null);
        privilege.setOnAction(null);

        if (subController != null) {
            subController.stop();
            subController = null;
        }
    }

    private void newSelectedButton(Button button) {
        if (selectedButton != null) {
            selectedButton.setStyle("-fx-background-color: #333333;-fx-border-color:#333333");
        }
        button.setStyle("-fx-background-color: #5c5c5c;-fx-border-color:#1a1a1a");
        selectedButton = button;
    }

    /**
     * load / open the sub server setting on the right field
     *
     * @param fxmlName the fxml sub name
     */
    public void openSettings(String fxmlName) {
        // stop current subController
        if (subController != null) {
            subController.stop();
        }

        // clear old and load new subSetting view
        try {
            this.serverSettingsContainer.getChildren().clear();
            Parent serverSettingsField = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettings_" + fxmlName + ".fxml"));

            switch (fxmlName) {
                case "Channel":
                    subController = new ServerSettingsChannelController(serverSettingsField, builder, server);
                    subController.init();
                    break;
                case "Privilege":
                    subController = new ServerPrivilegeSettingsController(view, builder, server);
                    subController.init();
                    break;
            }

            this.serverSettingsContainer.getChildren().add(serverSettingsField);
        } catch (Exception e) {
            System.err.println("Error on showing Server Settings Field Screen");
            e.printStackTrace();
        }
    }
}
