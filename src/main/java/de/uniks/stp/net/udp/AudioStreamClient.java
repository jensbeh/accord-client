package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.AudioMember;
import de.uniks.stp.model.ServerChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static de.uniks.stp.util.Constants.AUDIO_STREAM_ADDRESS;
import static de.uniks.stp.util.Constants.AUDIO_STREAM_PORT;

public class AudioStreamClient {


    private final ModelBuilder builder;
    private final ServerChannel currentAudioChannel;
    private AudioStreamReceiver receiver;
    private AudioStreamSender sender;
    private Thread receiverThread;
    private Thread senderThread;
    private static DatagramSocket socket;
    private static InetAddress address;

    public AudioStreamClient(ModelBuilder builder, ServerChannel currentAudioChannel) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
    }

    public void init() {
        try {
            if (address == null) {
                address = InetAddress.getByName(AUDIO_STREAM_ADDRESS);
            }
            int port = AUDIO_STREAM_PORT;

            // Create the socket on which to send data.
            try {
                if (socket == null || socket.isClosed()) {
                    socket = new DatagramSocket();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            //init first receiver and then sender
            receiver = new AudioStreamReceiver(builder, socket);
            receiver.init();
            sender = new AudioStreamSender(builder, currentAudioChannel, address, port, socket);
            sender.init();


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //set both on threads so that quality is better
        receiverThread = new Thread(receiver);
        senderThread = new Thread(sender);
    }

    /**
     * starts the threads with receiver and sender
     */
    public void startStream() {
        receiverThread.start();
        senderThread.start();
    }

    /**
     * stops the threads with receiver and sender
     */
    public void disconnectStream() {
        sender.stop();
        receiver.stop();
    }

    /**
     * set new audioReceiverUser for new Speaker
     */
    public void setNewAudioMemberReceiver(AudioMember audioMember) {
        receiver.newConnectedUser(audioMember);
    }

    /**
     * removes audioReceiverUser with Speaker
     */
    public void removeAudioMemberReceiver(AudioMember audioMember) {
        receiver.removeConnectedUser(audioMember);
    }

    /**
     * following methods only needed for testing
     */
    public static void setSocket(DatagramSocket newSocket) {
        socket = newSocket;
    }

    public static void setAddress(InetAddress inetAddress) {
        address = inetAddress;
    }

    /**
     * set User to MuteList
     */
    public void setMutedUser(String mutedUser) {
        receiver.setMutedUser(mutedUser);
    }

    /**
     * remove User from MuteList
     */
    public void setUnMutedUser(String mutedUser) {
        receiver.setUnMutedUser(mutedUser);
    }

    /**
     * get all mutedUsers
     */
    public ArrayList<String> getMutedAudioMember() {
        return receiver.getMutedAudioMember();
    }


    /**
     * Sets new Microphone when already connected to a channel
     */
    public void setNewMicrophone() {
        sender.stop();
        sender.setNewMicrophone();
        senderThread = new Thread(sender);
        senderThread.start();
    }

    /**
     * Sets new Speaker when already connected to a channel
     */
    public void setNewSpeaker() {
        receiver.setNewSpeaker();
    }
}
