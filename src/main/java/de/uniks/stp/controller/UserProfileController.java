package de.uniks.stp.controller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class UserProfileController {

    private final ModelBuilder builder;
    public VBox root;
    public Label userName;
    private Circle onlineStatus;
    private final Parent view;
    private HBox userBox;
    private Label bandAndSong;
    private Label timePlayed;
    private Label timeTotal;
    private ProgressBar progressBar;
    private ImageView spotifyArtwork;

    public UserProfileController(Parent view, ModelBuilder builder) {
        this.builder = builder;
        this.view = view;
    }

    public void init() {
        root = (VBox) view.lookup("#root");
        userBox = (HBox) view.lookup("#userBox");
        userName = (Label) view.lookup("#userName");
        onlineStatus = (Circle) view.lookup("#onlineStatus");
        userBox.setOnMouseClicked(this::spotifyPopup);
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
        userBox.setOnMouseClicked(null);
    }

    private void spotifyPopup(MouseEvent mouseEvent) {
        if (builder.getSpotifyToken() != null) {
            builder.getSpotifyConnection().showSpotifyPopupView(mouseEvent, true, null);
            userName.setStyle("-fx-background-color: transparent");
        }
    }
}
