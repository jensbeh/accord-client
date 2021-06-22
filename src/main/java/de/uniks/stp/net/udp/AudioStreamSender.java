package de.uniks.stp.net.udp;

import com.github.cliftonlabs.json_simple.JsonObject;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.ServerChannel;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static util.Constants.AUDIO_DATAGRAM_PAKET_SIZE;

public class AudioStreamSender implements Runnable {


    private final ModelBuilder builder;
    private final ServerChannel currentAudioChannel;
    private final InetAddress address;
    private final int port;
    private Microphone microphone;
    private DatagramSocket socket;
    private boolean senderActive;

    public AudioStreamSender(ModelBuilder builder, ServerChannel currentAudioChannel, InetAddress address, int port) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
        this.address = address;
        this.port = port;
    }

    public void init() {
        // Create the audio capture object to read information in.
        microphone = new Microphone();
        microphone.init();

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

        JSONObject obj1 = new JSONObject().put("channel", currentAudioChannel.getId()).put("name", builder.getPersonalUser().getName());


        // set 255 with jsonObject - sendData is automatically init with zeros
        byte[] jsonData = new byte[255];
        System.out.println("obj: " + obj1.toString());
        byte[] objData = obj1.toString().getBytes(StandardCharsets.UTF_8);
//        String objData1 = "{\"channel\":\""+ currentAudioChannel.getId() +"\",\"name\":\""+ builder.getPersonalUser().getName() +"\"}";
//        System.out.println(objData3);
//        byte[] objData = objData1.getBytes(StandardCharsets.UTF_8);
        // set every byte new which is from jsonObject and let the rest be still 0
        for (int i = 0; i < objData.length; i++) {
            Arrays.fill(jsonData, i, i + 1, objData[i]);
        }

        // start recording audio
        microphone.startRecording();

        // start sending
        while (senderActive) {
//            int port = 4445; // destination port
//            InetAddress address = null;
//            try {
//                address = InetAddress.getByName("localhost");
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }

            byte[] data = microphone.readData();

            // put both byteArrays in one
            byte[] sendData = new byte[AUDIO_DATAGRAM_PAKET_SIZE];
            System.arraycopy(jsonData, 0, sendData, 0, jsonData.length);
            System.arraycopy(data, 0, sendData, jsonData.length, data.length);
            JSONObject testV = new JSONObject(new String(jsonData));

            DatagramPacket packet = new DatagramPacket(sendData, AUDIO_DATAGRAM_PAKET_SIZE, address, port);
//            DatagramPacket packet = new DatagramPacket(data, 1024, address, port);

            try {
                // send to address
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // stop if senderActive is set to false in stop method in this class
        microphone.stopRecording();
        socket.close();
    }

    public void stop() {
        senderActive = false;
    }
}