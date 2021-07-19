package de.uniks.stp.util;

import javax.sound.sampled.*;
import java.util.HashMap;

public class LinePoolService {
    private HashMap<String, TargetDataLine> microphones;
    private HashMap<String, SourceDataLine> speakers;
    private TargetDataLine selectedMicrophone;
    private SourceDataLine selectedSpeaker;

    public LinePoolService() {
    }

    public void init() {
        microphones = new HashMap<>();
        speakers = new HashMap<>();

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(info);
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

    public HashMap<String, TargetDataLine> getMicrophones() {
        return this.microphones;
    }

    public HashMap<String, SourceDataLine> getSpeakers() {
        return this.speakers;
    }

    public void setSelectedMicrophone(String selectedMicrophoneName) {
        if (microphones.containsKey(selectedMicrophoneName)) {
            selectedMicrophone = microphones.get(selectedMicrophoneName);
            System.out.println("XXX: " + selectedMicrophoneName);
        } else {
            System.err.println("No microphone (" + selectedMicrophoneName + ") found!");
        }
    }

    public void setSelectedSpeaker(String selectedSpeakerName) {
        if (speakers.containsKey(selectedSpeakerName)) {
            selectedSpeaker = speakers.get(selectedSpeakerName);
            System.out.println("YYY: " + selectedSpeakerName);

        } else {
            System.err.println("No speaker (" + selectedSpeakerName + ") found!");
        }
    }
}
