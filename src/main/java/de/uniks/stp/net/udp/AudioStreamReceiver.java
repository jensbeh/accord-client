package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.AudioMember;
import de.uniks.stp.model.ServerChannel;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private final ServerChannel currentAudioChannel;
//    private Speaker speaker;
    private boolean receiverActive;
    private byte[] data;
    private DatagramSocket socket;
    private ArrayList<AudioMember> connectedUser;
    private HashMap<String, Speaker> receiverSpeakerMap;

    public AudioStreamReceiver(ModelBuilder builder, ServerChannel currentAudioChannel, InetAddress address, int port, DatagramSocket socket) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
        this.address = address;
        this.port = port;
        this.socket = socket;
    }

    public void init() {
//        speaker = new Speaker();
//        speaker.init();

        connectedUser = new ArrayList<>();
        receiverSpeakerMap = new HashMap<>();

        data = new byte[1279];
    }

    @Override
    public void run() {
        receiverActive = true;

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

//            System.out.println(jsonData);
//            if (!senderName.equals(builder.getPersonalUser().getName())) {
                receiverSpeakerMap.get(senderName).writeData(receivedData);
//            }
        }
        // stop all speaker
        for (AudioMember audioMember : connectedUser) {
            receiverSpeakerMap.get(audioMember.getName()).stopPlayback();
        }
        socket.close();
    }

    public void newConnectedUser(AudioMember audioMember) {
        connectedUser.add(audioMember);

        receiverSpeakerMap.put(audioMember.getName(), new Speaker());
        receiverSpeakerMap.get(audioMember.getName()).init();
        receiverSpeakerMap.get(audioMember.getName()).startPlayback();
    }


    public void stop() {
        receiverActive = false;
    }
}