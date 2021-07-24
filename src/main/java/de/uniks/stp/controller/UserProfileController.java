package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.CurrentUser;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.beans.PropertyChangeEvent;

public class UserProfileController {

    private final ModelBuilder builder;
    public VBox root;
    public Label userName;
    private Circle onlineStatus;
    private final Parent view;
    private VBox descriptionBox;


    public UserProfileController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        root = (VBox) view.lookup("#root");
        userName = (Label) view.lookup("#userName");
        onlineStatus = (Circle) view.lookup("#onlineStatus");
        descriptionBox = (VBox) view.lookup("#descriptionbox");
        if (builder.getPersonalUser().getDescription() != null && (!builder.getPersonalUser().getDescription().equals("") && !builder.getPersonalUser().getDescription().equals("\0"))) {
            addGame();
        }

        builder.getPersonalUser().addPropertyChangeListener(CurrentUser.PROPERTY_DESCRIPTION, this::onDescriptionChanged);
    }

    private void onDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        Label oldLabel = (Label) view.lookup("#currentGame");
        if (oldLabel != null) {
            Platform.runLater(() -> descriptionBox.getChildren().remove(oldLabel));
        }
        if (!builder.getPersonalUser().getDescription().equals("") && !builder.getPersonalUser().getDescription().equals("\0")) {
            addGame();
        }
    }

    private void addGame() {
        Label currentGame = new Label();
        currentGame.setText(builder.getPersonalUser().getDescription());
        currentGame.setStyle("-fx-text-fill: white;");
        currentGame.setId("currentGame");
        Platform.runLater(() -> descriptionBox.getChildren().add(currentGame));
    }

    public void setUserName(String name) {
        Platform.runLater(() -> userName.setText(name));
    }

    public void setOnline() {
        Platform.runLater(() -> {
            Color color = Color.web("#13d86b");
            onlineStatus.setFill(color);
        });
    }

    public void stop() {
        builder.getPersonalUser().removePropertyChangeListener(this::onDescriptionChanged);
    }
}
