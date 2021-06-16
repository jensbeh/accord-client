package de.uniks.stp;

import de.uniks.stp.controller.snake.SnakeGameController;
import de.uniks.stp.controller.snake.model.Food;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.uniks.stp.controller.snake.Constants.FIELD_SIZE;

public class SnakeControllerTest extends ApplicationTest {
    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
        this.restClient = new RestClient();
    }

    public void loginInit() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserMainName = body.getObject().getJSONObject("data").getString("name");
            testUserMainPw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);

        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(testUserMainPw);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    //@Test
    public void openStartGameViewTest() throws InterruptedException {
        loginInit();

        // clicks 15 times on home
        Circle homeButton = lookup("#homeButton").query();
        for (int i = 0; i < 10; i++) {
            clickOn(homeButton);
        }

        // check if title is correct
        boolean found = false;
        for (Object object : this.listTargetWindows()) {
            if (((Stage) object).getTitle().equals("Snake")) {
                Stage snake = (Stage) object;
                found = true;
            }
        }
        if (!found) {
            Assert.fail();
        }

        // close start Snake view
        for (Object object : this.listTargetWindows()) {
            if (((Stage) object).getTitle().equals("Snake")) {
                Platform.runLater(((Stage) object)::close);
                Thread.sleep(2000);
                break;
            }
        }
    }

    //@Test
    public void SnakeGameTest() throws InterruptedException {
        loginInit();

        // clicks 15 times on home
        Circle homeButton = lookup("#homeButton").query();
        for (int i = 0; i < 10; i++) {
            clickOn(homeButton);
        }
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#button_start");

        Label scoreLabel = lookup("#label_score").query();
        Label highScoreLabel = lookup("#label_highscore").query();
        Pane gameOverBox = lookup("#gameOverBox").query();
        Button restartButton = lookup("#restartButton").query();

        // set snake to center of the field
        SnakeGameController snakeGameController = app.getSnakeGameController();
        snakeGameController.setSnakeHeadPos(320, 360);

        // start countdown
        try {
            WaitForAsyncUtils.waitFor(20, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return !lookup("#countDownBox").query().isVisible();
                }
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // snake eats two foods
        Food food = new Food().setPosX(snakeGameController.getSnake().get(0).getPosX() + FIELD_SIZE).setPosY(snakeGameController.getSnake().get(0).getPosY());
        snakeGameController.setFood(food);
        WaitForAsyncUtils.sleep(600, TimeUnit.MILLISECONDS);
        food = new Food().setPosX(snakeGameController.getSnake().get(0).getPosX() + FIELD_SIZE).setPosY(snakeGameController.getSnake().get(0).getPosY());
        snakeGameController.setFood(food);
        WaitForAsyncUtils.sleep(350, TimeUnit.MILLISECONDS);
        food = new Food().setPosX(0).setPosY(0);
        snakeGameController.setFood(food);

        // now score == 200?
        Assert.assertEquals("Score: 200", scoreLabel.getText());

        // check all directions
        press(KeyCode.W).release(KeyCode.W);
        WaitForAsyncUtils.sleep(600, TimeUnit.MILLISECONDS);
        press(KeyCode.A).release(KeyCode.A);
        WaitForAsyncUtils.sleep(600, TimeUnit.MILLISECONDS);
        press(KeyCode.S).release(KeyCode.S);
        WaitForAsyncUtils.sleep(600, TimeUnit.MILLISECONDS);
        press(KeyCode.D).release(KeyCode.D);
        WaitForAsyncUtils.sleep(600, TimeUnit.MILLISECONDS);

        // snake kills itself
        snakeGameController.setSnakeHeadPos(snakeGameController.getSnake().get(0).getPosX() - 2 * FIELD_SIZE, snakeGameController.getSnake().get(0).getPosY());

        // gameOverScreen fadeIn
        try {
            WaitForAsyncUtils.waitFor(20, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return gameOverBox.getOpacity() == 1.0;
                }
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // gameOverScreen opacity now 1.0?
        Assert.assertEquals(String.valueOf(1.0), String.valueOf(gameOverBox.getOpacity()));

        // click restart
        clickOn(restartButton);

        // restart countdown
        try {
            WaitForAsyncUtils.waitFor(20, TimeUnit.SECONDS, new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return !lookup("#countDownBox").query().isVisible();
                }
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // now score == 0 && Highscore == 200?
        Assert.assertEquals("Score: 0", scoreLabel.getText());
        Assert.assertEquals("Highscore: 200", highScoreLabel.getText());

        // close game
        for (Object object : this.listTargetWindows()) {
            if (((Stage) object).getTitle().equals("Snake")) {
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }
}
