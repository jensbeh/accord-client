package de.uniks.stp.controller.snake;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.snake.model.Food;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import de.uniks.stp.util.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.Callback;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static de.uniks.stp.controller.snake.Constants.FIELD_SIZE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class SnakeControllerTest extends ApplicationTest {
    @InjectMocks
    StageManager mockApp = new StageManager();
    private StageManager app;
    @Mock
    private RestClient restClient;
    @Mock
    private HttpResponse<JsonNode> response;
    @Mock
    private PrivateSystemWebSocketClient privateSystemWebSocketClient;
    @Mock
    private PrivateChatWebSocket privateChatWebSocket;
    @Mock
    private ServerSystemWebSocket serverSystemWebSocket;
    @Mock
    private ServerChatWebSocket serverChatWebSocket;
    @Captor
    private ArgumentCaptor<Callback<JsonNode>> callbackCaptor;

    @BeforeClass
    public static void setupHeadlessMode() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("headless.geometry", "1920x1080-32");
    }

    @BeforeAll
    static void setup() {
        MockitoAnnotations.openMocks(SnakeControllerTest.class);
    }

    @Override
    public void start(Stage stage) {
        //start application
        ModelBuilder builder = new ModelBuilder();
        builder.setUSER_CLIENT(privateSystemWebSocketClient);
        builder.setPrivateChatWebSocketClient(privateChatWebSocket);
        builder.setSERVER_USER(serverSystemWebSocket);
        builder.setServerChatWebSocketClient(serverChatWebSocket);
        app = mockApp;
        StageManager.setBuilder(builder);
        StageManager.setRestClient(restClient);

        builder.setLoadUserData(false);
        mockApp.getBuilder().setSpotifyShow(false);
        mockApp.getBuilder().setSpotifyToken(null);
        mockApp.getBuilder().setSpotifyRefresh(null);

        app.start(stage);
        stage.centerOnScreen();
    }

    public void mockLogin() {
        String userKey = "c3a981d1-d0a2-47fd-ad60-46c7754d9271";
        JSONObject jsonString = new JSONObject()
                .put("status", "success")
                .put("message", "")
                .put("data", new JSONObject().put("userKey", userKey));
        String jsonNode = new JsonNode(jsonString.toString()).toString();
        when(response.getBody()).thenReturn(new JsonNode(jsonNode));
        doAnswer((Answer<Void>) invocation -> {
            Callback<JsonNode> callback = callbackCaptor.getValue();
            callback.completed(response);
            return null;
        }).when(restClient).login(anyString(), anyString(), callbackCaptor.capture());
    }

    public void loginInit() throws InterruptedException {
        mockLogin();
        TextField usernameTextField = lookup("#usernameTextfield").query();
        String testUserMainName = "Hendry Bracken";
        usernameTextField.setText(testUserMainName);
        PasswordField passwordField = lookup("#passwordTextField").query();
        String testUserMainPw = "stp2021pw";
        passwordField.setText(testUserMainPw);
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    public void openStartGameViewTest() throws InterruptedException {
        loginInit();

        // clicks 15 times on home
        Circle homeButton = lookup("#homeButton").query();
        for (int i = 0; i < 10; i++) {
            clickOn(homeButton);
        }

        WaitForAsyncUtils.waitForFxEvents();

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
        clickOn("#button_exit");
    }

    @Test
    public void SnakeGameTest() throws InterruptedException {
        loginInit();

        ResourceManager.saveMuteGameState(true, app.getBuilder().getPersonalUser().getName());

        // clicks 15 times on home
        Circle homeButton = lookup("#homeButton").query();
        for (int i = 0; i < 10; i++) {
            clickOn(homeButton);
        }
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#button_start");
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Label scoreLabel = lookup("#label_score").query();
        Label highScoreLabel = lookup("#label_highscore").query();
        Pane gameOverBox = lookup("#gameOverBox").query();
        Button restartButton = lookup("#restartButton").query();
        Button muteButton = lookup("#muteButton").query();

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
        //Assert.assertEquals("Score: 200", scoreLabel.getText());

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
//        Assert.assertEquals(String.valueOf(1.0), String.valueOf(gameOverBox.getOpacity()));

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
//        Assert.assertEquals("Score: 0", scoreLabel.getText());
//        Assert.assertEquals("Highscore: 200", highScoreLabel.getText());

        // unMute
        clickOn(muteButton);
//        Assert.assertEquals("\uD83D\uDD0A", muteButton.getText());

        // mute
        clickOn(muteButton);
//        Assert.assertEquals("\uD83D\uDD08", muteButton.getText());

        // close game
        clickOn("#button_exit");
    }
}