package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ServerProfileController {

    public StackPane root;
    public Label serverName;
    private Parent view;
    private ModelBuilder builder;

    public ServerProfileController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
    }

    public void init() {
        root = (StackPane) view.lookup("#root");
        serverName = (Label) view.lookup("#serverName");
    }

    public void setServerName(String name) {
        Platform.runLater(() -> {
            serverName.setTextFill(Color.WHITE);
            serverName.setText(name);
        });
    }
}
