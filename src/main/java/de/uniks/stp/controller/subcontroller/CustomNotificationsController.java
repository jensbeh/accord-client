package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomNotificationsController extends SubSetting {

    private final ModelBuilder builder;
    private final Parent view;
    private ComboBox<String> customSoundComboBox;
    private Button addButton;
    private Button deleteButton;
    private List<File> files;

    public CustomNotificationsController(Parent view, ModelBuilder builder) {
        this.builder = builder;
        this.view = view;
    }

    @Override
    public void init() {
        customSoundComboBox = (ComboBox<String>) view.lookup("#comboBox");
        addButton = (Button) view.lookup("#add");
        deleteButton = (Button) view.lookup("#delete");

        files = new ArrayList<>();
        addButton.setOnAction(this::add);
        deleteButton.setOnAction(this::delete);

        customSoundComboBox.getItems().add("default");
    }

    private void delete(ActionEvent actionEvent) {
        if(!customSoundComboBox.getPromptText().equals("saved sounds")){
            //get list of custom sounds
            //delete selected sound out of list
        }
    }

    private void add(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\Passi\\AppData\\Local\\Accord\\saves\\soundNotifications"));
        File selectedFile = fileChooser.showOpenDialog(null);
        fileChooser.setTitle("Select Sound");
        if(selectedFile != null){
            files.add(selectedFile);
        }else{
            System.out.println("File is not valid!");
        }
    }


}
