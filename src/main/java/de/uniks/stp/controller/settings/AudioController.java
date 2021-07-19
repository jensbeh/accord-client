package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;

public class AudioController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;

    private Label inputLabel;
    private Label outputLabel;
    private Label volumeInputLabel;
    private Label volumeOutputLabel;
    private Label microphoneCheckLabel;

    private ComboBox<String> inputDeviceComboBox;
    private ComboBox<String> outputDeviceComboBox;
    private Slider volumeInput;
    private Slider volumeOutput;
    private Button startButton;
    private ProgressBar microphoneProgressBar;


    public AudioController(Parent view, ModelBuilder builder) {
        this.view = view;
        this.builder = builder;
    }

    @SuppressWarnings("unchecked")
    public void init() {
        inputLabel = (Label) view.lookup("#label_input");
        outputLabel = (Label) view.lookup("#label_output");
        volumeInputLabel = (Label) view.lookup("#label_volumeInput");
        volumeOutputLabel = (Label) view.lookup("#label_volumeOutput");
        microphoneCheckLabel = (Label) view.lookup("#label_microphoneCheck");

        inputDeviceComboBox = (ComboBox<String>) view.lookup("#comboBox_input");
        outputDeviceComboBox = (ComboBox<String>) view.lookup("#comboBox_output");

        volumeInput = (Slider) view.lookup("#slider_volumeInput");
        volumeOutput = (Slider) view.lookup("#slider_volumeOutput");

        startButton = (Button) view.lookup("#button_audioStart");
        microphoneProgressBar = (ProgressBar) view.lookup("#progressBar_microphone");

        // ComboBox Settings
        this.inputDeviceComboBox.setPromptText("Select Input Device:"); // lang.getString("comboBox.inputDevice")
        this.inputDeviceComboBox.getItems().clear();
        this.inputDeviceComboBox.setOnAction(this::onInputDeviceClicked);

        this.outputDeviceComboBox.setPromptText("Select Output Device:"); // lang.getString("comboBox.outputDevice")
        this.outputDeviceComboBox.getItems().clear();
        this.outputDeviceComboBox.setOnAction(this::onOutputDeviceClicked);

        // Slider Settings
        volumeInput.setMin(-80.0);
        volumeInput.setMax(6.0206);
//        volumeInput.setValue(ResourceManager.getVolume(builder.getPersonalUser().getName()));
        volumeInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("volumeInput: " + newValue);
        });

        volumeOutput.setMin(-80.0);
        volumeOutput.setMax(6.0206);
//        volumeOutput.setValue(ResourceManager.getVolume(builder.getPersonalUser().getName()));
        volumeOutput.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("volumeOutput: " + newValue);
        });
    }

    /**
     * Sets the selected InputDevice (Microphone) when clicked on it in comboBox
     */
    private void onInputDeviceClicked(ActionEvent actionEvent) {

    }

    /**
     * Sets the selected OutputDevice (Speaker) when clicked on it in comboBox
     */
    private void onOutputDeviceClicked(ActionEvent actionEvent) {

    }
}
