package de.uniks.stp.controller.snake;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StartSnakeController {

    private Parent view;
    private ModelBuilder builder;
    private Label congrats;
    private Label youFound;
    private Button startGame;

    public StartSnakeController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        congrats = (Label) view.lookup("#label_congrats");
        youFound = (Label) view.lookup("#label_you-found");
        startGame = (Button) view.lookup("#button_start");

        startGame.setOnAction(this::startGame);
    }

    private void startGame(ActionEvent actionEvent) {
        System.out.println("Starting Snake...");
    }

    public void stop() {
        startGame.setOnAction(null);
    }
}