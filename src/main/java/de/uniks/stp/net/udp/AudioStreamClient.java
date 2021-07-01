package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.model.AudioMember;
import de.uniks.stp.model.ServerChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static util.Constants.AUDIO_STREAM_ADDRESS;
import static util.Constants.AUDIO_STREAM_PORT;

public class AudioStreamClient {


    private final ModelBuilder builder;
    private final ServerChannel currentAudioChannel;
    private AudioStreamReceiver receiver;
    private AudioStreamSender sender;
    private Thread receiverThread;
    private Thread senderThread;
    private static DatagramSocket socket;

    public AudioStreamClient(ModelBuilder builder, ServerChannel currentAudioChannel) {
        this.builder = builder;
        this.currentAudioChannel = currentAudioChannel;
    }

    public void init() {
        try {
            InetAddress address = InetAddress.getByName(AUDIO_STREAM_ADDRESS);
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

    public static void setSocket(DatagramSocket newSocket) {
        socket = newSocket;
    }

    public void muteMicrophone(boolean mute) {
        if (mute) {
            senderThread = new Thread(sender);
            senderThread.start();
        }
    }
}
