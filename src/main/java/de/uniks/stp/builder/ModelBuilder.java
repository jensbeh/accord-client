package de.uniks.stp.builder;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import de.uniks.stp.model.*;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.udp.AudioStreamClient;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import de.uniks.stp.util.ResourceManager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static de.uniks.stp.util.Constants.*;

public class ModelBuilder {
    private Server currentServer;
    private CurrentUser personalUser;
    private URL soundFile;
    private ServerSystemWebSocket serverSystemWebSocket;
    private PrivateSystemWebSocketClient USER_CLIENT;
    private PrivateChatWebSocket privateChatWebSocketClient;
    private ServerChatWebSocket serverChatWebSocketClient;

    private RestClient restClient;
    private boolean playSound;
    private boolean doNotDisturb;
    private boolean showNotifications;
    private String theme;
    private Clip clip;

    private AudioStreamClient audioStreamClient;
    private ServerChannel currentAudioChannel;
    private boolean muteMicrophone;
    private boolean muteHeadphones;
    private AudioInputStream audioInputStream;

    private boolean loadUserData = true;
    private boolean inServerChat;
    private PrivateChat currentPrivateChat;
    private boolean firstMuted;
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

    public void setSoundFile(URL soundFile) {
        this.soundFile = soundFile;
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


    public ServerSystemWebSocket getServerSystemWebSocket() {
        return serverSystemWebSocket;
    }

    private URL getSoundFile() {
        return soundFile;
    }

    public void setSERVER_USER(ServerSystemWebSocket serverSystemWebSocket) {
        this.serverSystemWebSocket = serverSystemWebSocket;
    }

    public PrivateSystemWebSocketClient getUSER_CLIENT() {
        return USER_CLIENT;
    }

    public void setUSER_CLIENT(PrivateSystemWebSocketClient USER_CLIENT) {
        this.USER_CLIENT = USER_CLIENT;
    }

    public PrivateChatWebSocket getPrivateChatWebSocketClient() {
        return privateChatWebSocketClient;
    }

    public void setPrivateChatWebSocketClient(PrivateChatWebSocket privateChatWebSocketClient) {
        this.privateChatWebSocketClient = privateChatWebSocketClient;
    }

    //Server WebSocket getter/setter
    public ServerChatWebSocket getServerChatWebSocketClient() {
        return serverChatWebSocketClient;
    }

    public void setServerChatWebSocketClient(ServerChatWebSocket serverChatWebSocketClient) {
        this.serverChatWebSocketClient = serverChatWebSocketClient;
    }

    public RestClient getRestClient() {
        return this.restClient;
    }

    public void playSound() {
        if (soundFile == null) {
            setSoundFile(ModelBuilder.class.getResource(ROOT_PATH + "/sounds/notification/default.wav"));
        }
        if (clip != null) {
            clip.stop();
        }
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(getSoundFile().openStream()));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(getVolume());
            clip.start();
            // If you want the sound to loop infinitely, then put: clip.loop(Clip.LOOP_CONTINUOUSLY);
            // If you want to stop the sound, then use clip.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setVolume(Float number) {
        ResourceManager.saveVolume(personalUser.getName(), number);
    }

    private float getVolume() {
        return ResourceManager.getVolume(personalUser.getName());
    }

    public void saveSettings() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/settings.json"));
            JsonObject settings = new JsonObject();
            settings.put("doNotDisturb", doNotDisturb);
            settings.put("showNotifications", showNotifications);
            settings.put("playSound", playSound);
            settings.put("theme", theme);
            settings.put("muteMicrophone", muteMicrophone);
            settings.put("muteHeadphones", muteHeadphones);
            settings.put("firstMuted", firstMuted);
            Jsoner.serialize(settings, writer);
            writer.close();
        } catch (Exception e) {
            System.out.println("Error in saveSettings");
            e.printStackTrace();
        }
    }

    public void loadSettings() {
        try {
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/settings.json"))) {
                Files.createFile(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/settings.json"));
                doNotDisturb = false;
                showNotifications = true;
                playSound = true;
                theme = "Dark";
                muteMicrophone = true;
                muteHeadphones = true;
                firstMuted = false;
                saveSettings();
            }
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/settings.json"));
            JsonObject parsedSettings = (JsonObject) Jsoner.deserialize(reader);
            doNotDisturb = (boolean) parsedSettings.get("doNotDisturb");
            showNotifications = (boolean) parsedSettings.get("showNotifications");
            playSound = (boolean) parsedSettings.get("playSound");
            theme = (String) parsedSettings.get("theme");
            muteMicrophone = (boolean) parsedSettings.get("muteMicrophone");
            muteHeadphones = (boolean) parsedSettings.get("muteHeadphones");
            firstMuted = (boolean) parsedSettings.get("firstMuted");
            reader.close();

        } catch (Exception e) {
            System.out.println("Error in loadSettings");
            e.printStackTrace();
        }
    }

    public boolean isDoNotDisturb() {
        return doNotDisturb;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public boolean isShowNotifications() {
        return showNotifications;
    }

    public void setDoNotDisturb(boolean doNotDisturb) {
        this.doNotDisturb = doNotDisturb;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
    }

    public void setAudioStreamClient(AudioStreamClient audioStreamClient) {
        this.audioStreamClient = audioStreamClient;
    }

    public AudioStreamClient getAudioStreamClient() {
        return this.audioStreamClient;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setCurrentAudioChannel(ServerChannel currentAudioChannel) {
        this.currentAudioChannel = currentAudioChannel;
    }

    public ServerChannel getCurrentAudioChannel() {
        return this.currentAudioChannel;
    }

    public void muteMicrophone(boolean muteMicrophone) {
        this.muteMicrophone = muteMicrophone;
    }

    public void setMuteMicrophone() {
        if (audioStreamClient != null) {
            this.audioStreamClient.muteMicrophone(muteMicrophone);
        }
    }

    public boolean getMuteMicrophone() {
        return muteMicrophone;
    }

    public void muteHeadphones(boolean muteHeadphones) {
        this.muteHeadphones = muteHeadphones;
        if (audioStreamClient != null) {
            this.audioStreamClient.muteHeadphone(muteHeadphones);
        }
    }

    public boolean getMuteHeadphones() {
        return muteHeadphones;
    }

    public void setLoadUserData(boolean loadUserData) {
        this.loadUserData = loadUserData;
    }

    public boolean getLoadUserData() {
        return loadUserData;
    }

    public void setInServerChat(boolean state) {
        this.inServerChat = state;
    }

    public boolean getInServerChat() {
        return this.inServerChat;
    }

    public void setCurrentPrivateChat(PrivateChat currentPrivateChat) {
        this.currentPrivateChat = currentPrivateChat;
    }

    public PrivateChat getCurrentPrivateChat() {
        return this.currentPrivateChat;
    }

    public void setMicrophoneFirstMuted(boolean muted) {
        this.firstMuted = muted;
    }

    public boolean getMicrophoneFirstMuted() {
        return firstMuted;
    }
}