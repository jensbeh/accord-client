package de.uniks.stp.net.udp;

import com.github.cliftonlabs.json_simple.JsonObject;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.ServerChannel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static util.Constants.AUDIO_DATAGRAM_PAKET_SIZE;

public class AudioStreamSender implements Runnable {


    private final ModelBuilder builder;
    private final ServerChannel currentAudioChannel;
    private final InetAddress address;
    private final int port;
    private Microphone sender;
    private DatagramSocket socket;
    private boolean senderActive;
    private byte[] sendData;
    private byte[] data;
    private DatagramPacket packet;

    public AudioStreamSender(ModelBuilder builder, ServerChannel currentAudioChannel, InetAddress address, int port) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
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
     * <p>
     * - Datagram-Pakete 1279 Bytes
     * - Erste 255 Bytes im Json Format - falls nicht 255 Bytes, dann mit 0 auffüllen
     * - Restliche 1024 Byte für Audiodatenpakete
     * <p>
     * {
     * "channel": "channelId"
     * "name": "currentUserName"
     * }
     */

    @Override
    public void run() {
        senderActive = true;
        JsonObject obj = new JsonObject();
        obj.put("channel", currentAudioChannel.getId());
        obj.put("name", builder.getPersonalUser().getName());

        // start recording audio
        sender.startRecording();

        // set 255 with jsonObject - sendData is automatically init with zeros
        byte[] jsonData = new byte[255];
        byte[] objData = obj.toString().getBytes(StandardCharsets.UTF_8);
        // set every byte new which is from jsonObject and let the rest be still 0
        for (int i = 0; i < objData.length; i++) {
            Arrays.fill(jsonData, i, i+1, objData[i]);
        }

        // start sending
        while (senderActive) {
            data = sender.readData();

            // put both byteArrays in one
            byte[] sendData = new byte[AUDIO_DATAGRAM_PAKET_SIZE];
            System.arraycopy(jsonData,0, sendData,0, jsonData.length);
            System.arraycopy(data,0, sendData, jsonData.length, data.length);

            packet = new DatagramPacket(sendData, AUDIO_DATAGRAM_PAKET_SIZE, address, port);

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