package de.uniks.stp.controller.snake;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.snake.model.Food;
import de.uniks.stp.controller.snake.model.Game;
import de.uniks.stp.controller.snake.model.Snake;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import util.ResourceManager;

import java.util.ArrayList;
import java.util.Random;

import static de.uniks.stp.controller.snake.Constants.*;

public class SnakeGameController {

    private final Parent view;
    private final ModelBuilder builder;
    private final Scene scene;
    private Label scoreLabel;
    private Label highScoreLabel;
    private GraphicsContext brush;
    private Game game;
    private int speed = 300;
    private ArrayList<Snake> snake;
    private Food food;
    private final int snakeHead = 0;
    private ArrayList<Snake> addNewBodyQueue;
    private Timeline gameTimeline;
    private boolean gameOver;
    private Pane gameOverBox;
    private Text gameOverScoreText;
    private Button restartButton;
    private VBox gameBox;
    private Pane countDownBox;
    private Text countdownText;

    public SnakeGameController(Scene scene, Parent view, ModelBuilder builder) {
        this.scene = scene;
        this.view = view;
        this.builder = builder;
    }

    public void init() throws InterruptedException {
        gameBox = (VBox) view.lookup("#gameBox");
        countDownBox = (Pane) view.lookup("#countDownBox");
        countdownText = (Text) view.lookup("#countdownText");
        gameOverBox = (Pane) view.lookup("#gameOverBox");
        gameOverScoreText = (Text) view.lookup("#gameOverScoreText");
        restartButton = (Button) view.lookup("#restartButton");
        scoreLabel = (Label) view.lookup("#label_score");
        highScoreLabel = (Label) view.lookup("#label_highscore");
        Canvas gameField = (Canvas) view.lookup("#gameField");
        brush = gameField.getGraphicsContext2D();

        gameOverBox.setVisible(false);
        gameOverBox.setOpacity(0.0);
        countDownBox.setVisible(false);
        countdownText.setOpacity(0.0);

        restartButton.setOnAction(this::restartGame);

        game = new Game(0, ResourceManager.loadHighScore());
        snake = new ArrayList<>();
        addNewBodyQueue = new ArrayList<>();
        gameOver = false;

        scoreLabel.setText("Score: " + game.getScore());
        highScoreLabel.setText("Highscore: " + game.getHighScore());

        scene.setOnKeyPressed(key -> {
            if ((key.getCode() == KeyCode.RIGHT || key.getCode() == KeyCode.D) && game.getCurrentDirection() != Game.Direction.LEFT) {
                game.setCurrentDirection(Game.Direction.RIGHT);

            } else if ((key.getCode() == KeyCode.LEFT || key.getCode() == KeyCode.A) && game.getCurrentDirection() != Game.Direction.RIGHT) {
                game.setCurrentDirection(Game.Direction.LEFT);

            } else if ((key.getCode() == KeyCode.UP || key.getCode() == KeyCode.W) && game.getCurrentDirection() != Game.Direction.DOWN) {
                game.setCurrentDirection(Game.Direction.UP);

            } else if ((key.getCode() == KeyCode.DOWN || key.getCode() == KeyCode.S) && game.getCurrentDirection() != Game.Direction.UP) {
                game.setCurrentDirection(Game.Direction.DOWN);
            }
        });

        drawMap();
        spawnFood();
        spawnSnake();

        showCountDown(() -> {
            gameTimeline = new Timeline(new KeyFrame(Duration.millis(speed), run -> loop()));
            gameTimeline.setCycleCount(Animation.INDEFINITE);
            gameTimeline.play();
        });

    }

    private void loop() {
        if (!gameOver) {
            drawMap();
            drawFood();
            moveSnake();

            if (isGameOver()) {
                gameOver = true;
            }
        }

        if (gameOver) {
            gameTimeline.stop();
            showGameOverScreen();
            drawMap();
            System.out.println("GAME OVER !!");
        }
    }

