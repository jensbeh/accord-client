package de.uniks.stp.builder;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.settings.Spotify.SpotifyConnection;
import de.uniks.stp.controller.home.HomeViewController;
import de.uniks.stp.model.*;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.net.udp.AudioStreamClient;
import de.uniks.stp.net.updateSteamGameController;
import de.uniks.stp.net.websocket.privatesocket.PrivateChatWebSocket;
import de.uniks.stp.net.websocket.privatesocket.PrivateSystemWebSocketClient;
import de.uniks.stp.net.websocket.serversocket.ServerChatWebSocket;
import de.uniks.stp.net.websocket.serversocket.ServerSystemWebSocket;
import de.uniks.stp.util.LinePoolService;
import de.uniks.stp.util.ResourceManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import kong.unirest.JsonNode;
import org.apache.commons.io.FileUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.uniks.stp.util.Constants.*;

public class ModelBuilder {
    private Server currentServer;
    private ChatViewController currentChatViewController;
    private CurrentUser personalUser;
    private URL soundFile;
    private URL channelSoundFile;
    private ServerSystemWebSocket serverSystemWebSocket;
    private PrivateSystemWebSocketClient USER_CLIENT;
    private PrivateChatWebSocket privateChatWebSocketClient;
    private ServerChatWebSocket serverChatWebSocketClient;
    private HomeViewController homeViewController;

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

    private boolean loadUserData = true;
    private boolean inServerState;
    private PrivateChat currentPrivateChat;
    private boolean firstMuted;

    private boolean spotifyShow;
    private boolean steamShow;
    private String spotifyToken;
    private String spotifyRefresh;
    private String steamToken;
    private LinePoolService linePoolService;
    private SpotifyConnection spotifyConnection;


    private Thread getSteamGame;
    private ObservableList<User> blockedUsers;


    private void updateDescription(PropertyChangeEvent propertyChangeEvent) {
        System.out.println("PropertyChange");
        getRestClient().updateDescription(getPersonalUser().getId(), getPersonalUser().getDescription(), getPersonalUser().getUserKey(), response -> {
            JsonNode body = response.getBody();
            if (!body.getObject().getString("status").equals("success")) {
                System.err.println("Error in updateDescription");
                System.err.println(body);
            }
        });
    }

    private boolean isSteamRun;
    /////////////////////////////////////////
    //  Setter
    /////////////////////////////////////////

    public void buildPersonalUser(String name, String password, String userKey) {
        personalUser = new CurrentUser().setName(name).setUserKey(userKey).setPassword(password).setDescription("#");
        personalUser.addPropertyChangeListener(CurrentUser.PROPERTY_DESCRIPTION, this::updateDescription);
    }

