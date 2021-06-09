package de.uniks.stp.builder;

import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.WebSocketClient;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class ModelBuilder {
    private Server currentServer;
    private CurrentUser personalUser;
    private ServerChannel currentServerChannel;

    private WebSocketClient SERVER_USER;
    private WebSocketClient USER_CLIENT;
    private WebSocketClient privateChatWebSocketCLient;
    private WebSocketClient serverChatWebSocketClient;

    private RestClient restClient;
    private Clip clip;
    /////////////////////////////////////////
    //  Setter
    /////////////////////////////////////////

    public void buildPersonalUser(String name, String password, String userKey) {
        personalUser = new CurrentUser().setName(name).setUserKey(userKey).setPassword(password);
    }

    public User buildUser(String name, String id) {
        for (User user : personalUser.getUser()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        User newUser = new User().setName(name).setId(id).setStatus(true);
        personalUser.withUser(newUser);
        return newUser;
    }

    public User buildServerUser(Server server, String name, String id, Boolean status) {
        for (User user : server.getUser()) {
            if (user.getId().equals(id)) {
                if (user.isStatus() == status) {
                    return user;
                } else {
                    server.withoutUser(user);
                    User updatedUser = new User().setName(name).setId(id).setStatus(status);
                    server.withUser(updatedUser);
                    return updatedUser;
                }
            }
        }
        User newUser = new User().setName(name).setId(id).setStatus(status);
        server.withUser(newUser);
        return newUser;
    }

    public Server buildServer(String name, String id) {
        for (Server server : personalUser.getServer()) {
            if (server.getId().equals(id)) {
                return server;
            }
        }
        Server newServer = new Server().setName(name).setId(id);
        personalUser.withServer(newServer);
        return newServer;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }


    /////////////////////////////////////////
    //  Getter
    /////////////////////////////////////////

    public List<Server> getServers() {
        return this.personalUser.getServer() != null ? Collections.unmodifiableList(this.personalUser.getServer()) : Collections.emptyList();
    }

    public CurrentUser getPersonalUser() {
        return personalUser;
    }

    public Server getCurrentServer() {
        return currentServer;
    }


    public WebSocketClient getSERVER_USER() {
        return SERVER_USER;
    }

    public void setSERVER_USER(WebSocketClient SERVER_USER) {
        this.SERVER_USER = SERVER_USER;
    }

    public WebSocketClient getUSER_CLIENT() {
        return USER_CLIENT;
    }

    public void setUSER_CLIENT(WebSocketClient USER_CLIENT) {
        this.USER_CLIENT = USER_CLIENT;
    }

    public WebSocketClient getPrivateChatWebSocketCLient() {
        return privateChatWebSocketCLient;
    }

    public void setPrivateChatWebSocketCLient(WebSocketClient privateChatWebSocketCLient) {
        this.privateChatWebSocketCLient = privateChatWebSocketCLient;
    }

    //Server WebSocket getter/setter
    public WebSocketClient getServerChatWebSocketClient() {
        return serverChatWebSocketClient;
    }

    public void setServerChatWebSocketClient(WebSocketClient serverChatWebSocketClient) {
        this.serverChatWebSocketClient = serverChatWebSocketClient;
    }

    public RestClient getRestClient() {
        return this.restClient;
    }

    public void playSound() {
        if (clip != null) {
            clip.stop();
        }
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("src/main/resources/de/uniks/stp/sounds/notification-sound.wav"));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
            // If you want the sound to loop infinitely, then put: clip.loop(Clip.LOOP_CONTINUOUSLY);
            // If you want to stop the sound, then use clip.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}