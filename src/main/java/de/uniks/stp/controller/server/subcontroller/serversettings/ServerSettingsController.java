package de.uniks.stp.controller.server.subcontroller.serversettings;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.SubSetting;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ServerSettingsController {
    private final Parent view;
    private final ModelBuilder builder;
    private final Server server;
    private Pane root;
    private Button selectedButton;
    private Button overview;
    private Button channel;
    private Button category;
    private Button privilege;
    private VBox serverSettingsContainer;
    private SubSetting subController;
    private VBox settingsContainer;
    private String userId;
    private VBox settingsBox;
    private TitleBarController titleBarController;


    public ServerSettingsController(Parent view, ModelBuilder modelBuilder, Server server) {
        this.view = view;
        this.builder = modelBuilder;
        this.server = server;
    }

    public void init(Stage stage) {
        // create titleBar
        HBox titleBarBox = (HBox) view.lookup("#titleBarBox");
        titleBarController = new TitleBarController(stage, titleBarBox, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);
        titleBarController.setTitle(StageManager.getLangBundle().getString("window_title_serverSettings"));

        //init of sideButtons
        root = (Pane) view.lookup("#root");
        overview = (Button) view.lookup("#overviewBtn");
        channel = (Button) view.lookup("#channelBtn");
        category = (Button) view.lookup("#categoryBtn");
        privilege = (Button) view.lookup("#privilegeBtn");
        settingsContainer = (VBox) view.lookup("#serverSettingsContainer");
        settingsBox = (VBox) view.lookup("#settingsBox");

        // userId from currentUser
        userId = "";
        for (User user : builder.getCurrentServer().getUser()) {
            if (user.getName().equals(builder.getPersonalUser().getName())) {
                userId = user.getId();
            }
        }
        if (!builder.getCurrentServer().getOwner().equals(userId)) {
            channel.setVisible(false);
            category.setVisible(false);
            privilege.setVisible(false);
        }

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
        System.out.println("builder: " + builder.getCurrentServer().getOwner());
        System.out.println("userid: " + userId);
        if (selectedButton != overview) {
            newSelectedButton(overview);
        }
        try {
            Parent root;
            if (builder.getCurrentServer().getOwner().equals(userId)) {
                root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/serversettings/OverviewOwner.fxml")), StageManager.getLangBundle());
                OverviewOwnerController overviewOwnerController = new OverviewOwnerController(root, builder);
                overviewOwnerController.init();
            } else {
                root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/serversettings/Overview.fxml")), StageManager.getLangBundle());
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
            Parent serverSettingsField = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/serversettings/ServerSettings_" + fxmlName + ".fxml")), StageManager.getLangBundle());

            switch (fxmlName) {
                case "Channel":
                    subController = new ServerSettingsChannelController(serverSettingsField, builder, server);
                    subController.init();
                    subController.setTheme();
                    break;
                case "Category":
                    subController = new ServerSettingsCategoryController(serverSettingsField, builder, server);
                    subController.init();
                    subController.setTheme();
                    break;
                case "Privilege":
                    subController = new ServerSettingsPrivilegeController(serverSettingsField, builder, server);
                    subController.init();
                    subController.setTheme();
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
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/ServerSettings.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }
}
