package de.uniks.stp.controller.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.SubSetting;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;

public class CustomNotificationsController extends SubSetting {

    private final ModelBuilder builder;
    private final Parent view;
    private ComboBox customSoundComboBox;
    private Button addButton;
    private Button deleteButton;

    public CustomNotificationsController(Parent view, ModelBuilder builder) {
        this.builder = builder;
        this.view = view;
    }

    @Override
    public void init() {
        customSoundComboBox = (ComboBox) view.lookup("#doNotDisturbSelected");
        addButton = (Button) view.lookup("");
        deleteButton = (Button) view.lookup("");

        addButton.setOnAction(this::add);
        deleteButton.setOnAction(this::delete);
    }

    private void delete(ActionEvent actionEvent) {
        if(!customSoundComboBox.getPromptText().equals("saved sounds")){
            //get list of custom sounds
            //delete selected sound out of list
        }
    }

    private void add(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sound");
    }


}
