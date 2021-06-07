package de.uniks.stp;

import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class ServerSettingsControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
    private static String testUserMainName;
    private static String testUserMainPw;
    private static String testUserOneName;
    private static String testUserOnePw;
    private static String testUserOne_UserKey;
    private static String testServerId;

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

    public void loginInitWithTempUser() throws InterruptedException {
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

    public void loginInit(String name, String password) throws InterruptedException {
        TextField usernameTextField = lookup("#usernameTextfield").query();
        usernameTextField.setText(name);
        PasswordField passwordField = lookup("#passwordTextField").query();
        passwordField.setText(password);

        clickOn("#loginButton");

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
    }

    public void getServerId() throws InterruptedException {
        restClient.loginTemp(response -> {
            JsonNode body = response.getBody();
            //get name and password from server
            testUserOneName = body.getObject().getJSONObject("data").getString("name");
            testUserOnePw = body.getObject().getJSONObject("data").getString("password");
        });
        Thread.sleep(2000);
        restClient.login(testUserOneName, testUserOnePw, response -> {
            JsonNode body = response.getBody();
            testUserOne_UserKey = body.getObject().getJSONObject("data").getString("userKey");
        });
        Thread.sleep(2000);

        JsonNode body = restClient.postServer(testUserOne_UserKey, "TestServer Team Bit Shift");
        testServerId = body.getObject().getJSONObject("data").getString("id");

        restClient.logout(testUserOne_UserKey, response -> {
        });
    }

    //@Test
    public void openServerSettingsTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);

        TextField serverName = lookup("#serverName").query();
        Button createServer = lookup("#createServer").query();
        serverName.setText("TestServer Team Bit Shift");
        clickOn(createServer);

        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals(2, this.listTargetWindows().size());
        String serverSettingsTitle = "";
        for (Object object : this.listTargetWindows()) {
            if (!((Stage) object).getTitle().equals("Accord - Main")) {
                serverSettingsTitle = ((Stage) object).getTitle();
                Assert.assertNotEquals("", serverSettingsTitle);
                Platform.runLater(((Stage) object)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
        Thread.sleep(2000);
    }

    //@Test
    public void clickOnOwnerOverview() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        clickOn(serverList.lookup("#server"));
        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        Assert.assertNotEquals(1, this.listTargetWindows().size());
        clickOn("#overview");
        clickOn("#deleteServer");
        Label serverNameLabel = lookup("#serverName").query();
        Button leaveButton = lookup("#deleteServer").query();
        Assert.assertEquals("TestServer Team Bit Shift", serverNameLabel.getText());
        Assert.assertEquals("Delete Server", leaveButton.getText());
    }

    //@Test
    public void changeServerNameAndDeleteServer() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);
        Circle addServer = lookup("#addServer").query();
        clickOn(addServer);
        TextField serverName = lookup("#serverName").query();
        serverName.setText("TestServer");
        clickOn("#createServer");
        Assert.assertEquals("TestServer", serverName.getText());
        Thread.sleep(2000);
        ListView<Server> serverList = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        System.out.println("ServerList: " + serverList.getItems().toString());
        String serverId = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("TestServer")) {
                serverId = server.getId();
            }
        }
        Thread.sleep(2000);
        clickOn(serverList.lookup("#serverName_" + serverId));
        clickOn("#serverMenuButton");
        moveBy(0, 25);
        write("\n");
        Assert.assertNotEquals(1, this.listTargetWindows().size());
        MenuButton menuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals("TestServer", menuButton.getText());

        //change ServerName
        TextField serverNameField = lookup("#nameText").query();
        serverNameField.setText("Test2");
        clickOn("#changeName");
        String serverIdChangedName = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("Test2")) {
                serverIdChangedName = server.getId();
            }
        }
        Assert.assertEquals(serverId, serverIdChangedName);

        //delete Server
        clickOn("#deleteServer");
        String result = "";
        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                result = ((Stage) s).getTitle();
                Assert.assertEquals("ServerSettings", result);
                Platform.runLater(((Stage) s)::close);
                Thread.sleep(2000);
                break;
            }
        }

        //check all changed Names
        menuButton = lookup("#serverMenuButton").query();
        Assert.assertEquals("Test2", menuButton.getText());

        //click on delete Button (Alert)
        moveBy(-50, -105);
        clickOn();

        //check if server doesn't exist anymore
        serverId = "";
        for (Server server : serverList.getItems()) {
            if (server.getName().equals("TestServer")) {
                serverId = server.getId();
            }
        }
        Assert.assertEquals("", serverId);
        System.out.println("ServerList: " + serverList.getItems().toString());
    }
}
