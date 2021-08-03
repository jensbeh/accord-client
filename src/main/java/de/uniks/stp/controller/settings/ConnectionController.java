package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.subcontroller.SteamLoginController;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ConnectionController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private final Button spotifyToggleButton = new Button();
    private final Button steamToggleButton = new Button();
    private final Rectangle backgroundSpotifyButton = new Rectangle(30, 10, Color.RED);
    private final Rectangle backgroundSteamButton = new Rectangle(30, 10, Color.RED);
    StackPane spotifyToggleStackPane;
    StackPane steamToggleStackPane;
    private SteamLoginController steamLoginController;
    private VBox steamVbox;
    private boolean spotifyShow;
    private boolean steamShow;
    private Button spotifyDisconnect;
    private VBox spotifyVbox;

    public ConnectionController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        ImageView spotifyView = (ImageView) view.lookup("#spotify");
        ImageView steamView = (ImageView) view.lookup("#steam");
        spotifyToggleStackPane = (StackPane) view.lookup("#spotifyToggleStackPane");
        steamToggleStackPane = (StackPane) view.lookup("#steamToggleStackPane");
        spotifyToggleStackPane.setOnMouseClicked(this::spotifyToggle);
        steamToggleStackPane.setOnMouseClicked(this::steamToggle);

        spotifyDisconnect = (Button) view.lookup("#disconnectSpotify");
        spotifyDisconnect.setOnMouseClicked(this::disconnectSpotify);

        spotifyVbox = (VBox) view.lookup("#spotifyVbox");
        steamVbox = (VBox) view.lookup("#steamVbox");
        spotifyVbox.setVisible(false);
        steamVbox.setVisible(false);

        spotifyView.setOnMouseClicked(this::onSpotifyChange);
        steamView.setOnMouseClicked(this::onSteamChange);
        if (builder.getSpotifyToken() != null) {
            spotifyShow = builder.isSpotifyShow();
            toggleInit(spotifyToggleStackPane, backgroundSpotifyButton, spotifyToggleButton, spotifyShow);
            spotifyVbox.setVisible(true);
        }
        if (!builder.getSteamToken().equals("")) {
            steamShow = builder.isSteamShow();
            toggleInit(steamToggleStackPane, backgroundSteamButton, steamToggleButton, steamShow);
            steamVbox.setVisible(true);
        }
        steamToggleButton.setOnAction(this::startGame);
    }

    private void startGame(ActionEvent actionEvent) {
        if (builder.isSteamShow()) {
            builder.getGame();
        }
    }

    private void onSpotifyChange(MouseEvent mouseEvent) {
        builder.getSpotifyConnection().init(this);
        builder.getSpotifyConnection().setTheme();
    }

    private void onSteamChange(MouseEvent mouseEvent) {
        steamLoginController = new SteamLoginController(builder);
        steamLoginController.refresh(this::refreshSteam);
        steamLoginController.init();
        steamLoginController.setTheme();
        init();
    }

    private void refreshSteam() {
        if (!steamVbox.isVisible()) {
            steamVbox.setVisible(true);
        }
        steamLoginController = null;
        init();
    }

    private void disconnectSpotify(MouseEvent mouseEvent) {
        builder.setSpotifyRefresh(null);
        builder.setSpotifyToken(null);
        builder.setSpotifyShow(false);
        if (builder.getSpotifyConnection() != null) {
            builder.getSpotifyConnection().stopDescriptionScheduler();
        }
        builder.saveSettings();
        spotifyVbox.setVisible(false);
    }

    private void spotifyToggle(MouseEvent mouseEvent) {
        if (builder.isSpotifyShow() && spotifyToggleStackPane.getAlignment() == Pos.CENTER_RIGHT) {
            spotifyToggleButton.getStyleClass().clear();
            spotifyToggleButton.getStyleClass().add("buttonOff");
            backgroundSpotifyButton.getStyleClass().clear();
            backgroundSpotifyButton.getStyleClass().add("backgroundOff");
            spotifyToggleStackPane.setAlignment(spotifyToggleButton, Pos.CENTER_LEFT);
        } else {
            spotifyToggleButton.getStyleClass().clear();
            spotifyToggleButton.getStyleClass().add("buttonOn");
            backgroundSpotifyButton.getStyleClass().clear();
            backgroundSpotifyButton.getStyleClass().add("backgroundOn");
            spotifyToggleStackPane.setAlignment(spotifyToggleButton, Pos.CENTER_RIGHT);

            steamToggleButton.getStyleClass().clear();
            steamToggleButton.getStyleClass().add("buttonOff");
            backgroundSteamButton.getStyleClass().clear();
            backgroundSteamButton.getStyleClass().add("backgroundOff");
            steamToggleStackPane.setAlignment(steamToggleButton, Pos.CENTER_LEFT);

            builder.setSpotifyShow(true);
            builder.setSteamShow(false);
            builder.getPersonalUser().setDescription("#");
        }
    }

    private void steamToggle(MouseEvent mouseEvent) {
        if (builder.isSteamShow() && steamToggleStackPane.getAlignment() == Pos.CENTER_RIGHT) {
            spotifyToggleButton.getStyleClass().clear();
            spotifyToggleButton.getStyleClass().add("buttonOff");
            backgroundSpotifyButton.getStyleClass().clear();
            backgroundSpotifyButton.getStyleClass().add("backgroundOff");
            spotifyToggleStackPane.setAlignment(spotifyToggleButton, Pos.CENTER_LEFT);


        } else {
            steamToggleButton.getStyleClass().clear();
            steamToggleButton.getStyleClass().add("buttonOn");
            backgroundSteamButton.getStyleClass().clear();
            backgroundSteamButton.getStyleClass().add("backgroundOn");
            steamToggleStackPane.setAlignment(steamToggleButton, Pos.CENTER_RIGHT);

            spotifyToggleButton.getStyleClass().clear();
            spotifyToggleButton.getStyleClass().add("buttonOff");
            backgroundSpotifyButton.getStyleClass().clear();
            backgroundSpotifyButton.getStyleClass().add("backgroundOff");
            spotifyToggleStackPane.setAlignment(spotifyToggleButton, Pos.CENTER_LEFT);

            builder.setSpotifyShow(false);
            builder.setSteamShow(true);
            builder.getGame();
            builder.getPersonalUser().setDescription("?");
        }
    }

    private void toggleInit(StackPane stackPane, Rectangle backgroundToggle, Button toggleButton, Boolean toggleShow) {
        setBackgroundToggleButton(stackPane, backgroundToggle, toggleButton);
        if (toggleShow) {
            toggleButton.getStyleClass().clear();
            toggleButton.getStyleClass().add("buttonOn");
            backgroundToggle.getStyleClass().clear();
            backgroundToggle.getStyleClass().add("backgroundOn");
            StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
        } else {
            toggleButton.getStyleClass().clear();
            toggleButton.getStyleClass().add("buttonOff");
            backgroundToggle.getStyleClass().clear();
            backgroundToggle.getStyleClass().add("backgroundOff");
            StackPane.setAlignment(toggleButton, Pos.CENTER_LEFT);
        }

        toggleButton.setFocusTraversable(false);
    }

    private void setBackgroundToggleButton(StackPane toggleStackPane, Rectangle backgroundToggleButton, Button toggleButton) {
        toggleStackPane.getChildren().clear();
        toggleStackPane.getChildren().addAll(backgroundToggleButton, toggleButton);
        toggleStackPane.setMinSize(30, 15);
        backgroundToggleButton.maxWidth(30);
        backgroundToggleButton.minWidth(30);
        backgroundToggleButton.maxHeight(10);
        backgroundToggleButton.minHeight(10);
        backgroundToggleButton.setArcHeight(backgroundToggleButton.getHeight());
        backgroundToggleButton.setArcWidth(backgroundToggleButton.getHeight());
        backgroundToggleButton.setFill(Color.valueOf("#ced5da"));
        toggleButton.setShape(new Circle(5));
        StackPane.setAlignment(toggleButton, Pos.CENTER_LEFT);
        toggleButton.setMaxSize(15, 15);
        toggleButton.setMinSize(15, 15);
    }
}
