package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private Speaker speaker;
    private boolean receiverActive;
    private byte[] data;
    private MulticastSocket socket;

    public AudioStreamReceiver(ModelBuilder builder, InetAddress address, int port, MulticastSocket socket) {
        this.builder = builder;
        this.address = address;
        this.port = port;
        this.socket = socket;
    }

    public void init() {
        speaker = new Speaker();
        speaker.init();

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

//            JSONObject jsonData = new JSONObject(new String(receivedJson));
//            System.out.println(jsonData);
//            if (!jsonData.getString("name").equals(builder.getPersonalUser().getName())) {
            speaker.writeData(receivedData);
//            }
        }
        speaker.stopPlayback();
        socket.close();
    }

    public void stop() {
        receiverActive = false;
    }
}