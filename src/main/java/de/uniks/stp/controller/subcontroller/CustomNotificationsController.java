package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import util.ResourceManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static util.Constants.*;

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
    public void init() {
        customSoundComboBox = (ComboBox<String>) view.lookup("#comboBox");
        addButton = (Button) view.lookup("#add");
        deleteButton = (Button) view.lookup("#delete");
        if(addedFiles == null){
            addedFiles = new ArrayList<>();
        }
        files = new ArrayList<>();
        addButton.setOnAction(this::add);
        deleteButton.setOnAction(this::delete);
        customSoundComboBox.getItems().add("default");
        fileNames = new ArrayList<>();
        ob = FXCollections.observableList(ResourceManager.getNotificationSoundFiles());
        if(!ResourceManager.getComboValue().equals("")){
            customSoundComboBox.setPromptText(ResourceManager.getComboValue());
        }

        for(File file : ob){
            String fileName = file.getName().substring(0, file.getName().length() - 4);
            fileNames.add(fileName);
            files.add(file);
            customSoundComboBox.getItems().add(fileName);
        }
        customSoundComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            ResourceManager.setComboValue(newValue);
            for(File file : files){
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                if(fileName.equals(newValue)){
                    try {
                        stream = new FileInputStream(file);
                        builder.setSoundFile(stream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void delete(ActionEvent actionEvent){
        if(stream != null){
            cleanUp();
        }
        if(customSoundComboBox.getValue() != null){
            ResourceManager.deleteNotificationSound(customSoundComboBox.getValue());
            fileNames.remove(customSoundComboBox.getValue());
            customSoundComboBox.getItems().remove(customSoundComboBox.getValue());
            customSoundComboBox.setPromptText("saved sounds");
        }
    }

    private void add(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        fileChooser.setTitle("Select Sound");
        if(selectedFile != null){
            files.add(selectedFile);
            addedFiles.add(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
            customSoundComboBox.getItems().add(selectedFile.getName().substring(0, selectedFile.getName().length() - 4));
            File file = new File(APPDIR_ACCORD_PATH + SAVES_PATH + NOTIFICATION_PATH + "/" + selectedFile.getName());
            ResourceManager.saveNotifications(selectedFile);
            ob.add(file);
        }else{
            System.out.println("File is not valid!");
        }
    }

    public void cleanUp(){
        try {
            stream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
