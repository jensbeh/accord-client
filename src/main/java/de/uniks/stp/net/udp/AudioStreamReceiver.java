package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.ServerChannel;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashMap;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private final ServerChannel currentAudioChannel;
    private Speaker speaker;
    private boolean receiverActive;
    private byte[] data;
    private MulticastSocket socket;

    private HashMap<String, byte[]> receiverMap;

    public AudioStreamReceiver(ModelBuilder builder, ServerChannel currentAudioChannel, InetAddress address, int port, MulticastSocket socket) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
        this.address = address;
        this.port = port;
        this.socket = socket;
    }

    public void init() {
        speaker = new Speaker();
        speaker.init();

        receiverMap = new HashMap<>();

        data = new byte[1279];
    }

    @Override
    public void run() {
        receiverActive = true;
        speaker.startPlayback();

        while (receiverActive) {
            DatagramPacket packet = new DatagramPacket(data, data.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] receivedJson = new byte[255];
            byte[] receivedData = new byte[1024];
//            System.arraycopy(data, 0, receivedJson, 0, receivedJson.length);
//            System.arraycopy(data, receivedJson.length, receivedData, 0, receivedData.length);

            // TODO use Fill
            // set every byte new which is from jsonObject and let the rest be still 0
            for (int i = 0; i < data.length; i++) {
                if (i < 255) {
                    Arrays.fill(receivedJson, i, i + 1, data[i]);
                } else {
                    Arrays.fill(receivedData, i - 255, i - 255 + 1, data[i]);
                }
            }

            JSONObject jsonData = new JSONObject(new String(receivedJson));
            String senderName = jsonData.getString("name");

            receiverMap.put(senderName, receivedData); // works for existing too

//            System.out.println(jsonData);
            if (receiverMap.size() == 1) {
                speaker.writeData(receivedData);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (receiverMap.size() > 1) {
                // convert to samples
                final int[] aSamples = toSamples(receiverMap.get(currentAudioChannel.getAudioMember().get(0).getName()));
                final int[] bSamples = toSamples(receiverMap.get(currentAudioChannel.getAudioMember().get(1).getName()));
                // mix by adding
                final int[] mix = new int[aSamples.length];
                for (int i = 0; i < mix.length; i++) {
                    mix[i] = aSamples[i] + bSamples[i];
                    // enforce min and max (may introduce clipping)
                    mix[i] = Math.min(Short.MAX_VALUE, mix[i]);
                    mix[i] = Math.max(Short.MIN_VALUE, mix[i]);
                }

                receiverMap.clear();
                System.out.println("more");
                speaker.writeData(toBytes(mix));
            }
        }
        speaker.stopPlayback();
        socket.close();
    }

    private static int[] toSamples(final byte[] byteSamples) {
        final int bytesPerChannel = 2;
        final int length = byteSamples.length / bytesPerChannel;
        if ((length % 2) != 0) throw new IllegalArgumentException("For 16 bit audio, length must be even: " + length);
        final int[] samples = new int[length];
        for (int sampleNumber = 0; sampleNumber < length; sampleNumber++) {
            final int sampleOffset = sampleNumber * bytesPerChannel;
            final int sample = byteToIntLittleEndian(byteSamples, sampleOffset, bytesPerChannel);
            samples[sampleNumber] = sample;
        }
        return samples;
    }

    // from https://github.com/hendriks73/jipes/blob/master/src/main/java/com/tagtraum/jipes/audio/AudioSignalSource.java#L238
    private static int byteToIntLittleEndian(final byte[] buf, final int offset, final int bytesPerSample) {
        int sample = 0;
        for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
            final int aByte = buf[offset + byteIndex] & 0xff;
            sample += aByte << 8 * (byteIndex);
        }
        return (short) sample;
    }

    private static byte[] toBytes(final int[] intSamples) {
        final int bytesPerChannel = 2;
        final int length = intSamples.length * bytesPerChannel;
        final byte[] bytes = new byte[length];
        for (int sampleNumber = 0; sampleNumber < intSamples.length; sampleNumber++) {
            final byte[] b = intToByteLittleEndian(intSamples[sampleNumber], bytesPerChannel);
            System.arraycopy(b, 0, bytes, sampleNumber * bytesPerChannel, bytesPerChannel);
        }
        return bytes;
    }

    private static byte[] intToByteLittleEndian(final int sample, final int bytesPerSample) {
        byte[] buf = new byte[bytesPerSample];
        for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
            buf[byteIndex] = (byte) ((sample >>> (8 * byteIndex)) & 0xFF);
        }
        return buf;
    }

    public void stop() {
        receiverActive = false;
    }
}