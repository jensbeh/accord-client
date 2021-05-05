package de.uniks.stp;

import de.uniks.stp.net.RestClient;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

public class HomeScreenControllerTest extends ApplicationTest {
    private static Stage stage;
    private StageManager app;

    @Override
    public void start(Stage stage) {
        // start application
        this.stage = stage;
        app = new StageManager();
        app.start(stage);
        this.stage.centerOnScreen();
    }
    @Test
    public void propertyChangeTest() {
        new RestClient().login("Mr","Spock",null);
        WaitForAsyncUtils.waitForFxEvents();
        TextField name = (TextField) lookup("#usernameTextfield").query();
        name.setText("Peter_Lustig");
        TextField password = (TextField) lookup("#passwordTextField").query();
        password.setText("1234");
        clickOn("#loginButton");
        WaitForAsyncUtils.waitForFxEvents();
        String tmp = app.getBuilder().getPersonalUser().getName();
        WaitForAsyncUtils.waitForFxEvents();
        Assert.assertEquals("Peter_Lustig",app.getBuilder().getPersonalUser().getName());
        while(true);
    }

}
