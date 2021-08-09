package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.CurrentUser;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.beans.PropertyChangeEvent;

public class UserProfileController {

    private final ModelBuilder builder;
    private final Parent view;
    public VBox root;
    public Label userName;
    private Circle onlineStatus;
    private VBox descriptionBox;
    private ImageView doNotDisturbIcon;


    public UserProfileController(Parent view, ModelBuilder builder) {
        this.builder = builder;
        this.view = view;
    }

    public void init() {
        root = (VBox) view.lookup("#root");
        userName = (Label) view.lookup("#userName");
        onlineStatus = (Circle) view.lookup("#onlineStatus");
        descriptionBox = (VBox) view.lookup("#descriptionbox");
        doNotDisturbIcon = (ImageView) view.lookup("#doNotDisturbIcon");
        showHideDoNotDisturb();
        descriptionBox.setOnMouseClicked(this::spotifyPopup);
        if (builder.getPersonalUser().getDescription() != null && !builder.getPersonalUser().getDescription().equals("") && !builder.getPersonalUser().getDescription().equals("?") && Character.toString(builder.getPersonalUser().getDescription().charAt(0)).equals("?")) {
            addGame();
        }
        Platform.runLater(() -> builder.getPersonalUser().addPropertyChangeListener(CurrentUser.PROPERTY_DESCRIPTION, this::onDescriptionChanged));
    }

    public void showHideDoNotDisturb() {
        doNotDisturbIcon.setVisible(builder.isDoNotDisturb());
    }

    private void onDescriptionChanged(PropertyChangeEvent propertyChangeEvent) {
        Label oldLabel = (Label) view.lookup("#currentGame");
        if (oldLabel != null) {
            Platform.runLater(() -> descriptionBox.getChildren().remove(oldLabel));
        }
        if (builder.isSteamShow() && !builder.getPersonalUser().getDescription().equals("") && !builder.getPersonalUser().getDescription().equals("?") && Character.toString(builder.getPersonalUser().getDescription().charAt(0)).equals("?")) {
            addGame();
        }
    }

    private void addGame() {
        if (!builder.getPersonalUser().getDescription().contains("i.scdn.co")) {
            Label currentGame = new Label();
            currentGame.setText(StageManager.getLangBundle().getString("label.steam_playing") + " " + builder.getPersonalUser().getDescription().substring(1));
            currentGame.setId("currentGame");
            Platform.runLater(() -> descriptionBox.getChildren().add(currentGame));
        }
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
        descriptionBox.setOnMouseClicked(null);
        builder.getPersonalUser().removePropertyChangeListener(this::onDescriptionChanged);
    }

    private void spotifyPopup(MouseEvent mouseEvent) {
        if (builder.getSpotifyToken() != null && builder.isSpotifyShow()) {
            builder.getSpotifyConnection().showSpotifyPopupView((HBox) ((VBox) mouseEvent.getSource()).getChildren().get(0), true, null);
            userName.setStyle("-fx-background-color: transparent");
        }
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        onDescriptionChanged(null);
    }
}
