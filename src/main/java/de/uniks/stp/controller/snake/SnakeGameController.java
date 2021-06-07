package de.uniks.stp.controller.snake;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.snake.model.Food;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import static de.uniks.stp.controller.snake.Constants.*;

public class SnakeGameController {

    private Parent view;
    private ModelBuilder builder;
    private Label scoreLabel;
    private Label highScoreLabel;
    private Canvas gameField;

    public SnakeGameController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() throws InterruptedException {
        scoreLabel = (Label) view.lookup("#label_score");
        highScoreLabel = (Label) view.lookup("#label_highscore");
        gameField = (Canvas) view.lookup("#gameField");
        GraphicsContext gc = gameField.getGraphicsContext2D();

        drawFieldMap(gc);

        spawnFood(gc);
    }

    private void spawnFood(GraphicsContext gc) {
        Food food = new Food();
        gc.setFill(Color.web("FFFFFF"));
        gc.fillRect(food.getPosX() * FIELD_SIZE, food.getPosY() * FIELD_SIZE, FIELD_SIZE, FIELD_SIZE);
    }

    private void drawFieldMap(GraphicsContext gc) {
        for (int row = 0; row < ROW; row++) {
            for (int column = 0; column < COLUMN; column++) {
                if (row % 2 == 0) {
                    if (column % 2 == 0) {
                        gc.setFill(Color.web("8FDD37"));
                    } else {
                        gc.setFill(Color.web("6DCC01"));
                    }
                } else {
                    if (column % 2 == 1) {
                        gc.setFill(Color.web("8FDD37"));
                    } else {
                        gc.setFill(Color.web("6DCC01"));
                    }
                }
                gc.fillRect(column * FIELD_SIZE, row * FIELD_SIZE, FIELD_SIZE, FIELD_SIZE);
            }
        }
    }

    public void stop() {

    }
}
