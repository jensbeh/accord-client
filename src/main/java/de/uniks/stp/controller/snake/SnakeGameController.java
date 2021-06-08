package de.uniks.stp.controller.snake;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.snake.model.Food;
import de.uniks.stp.controller.snake.model.Game;
import de.uniks.stp.controller.snake.model.Snake;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import util.ResourceManager;

import static de.uniks.stp.controller.snake.Constants.*;

public class SnakeGameController {

    private Parent view;
    private ModelBuilder builder;
    private Scene scene;
    private Label scoreLabel;
    private Label highScoreLabel;
    private Canvas gameField;
    private Game game;
    private Snake snake;
    private Food food;


    public SnakeGameController(Scene scene, Parent view, ModelBuilder builder) {
        this.scene = scene;
        this.view = view;
        this.builder = builder;
    }

    public void init() throws InterruptedException {
        scoreLabel = (Label) view.lookup("#label_score");
        highScoreLabel = (Label) view.lookup("#label_highscore");
        gameField = (Canvas) view.lookup("#gameField");
        GraphicsContext brush = gameField.getGraphicsContext2D();
        scoreLabel.setText("Score:");

        game = new Game(0, ResourceManager.loadHighScore());

        scene.setOnKeyPressed(key -> {
            if (key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.D) {
                game.setCurrentDirection(Game.Direction.RIGHT);

            } else if (key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.A) {
                game.setCurrentDirection(Game.Direction.LEFT);

            } else if (key.getCode() == KeyCode.UP || key.getCode() == KeyCode.W) {
                game.setCurrentDirection(Game.Direction.UP);

            } else if (key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.S) {
                game.setCurrentDirection(Game.Direction.DOWN);
            }
        });

        drawFieldMap(brush);
        spawnFood(brush);
        spawnSnake(brush);

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), run -> main(brush)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void main(GraphicsContext brush) {
        System.out.println("RUN");
        drawFieldMap(brush);
        drawFood(brush);
        moveSnake(brush);
    }


    private void spawnSnake(GraphicsContext brush) {
        snake = new Snake();
        brush.setFill(Color.web("000000"));
        brush.fillRect(snake.getPosX(), snake.getPosY(), FIELD_SIZE, FIELD_SIZE);
    }

    private void moveSnake(GraphicsContext brush) {
        switch (game.getCurrentDirection()) {
            case UP:
                snake.addPosY(-FIELD_SIZE);
                break;

            case DOWN:
                snake.addPosY(FIELD_SIZE);
                break;

            case LEFT:
                snake.addPosX(-FIELD_SIZE);
                break;

            case RIGHT:
                snake.addPosX(FIELD_SIZE);
                break;
        }
        brush.setFill(Color.web("000000"));
        brush.fillRect(snake.getPosX(), snake.getPosY(), FIELD_SIZE, FIELD_SIZE);
    }

    private void spawnFood(GraphicsContext brush) {
        food = new Food();
        brush.setFill(Color.web("FFFFFF"));
        brush.fillRect(food.getPosX() * FIELD_SIZE, food.getPosY() * FIELD_SIZE, FIELD_SIZE, FIELD_SIZE);
    }

    private void drawFood(GraphicsContext brush) {
        brush.setFill(Color.web("FFFFFF"));
        brush.fillRect(food.getPosX() * FIELD_SIZE, food.getPosY() * FIELD_SIZE, FIELD_SIZE, FIELD_SIZE);
    }

    private void drawFieldMap(GraphicsContext brush) {
        for (int row = 0; row < ROW; row++) {
            for (int column = 0; column < COLUMN; column++) {
                if (row % 2 == 0) {
                    if (column % 2 == 0) {
                        brush.setFill(Color.web("8FDD37"));
                    } else {
                        brush.setFill(Color.web("6DCC01"));
                    }
                } else {
                    if (column % 2 == 1) {
                        brush.setFill(Color.web("8FDD37"));
                    } else {
                        brush.setFill(Color.web("6DCC01"));
                    }
                }
                brush.fillRect(column * FIELD_SIZE, row * FIELD_SIZE, FIELD_SIZE, FIELD_SIZE);
            }
        }
    }

    public void stop() {
        ResourceManager.saveHighScore(game.getHighScore());
    }
}
