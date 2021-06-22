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
        format = new AudioFormat(AUDIO_BITRATE, AUDIO_SAMPLE_SIZE, AUDIO_CHANNELS, AUDIO_SIGNING, AUDIO_BYTE_ORDER);

        // audio object (microphone information?)
        info = new DataLine.Info(TargetDataLine.class, format);

        try {
            // get microphoneLine
            microphone = (TargetDataLine) AudioSystem.getLine(info);

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        data = new byte[1024]; // TODO 1024 Byte ???
    }

    /**
     * the method start reading with the microphone
     */
    public void startRecording() {
        try {
            // open microphone line
            microphone.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        // start reading
        microphone.start();
    }

    /**
     * the method returns the data which the microphone is reading
     */
    public byte[] readData() {
        // store audio in data
        microphone.read(data, 0, data.length);
        return data;
    }

    /**
     * the method stops the microphone
     */
    public void stopRecording() {
        microphone.drain();
        microphone.stop();
        microphone.close();
    }
}
