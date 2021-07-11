package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.util.ResourceManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.uniks.stp.util.Constants.*;

public class CustomNotificationsController extends SubSetting {

    private final ModelBuilder builder;
    private final Parent view;
    private ComboBox<String> customSoundComboBox;
    private Button addButton;
    private Button deleteButton;
    private List<File> files;
    private List<String> fileNames;
    private List<String> addedFiles;
    private ObservableList<File> ob;
    private InputStream stream;

    public CustomNotificationsController(Parent view, ModelBuilder builder) {
        this.builder = builder;
        this.view = view;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init() {
        customSoundComboBox = (ComboBox<String>) view.lookup("#comboBox");
        addButton = (Button) view.lookup("#add");
        deleteButton = (Button) view.lookup("#delete");
        if (addedFiles == null) {
            addedFiles = new ArrayList<>();
        }
        files = new ArrayList<>();
        addButton.setOnAction(this::add);
        deleteButton.setOnAction(this::delete);
        fileNames = new ArrayList<>();
        ob = FXCollections.observableList(ResourceManager.getNotificationSoundFiles());
        if (!ResourceManager.getComboValue(builder.getPersonalUser().getName()).equals("")) {
            customSoundComboBox.setPromptText(ResourceManager.getComboValue(builder.getPersonalUser().getName()));
        }
        deleteButton.setVisible(!customSoundComboBox.getPromptText().equals("default"));
        for (File file : ob) {
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            fileNames.add(fileName);
            files.add(file);
            customSoundComboBox.getItems().add(fileName);
        }
        customSoundComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            deleteButton.setVisible(!newValue.equals("default"));
            customSoundComboBox.setPromptText(newValue);
            ResourceManager.setComboValue(builder.getPersonalUser().getName(), newValue);
            for (File file : files) {
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                fileNames.add(fileName);
                if (fileName.equals(newValue)) {
                    try {
                        stream = new FileInputStream(file);
                        URL url = file.toURI().toURL();
                        builder.setSoundFile(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void delete(ActionEvent actionEvent) {
        if (stream != null) {
            cleanUp();
        }
        if (customSoundComboBox.getValue() != null) {
            String newValue = customSoundComboBox.getValue();
            ResourceManager.deleteNotificationSound(newValue);
            fileNames.remove(newValue);
            customSoundComboBox.getItems().remove(newValue);
            customSoundComboBox.setPromptText("saved sounds");
        }
    }

    private void add(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("WAV Documents", "*.wav"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null && !fileNames.contains(selectedFile.getName().substring(0, selectedFile.getName().length() - 4))) {
            String fileName = selectedFile.getName().substring(0, selectedFile.getName().length() - 4);
            customSoundComboBox.setPromptText(fileName);
            files.add(selectedFile);
            fileNames.add(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
            customSoundComboBox.getItems().add(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
            File file = new File(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH + "/" + selectedFile.getName());
            ResourceManager.saveNotifications(selectedFile);
            ob.add(file);
            ResourceManager.setComboValue(builder.getPersonalUser().getName(),
                    selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
        } else {
            System.out.println("File is not valid!");
        }
    }

    public void cleanUp() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
