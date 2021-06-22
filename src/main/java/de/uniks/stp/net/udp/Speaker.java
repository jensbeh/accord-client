package de.uniks.stp.net.udp;

import javax.sound.sampled.*;

import static util.Constants.*;

public class Speaker {
    private AudioFormat format;
    private SourceDataLine speaker;

    public Speaker() {
    }

    public void init() {
        // audio format
        format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

        // audio object (speaker information?)
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
        try {
            // get speakerLine
            speaker = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * the method opens and starts the speaker
     */
    public void startPlayback() {
        try {
            // open speaker line
            speaker.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        // start output
        speaker.start();
    }

    /**
     * the method writes receivedData into the speaker for audio sound
     */
    public void writeData(byte[] receivedData) {
        // writes audio in speaker
        speaker.write(receivedData, 0, receivedData.length);
    }

    /**
     * the method stops the speaker
     */
    public void stopPlayback() {
        speaker.drain();
        speaker.stop();
        speaker.close();
    }

    public AudioFormat getFormat() {
        return format;
    }
}
