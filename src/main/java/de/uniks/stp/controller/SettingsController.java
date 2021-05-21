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
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SettingsController {
    private Parent view;
    private VBox settingsItems;
    private VBox settingsContainer;
    private List<Button> itemList;

    private SubSetting subController;

    public static void setup() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);

        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;

        Properties prop = new Properties();
        File file = new File(path_to_config + Constants.SETTINGS_FILE);
        File dir = new File(path_to_config);
        if (!file.exists()) {
            try {
                dir.mkdirs();
                if(file.createNewFile()) {
                    FileOutputStream op = new FileOutputStream(path_to_config + Constants.SETTINGS_FILE);
                    prop.setProperty("LANGUAGE", "en");
                    prop.store(op, null);
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

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
        button.setId("button_" + buttonName);

        this.itemList.add(button);
        this.settingsItems.getChildren().add(button);
        button.textProperty().bind(LangString.lStr("button." + buttonName));

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
            Parent settingsField = FXMLLoader.load(StageManager.class.getResource("view/settings/Settings_" + fxmlName + ".fxml"), StageManager.getLangBundle());

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
