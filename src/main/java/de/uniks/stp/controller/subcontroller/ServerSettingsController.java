package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.HomeViewController;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.awt.event.MouseEvent;
import java.io.IOException;

public class ServerSettingsController {
    private final HomeViewController homeViewController;
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
    private VBox settingsContainer;


    public ServerSettingsController(Parent view, ModelBuilder modelBuilder, HomeViewController homeViewController, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
        this.homeViewController = homeViewController;
    }

    public void init() {
        //init of sideButtons
        overview = (Button) view.lookup("#overview");
        channel = (Button) view.lookup("#channel");
        category = (Button) view.lookup("#category");
        privilege = (Button) view.lookup("#privilege");
        settingsContainer = (VBox) view.lookup("#serverSettingsContainer");

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
        ActionEvent actionEvent = new ActionEvent();
        onOverViewClicked(actionEvent);
    }


    /**
     * Show Overview settings decide between owner and not owner
     */
    private void onOverViewClicked(ActionEvent actionEvent) {
        if (selectedButton != overview) {
            newSelectedButton(overview);
        }
        String userId = "";
        for (User user : builder.getCurrentServer().getUser()) {
            if (user.getName().equals(builder.getPersonalUser().getName())) {
                userId = user.getId();
            }
        }
        try {
            Parent root;
            if (builder.getCurrentServer().getOwner().equals(userId)) {
                root = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettingsSubView/OverviewOwner.fxml"), StageManager.getLangBundle());
                OverviewOwnerController overviewOwnerController = new OverviewOwnerController(root, builder, homeViewController);
                overviewOwnerController.init();
            } else {
                root = FXMLLoader.load(StageManager.class.getResource("view/settings/ServerSettingsSubView/Overview.fxml"), StageManager.getLangBundle());
                OverviewController overviewController = new OverviewController(root, builder);
                overviewController.init();
            }
            this.settingsContainer.getChildren().clear();
            this.settingsContainer.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
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
            openSettings("Category");
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
                case "Category":
                    subController = new ServerSettingsCategoryController(serverSettingsField, builder, server);
                    subController.init();
                    break;
                case "Privilege":
                    subController = new ServerSettingsPrivilegeController(serverSettingsField, builder, server);
                    subController.init();
                    break;
                default:
                    break;
            }

            this.serverSettingsContainer.getChildren().add(serverSettingsField);
        } catch (Exception e) {
            System.err.println("Error on showing Server Settings Field Screen");
            e.printStackTrace();
        }
    }
}
