package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import static de.uniks.stp.util.Constants.*;

public class Speaker {
    private final ModelBuilder builder;
    private AudioFormat format;
    private SourceDataLine speaker;

    public Speaker(ModelBuilder builder) {
        this.builder = builder;
    }

    public void init() {
        // audio format
        format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

        speaker = builder.getLinePoolService().getSelectedSpeaker();
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
}
