package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.ServerChannel;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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

    public AudioStreamSender(ModelBuilder builder, ServerChannel currentAudioChannel, InetAddress address, int port, DatagramSocket socket) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
        this.address = address;
        this.port = port;
        this.socket = socket;
    }

    public void init() {
        // Create the audio capture object to read information in.
        microphone = new Microphone();
        microphone.init();
    }

    @Override
    public void run() {
        senderActive = true;

        JSONObject obj1 = new JSONObject().put("channel", currentAudioChannel.getId())
                .put("name", builder.getPersonalUser().getName());


        // set 255 with jsonObject - sendData is automatically init with zeros
        byte[] jsonData = new byte[255];
        byte[] objData = new byte[0];
        try {
            objData = obj1.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // set every byte new which is from jsonObject and let the rest be still 0
        for (int i = 0; i < objData.length; i++) {
            Arrays.fill(jsonData, i, i + 1, objData[i]);
        }

        // start recording audio
        microphone.startRecording();

        // start sending
        while (senderActive) {

            byte[] data = microphone.readData();

            // put both byteArrays in one
            byte[] sendData = new byte[AUDIO_DATAGRAM_PAKET_SIZE];
            System.arraycopy(jsonData, 0, sendData, 0, jsonData.length);
            System.arraycopy(data, 0, sendData, jsonData.length, data.length);

            DatagramPacket packet = new DatagramPacket(sendData, AUDIO_DATAGRAM_PAKET_SIZE, address, port);

            try {
                // send to address
                socket.send(packet);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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