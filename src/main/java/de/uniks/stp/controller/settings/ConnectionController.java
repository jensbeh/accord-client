package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
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

import java.util.Stack;

public class ConnectionController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private ImageView steamView;
    private ImageView spotifyView;
    private StackPane spotifyToggleStackPane;
    private Button spotifyToggleButton = new Button();
    private Boolean spotifyToggle;
    private StackPane steamToggleStackPane;
    private Button steamToggleButton = new Button();
    private Boolean steamToggle;
    private final Rectangle backgroundSpotifyButton = new Rectangle(30, 10, Color.RED);
    private final Rectangle backgroundSteamButton = new Rectangle(30, 10, Color.RED);
    private VBox spotifyVbox;
    private VBox steamVbox;

    public ConnectionController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        spotifyView = (ImageView) view.lookup("#spotify");
        steamView = (ImageView) view.lookup("#steam");
        spotifyToggleStackPane = (StackPane) view.lookup("#spotifyToggleStackPane");
        steamToggleStackPane = (StackPane) view.lookup("#steamToggleStackPane");

        spotifyVbox = (VBox) view.lookup("#spotifyVbox");
        steamVbox = (VBox) view.lookup("#steamVbox");
        spotifyVbox.setVisible(false);
        steamVbox.setVisible(false);

        spotifyView.setOnMouseClicked(this::onSpotifyChange);
        steamView.setOnMouseClicked(this::onSteamChange);
        if (!builder.getSpotifyToken().equals("")) {
            toggleInit(spotifyToggleStackPane, backgroundSpotifyButton, spotifyToggleButton, builder.isSpotifyShow());
            spotifyVbox.setVisible(true);
        }
        if (!builder.getSteamToken().equals("")) {
            toggleInit(steamToggleStackPane, backgroundSteamButton, steamToggleButton, builder.isSteamShow());
            steamVbox.setVisible(true);
        }
    }

    private void onSpotifyChange(MouseEvent mouseEvent) {
        System.out.println("Spotify");
        builder.setSpotifyToken("test");
        builder.saveSettings();
        init();
    }

    private void onSteamChange(MouseEvent mouseEvent) {
        //TODO functionality for steam connect
        System.out.println("Steam");
        builder.setSteamToken("test");
        builder.saveSettings();
        init();
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
        EventHandler<Event> click = new EventHandler<Event>() {
            @Override
            public void handle(Event e) {
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
                    } else {
                        builder.setSteamShow(false);
                    }
                } else {
                    //Button on
                    toggleButton.getStyleClass().add("buttonOn");
                    backgroundToggle.getStyleClass().add("backgroundOn");
                    StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);
                    toggleShowFinal[0] = true;
                    if (stackPane.getId().contains("spotifyToggleStackPane")) {
                        builder.setSpotifyShow(true);
                    } else {
                        builder.setSteamShow(true);
                    }
                }
                builder.saveSettings();
            }
        };
        toggleButton.setFocusTraversable(false);
        stackPane.setOnMouseClicked(click);
        toggleButton.setOnMouseClicked(click);
    }
}
