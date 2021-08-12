package de.uniks.stp.controller.snake;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class StartSnakeController {

    private final Parent view;
    private final ModelBuilder builder;
    private Button startGame;
    private Button exitGame;
    private TitleBarController titleBarController;

    /**
     * Controller to control the start Snake view
     */
    public StartSnakeController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init(Stage stage) {
        // create titleBar
        HBox titleBarBox = (HBox) view.lookup("#titleBarBox");
        Parent titleBarView = null;
        try {
            titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), builder.getStageManager().getLangBundle());
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleBarBox.getChildren().add(titleBarView);
        titleBarController = new TitleBarController(stage, titleBarView, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);
        titleBarController.setTitle("Snake");
        stage.setTitle("Snake");

        startGame = (Button) view.lookup("#button_start");
        exitGame = (Button) view.lookup("#button_exit");

        startGame.setOnAction(this::startGame);
        exitGame.setOnAction(this::exitGame);
    }

    /**
     * OnClick method -> the game will starts
     */
    private void startGame(ActionEvent actionEvent) {
        Platform.runLater(() -> builder.getStageManager().snakeScreen());
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

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        view.getStylesheets().clear();
        view.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/snake.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }

    private void setDarkMode() {
        view.getStylesheets().clear();
        view.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/snake.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }
}