package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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
        this.inputDeviceComboBox.setPromptText(builder.getLinePoolService().getSelectedMicrophoneName());
        this.inputDeviceComboBox.getItems().clear();
        this.inputDeviceComboBox.setOnAction(this::onInputDeviceClicked);

        // set microphone names
        for (var microphone : builder.getLinePoolService().getMicrophones().entrySet()) {
            this.inputDeviceComboBox.getItems().add(microphone.getKey());
        }

        this.outputDeviceComboBox.setPromptText(builder.getLinePoolService().getSelectedSpeakerName());
        this.outputDeviceComboBox.getItems().clear();
        this.outputDeviceComboBox.setOnAction(this::onOutputDeviceClicked);

        // set speaker names
        for (var speaker : builder.getLinePoolService().getSpeakers().entrySet()) {
            this.outputDeviceComboBox.getItems().add(speaker.getKey());
        }

        // Slider Settings
        // set values
        volumeInput.setMin(0.0);
        volumeInput.setValue(builder.getLinePoolService().getMicrophoneVolume());
        volumeInput.setMax(1.0);

        // set thumb text & style
        volumeInput.applyCss();
        volumeInput.layout();
        Pane thumbInputSlider = (Pane) volumeInput.lookup(".thumb");
        Text valueTextInputSlider = new Text();
        if (builder.getTheme().equals("Dark")) {
            valueTextInputSlider.setFill(Color.BLACK);
        } else {
            valueTextInputSlider.setFill(Color.WHITE);
        }
        valueTextInputSlider.setText(String.valueOf((int) (volumeInput.getValue() * 100) + 50));
        thumbInputSlider.getChildren().add(valueTextInputSlider);

        // get new Value
        volumeInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            builder.getLinePoolService().setMicrophoneVolume(newValue.floatValue());
            valueTextInputSlider.setText(String.valueOf((int) (volumeInput.getValue() * 100) + 50));
            builder.saveSettings();
        });

        // set values
        volumeOutput.setMin(0.0);
        volumeOutput.setValue(builder.getLinePoolService().getSpeakerVolume());
        volumeOutput.setMax(1.0);

        // set thumb text & style
        volumeOutput.applyCss();
        volumeOutput.layout();
        Pane thumbOutputSlider = (Pane) volumeOutput.lookup(".thumb");
        Text valueTextOutputSlider = new Text();
        if (builder.getTheme().equals("Dark")) {
            valueTextOutputSlider.setFill(Color.BLACK);
        } else {
            valueTextOutputSlider.setFill(Color.WHITE);
        }
        valueTextOutputSlider.setText(String.valueOf((int) (volumeOutput.getValue() * 100)));
        thumbOutputSlider.getChildren().add(valueTextOutputSlider);

        // get new Value
        volumeOutput.valueProperty().addListener((observable, oldValue, newValue) -> {
            builder.getLinePoolService().setSpeakerVolume(newValue.floatValue());
            valueTextOutputSlider.setText(String.valueOf((int) (volumeOutput.getValue() * 100)));
            builder.saveSettings();
        });
    }

    /**
     * Sets the selected InputDevice (Microphone) when clicked on it in comboBox
     */
    private void onInputDeviceClicked(ActionEvent actionEvent) {
        builder.getLinePoolService().setSelectedMicrophone(this.inputDeviceComboBox.getValue());
        builder.saveSettings();

        if (builder.getAudioStreamClient() != null) {
            builder.getAudioStreamClient().setNewMicrophone();
        }
    }

    /**
     * Sets the selected OutputDevice (Speaker) when clicked on it in comboBox
     */
    private void onOutputDeviceClicked(ActionEvent actionEvent) {
        builder.getLinePoolService().setSelectedSpeaker(this.outputDeviceComboBox.getValue());
        builder.saveSettings();

        if (builder.getAudioStreamClient() != null) {
            builder.getAudioStreamClient().setNewSpeaker();
        }
    }
}
