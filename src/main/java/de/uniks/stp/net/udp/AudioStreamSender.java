package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class AudioStreamSender implements Runnable {


    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private Microphone sender;
    private DatagramSocket socket;

    public AudioStreamSender(ModelBuilder builder, InetAddress address, int port) {
        this.builder = builder;
        this.address = address;
        this.port = port;
    }

    public void init() {
        // Create the audio capture object to read information in.
        this.sender = new Microphone();
        this.sender.init();

        // Create the socket on which to send data.
        try {
            this.socket = new DatagramSocket();
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
     *     "channel": "channelId"
     *     "name": "currentUserName"
     * }
     *
     */

    @Override
    public void run() {
        this.sender.startRecording();
    }
}