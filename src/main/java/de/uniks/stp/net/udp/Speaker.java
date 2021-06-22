package de.uniks.stp.net.udp;

import javax.sound.sampled.*;

import static util.Constants.*;

public class Speaker {
    private AudioFormat format;
    private SourceDataLine speaker;

    public Speaker() {
    }

    public void init() {
        format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

        try {
            speaker = AudioSystem.getSourceDataLine(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    public void startPlayback() {
        try {
            speaker.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        speaker.start();
    }

    public void writeData(byte[] receivedData) {
        speaker.write(receivedData, 0, receivedData.length);
    }

    public void stopPlayback() {
        speaker.drain();
        speaker.stop();
        speaker.close();
    }

    public AudioFormat getFormat() {
        return format;
    }
}
