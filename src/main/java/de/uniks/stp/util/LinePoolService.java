package de.uniks.stp.util;

import javax.sound.sampled.*;
import java.util.HashMap;

public class LinePoolService {
    private HashMap<String, TargetDataLine> microphones;
    private HashMap<String, SourceDataLine> speaker;

    public LinePoolService() {
    }

    public void init() {
        microphones = new HashMap<>();
        speaker = new HashMap<>();

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
                    speaker.put(info.getName(), speakerDataLine);
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
