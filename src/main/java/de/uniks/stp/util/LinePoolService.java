package de.uniks.stp.util;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.uniks.stp.util.Constants.*;

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
            Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);

            // get Port Mixer
            if (thisMixerInfo.getDescription().equals("Port Mixer")) {
                sourceLineInfo(thisMixer, thisMixerInfo);
                targetLineInfo(thisMixer, thisMixerInfo);
                // get Devices
            } else if (thisMixerInfo.getDescription().contains("Direct Audio Device: DirectSound")) {
                detDevices(thisMixer, thisMixerInfo);
            }
        }

        removeDefaultMicAndSpeaker();
    }

    private void detDevices(Mixer thisMixer, Mixer.Info thisMixerInfo) {
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

    private void targetLineInfo(Mixer thisMixer, Mixer.Info thisMixerInfo) {
        for (Line.Info thisLineInfo : thisMixer.getTargetLineInfo()) {
            try {
                Line thisLine = thisMixer.getLine(thisLineInfo);
                portMixer.put(thisMixerInfo.getName(), thisLine);
            } catch (LineUnavailableException lineUnavailableException) {
                lineUnavailableException.printStackTrace();
            }
        }
    }

    private void sourceLineInfo(Mixer thisMixer, Mixer.Info thisMixerInfo) {
        for (Line.Info thisLineInfo : thisMixer.getSourceLineInfo()) {
            try {
                Line thisLine = thisMixer.getLine(thisLineInfo);
                portMixer.put(thisMixerInfo.getName(), thisLine);
            } catch (LineUnavailableException lineUnavailableException) {
                lineUnavailableException.printStackTrace();
            }
        }
    }

    /**
     * remove default mic and speaker because there is no port mixer to set controls
     */
    private void removeDefaultMicAndSpeaker() {
        // remove default mics because there is no port mixer
        removeDefaultDevice(microphones);
        // remove default speaker because there is no port mixer
        removeDefaultDevice(speakers);
    }

    private void removeDefaultDevice(HashMap<String, ?> devices) {
        List<String> toRemove = new ArrayList<>();
        for (String devName : devices.keySet()) {
            boolean foundDevice = foundDevice(devName);
            if (!foundDevice) {
                toRemove.add(devName);
            }
        }
        if (toRemove.size() > 0) {
            for (String nameToRemove : toRemove) {
                devices.remove(nameToRemove);
            }
            toRemove.clear();
        }
    }

    private boolean foundDevice(String devName) {
        for (String portName : portMixer.keySet()) {
            if (portName.contains(devName.substring(0, 20))) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns the Map with all microphones for comboBox in AudioController
     */
    public HashMap<String, TargetDataLine> getMicrophones() {
        return this.microphones;
    }

    /**
     * returns the selected microphone
     * IMPORTANT: Travis.ci has no real microphone with port mixer. In thís case the mic is always null and i return the default system mic. Happens only when Travis is running.
     */
    public TargetDataLine getSelectedMicrophone() {
        if (selectedMicrophone != null) {
            return this.selectedMicrophone;
        } else {
            AudioFormat format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = null;
            try {
                microphone = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            return microphone;
        }
    }

    /**
     * sets a new selected microphone from comboBox - if no one is saved at start the first mic will set to selected
     */
    public void setSelectedMicrophone(String newMicrophoneName) {
        if (microphones.containsKey(newMicrophoneName)) {
            this.selectedMicrophone = microphones.get(newMicrophoneName);
            this.selectedMicrophoneName = newMicrophoneName;
            System.out.println("Microphone: " + newMicrophoneName);
        } else {
            System.err.println("No microphone found! Set microphone to the default one...");
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
     * IMPORTANT: Travis.ci has no real speaker with port mixer. In thís case there is no mixer for the speaker and i return the default system speaker. Happens only when Travis is running.
     */
    public SourceDataLine getSelectedSpeaker() {
        if (mixerMap.size() > 0) {
            Line.Info[] sourceLineInfo = mixerMap.get(selectedSpeakerName).getSourceLineInfo();
            SourceDataLine speakerDataLine = null;
            try {
                speakerDataLine = (SourceDataLine) mixerMap.get(selectedSpeakerName).getLine(sourceLineInfo[0]);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            return speakerDataLine;
        } else {
            AudioFormat format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine speaker = null;
            try {
                speaker = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            }
            return speaker;
        }
    }

    /**
     * sets a new selected speaker from comboBox - if no one is saved at start the first speaker will set to selected
     */
    public void setSelectedSpeaker(String newSpeakerName) {
        if (speakers.containsKey(newSpeakerName)) {
            this.selectedSpeakerName = newSpeakerName;
            this.selectedSpeaker = speakers.get(newSpeakerName);
            System.out.println("Speaker: " + newSpeakerName);
        } else {
            System.err.println("No speaker found! Set speaker to the default one...");
            // set first speaker in list to selected
            for (var speaker : speakers.entrySet()) {
                this.selectedSpeakerName = speaker.getKey();
                this.selectedSpeaker = speaker.getValue();
                break;
            }
        }

        setSpeakerVolumeToPort();
    }

    /**
     * returns the selected speaker name
     */
    public String getSelectedSpeakerName() {
        return this.selectedSpeakerName;
    }

    /**
     * returns the current microphone volume
     */
    public float getMicrophoneVolume() {
        return this.microphoneVolume;
    }

    /**
     * sets new microphone volume
     */
    public void setMicrophoneVolume(float microphoneVolume) {
        this.microphoneVolume = microphoneVolume;
        if (selectedMicrophone != null) {
            setMicVolumeToPort();
        }
    }

    /**
     * sets new microphone volume to the microphone port mixer
     */
    private void setMicVolumeToPort() {
        // set mic volume
        for (var portName : portMixer.keySet()) {
            if (portName.contains(selectedMicrophoneName.substring(0, 24))) {
                setVolumeToPort(portName, microphoneVolume);
                break;
            }
        }
    }

    /**
     * returns the current speaker volume
     */
    public float getSpeakerVolume() {
        return this.speakerVolume;
    }

    /**
     * sets new speaker volume
     */
    public void setSpeakerVolume(float speakerVolume) {
        this.speakerVolume = speakerVolume;
        if (selectedSpeaker != null) {
            setSpeakerVolumeToPort();
        }
    }

    /**
     * sets new speaker volume to the speaker port mixer
     */
    private void setSpeakerVolumeToPort() {
        // set speaker volume
        for (var portName : portMixer.keySet()) {
            if (portName.contains(selectedSpeakerName.substring(0, 20))) {
                setVolumeToPort(portName, speakerVolume);
                break;
            }
        }
    }

    /**
     * set volume to the port mixer
     */
    private void setVolumeToPort(String selectedDeviceName, float volume) {
        Line thisLine = portMixer.get(selectedDeviceName);
        try {
            // need to open the Line to get Control - important to close after
            thisLine.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        for (Control thisControl : thisLine.getControls()) {
            if (thisControl.getType().equals(FloatControl.Type.VOLUME)) {
                FloatControl volumeControl = (FloatControl) thisControl; // range 0.0 - 1.0     :   0.27058825
                volumeControl.setValue(volume);
                break;
            }
        }
        thisLine.close();
    }
}