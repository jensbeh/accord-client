package de.uniks.stp.net.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AudioStreamClient {


    private AudioStreamSender sender;
    private AudioStreamReceiver receiver;
    private InetAddress host;

    public void init() {
        try {
            this.host = InetAddress.getByName(String.valueOf(host));

            sender = new AudioStreamSender();
            sender.init();
            receiver = new AudioStreamReceiver();
            receiver.init();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }

    public void startStream() {

    }
}