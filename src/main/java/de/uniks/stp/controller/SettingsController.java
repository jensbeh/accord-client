package de.uniks.stp.controller;

import de.uniks.stp.LangString;
import de.uniks.stp.StageManager;
import de.uniks.stp.controller.subcontroller.LanguageController;
import de.uniks.stp.controller.subcontroller.SubSetting;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class SettingsController {
    private Parent view;
    private VBox settingsItems;
    private VBox settingsContainer;
    private List<Button> itemList;

    private SubSetting subController;

    public static void setup() {
        LanguageController.setup();
    }

    public SettingsController(Parent view) {
        this.view = view;
    }

    public void init() {
        //init view
        this.settingsItems = (VBox) view.lookup("#settingsItems");
        this.settingsItems.getChildren().clear();
        this.settingsContainer = (VBox) view.lookup("#settingsContainer");
        this.settingsContainer.getChildren().clear();

        this.itemList = new ArrayList<>();

        // add categories
        Button languageButton = addItem("Language");
        addAction(languageButton, "Language");
    }

    public void stop() {
        if(subController != null) {
            subController.stop();
            subController = null;
        }

        for(Button b : this.itemList) {
            b.setOnAction(null);
        }
    }

    public Button addItem(String buttonName) {
        Button button = new Button();
        button.setPrefWidth(198);
        button.setPrefHeight(32);

        this.itemList.add(button);
        this.settingsItems.getChildren().add(button);
        button.textProperty().bind(LangString.lStr(buttonName));

        return button;
    }

    public void addAction(Button button, String viewName) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openSettings(viewName);
            }
        });
    }

    public void openSettings(String fxmlName) {
        // stop current subController
        if(subController != null) {
            subController.stop();
        }

        // clear old and load new subSetting view
        try {
            this.settingsContainer.getChildren().clear();
            Parent settingsField = FXMLLoader.load(StageManager.class.getResource("view/settings/Settings_" + fxmlName + ".fxml"));

            switch(fxmlName) {
                case "Language":
                    subController = new LanguageController(settingsField);
                    subController.init();
                    break;
            }

            this.settingsContainer.getChildren().add(settingsField);
        } catch (Exception e) {
            System.err.println("Error on showing Settings Field Screen");
            e.printStackTrace();
        }
    }
}
