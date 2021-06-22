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
            this.address = InetAddress.getByName(AUDIO_STREAM_ADDRESS);
            this.port = AUDIO_STREAM_PORT;

            this.sender = new AudioStreamSender(builder, address, port);
            this.sender.init();
            this.receiver = new AudioStreamReceiver(builder, address, port);
            this.receiver.init();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.senderThread = new Thread(this.sender);
        this.receiverThread = new Thread(this.receiver);
    }

    public void startStream() {
        this.senderThread.start();
        this.receiverThread.start();
    }

    public void stopStream() {
        this.senderThread.stop(); //TODO should be stop safer!
        this.receiverThread.stop(); //TODO should be stop safer!
    }
}