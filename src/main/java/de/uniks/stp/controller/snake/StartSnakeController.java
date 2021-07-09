package de.uniks.stp.controller.snake;

import de.uniks.stp.StageManager;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class StartSnakeController {

    private final Parent view;
    private Button startGame;
    private Button exitGame;

    /**
     * Controller to control the start Snake view
     */
    public StartSnakeController(Parent view) {
        this.view = view;
    }

    public void init() {
        startGame = (Button) view.lookup("#button_start");
        exitGame = (Button) view.lookup("#button_exit");

        startGame.setOnAction(this::startGame);
        exitGame.setOnAction(this::exitGame);
    }

    /**
     * OnClick method -> the game will starts
     */
    private void startGame(ActionEvent actionEvent) {
        StageManager.snakeScreen();
    }

    /**
     * OnClick method -> the stage will close
     */
    private void exitGame(ActionEvent actionEvent) {
        Stage thisStage = (Stage) exitGame.getScene().getWindow();
        thisStage.fireEvent(new WindowEvent(thisStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void stop() {
        startGame.setOnAction(null);
        exitGame.setOnAction(null);
    }
}