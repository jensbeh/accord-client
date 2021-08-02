package de.uniks.stp.controller.titlebar;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class TitleBarController {
    private final Stage stage;
    private final ModelBuilder builder;
    private Parent titleBarView;
    private HBox titleBarSpaceBox;
    private HBox logoAndLabelBox;
    private Button minButton;
    private Button maxButton;
    private Button closeButton;

    private double x, y;
    private boolean topBorderIsSized;

    public TitleBarController(Stage stage, HBox titleBarBox, ModelBuilder builder) {
        this.stage = stage;
        this.builder = builder;
        
        try {
            this.titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBar.fxml")), StageManager.getLangBundle());
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleBarBox.getChildren().add(titleBarView);

    }


    public void init() {
        titleBarSpaceBox = (HBox) titleBarView.lookup("#titleBarSpace");
        logoAndLabelBox = (HBox) titleBarView.lookup("#titleLogoAndLabel");
        HBox buttonsBox = (HBox) titleBarView.lookup("#titleButtons");
        minButton = (Button) titleBarView.lookup("#Button_minTitleBar");
        maxButton = (Button) titleBarView.lookup("#Button_maxTitleBar");
        closeButton = (Button) titleBarView.lookup("#Button_closeTitleBar");

        titleBarSpaceBox.prefWidthProperty().bind(stage.widthProperty().subtract(logoAndLabelBox.getPrefWidth() + buttonsBox.getPrefWidth())); // 78 + 109 = 187

        setOnListener();
    }

    private void setOnListener() {
        minButton.setOnAction(event -> stage.setIconified(true));
        maxButton.setOnAction(event -> {
            if (stage.isMaximized()) {
                stage.setMaximized(false);
                maxButton.setText("\uD83D\uDDD6");
            } else {
                stage.setMaximized(true);
                maxButton.setText("\uD83D\uDDD7");
            }
        });

        closeButton.setOnAction(event -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));

        titleBarSpaceBox.setOnMouseDragged(this::setStagePos);
        titleBarSpaceBox.setOnMousePressed(this::getScenePos);
        titleBarSpaceBox.setOnMouseReleased(event -> topBorderIsSized = false);

        logoAndLabelBox.setOnMouseDragged(this::setStagePos);
        logoAndLabelBox.setOnMousePressed(this::getScenePos);
        logoAndLabelBox.setOnMouseReleased(event -> topBorderIsSized = false);
    }

    private void setStagePos(MouseEvent event) {
        if (!topBorderIsSized) {
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
        }
    }

    private void getScenePos(MouseEvent event) {
        int border = 5;
        double mouseEventX = event.getSceneX(),
                mouseEventY = event.getSceneY(),
                sceneWidth = stage.getScene().getWidth();

        if ((mouseEventX < border && mouseEventY < border) || (mouseEventX > sceneWidth - border && mouseEventY < border) || (mouseEventY < border)) {
            topBorderIsSized = true;
        }
        x = event.getSceneX();
        y = event.getSceneY();
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        titleBarView.getStylesheets().clear();
        titleBarView.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/TitleBar.css")).toExternalForm());
    }

    private void setDarkMode() {
        titleBarView.getStylesheets().clear();
        titleBarView.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/TitleBar.css")).toExternalForm());
    }

    public void setMaximizable(boolean state) {
        maxButton.setDisable(!state);
    }
}