    ////////////////////////////////////////////////
    //// Snake
    ////////////////////////////////////////////////
    private void spawnSnake() {
        Random rand = new Random();
        snake.add(snakeHead, new Snake().setPosX(rand.nextInt(COLUMN) * FIELD_SIZE).setPosY(rand.nextInt(ROW) * FIELD_SIZE));
        snake.add(1, new Snake().setPosX(snake.get(snakeHead).getPosX() - FIELD_SIZE).setPosY(snake.get(snakeHead).getPosY()));
        snake.add(2, new Snake().setPosX(snake.get(1).getPosX() - FIELD_SIZE).setPosY(snake.get(1).getPosY()));

        // draw snake
        for (int i = 0; i < snake.size(); i++) {
            // head
            if (i == 0) {
                brush.setFill(Color.web("FF0000"));
            }
            // tail
            else if (i == snake.size() - 1) {
                brush.setFill(Color.web("0000FF"));
            }
            // body
            else {
                brush.setFill(Color.web("000000"));
            }
            brush.fillRect(snake.get(i).getPosX(), snake.get(i).getPosY(), FIELD_SIZE, FIELD_SIZE);
        }
    }

    private void moveSnake() {
        for (int i = snake.size() - 1; i > snakeHead; i--) {
            snake.get(i).setPosX(snake.get(i - 1).getPosX()).setPosY(snake.get(i - 1).getPosY());
        }
        switch (game.getCurrentDirection()) {
            case UP:
                System.out.println("UP: " + snake.get(snakeHead).getPosY());
                if (snake.get(snakeHead).getPosY() == 0) {
                    snake.get(snakeHead).setPosY(HEIGHT - FIELD_SIZE);
                } else {
                    snake.get(snakeHead).addPosY(-FIELD_SIZE);
                }
                break;

            case DOWN:
                System.out.println("DOWN: " + snake.get(snakeHead).getPosY());
                if (snake.get(snakeHead).getPosY() == HEIGHT - FIELD_SIZE) {
                    snake.get(snakeHead).setPosY(0);
                } else {
                    snake.get(snakeHead).addPosY(FIELD_SIZE);
                }
                break;

            case LEFT:
                if (snake.get(snakeHead).getPosX() == 0) {
                    snake.get(snakeHead).setPosX(WIGHT - FIELD_SIZE);
                } else {
                    snake.get(snakeHead).addPosX(-FIELD_SIZE);
                }
                break;

            case RIGHT:
                if (snake.get(snakeHead).getPosX() == WIGHT - FIELD_SIZE) {
                    snake.get(snakeHead).setPosX(0);
                } else {
                    snake.get(snakeHead).addPosX(FIELD_SIZE);
                }
                break;
        }

        // draw snake
        for (int i = 0; i < snake.size(); i++) {
            // head
            if (i == 0) {
                brush.setFill(Color.web("FF0000"));
            }
            // tail
            else if (i == snake.size() - 1) {
                brush.setFill(Color.web("0000FF"));
            }
            // body
            else {
                brush.setFill(Color.web("000000"));
            }
            brush.fillRect(snake.get(i).getPosX(), snake.get(i).getPosY(), FIELD_SIZE, FIELD_SIZE);
        }

        eatFoot();
        addNewBody();
    }

    private void addNewBody() {
        if (addNewBodyQueue.size() > 0) {
            if (addNewBodyQueue.get(0).getPosX() == snake.get(snake.size() - 1).getPosX() && addNewBodyQueue.get(0).getPosY() == snake.get(snake.size() - 1).getPosY()) {
                snake.add(addNewBodyQueue.remove(0));
            }
        }
    }


    ////////////////////////////////////////////////
    //// Food
    ////////////////////////////////////////////////
    private void spawnFood() {
        food = new Food();
        drawFood();
    }

    private void drawFood() {
        brush.drawImage(food.getFoodPic(), food.getPosX(), food.getPosY(), FIELD_SIZE, FIELD_SIZE);
    }

    private void eatFoot() {
        if (snake.get(snakeHead).getPosX() == food.getPosX() && snake.get(snakeHead).getPosY() == food.getPosY()) {
            addNewBodyQueue.add(new Snake().setPosX(food.getPosX()).setPosY(food.getPosY()));
            spawnFood();
            addScore();
        }
    }

