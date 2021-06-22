package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private Speaker speaker;
    private boolean receiverActive;
    private byte[] data;
    private DatagramSocket socket;

    public AudioStreamReceiver(ModelBuilder builder, InetAddress address, int port) {
        this.builder = builder;
        this.address = address;
        this.port = port;
    }

    public void init() {
        speaker = new Speaker();
        speaker.init();

        // Create the socket on which to receive data.
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        data = new byte[1024];
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
            System.out.println("received: " + packet);
            //speaker.writeData(data);
        }
        speaker.stopPlayback();
        socket.close();
    }

    public void stop() {
        receiverActive = false;
    }
}