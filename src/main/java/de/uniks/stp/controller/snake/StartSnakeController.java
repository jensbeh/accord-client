package de.uniks.stp.controller.snake;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class StartSnakeController {

    private Parent view;
    private ModelBuilder builder;
    private Label congratsLabel;
    private Label easterEggFoundLabel;
    private Button startGame;

    /**
     * Controller to control the start Snake view
     */
    public StartSnakeController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        congratsLabel = (Label) view.lookup("#label_congrats");
        easterEggFoundLabel = (Label) view.lookup("#label_you-found");
        startGame = (Button) view.lookup("#button_start");

        startGame.setOnAction(this::startGame);
    }

    /**
     * OnClick method -> the game will starts
     */
    private void startGame(ActionEvent actionEvent) {
        System.out.println("Starting Snake...");

        StageManager.snakeScreen();
    }

    public void stop() {
        startGame.setOnAction(null);
    }
}