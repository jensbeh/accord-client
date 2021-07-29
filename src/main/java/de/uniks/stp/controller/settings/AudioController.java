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

public class AudioController extends SubSetting {

    private final Parent view;
    private final ModelBuilder builder;
    private boolean senderActive;
    private volatile boolean stopped;
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
        stopped = true;
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

    private void onMicrophoneTestStart(ActionEvent actionEvent) {
        if (this.builder.getPersonalUser() != null) {
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
        if (this.builder.getPersonalUser() != null) {
            if (!isMuted) {
                this.builder.muteHeadphones(false);
            }
        }

        stopRecord();

        microphoneTestChangeAction(true);
    }

    private void microphoneTestChangeAction(Boolean stopTest) {
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
//                Progress Bar
//                https://stackoverflow.com/questions/13357077/javafx-progressbar-how-to-change-bar-color/13372086#13372086
//                Gradient
//                http://www.java2s.com/Tutorials/Java/JavaFX/0110__JavaFX_Gradient_Color.htm
//                Stop[] stops = new Stop[] { new Stop(0, Color.BLACK), new Stop(1, Color.RED)};
//                LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);

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

    public int calculateRMSLevel(byte[] audioData) {
        double lSum = 0;
        for (byte audioDatum : audioData) {
            lSum = lSum + audioDatum;
        }
        double dAvg = lSum / audioData.length;
        double sumMeanSquare = 0d;

        for (byte audioDatum : audioData) {
            sumMeanSquare += Math.pow(audioDatum - dAvg, 2d);
        }

        double averageMeanSquare = sumMeanSquare / audioData.length;

        return (int) (Math.pow(averageMeanSquare, 0.5d) + 0.5);
    }

}
