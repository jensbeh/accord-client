package de.uniks.stp.controller;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class AudioConnectedBoxController {

    private final Parent view;
    public Label serverNameLabel;
    public Label audioChannelNameLabel;

    public AudioConnectedBoxController(Parent view) {
        this.view = view;
    }

    public void init() {
        serverNameLabel = (Label) view.lookup("#label_serverName");
        audioChannelNameLabel = (Label) view.lookup("#label_audioChannelName");
    }

    public void setServerName(String serverNameText) {
        Platform.runLater(() -> serverNameLabel.setText(serverNameText));
    }

    public void setAudioChannelName(String audioChannelNameText) {
        Platform.runLater(() -> audioChannelNameLabel.setText(audioChannelNameText));
    }
}
