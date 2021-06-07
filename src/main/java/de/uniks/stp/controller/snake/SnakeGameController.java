package de.uniks.stp.controller.snake;

import de.uniks.stp.builder.ModelBuilder;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class SnakeGameController {

    private Parent view;
    private ModelBuilder builder;
    private Label scoreLabel;
    private Label highScoreLabel;
    private Canvas gameField;

    private static final int HEIGHT = 800;
    private static final int WIGHT = 800;
    private static final int ROW = 20;
    private static final int COLUMN = 20;
    private static final int FIELD_SIZE = HEIGHT / ROW;


    public SnakeGameController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    public void init() {
        scoreLabel = (Label) view.lookup("#label_score");
        highScoreLabel = (Label) view.lookup("#label_highscore");
        gameField = (Canvas) view.lookup("#gameField");
        GraphicsContext gc = gameField.getGraphicsContext2D();

        drawFieldMap(gc);
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
