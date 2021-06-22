package de.uniks.stp.net.udp;

import de.uniks.stp.builder.ModelBuilder;

import java.net.InetAddress;

public class AudioStreamReceiver implements Runnable {

    private final ModelBuilder builder;
    private final InetAddress address;
    private final int port;
    private Speaker speaker;

    public AudioStreamReceiver(ModelBuilder builder, InetAddress address, int port) {
        this.builder = builder;
        this.address = address;
        this.port = port;
    }

    public void init() {
        speaker = new Speaker();
        speaker.init();
    }

    @Override
    public void run() {

    }
}