    ////////////////////////////////////////////////
    //// Map
    ////////////////////////////////////////////////
    private void drawMap() {
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

    ////////////////////////////////////////////////
    //// Additional methods
    ////////////////////////////////////////////////
    private boolean isGameOver() {
        // snake eats its itself
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(snakeHead).getPosX() == snake.get(i).getPosX() && snake.get(snakeHead).getPosY() == snake.get(i).getPosY()) {
                gameOver = true;
                return true;
            }
        }
        return false;
    }

    private void showGameOverScreen() {
        GaussianBlur blur = new GaussianBlur(0.0);
        gameBox.setEffect(blur);

        Timeline blurTimeline = new Timeline();
        KeyValue keyValue = new KeyValue(blur.radiusProperty(), 10.0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(2000), keyValue);
        blurTimeline.getKeyFrames().add(keyFrame);
        blurTimeline.play();

        if (game.getScore() > game.getHighScore()) {
            gameOverScoreText.setText(builder.getPersonalUser().getName() + ", your new highscore is " + game.getScore() + " !!!");
            game.setHighScore(game.getScore());
        } else {
            gameOverScoreText.setText(builder.getPersonalUser().getName() + ", your score is: " + game.getScore() + " !");
        }

        gameOverBox.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(2000), gameOverBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void restartGame(ActionEvent actionEvent) {
        System.out.println("RESTART GAME...");
        gameOver = false;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), gameOverBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.play();
        fadeOut.setOnFinished((ae) -> gameOverBox.setVisible(false));

        speed = 300;
        snake.clear();
        addNewBodyQueue.clear();
        food = null;
        game.setCurrentDirection(Game.Direction.RIGHT);

        game.setScore(0);
        scoreLabel.setText("Score: " + game.getScore());
        highScoreLabel.setText("Highscore: " + game.getHighScore());


        drawMap();
        spawnFood();
        spawnSnake();

        showCountDown(() -> gameTimeline.play());
    }

    public interface CountDownCallback {
        void onFinished();
    }

    private void showCountDown(CountDownCallback countDownCallback) {
        countDownBox.setVisible(true);
        countdownText.setText("3");

        GaussianBlur blur = new GaussianBlur(10.0);
        gameBox.setEffect(blur);

        Timeline blurTimeline = new Timeline();
        KeyValue keyValue = new KeyValue(blur.radiusProperty(), 00.0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), keyValue);
        blurTimeline.getKeyFrames().add(keyFrame);

        FadeTransition fadeIn1 = new FadeTransition(Duration.millis(200), countdownText);
        fadeIn1.setFromValue(0.0);
        fadeIn1.setToValue(1.0);

        FadeTransition fadeOut1 = new FadeTransition(Duration.millis(800), countdownText);
        fadeOut1.setFromValue(1.0);
        fadeOut1.setToValue(0.0);
        fadeOut1.setOnFinished((ae) -> countdownText.setText("2"));

        FadeTransition fadeIn2 = new FadeTransition(Duration.millis(200), countdownText);
        fadeIn2.setFromValue(0.0);
        fadeIn2.setToValue(1.0);

        FadeTransition fadeOut2 = new FadeTransition(Duration.millis(800), countdownText);
        fadeOut2.setFromValue(1.0);
        fadeOut2.setToValue(0.0);
        fadeOut2.setOnFinished((ae) -> countdownText.setText("1"));

        FadeTransition fadeIn3 = new FadeTransition(Duration.millis(200), countdownText);
        fadeIn3.setFromValue(0.0);
        fadeIn3.setToValue(1.0);

        FadeTransition fadeOut3 = new FadeTransition(Duration.millis(800), countdownText);
        fadeOut3.setFromValue(1.0);
        fadeOut3.setToValue(0.0);
        fadeOut3.setOnFinished((ae) -> countdownText.setText("GO!"));

        FadeTransition fadeInGO = new FadeTransition(Duration.millis(200), countdownText);
        fadeInGO.setFromValue(0.0);
        fadeInGO.setToValue(1.0);

        FadeTransition fadeOutGO = new FadeTransition(Duration.millis(1200), countdownText);
        fadeOutGO.setFromValue(1.0);
        fadeOutGO.setToValue(0.0);

        SequentialTransition seqT = new SequentialTransition(fadeIn1, fadeOut1, fadeIn2, fadeOut2, fadeIn3, fadeOut3, fadeInGO, fadeOutGO, blurTimeline);
        seqT.play();
        seqT.setOnFinished((ae) -> {
            countDownCallback.onFinished();
            countDownBox.setVisible(false);
        });
    }

    private void addScore() {
        game.setScore(game.getScore() + 100);
        scoreLabel.setText("Score: " + game.getScore());

        if (game.getScore() % 200 == 0 && speed > 100) {
            speed = speed - 10;
            gameTimeline.stop();
            gameTimeline = new Timeline(new KeyFrame(Duration.millis(speed), run -> loop()));
            gameTimeline.setCycleCount(Animation.INDEFINITE);
            gameTimeline.play();
        }
    }

    public void stop() {
        ResourceManager.saveHighScore(game.getHighScore());
        scene.setOnKeyPressed(null);
        restartButton.setOnAction(null);
        gameTimeline.stop();
    }
}