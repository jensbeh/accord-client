package de.uniks.stp.util;

import javax.sound.sampled.*;
import java.util.HashMap;

public class LinePoolService {
    private HashMap<String, TargetDataLine> microphones;
    private HashMap<String, SourceDataLine> speakers;
    private HashMap<String, Mixer> mixerMap;
    private TargetDataLine selectedMicrophone;
    private SourceDataLine selectedSpeaker;
    private String selectedMicrophoneName;
    private String selectedSpeakerName;
    private HashMap<String, Line> portMixer;
    private float microphoneVolume;
    private float speakerVolume;

    public LinePoolService() {
    }

    /**
     * Collects all microphones and speaker and a mixerMap for later to set new Speaker while in/join a channel
     */
    public void init() {
        microphones = new HashMap<>();
        speakers = new HashMap<>();
        portMixer = new HashMap<>();

        mixerMap = new HashMap<>();

        for (Mixer.Info thisMixerInfo : AudioSystem.getMixerInfo()) {
            System.out.println("Mixer: " + thisMixerInfo.getDescription() +
                    " [" + thisMixerInfo.getName() + "]");
            Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);

            if (thisMixerInfo.getDescription().equals("Port Mixer")) {
                // sourceLineInfo
                for (Line.Info thisLineInfo : thisMixer.getSourceLineInfo()) {
                    try {
                        Line thisLine = thisMixer.getLine(thisLineInfo);
                        portMixer.put(thisMixerInfo.getName(), thisLine);
                    } catch (LineUnavailableException lineUnavailableException) {
                        lineUnavailableException.printStackTrace();
                    }
                }

                // targetLineInfo
                for (Line.Info thisLineInfo : thisMixer.getTargetLineInfo()) {
                    try {
                        Line thisLine = thisMixer.getLine(thisLineInfo);
                        portMixer.put(thisMixerInfo.getName(), thisLine);
                    } catch (LineUnavailableException lineUnavailableException) {
                        lineUnavailableException.printStackTrace();
                    }
                }
            } else if (thisMixerInfo.getDescription().contains("Direct Audio Device: DirectSound")) {
                mixerMap.put(thisMixerInfo.getName(), thisMixer);
                Line.Info[] targetLineInfo = thisMixer.getTargetLineInfo();
                Line.Info[] sourceLineInfo = thisMixer.getSourceLineInfo();
                //Gets Microphones
                if (targetLineInfo.length >= 1 && targetLineInfo[0].getLineClass() == TargetDataLine.class) {
                    try {
                        TargetDataLine targetDataLine = (TargetDataLine) thisMixer.getLine(targetLineInfo[0]);
                        microphones.put(thisMixerInfo.getName(), targetDataLine);
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
                //Gets Speakers
                if (sourceLineInfo.length >= 1 && sourceLineInfo[0].getLineClass() == SourceDataLine.class) {
                    try {
                        SourceDataLine speakerDataLine = (SourceDataLine) thisMixer.getLine(sourceLineInfo[0]);
                        speakers.put(thisMixerInfo.getName(), speakerDataLine);
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // remove defaults because there is no port mixer
        microphones.remove(microphones.entrySet().iterator().next().getKey());
        speakers.remove(speakers.entrySet().iterator().next().getKey());

//                if (thisLineInfo.getLineClass().getName().equals("javax.sound.sampled.Port")) {
//                    Line thisLine = null;
//                    try {
//                        thisLine = thisMixer.getLine(thisLineInfo);
//                        PortMixer.put(thisMixerInfo.getName(), thisLine);
//                    } catch (LineUnavailableException lineUnavailableException) {
//                        lineUnavailableException.printStackTrace();
//                    }

//                    try {
//                        thisLine.open();
//                    } catch (LineUnavailableException lineUnavailableException) {
//                        lineUnavailableException.printStackTrace();
//                    }
//                    System.out.println("  Source Port: "
//                            + thisLineInfo.toString());
//                    for (Control thisControl : thisLine.getControls()) {
//                        System.out.println(thisControl.getType());
//                    }
//                    thisLine.close();
//                }
    }

    /**
     * returns the Map with all microphones for comboBox in AudioController
     */
    public HashMap<String, TargetDataLine> getMicrophones() {
        return this.microphones;
    }

    /**
     * returns the selected microphone
     */
    public TargetDataLine getSelectedMicrophone() {
        return this.selectedMicrophone;
    }

    /**
     * returns the selected microphone name
     */
    public String getSelectedMicrophoneName() {
        return this.selectedMicrophoneName;
    }

    /**
     * returns the Map with all speaker for comboBox in AudioController
     */
    public HashMap<String, SourceDataLine> getSpeakers() {
        return this.speakers;
    }

    /**
     * returns the selected speaker - make every time a new one because of horrible audio when more then 1 user in a channel
     */
    public SourceDataLine getSelectedSpeaker() {
        Line.Info[] sourceLineInfo = mixerMap.get(selectedSpeakerName).getSourceLineInfo();
        SourceDataLine speakerDataLine = null;
        try {
            speakerDataLine = (SourceDataLine) mixerMap.get(selectedSpeakerName).getLine(sourceLineInfo[0]);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return speakerDataLine;
    }

    /**
     * returns the selected speaker name
     */
    public String getSelectedSpeakerName() {
        return this.selectedSpeakerName;
    }

    /**
     * sets a new selected microphone from comboBox - if no one is saved at start the first mic will set to selected
     */
    public void setSelectedMicrophone(String newMicrophoneName) {
        if (microphones.containsKey(newMicrophoneName)) {
            this.selectedMicrophone = microphones.get(newMicrophoneName);
            this.selectedMicrophoneName = newMicrophoneName;
            System.out.println("mic: " + newMicrophoneName);
        } else {
            System.err.println("No microphone (" + newMicrophoneName + ") found! resetting microphone...");
            // set first microphone in list to selected
            for (var microphone : microphones.entrySet()) {
                this.selectedMicrophoneName = microphone.getKey();
                this.selectedMicrophone = microphone.getValue();
                break;
            }
        }

        setMicVolumeToPort();
    }

    /**
     * sets a new selected speaker from comboBox - if no one is saved at start the first speaker will set to selected
     */
    public void setSelectedSpeaker(String newSpeakerName) {
        if (speakers.containsKey(newSpeakerName)) {
            this.selectedSpeakerName = newSpeakerName;
            this.selectedSpeaker = speakers.get(newSpeakerName);
            System.out.println("speaker: " + newSpeakerName);
        } else {
            System.err.println("No speaker (" + newSpeakerName + ") found!");
            // set first speaker in list to selected
            for (var speaker : speakers.entrySet()) {
                this.selectedSpeakerName = speaker.getKey();
                this.selectedSpeaker = speaker.getValue();
                break;
            }
        }
    }

    public float getMicrophoneVolume() {
        return this.microphoneVolume;
    }

    public void setMicrophoneVolume(float microphoneVolume) {
        this.microphoneVolume = microphoneVolume;
        if (selectedMicrophone != null) {
            setMicVolumeToPort();
        }
    }

    private void setMicVolumeToPort() {
        // set mic volume
        for (var portName : portMixer.keySet()) {
            if (portName.contains(selectedMicrophoneName.substring(0, 24))) {
                Line thisMicrophoneLine = portMixer.get(portName);
                try {
                    thisMicrophoneLine.open();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
                for (Control thisControl : thisMicrophoneLine.getControls()) {
                    if (thisControl.getType().equals(FloatControl.Type.VOLUME)) {
                        FloatControl volumeControl = (FloatControl) thisControl; // range 0.0 - 1.0     :   0.27058825
                        volumeControl.setValue(microphoneVolume);
                        System.out.println(volumeControl.getValue() + " : " + volumeControl.getMinimum() + " : " + volumeControl.getMaximum());
                        break;
                    }
                }
                thisMicrophoneLine.close();
            }
        }
    }

    public float getSpeakerVolume() {
        return this.speakerVolume;
    }

    public void setSpeakerVolume(float speakerVolume) {
        this.speakerVolume = speakerVolume;
    }
}