    public User buildUser(String name, String id, String description) {
        for (User user : personalUser.getUser()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        User newUser = new User().setName(name).setId(id).setStatus(true).setDescription(description);
        personalUser.withUser(newUser);
        return newUser;
    }

    public User buildServerUser(Server server, String name, String id, Boolean status, String description) {
        for (User user : server.getUser()) {
            if (user.getId().equals(id)) {
                if (user.isStatus() == status) {
                    return user;
                } else {
                    server.withoutUser(user);
                    User updatedUser = new User().setName(name).setId(id).setStatus(status).setDescription(description);
                    server.withUser(updatedUser);
                    return updatedUser;
                }
            }
        }
        User newUser = new User().setName(name).setId(id).setStatus(status).setDescription(description);
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

    /**
     * play notification sound
     */
    public void playSound() {
        if (ResourceManager.getComboValue(personalUser.getName()).isEmpty()) {
            setSoundFile(ModelBuilder.class.getResource(ROOT_PATH + "/sounds/notification/default.wav"));
        } else {
            String newValue = ResourceManager.getComboValue(personalUser.getName());
            for (File file : ResourceManager.getNotificationSoundFiles()) {
                String fileName = file.getName().substring(0, file.getName().length() - 4);
                if (fileName.equals(newValue)) {
                    try {
                        URL url = file.toURI().toURL();
                        this.setSoundFile(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (clip != null) {
            clip.stop();
        }
        try {
            System.out.println("ComboBox: " + ResourceManager.getComboValue(personalUser.getName()));
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

    /**
     * play notification sound when you join/leave an audio channel
     */
    public void playChannelSound(String action) {
        if (action.equals("join")) {
            setChannelSoundFile(ModelBuilder.class.getResource(ROOT_PATH + "/sounds/channelAction/join.wav"));
        } else {
            setChannelSoundFile(ModelBuilder.class.getResource(ROOT_PATH + "/sounds/channelAction/left.wav"));
        }
        if (clip != null) {
            clip.stop();
        }
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(getChannelSoundFile().openStream()));
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(0.0f);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private URL getChannelSoundFile() {
        return this.channelSoundFile;
    }

    private void setChannelSoundFile(URL resource) {
        this.channelSoundFile = resource;
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
            settings.put("spotifyShow", spotifyShow);
            settings.put("spotifyToken", spotifyToken);
            settings.put("spotifyRefresh", spotifyRefresh);
            settings.put("steamShow", steamShow);
            settings.put("steamToken", steamToken);
            settings.put("microphone", getLinePoolService().getSelectedMicrophoneName());
            settings.put("speaker", getLinePoolService().getSelectedSpeakerName());
            settings.put("microphoneVolume", getLinePoolService().getMicrophoneVolume());
            settings.put("speakerVolume", getLinePoolService().getSpeakerVolume());
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
                muteMicrophone = false;
                muteHeadphones = false;
                firstMuted = false;
                spotifyShow = false;
                spotifyToken = null;
                spotifyRefresh = null;
                steamShow = false;
                steamToken = "";
                getLinePoolService().setMicrophoneVolume(0.2f);
                getLinePoolService().setSpeakerVolume(0.2f);
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
            spotifyShow = (boolean) parsedSettings.get("spotifyShow");
            steamShow = (boolean) parsedSettings.get("steamShow");
            spotifyToken = (String) parsedSettings.get("spotifyToken");
            spotifyRefresh = (String) parsedSettings.get("spotifyRefresh");
            steamToken = (String) parsedSettings.get("steamToken");
            getLinePoolService().setMicrophoneVolume(((BigDecimal) parsedSettings.get("microphoneVolume")).floatValue());
            getLinePoolService().setSpeakerVolume(((BigDecimal) parsedSettings.get("speakerVolume")).floatValue());
            getLinePoolService().setSelectedMicrophone((String) parsedSettings.get("microphone"));
            getLinePoolService().setSelectedSpeaker((String) parsedSettings.get("speaker"));
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

    public boolean getMuteMicrophone() {
        return muteMicrophone;
    }

    public void muteHeadphones(boolean muteHeadphones) {
        this.muteHeadphones = muteHeadphones;
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

    public void setInServerState(boolean state) {
        this.inServerState = state;
    }

    public boolean getInServerState() {
        return this.inServerState;
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


    public ChatViewController getCurrentChatViewController() {
        return currentChatViewController;
    }

    public void setCurrentChatViewController(ChatViewController currentChatViewController) {
        this.currentChatViewController = currentChatViewController;
    }

    public boolean isSpotifyShow() {
        return spotifyShow;
    }

    public void setSpotifyShow(boolean spotifyShow) {
        this.spotifyShow = spotifyShow;
    }

    public boolean isSteamShow() {
        return steamShow;
    }

    public void setSteamShow(boolean steamShow) {
        this.steamShow = steamShow;
    }

    public String getSpotifyToken() {
        return spotifyToken;
    }

    public void setSpotifyToken(String spotifyToken) {
        this.spotifyToken = spotifyToken;
    }

    public String getSpotifyRefresh() {
        return spotifyRefresh;
    }

    public void setSpotifyRefresh(String spotifyRefresh) {
        this.spotifyRefresh = spotifyRefresh;
    }

    public SpotifyConnection getSpotifyConnection() {
        return spotifyConnection;
    }

    public void setSpotifyConnection(SpotifyConnection spotifyConnection) {
        this.spotifyConnection = spotifyConnection;
    }

    public String getSteamToken() {
        return steamToken;
    }

    public void setSteamToken(String steamToken) {
        this.steamToken = steamToken;
    }

    public void setLinePoolService(LinePoolService linePoolService) {
        this.linePoolService = linePoolService;
    }

    public LinePoolService getLinePoolService() {
        return this.linePoolService;
    }

    public ObservableList<User> getBlockedUsers() {
        return this.blockedUsers;
    }

    public void setBlockedUsers(List<User> userList) {
        this.blockedUsers = FXCollections.observableList(userList);
    }

    public void addBlockedUser(User user) {
        if (this.blockedUsers == null) {
            this.blockedUsers = FXCollections.observableList(new ArrayList<>());
        }
        this.blockedUsers.add(user);
    }

    public void removeBlockedUser(User blockedUser) {
        if (this.blockedUsers != null) {
            for (User user : this.getBlockedUsers()) {
                if (blockedUser.getId().equals(user.getId())) {
                    this.blockedUsers.remove(user);
                    return;
                }
            }
        }
    }

    public void getGame() {
        if (getSteamGame == null) {
            isSteamRun = true;
            getSteamGame = new Thread(new updateSteamGameController(this));
            getSteamGame.start();
        }
    }

    public void stopGame() {
        if (getSteamGame != null) {
            isSteamRun = false;
            getSteamGame = null;
        }
    }

    public void setHomeViewController(HomeViewController homeViewController) {
        this.homeViewController = homeViewController;
    }

    public HomeViewController getHomeViewController() {
        return homeViewController;
    }

    public boolean isSteamRun() {
        return isSteamRun;
    }
}