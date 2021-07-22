package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

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
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/SpotifyView.fxml")));
                VBox spotifyRoot = (VBox) root.lookup("spotifyRoot");
                spotifyArtwork = (ImageView) root.lookup("#spotifyArtwork");
                bandAndSong = (Label) root.lookup("#bandAndSong");
                timePlayed = (Label) root.lookup("#timePlayed");
                timeTotal = (Label) root.lookup("#timeTotal");
                progressBar = (ProgressBar) root.lookup("#progressBar");
                builder.getSpotifyConnection().spotifyListener(bandAndSong, spotifyArtwork, timePlayed, timeTotal, progressBar);

                HBox hBox = (HBox) mouseEvent.getSource();
                hBox.setStyle("-fx-background-color: #1db954; -fx-background-radius: 0 5 5 0;");
                userName.setStyle("-fx-background-color: transparent");
                Bounds bounds = ((HBox) mouseEvent.getSource()).localToScreen(((HBox) mouseEvent.getSource()).getBoundsInLocal());
                double x = bounds.getMinX() - 200;
                double y = bounds.getMinY();

                final Stage dialog = new Stage();
                dialog.initOwner(hBox.getScene().getWindow());
                dialog.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        dialog.close();
                        hBox.setStyle("-fx-background-color: transparent;");
                    }
                });
                Scene scene = new Scene(root);
                scene.setFill(Color.TRANSPARENT);
                dialog.initStyle(StageStyle.TRANSPARENT);
                dialog.setX(x);
                dialog.setY(y);
                dialog.setScene(scene);
                dialog.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
