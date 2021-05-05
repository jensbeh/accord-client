package de.uniks.stp;

import de.uniks.stp.StageManager;
import javafx.application.Platform;
import javafx.scene.control.Label;
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
        while (true);
    }

}
