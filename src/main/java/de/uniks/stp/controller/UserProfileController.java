package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class UserProfileController {

    public HBox root;
    public Label userName;
    private Circle onlineStatus;
    private Parent view;
    private ModelBuilder builder;


    public UserProfileController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        root = (HBox) view.lookup("#root");
        userName = (Label) view.lookup("#userName");
        onlineStatus = (Circle) view.lookup("#onlineStatus");
    }

    public void setUserName(String name) {
        Platform.runLater(() -> {
            userName.setText(name);
        });
    }

    public void setOnline() {
        Platform.runLater(() -> {
            Color color = Color.web("#13d86b");
            onlineStatus.setFill(color);
        });
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }
}
