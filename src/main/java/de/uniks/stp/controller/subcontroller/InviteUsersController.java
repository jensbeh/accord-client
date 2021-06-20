package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class InviteUsersController {
    private static RadioButton tempSelected;
    private static RadioButton userLimitSelected;
    private static VBox inviteBox;
    private final Server server;
    private final ModelBuilder builder;
    private static Parent view;
    private InviteUsersTempSubController inviteUsersTempSubController;
    private InviteUsersUserLimitSubController inviteUsersUserLimitSubController;

    public InviteUsersController(Parent view, ModelBuilder builder, Server currentServer) {
        InviteUsersController.view = view;
        this.builder = builder;
        this.server = currentServer;
    }

    public void init() {
        tempSelected = (RadioButton) view.lookup("#tempSelected");
        userLimitSelected = (RadioButton) view.lookup("#userLimitSelected");
        tempSelected.setOnAction(this::tempSelected);
        userLimitSelected.setOnAction(this::userLimitSelected);
        inviteBox = (VBox) view.lookup("#inviteBox");
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
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/invite users/inviteUsersTemp.fxml")));
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
            Parent view = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("view/invite users/inviteUsersUserLimit.fxml")));
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


}
