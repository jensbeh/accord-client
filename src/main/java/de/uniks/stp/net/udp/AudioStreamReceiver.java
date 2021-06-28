package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.AudioMember;
import de.uniks.stp.model.ServerChannel;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final ServerChannel currentAudioChannel;
    private DatagramSocket socket;
    private boolean receiverActive;
    private byte[] data;
    private ArrayList<AudioMember> connectedUser;
    private HashMap<String, Speaker> receiverSpeakerMap;

    public AudioStreamReceiver(ModelBuilder builder, ServerChannel currentAudioChannel, DatagramSocket socket) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
        this.socket = socket;
    }

    public void init() {
        connectedUser = new ArrayList<>();
        receiverSpeakerMap = new HashMap<>();

        data = new byte[1279];
    }

    @Override
    public void run() {
        receiverActive = true;

        while (receiverActive) {
            if (!socket.isClosed()) {

                DatagramPacket packet = new DatagramPacket(data, data.length);

                try {
                    socket.receive(packet);
                    data = packet.getData(); // important to set because of testing - there is no manipulation of packet in test
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] receivedJson = new byte[255];
                byte[] receivedData = new byte[1024];

                // get all information from data
                for (int i = 0; i < data.length; i++) {
                    if (i < 255) {
                        Arrays.fill(receivedJson, i, i + 1, data[i]);
                    } else {
                        Arrays.fill(receivedData, i - 255, i - 255 + 1, data[i]);
                    }
                }

                String jsonStr = new String(receivedJson);
                if (jsonStr.contains("{")) {
                    JSONObject jsonData = new JSONObject(jsonStr);
                    String senderName = jsonData.getString("name");

                    // set receivedData to speaker of the senderName
//            if (!senderName.equals(builder.getPersonalUser().getName())) {
                    if (receiverSpeakerMap != null) {
                        receiverSpeakerMap.get(senderName).writeData(receivedData);
                    }
//            }
                }
            }
        }
        // stop speaker from all connectedUser
        for (AudioMember audioMember : connectedUser) {
            receiverSpeakerMap.get(audioMember.getName()).stopPlayback();
        }

        socket.close();
    }

    /**
     * the method creates a new speaker for the new connected user and stores it in a HashMap
     */
    public void newConnectedUser(AudioMember newMember) {
        connectedUser.add(newMember);

        receiverSpeakerMap.put(newMember.getName(), new Speaker());
        receiverSpeakerMap.get(newMember.getName()).init();
        receiverSpeakerMap.get(newMember.getName()).startPlayback();
    }

    /**
     * the method stops the speaker for the user which disconnects and delete the user
     */
    public void removeConnectedUser(AudioMember removeMember) {
        connectedUser.remove(removeMember);

        receiverSpeakerMap.get(removeMember.getName()).stopPlayback();
        receiverSpeakerMap.remove(removeMember.getName());
    }

    public void stop() {
        receiverActive = false;
    }

}