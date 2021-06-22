package de.uniks.stp.net.udp;

import javax.sound.sampled.*;

import static util.Constants.*;

public class Microphone {
    private AudioFormat format;
    private DataLine.Info info;
    private TargetDataLine microphone;
    private byte[] data;

    public Microphone() {
    }

    public void init() {
        // audio format
        this.format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

        // audio object (microphone information?)
        this.info = new DataLine.Info(TargetDataLine.class, this.format);

        try {
            // get microphoneLine
            this.microphone = (TargetDataLine) AudioSystem.getLine(this.info);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        this.data = new byte[1024]; // TODO 1024 Byte ???
    }

    /**
     * the method start reading with the microphone
     */
    public void startRecording() {
        try {
            // open microphone line
            this.microphone.open(this.format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        // start reading
        this.microphone.start();
    }

    /**
     * the method stops the microphone
     */
    public void stopRecording() {
        this.microphone.drain();
        this.microphone.stop();
        this.microphone.close();
    }
}
