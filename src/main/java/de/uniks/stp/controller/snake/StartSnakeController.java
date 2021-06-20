package de.uniks.stp.controller.snake;

import de.uniks.stp.StageManager;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class StartSnakeController {

    private final Parent view;
    private Button startGame;

    /**
     * Controller to control the start Snake view
     */
    public StartSnakeController(Parent view) {
        this.view = view;
    }

    public void init() {
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