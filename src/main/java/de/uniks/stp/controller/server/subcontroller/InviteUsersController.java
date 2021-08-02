package de.uniks.stp.controller.server.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ResourceBundle;

public class InviteUsersController {
    private RadioButton tempSelected;
    private RadioButton userLimitSelected;
    private VBox inviteBox;
    private Label tempLabel;
    private Label userLimitLabel;
    private final Server server;
    private final ModelBuilder builder;
    private Parent view;
    private VBox root;
    private InviteUsersTempSubController inviteUsersTempSubController;
    private InviteUsersUserLimitSubController inviteUsersUserLimitSubController;
    private TitleBarController titleBarController;

    public InviteUsersController(Parent view, ModelBuilder builder, Server currentServer) {
        this.view = view;
        this.builder = builder;
        this.server = currentServer;
    }

    public void init(Stage stage) {
        // create titleBar
        HBox titleBarBox = (HBox) view.lookup("#titleBarBox");
        titleBarController = new TitleBarController(stage, titleBarBox, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);

        root = (VBox) view.lookup("#rootInvite");
        tempSelected = (RadioButton) view.lookup("#tempSelected");
        userLimitSelected = (RadioButton) view.lookup("#userLimitSelected");
        tempSelected.setOnAction(this::tempSelected);
        userLimitSelected.setOnAction(this::userLimitSelected);
        inviteBox = (VBox) view.lookup("#inviteBox");
        tempLabel = (Label) view.lookup("#tempLabel");
        userLimitLabel = (Label) view.lookup("#userLimitLabel");
        tempSelected(null);
    }

    public void stop() {
        tempSelected.setOnAction(null);
        userLimitSelected.setOnAction(null);
    }

    private void tempSelected(ActionEvent actionEvent) {
        cleanup();
        inviteBox.getChildren().clear();
        try {
            //view
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/invite users/inviteUsersTemp.fxml")), StageManager.getLangBundle());
            //Controller
            inviteUsersTempSubController = new InviteUsersTempSubController(view, builder, server);
            inviteUsersTempSubController.init();
            inviteBox.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error in tempSelected");
            e.printStackTrace();
        }
    }

    private void userLimitSelected(ActionEvent actionEvent) {
        cleanup();
        inviteBox.getChildren().clear();
        try {
            //view
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/invite users/inviteUsersUserLimit.fxml")), StageManager.getLangBundle());
            //Controller
            inviteUsersUserLimitSubController = new InviteUsersUserLimitSubController(view, builder, server);
            inviteUsersUserLimitSubController.init();
            inviteBox.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error in tempSelected");
            e.printStackTrace();
        }
    }

    private void cleanup() {
        if (inviteUsersTempSubController != null) {
            inviteUsersTempSubController.stop();
            inviteUsersTempSubController = null;
        }
        if (inviteUsersUserLimitSubController != null) {
            inviteUsersUserLimitSubController.stop();
            inviteUsersUserLimitSubController = null;
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (tempLabel != null)
            tempLabel.setText(lang.getString("label.temp"));

        if (userLimitLabel != null)
            userLimitLabel.setText(lang.getString("label.userLimit"));

        if (inviteUsersTempSubController != null) {
            inviteUsersTempSubController.onLanguageChanged();
        }
        if (inviteUsersUserLimitSubController != null) {
            inviteUsersUserLimitSubController.onLanguageChanged();
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
