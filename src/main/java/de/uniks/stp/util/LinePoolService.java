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

    public LinePoolService() {
    }

    /**
     * Collects all microphones and speaker and a mixerMap for later to set new Speaker while in/join a channel
     */
    public void init() {
        microphones = new HashMap<>();
        speakers = new HashMap<>();

        mixerMap = new HashMap<>();

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(info);
            mixerMap.put(info.getName(), mixer);
            Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
            //Gets Microphones
            if (targetLineInfo.length >= 1 && targetLineInfo[0].getLineClass() == TargetDataLine.class) {
                try {
                    TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(targetLineInfo[0]);
                    microphones.put(info.getName(), targetDataLine);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
            //Gets Speakers
            if (sourceLineInfo.length >= 1 && sourceLineInfo[0].getLineClass() == SourceDataLine.class) {
                try {
                    SourceDataLine speakerDataLine = (SourceDataLine) mixer.getLine(sourceLineInfo[0]);
                    speakers.put(info.getName(), speakerDataLine);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
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
}
