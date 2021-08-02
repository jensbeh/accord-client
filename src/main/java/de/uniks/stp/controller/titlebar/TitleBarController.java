package de.uniks.stp.controller.titlebar;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TitleBarController {
    private final Stage stage;
    private final Parent titleBarView;
    private HBox titleBarSpace;
    private HBox logoAndLabel;
    private HBox Buttons;
    private Button minButton;
    private Button maxButton;
    private Button closeButton;

    private double x, y;

    public TitleBarController(Stage stage, Parent titleBarView) {
        this.stage = stage;
        this.titleBarView = titleBarView;
    }


    public void init() {
        titleBarSpace = (HBox) titleBarView.lookup("#titleBarSpace");
        logoAndLabel = (HBox) titleBarView.lookup("#titleLogoAndLabel");
        Buttons = (HBox) titleBarView.lookup("#titleButtons");
        minButton = (Button) titleBarView.lookup("#Button_minTitleBar");
        maxButton = (Button) titleBarView.lookup("#Button_maxTitleBar");
        closeButton = (Button) titleBarView.lookup("#Button_closeTitleBar");

        maxButton.setDisable(true);

        titleBarSpace.prefWidthProperty().bind(stage.widthProperty().subtract(73 + 83));

        setOnListener();
    }

    private void setOnListener() {
        minButton.setOnAction(event -> {
            Stage thisStage = (Stage) minButton.getScene().getWindow();
            thisStage.setIconified(true);
        });
        maxButton.setOnAction(event -> {
            Stage thisStage = (Stage) maxButton.getScene().getWindow();
            if (thisStage.isMaximized()) {
                thisStage.setMaximized(false);
            } else {
                thisStage.setMaximized(true);
            }
        });
        closeButton.setOnAction(event -> {
            Stage thisStage = (Stage) closeButton.getScene().getWindow();
            thisStage.fireEvent(new WindowEvent(thisStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        titleBarSpace.setOnMouseDragged(event -> {
            Stage thisStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            thisStage.setX(event.getScreenX() - x);
            thisStage.setY(event.getScreenY() - y);
        });

        titleBarSpace.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });
    }
}