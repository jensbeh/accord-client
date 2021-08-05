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
import javafx.scene.layout.HBox;
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
    private HBox steamHBox;

    public ConnectionController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        ImageView spotifyView = (ImageView) view.lookup("#spotify");
        ImageView steamView = (ImageView) view.lookup("#steam");
        spotifyToggleStackPane = (StackPane) view.lookup("#spotifyToggleStackPane");
        steamToggleStackPane = (StackPane) view.lookup("#steamToggleStackPane");
        Button steamDisconnectButton = (Button) view.lookup("#disconnectSteam");
        steamDisconnectButton.setOnAction(this::disconnectSteam);

        VBox spotifyVbox = (VBox) view.lookup("#spotifyVbox");
        steamHBox = (HBox) view.lookup("#steamHBox");
        spotifyVbox.setVisible(false);
        steamHBox.setVisible(false);

        spotifyView.setOnMouseClicked(this::onSpotifyChange);
        steamView.setOnMouseClicked(this::onSteamChange);
        if (builder.getSpotifyToken() != null) {
            boolean spotifyShow = builder.isSpotifyShow();
            toggleInit(spotifyToggleStackPane, backgroundSpotifyButton, spotifyToggleButton, spotifyShow);
            spotifyVbox.setVisible(true);
        }
        showSteam();
        steamToggleButton.setOnAction(this::startGame);
    }

    private void showSteam() {
        if (!builder.getSteamToken().equals("")) {
            boolean steamShow = builder.isSteamShow();
            toggleInit(steamToggleStackPane, backgroundSteamButton, steamToggleButton, steamShow);
            steamHBox.setVisible(true);
        }
    }

    private void disconnectSteam(ActionEvent actionEvent) {
        builder.setSteamToken("");
        builder.getPersonalUser().setDescription("?");
        builder.stopGame();
        builder.setSteamShow(false);
        builder.saveSettings();
        init();
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
    }

    private void refreshSteam() {
        steamLoginController = null;
        init();
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

        final boolean[] toggleShowFinal = {toggleShow};
        EventHandler<Event> click = e -> {
            toggleButton.getStyleClass().clear();
            backgroundToggle.getStyleClass().clear();
            if (toggleShowFinal[0]) {
                //Button off
                toggleButton.getStyleClass().add("buttonOff");
                backgroundToggle.getStyleClass().add("backgroundOff");
                StackPane.setAlignment(toggleButton, Pos.CENTER_LEFT);
                toggleShowFinal[0] = false;
                if (stackPane.getId().contains("spotifyToggleStackPane")) {
                    builder.setSpotifyShow(false);
                    builder.getPersonalUser().setDescription("#");
                } else {
                    builder.setSteamShow(false);
                    builder.getPersonalUser().setDescription("?");
                }
            } else {
                //Button on
                toggleButton.getStyleClass().add("buttonOn");
                backgroundToggle.getStyleClass().add("backgroundOn");
                StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
                toggleShowFinal[0] = true;
                if (stackPane.getId().contains("spotifyToggleStackPane")) {
                    StackPane.setAlignment(steamToggleButton, Pos.CENTER_LEFT);
                    backgroundSteamButton.getStyleClass().clear();
                    backgroundSteamButton.getStyleClass().add("backgroundOff");
                    builder.getPersonalUser().setDescription("#");
                    builder.setSpotifyShow(true);
                    builder.setSteamShow(false);
                } else {
                    StackPane.setAlignment(spotifyToggleButton, Pos.CENTER_LEFT);
                    backgroundSpotifyButton.getStyleClass().clear();
                    backgroundSpotifyButton.getStyleClass().add("backgroundOff");
                    builder.getPersonalUser().setDescription("?");
                    builder.setSteamShow(true);
                    builder.setSpotifyShow(false);
                    builder.getGame();
                }
            }
            builder.saveSettings();
        };
        toggleButton.setFocusTraversable(false);
        stackPane.setOnMouseClicked(click);
        toggleButton.setOnMouseClicked(click);
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
