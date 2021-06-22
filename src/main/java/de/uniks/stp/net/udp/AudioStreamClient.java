package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static util.Constants.AUDIO_STREAM_ADDRESS;
import static util.Constants.AUDIO_STREAM_PORT;

public class AudioStreamClient {


    private final ModelBuilder builder;
    private AudioStreamSender sender;
    private AudioStreamReceiver receiver;
    private InetAddress address;
    private Thread senderThread;
    private Thread receiverThread;
    private int port;

    public AudioStreamClient(ModelBuilder builder) {
        this.builder = builder;
    }

    public void init() {
        try {
            address = InetAddress.getByName(AUDIO_STREAM_ADDRESS);
            port = AUDIO_STREAM_PORT;

            sender = new AudioStreamSender(builder, address, port);
            sender.init();
            receiver = new AudioStreamReceiver(builder, address, port);
            receiver.init();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        senderThread = new Thread(sender);
        receiverThread = new Thread(receiver);
    }

    public void startStream() {
        senderThread.start();
        receiverThread.start();
    }

    public void stopStream() {
        senderThread.stop(); //TODO should be stop safer!
        receiverThread.stop(); //TODO should be stop safer!
    }
}