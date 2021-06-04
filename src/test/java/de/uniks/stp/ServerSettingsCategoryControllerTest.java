package de.uniks.stp;

import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class ServerSettingsCategoryControllerTest extends ApplicationTest {

    private Stage stage;
    private StageManager app;
    private RestClient restClient;
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
    public void openServerSettingsCategoryTest() throws InterruptedException {
        getServerId();
        loginInit(testUserOneName, testUserOnePw);

        Server currentServer = null;
        ListView<Server> serverListView = lookup("#scrollPaneServerBox").lookup("#serverList").query();
        for (Server server : serverListView.getItems()) {
            if (server.getName().equals("TestServer Team Bit Shift")) {
                currentServer = server;
            }
        }

        clickOn(serverListView.lookup("#serverName_" + testServerId));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("#serverMenuButton");
        clickOn("#ServerSettings");
        clickOn("#category");

        Label editCategoryLabel = lookup("#editCategoryLabel").query();
        Label createCategoryLabel = lookup("#createCategoryLabel").query();
        ComboBox<Categories> categoriesSelector = lookup("#editCategoriesSelector").query();
        TextField categoryNameTextField = lookup("#editCategoryNameTextField").query();
        Button changeCategoryNameButton = lookup("#changeCategoryNameButton").query();
        Button deleteCategoryButton = lookup("#deleteCategoryButton").query();
        TextField createCategoryNameTextField = lookup("#createCategoryNameTextField").query();
        Button createCategoryButton = lookup("#createCategoryButton").query();

        Assert.assertEquals("Edit Category", editCategoryLabel.getText());
        Assert.assertEquals("Create Category", createCategoryLabel.getText());
        Assert.assertEquals("change", changeCategoryNameButton.getText());
        Assert.assertEquals("Delete", deleteCategoryButton.getText());
        Assert.assertEquals("create", createCategoryButton.getText());


        // type in new name for channel
        createCategoryNameTextField.setText("NewCategory");
        // click on create
        clickOn(createCategoryButton);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Categories newCategory = new Categories();
        for (Categories category : categoriesSelector.getItems()) {
            if (category.getName().equals("NewCategory")) {
                newCategory = category;
            }
        }

        Assert.assertTrue(createCategoryNameTextField.getText().equals(""));
        Assert.assertTrue(newCategory.getName().equals("NewCategory"));
        for (Categories categories : app.getBuilder().getCurrentServer().getCategories()) {
            if (categories.getId().equals(newCategory.getId())) {
                Assert.assertTrue(categories.getId().equals(newCategory.getId()));
            }
        }

        clickOn(categoriesSelector);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(newCategory.getName());
        WaitForAsyncUtils.waitForFxEvents();

        categoryNameTextField.setText("NewCategoryName");
        clickOn(changeCategoryNameButton);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        for (Categories category : categoriesSelector.getItems()) {
            if (category.getId().equals(newCategory.getId())) {
                newCategory = category;
            }
        }

        Assert.assertTrue(categoryNameTextField.getText().equals(""));
        Assert.assertTrue(newCategory.getName().equals("NewCategoryName"));
        Assert.assertTrue(currentServer.getCategories().contains(newCategory));

        clickOn(categoriesSelector);
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(newCategory.getName());
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        clickOn(deleteCategoryButton);
        WaitForAsyncUtils.waitForFxEvents();
        Thread.sleep(2000);

        Assert.assertFalse(app.getBuilder().getCurrentServer().getCategories().contains(newCategory));

        Assert.assertFalse(categoriesSelector.getItems().contains(newCategory));

        for (Object s : this.listTargetWindows()) {
            if (s != stage) {
                Platform.runLater(((Stage) s)::close);
                WaitForAsyncUtils.waitForFxEvents();
                break;
            }
        }
    }
}
