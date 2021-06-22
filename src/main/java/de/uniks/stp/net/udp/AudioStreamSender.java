package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class AudioStreamSender implements Runnable {


    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private Microphone sender;
    private DatagramSocket socket;
    private boolean senderActive;
    private byte[] data;
    private DatagramPacket packet;

    public AudioStreamSender(ModelBuilder builder, InetAddress address, int port) {
        this.builder = builder;
        this.address = address;
        this.port = port;
    }

    public void init() {
        // Create the audio capture object to read information in.
        sender = new Microphone();
        sender.init();

        // Create the socket on which to send data.
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * SAMPLE METADATA:
     *
     * - Datagram-Pakete 1279 Bytes
     * - Erste 255 Bytes im Json Format - falls nicht 255 Bytes, dann mit 0 auffüllen
     * - Restliche 1024 Byte für Audiodatenpakete
     *
     * {
     * "channel": "channelId"
     * "name": "currentUserName"
     * }
     *
     */

    @Override
    public void run() {
        senderActive = true;

        // start recording audio
        sender.startRecording();

        while (senderActive) {
            data = sender.readData();
            packet = new DatagramPacket(data, data.length, address, port);

            try {
                // send to address
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // stop if senderActive is set to false in stop method in this class
        sender.stopRecording();
    }

    public void stop() {
        senderActive = false;
    }
}