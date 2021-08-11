package de.uniks.stp.builder;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.UserProfileController;
import de.uniks.stp.controller.home.HomeViewController;
import de.uniks.stp.controller.settings.AudioController;
import de.uniks.stp.controller.settings.Spotify.SpotifyConnection;
import de.uniks.stp.controller.settings.subcontroller.SteamLoginController;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import kong.unirest.JsonNode;

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
    private SteamLoginController steamLoginController;


    private Thread getSteamGame;
    private ObservableList<User> blockedUsers;
    private boolean isSteamRun;
    private Runnable handleMicrophoneHeadphone;
    private AudioController audiocontroller;

    private UserProfileController userProfileController;


    private void updateDescription(PropertyChangeEvent propertyChangeEvent) {
        if (isSteamShow() || isSpotifyShow()) {
            getRestClient().updateDescription(getPersonalUser().getId(), getPersonalUser().getDescription(), getPersonalUser().getUserKey(), response -> {
                JsonNode body = response.getBody();
                if (!body.getObject().getString("status").equals("success")) {
                    System.err.println("Error in updateDescription");
                    System.err.println(body);
                }
            });
        }
    }

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
        User newUser = new User().setName(name).setId(id).setStatus(true).setDescription(description).setUserVolume(100.0);
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

    public List<Server> getServers() {
        return this.personalUser.getServer() != null ? Collections.unmodifiableList(this.personalUser.getServer()) : Collections.emptyList();
    }

    public CurrentUser getPersonalUser() {
        return personalUser;
    }

    public Server getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }

    public ServerSystemWebSocket getServerSystemWebSocket() {
        return serverSystemWebSocket;
    }

    private URL getSoundFile() {
        return soundFile;
    }

    public void setSoundFile(URL soundFile) {
        this.soundFile = soundFile;
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

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
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

    private float getVolume() {
        return ResourceManager.getVolume(personalUser.getName());
    }

    public void setVolume(Float number) {
        ResourceManager.saveVolume(personalUser.getName(), number);
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

    public void saveUserVolume(String userName, double value) {
        try {
            Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"));
            JsonObject volumeSettings = new JsonObject();
            if (reader.read() != -1) {
                reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"));
                JsonObject parsedSettings = (JsonObject) Jsoner.deserialize(reader);
                volumeSettings.put(userName, value);
                for (var name : parsedSettings.keySet()) {
                    if (!userName.equals(name)) {
                        volumeSettings.put(name, parsedSettings.get(name));
                    }
                }
            }

            BufferedWriter writer = Files.newBufferedWriter(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"));
            Jsoner.serialize(volumeSettings, writer);
            writer.close();

            for (User user : personalUser.getUser()) {
                if (user.getName().equals(userName)) {
                    user.setUserVolume(value);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error in saveUserVolume");
            e.printStackTrace();
        }
    }

    public void loadUserVolumes() {
        try {
            if (!Files.exists(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"))) {
                Files.createFile(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"));
            } else {
                Reader reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"));
                if (reader.read() != -1) {
                    reader = Files.newBufferedReader(Path.of(APPDIR_ACCORD_PATH + CONFIG_PATH + "/userVolumes.json"));
                    JsonObject parsedSettings = (JsonObject) Jsoner.deserialize(reader);
                    for (var userName : parsedSettings.keySet()) {
                        for (User user : personalUser.getUser()) {
                            if (user.getName().equals(userName)) {
                                user.setUserVolume(((BigDecimal) parsedSettings.get(userName)).doubleValue());
                            }
                        }
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            System.out.println("Error in loadUserVolumes");
            e.printStackTrace();
        }
    }

    public boolean isDoNotDisturb() {
        return doNotDisturb;
    }

    public void setDoNotDisturb(boolean doNotDisturb) {
        this.doNotDisturb = doNotDisturb;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public boolean isShowNotifications() {
        return showNotifications;
    }

    public void setShowNotifications(boolean showNotifications) {
        this.showNotifications = showNotifications;
    }

    public AudioStreamClient getAudioStreamClient() {
        return this.audioStreamClient;
    }

    public void setAudioStreamClient(AudioStreamClient audioStreamClient) {
        this.audioStreamClient = audioStreamClient;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public ServerChannel getCurrentAudioChannel() {
        return this.currentAudioChannel;
    }

    public void setCurrentAudioChannel(ServerChannel currentAudioChannel) {
        this.currentAudioChannel = currentAudioChannel;
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

    public boolean getLoadUserData() {
        return loadUserData;
    }

    public void setLoadUserData(boolean loadUserData) {
        this.loadUserData = loadUserData;
    }

    public boolean getInServerState() {
        return this.inServerState;
    }

    public void setInServerState(boolean state) {
        this.inServerState = state;
    }

    public PrivateChat getCurrentPrivateChat() {
        return this.currentPrivateChat;
    }

    public void setCurrentPrivateChat(PrivateChat currentPrivateChat) {
        this.currentPrivateChat = currentPrivateChat;
    }

    public boolean getMicrophoneFirstMuted() {
        return firstMuted;
    }

    public void setMicrophoneFirstMuted(boolean muted) {
        this.firstMuted = muted;
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

    public SteamLoginController getSteamLoginController() {
        return steamLoginController;
    }

    public void setSteamLoginController(SteamLoginController steamLoginController) {
        this.steamLoginController = steamLoginController;
    }

    public String getSteamToken() {
        return steamToken;
    }

    public void setSteamToken(String steamToken) {
        this.steamToken = steamToken;
    }

    public LinePoolService getLinePoolService() {
        return this.linePoolService;
    }

    public void setLinePoolService(LinePoolService linePoolService) {
        this.linePoolService = linePoolService;
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
        if (getSteamGame != null) {
            isSteamRun = false;
            stopGame();
        }
        isSteamRun = true;
        getSteamGame = new Thread(new updateSteamGameController(this));
        getSteamGame.start();
    }

    public void stopGame() {
        isSteamRun = false;
        if (getSteamGame != null) {
            getSteamGame.interrupt();
        }
        getSteamGame = null;

    }

    public HomeViewController getHomeViewController() {
        return homeViewController;
    }

    public void setHomeViewController(HomeViewController homeViewController) {
        this.homeViewController = homeViewController;
    }

    public boolean isSteamRun() {
        return isSteamRun;
    }

    public void setSteamRun(boolean steamRun) {
        isSteamRun = steamRun;
    }

    public Runnable getHandleMicrophoneHeadphone() {
        return this.handleMicrophoneHeadphone;
    }

    public void setHandleMicrophoneHeadphone(Runnable handleMicrophoneHeadphone) {
        this.handleMicrophoneHeadphone = handleMicrophoneHeadphone;
    }

    public AudioController getAudioController() {
        return this.audiocontroller;
    }

    public void setAudioController(AudioController audiocontroller) {
        this.audiocontroller = audiocontroller;
    }

    public void clear() {
        homeViewController = null;
        currentPrivateChat = null;
        currentChatViewController = null;
        currentAudioChannel = null;
        currentServer = null;
        serverSystemWebSocket = null;
        serverChatWebSocketClient = null;
        audioStreamClient = null;
        audiocontroller = null;
        USER_CLIENT = null;
    }

    public UserProfileController getUserProfileController() {
        return userProfileController;
    }

    public void setUserProfileController(UserProfileController userProfileController) {
        this.userProfileController = userProfileController;
    }
}