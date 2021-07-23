package de.uniks.stp.controller.settings;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.udp.Microphone;
import de.uniks.stp.net.udp.Speaker;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class AudioController extends SubSetting{

    private final Parent view;
    private final ModelBuilder builder;
    private boolean senderActive;
    private boolean stopped;
    private boolean isMuted;
    private Runnable myRunnable;
    private Thread soundThread;

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
    private Microphone microphone;
    private Speaker speaker;


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
        startButton.setOnAction(this::onMicrophoneTestStart);
        senderActive = false;
        myRunnable = this::runMicrophoneTest;

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
        valueTextInputSlider.textProperty().bind(volumeInput.valueProperty().asString("%.2f"));
        valueTextInputSlider.setFill(Color.WHITE);
        thumbInputSlider.getChildren().add(valueTextInputSlider);

        // get new Value
        volumeInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            builder.getLinePoolService().setMicrophoneVolume(newValue.floatValue());
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
        valueTextOutputSlider.textProperty().bind(volumeOutput.valueProperty().asString("%.2f"));
        valueTextOutputSlider.setFill(Color.WHITE);
        thumbOutputSlider.getChildren().add(valueTextOutputSlider);

        // get new Value
        volumeOutput.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("volumeOutput: " + newValue.floatValue());
            builder.getLinePoolService().setSpeakerVolume(newValue.floatValue());
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

    private void onMicrophoneTestStart(ActionEvent actionEvent) {
        if (this.builder.getCurrentServer() != null) {
            if (this.builder.getMuteHeadphones()) {
                isMuted = true;
            } else {
                isMuted = false;
                this.builder.muteHeadphones(true);
            }
        }
        microphone = new Microphone(this.builder);
        microphone.init();
        speaker = new Speaker(this.builder);
        speaker.init();
        senderActive = true;
        soundThread = new Thread(myRunnable);
        soundThread.start();

        microphoneTestChangeAction(false);
    }

    private void onMicrophoneTestStop(ActionEvent actionEvent) {
        senderActive = false;
        if (this.builder.getCurrentServer() != null) {
            if (!isMuted) {
                this.builder.muteHeadphones(false);
            }
        }

        stopRecord();

        microphoneTestChangeAction(true);
    }

    private void microphoneTestChangeAction(Boolean stopTest){
        if (stopTest) {
            startButton.setText("Start");
            startButton.setOnAction(this::onMicrophoneTestStart);
        } else {
            startButton.setText("Stop");
            startButton.setOnAction(this::onMicrophoneTestStop);
        }
    }

    public void stopRecord() {
        senderActive = false;
        while (!stopped) {
            Thread.onSpinWait();
        }
    }
    public void stop() {
        stopRecord();
        startButton.setOnAction(null);
        soundThread = null;
        myRunnable = null;
    }

    public void runMicrophoneTest() {
        // start sending
        if (senderActive) {
            microphone.startRecording();
            speaker.startPlayback();
            while (senderActive) {

                stopped = false;
                // start recording audio
                byte[] data = microphone.readData();
                int volumeInPer = calculateRMSLevel(data);
                microphoneProgressBar.setProgress(volumeInPer * 0.01);
                speaker.writeData(data);

            }
            microphoneProgressBar.setProgress(0);
            // stop if senderActive is set to false in stop method in this class
            microphone.stopRecording();
            speaker.stopPlayback();
            stopped = true;
        }
    }

    public int calculateRMSLevel(byte[] audioData)
    {
        long lSum = 0;
        for(int i=0; i < audioData.length; i++)
            lSum = lSum + audioData[i];

        double dAvg = lSum / audioData.length;
        double sumMeanSquare = 0d;

        for(int j=0; j < audioData.length; j++)
            sumMeanSquare += Math.pow(audioData[j] - dAvg, 2d);

        double averageMeanSquare = sumMeanSquare / audioData.length;

        return (int)(Math.pow(averageMeanSquare,0.5d) + 0.5);
    }

}
