package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private Speaker speaker;
    private boolean receiverActive;
    private byte[] data;
    private DatagramSocket socket;

    public AudioStreamReceiver(ModelBuilder builder, InetAddress address, int port, DatagramSocket socket) {
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
            System.arraycopy(data, 0, receivedJson, 0, receivedJson.length);
            System.arraycopy(data, receivedJson.length, receivedData, 0, receivedData.length);

            JSONObject jsonData = new JSONObject(new String(receivedJson));
//            System.out.println(jsonData);
            if (!jsonData.getString("name").equals(builder.getPersonalUser().getName())) {
                speaker.writeData(receivedData);
            }
        }
        speaker.stopPlayback();
        socket.close();
    }

    public void stop() {
        receiverActive = false;
    }
}