package de.uniks.stp.net.websocket.serversocket;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.ChatViewController;
import de.uniks.stp.controller.server.ServerViewController;
import de.uniks.stp.model.*;
import de.uniks.stp.net.udp.AudioStreamClient;
import de.uniks.stp.net.websocket.CustomWebSocketConfigurator;
import de.uniks.stp.util.JsonUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class ServerSystemWebSocket extends Endpoint {

    public final String COM_NOOP = "noop";
    private Timer noopTimer;
    private Session session;
    private ModelBuilder builder;
    private ServerViewController serverViewController;
    private ChatViewController chatViewController;

    public ServerSystemWebSocket(URI endpoint, String userKey) {
        startNoopTimer();
        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(userKey))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientConfig, endpoint);
        } catch (Exception e) {
            System.err.println("Error during establishing WebSocket connection:");
        }
    }

    public ServerViewController getServerViewController() {
        return this.serverViewController;
    }

    public void setServerViewController(ServerViewController serverViewController) {
        this.serverViewController = serverViewController;
    }

    public ModelBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // Store session
        this.session = session;
        // add MessageHandler
        this.session.addMessageHandler(String.class, this::onMessage);

        this.noopTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Send NOOP Message
                try {
                    sendMessage(COM_NOOP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000 * 30);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        // cancel timer
        this.noopTimer.cancel();
        // set session null
        this.session = null;
    }

    private void onMessage(String message) {
        // Process Message
        JsonObject jsonObject = (JsonObject) org.glassfish.json.JsonUtil.toJson(message);
        // Use callback to handle it
        this.handleMessage(jsonObject);
    }

    public void sendMessage(String message) throws IOException {
        // check if session is still open
        if (this.session != null && this.session.isOpen()) {
            // send message
            this.session.getBasicRemote().sendText(message);
            this.session.getBasicRemote().flushBatch();
        }
    }

    public void stop() throws IOException {
        // cancel timer
        this.noopTimer.cancel();
        // close session
        this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "NORMAL_CLOSURE"));
    }

    public Session getSession() {
        return session;
    }

    public void handleMessage(JsonStructure msg) {
        JsonObject jsonMsg = JsonUtil.parse(msg.toString());
        String userAction = jsonMsg.getString("action");
        JsonObject jsonData = jsonMsg.getJsonObject("data");
        String userName = "";
        String userId = "";
        if (!userAction.equals("audioJoined") && !userAction.equals("audioLeft") && !userAction.equals("messageUpdated") && !userAction.equals("messageDeleted")) {
            userName = jsonData.getString("name");
            userId = jsonData.getString("id");
        }
        if (userAction.equals("categoryCreated")) {
            createCategory(jsonData);
        }
        if (userAction.equals("categoryDeleted")) {
            deleteCategory(jsonData);
        }
        if (userAction.equals("categoryUpdated")) {
            updateCategory(jsonData);
        }

        if (userAction.equals("channelCreated")) {
            createChannel(jsonData);
        }
        if (userAction.equals("channelDeleted")) {
            deleteChannel(jsonData);
        }
        if (userAction.equals("channelUpdated")) {
            updateChannel(jsonData);
        }

        if (userAction.equals("userArrived")) {
            userArrived(jsonData);
        }
        if (userAction.equals("userExited")) {
            userExited(jsonData);
        }

        if (userAction.equals("userJoined")) {
            buildServerUser(userName, userId, true, "");
            serverViewController.showOnlineOfflineUsers();
        }
        if (userAction.equals("userLeft")) {
            if (userName.equals(builder.getPersonalUser().getName()) && builder.getCurrentServer() == serverViewController.getServer()) {
                Platform.runLater(() -> builder.getStageManager().showLoginScreen());
            }
            buildServerUser(userName, userId, false, "");
            serverViewController.showOnlineOfflineUsers();
        }

        if (userAction.equals("serverDeleted")) {
            deleteServer();
        }
        if (userAction.equals("serverUpdated")) {
            updateServer(userName);
        }

        // audioChannel
        if (userAction.equals("audioJoined")) {
            joinAudio(jsonData);
        }
        if (userAction.equals("audioLeft")) {
            leaveAudio(jsonData);
        }

        if (userAction.equals("messageUpdated")) {
            updateMessage(jsonData);
        }

        if (userAction.equals("messageDeleted")) {
            deleteMessage(jsonData);
        }

        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.showOnlineOfflineUsers();
        }
    }


    /**
     * refreshes the current category view and starts a new udp session
     */
    private void joinAudio(JsonObject jsonData) {
        for (Categories category : this.serverViewController.getServer().getCategories()) {
            if (jsonData.getString("category").equals(category.getId())) {
                joinVoiceChannel(jsonData, category);
            }
        }
    }

    private void joinVoiceChannel(JsonObject jsonData, Categories category) {
        String userId = jsonData.getString("id");
        for (ServerChannel serverChannel : category.getChannel()) {
            if (jsonData.getString("channel").equals(serverChannel.getId())) {
                UserJoinVoiceChannel(userId, serverChannel);
                playJoinSound(jsonData);
                // create new UDP-connection for personalUser when joined
                if (userId.equals(builder.getPersonalUser().getId())) {
                    createAudioCon(userId, serverChannel);
                }
                serverViewController.refreshAllChannelLists();
            }
        }
    }

    private void playJoinSound(JsonObject jsonData) {
        if (builder.getPersonalUser().getId().equals(jsonData.getString("id")) || (builder.getCurrentAudioChannel() != null && builder.getCurrentAudioChannel().getId().equals(jsonData.getString("channel")))) {
            builder.playChannelSound("join");
        }
    }

    private void UserJoinVoiceChannel(String userId, ServerChannel serverChannel) {
        String userName = "";
        for (User user : builder.getPersonalUser().getUser()) {
            if (user.getId().equals(userId)) {
                userName = user.getName();
                break;
            }
        }
        if (!userName.equals("")) {
            AudioMember audioMemberUser = new AudioMember().setId(userId).setName(userName);
            serverChannel.withAudioMember(audioMemberUser);
            if (builder.getAudioStreamClient() != null) {
                builder.getAudioStreamClient().setNewAudioMemberReceiver(audioMemberUser);
            }
        }
    }

    private void createAudioCon(String userId, ServerChannel serverChannel) {
        if (builder.getAudioStreamClient() != null) {
            builder.getAudioStreamClient().disconnectStream();
            builder.setAudioStreamClient(null);
        }
        AudioMember audioMemberPersonalUser = new AudioMember().setId(userId).setName(builder.getPersonalUser().getName());
        serverChannel.withAudioMember(audioMemberPersonalUser);

        builder.setCurrentAudioChannel(serverChannel);
        serverViewController.showAudioConnectedBox();
        AudioStreamClient audiostreamClient = new AudioStreamClient(builder, serverChannel);
        builder.setAudioStreamClient(audiostreamClient);
        audiostreamClient.init();
        for (AudioMember audioMember : serverChannel.getAudioMember()) {
            audiostreamClient.setNewAudioMemberReceiver(audioMember);
        }
        audiostreamClient.startStream();
    }

    /**
     * refreshes the current category view and stops the udp session
     */
    private void leaveAudio(JsonObject jsonData) {
        for (Categories category : this.serverViewController.getServer().getCategories()) {
            if (jsonData.getString("category").equals(category.getId())) {
                leaveVoiceChannel(jsonData, category);
            }
        }
    }

    private void leaveVoiceChannel(JsonObject jsonData, Categories category) {
        for (ServerChannel serverChannel : category.getChannel()) {
            if (jsonData.getString("channel").equals(serverChannel.getId())) {
                removeAudioMember(serverChannel, jsonData);
                serverViewController.refreshAllChannelLists();
            }
        }
    }

    private void removeAudioMember(ServerChannel serverChannel, JsonObject jsonData) { // which audioMember disconnects?
        String userId = jsonData.getString("id");
        for (AudioMember audioMember : serverChannel.getAudioMember()) {
            if (audioMember.getId().equals(userId)) {
                removeUserFromVoiceChat(jsonData, serverChannel, audioMember, userId);
                break;
            }
        }
    }

    private void removeUserFromVoiceChat(JsonObject jsonData, ServerChannel serverChannel, AudioMember audioMember, String userId) {
        serverChannel.withoutAudioMember(audioMember);
        playLeaveSound(audioMember, jsonData);
        // personalUser disconnects
        if (userId.equals(builder.getPersonalUser().getId())) {
            builder.setCurrentAudioChannel(null);
            builder.getAudioStreamClient().disconnectStream();
            builder.setAudioStreamClient(null);
        }
        // other user disconnects
        else {
            if (builder.getAudioStreamClient() != null) {
                builder.getAudioStreamClient().removeAudioMemberReceiver(audioMember);
            }
        }
    }

    private void playLeaveSound(AudioMember audioMember, JsonObject jsonData) {
        if (!audioMember.getId().equals(builder.getPersonalUser().getId()) &&
                builder.getCurrentAudioChannel() != null &&
                builder.getCurrentAudioChannel().getId().equals(jsonData.getString("channel"))) {
            builder.playChannelSound("left");
        }
    }

    /**
     * set new message Text and refresh the ListView
     */
    private void updateMessage(JsonObject jsonData) {
        String msgId = jsonData.getString("id");
        String text = jsonData.getString("text");

        for (Message msg : serverViewController.getCurrentChannel().getMessage()) {
            if (msg.getId().equals(msgId)) {
                msg.setMessage(text);
                chatViewController.updateMessage(msg);
                break;
            }
        }
    }


    /**
     * deletes the message
     */
    private void deleteMessage(JsonObject jsonData) {
        String msgId = jsonData.getString("id");

        for (Message msg : serverViewController.getCurrentChannel().getMessage()) {
            if (msg.getId().equals(msgId)) {
                chatViewController.removeMessage(msg);
                msg.removeYou();
                break;
            }
        }
    }

    /**
     * Build a serverUser with this instance of server.
     */
    private User buildServerUser(String userName, String userId, boolean online, String description) {
        return builder.buildServerUser(serverViewController.getServer(), userName, userId, online, description);
    }

    /**
     * update server
     */
    private void updateServer(String serverName) {
        boolean audioIsOpen = builder.getCurrentAudioChannel() != null && serverViewController.getServer().getName().equals(builder.getCurrentAudioChannel().getCategories().getServer().getName());

        serverViewController.getServer().setName(serverName);
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            Platform.runLater(serverViewController::changeServerName);
        }
        serverViewController.getHomeViewController().showServerUpdate();

        if (audioIsOpen) {
            showAudioBox();
        }
    }

    /**
     * deletes server
     */
    private void deleteServer() {
        // thrown out from audioChannel
        if (builder.getAudioStreamClient() != null && builder.getCurrentAudioChannel() != null) {
            for (Categories categories : serverViewController.getServer().getCategories()) {
                if (categories.getId().equals(builder.getCurrentAudioChannel().getCategories().getId())) {
                    disconnectFromAudioChannel(categories);
                }
            }
        }

        Platform.runLater(this::serverDeletedAlert);
        serverViewController.getHomeViewController().stopServer(serverViewController.getServer());
    }

    private void disconnectFromAudioChannel(Categories categories) {
        for (ServerChannel channel : categories.getChannel()) {
            if (channel.getId().equals(builder.getCurrentAudioChannel().getId())) {
                disconnectAudioChannelNotOwner(serverViewController.getServer().getId(), categories.getId(), builder.getCurrentAudioChannel().getId());
                break;
            }
        }
    }

    private void serverDeletedAlert() {
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            builder.getPersonalUser().withoutServer(serverViewController.getServer());
            builder.setCurrentServer(null);
            serverViewController.getHomeViewController().serverDeleted();
        } else {
            builder.getPersonalUser().withoutServer(serverViewController.getServer());
            builder.setCurrentServer(null);
            serverViewController.getHomeViewController().refreshServerList();
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
        alert.setTitle("Server deleted!");
        alert.setHeaderText("Server " + serverViewController.getServer().getName() + " was deleted!");
        alert.showAndWait();
    }

    /**
     * adds a new Controller for a new Category with new view
     */
    private void createCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("server");
        String categoryId = jsonData.getString("id");
        String name = jsonData.getString("name");
        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                boolean found = checkForCategory(server, categoryId);
                if (!found) {
                    createNewCategory(server, categoryId, name);
                }
            }
        }
    }

    private void createNewCategory(Server server, String categoryId, String name) {
        Categories category = new Categories().setName(name).setId(categoryId);
        server.withCategories(category);
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.generateCategoryChannelView(category);
        }
        serverViewController.refreshAllChannelLists();
    }

    private boolean checkForCategory(Server server, String categoryId) {
        for (Categories categories : server.getCategories()) {
            if (categories.getId().equals(categoryId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * deletes a category with controller and view
     */
    private void deleteCategory(JsonObject jsonData) {
        Server currentServer;
        Categories deletedCategory;
        Node deletedNode;

        ArrayList<Object> objectArrayList = findDeletedCategory(jsonData);
        currentServer = (Server) objectArrayList.get(0);
        deletedCategory = (Categories) objectArrayList.get(1);
        deletedNode = (Node) objectArrayList.get(2);

        if (deletedNode != null) {
            // thrown out from audioChannel
            if (builder.getAudioStreamClient() != null && builder.getCurrentAudioChannel() != null) {
                disconnectFromAudioChannel(deletedCategory);
            }

            currentServer.withoutCategories(deletedCategory);
            Platform.runLater(() -> this.serverViewController.getCategoryBox().getChildren().remove(deletedNode));
            serverViewController.getCategorySubControllerList().get(deletedCategory).stop();
            serverViewController.getCategorySubControllerList().remove(deletedCategory);
            if (deletedCategory.getChannel().contains(serverViewController.getCurrentChannel()) || serverViewController.getServer().getCategories().size() == 0) {
                serverViewController.throwOutUserFromChatView();
            }
            serverViewController.refreshAllChannelLists();
        }
    }

    private ArrayList<Object> findDeletedCategory(JsonObject jsonData) {
        ArrayList<Object> objectArrayList = new ArrayList<>();
        String serverId = jsonData.getString("server");
        String categoryId = jsonData.getString("id");
        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                objectArrayList.add(server);
                objectArrayList.addAll(getCategoryAndNode(server, categoryId));
            }
        }
        return objectArrayList;
    }

    private ArrayList<Object> getCategoryAndNode(Server server, String categoryId) {
        for (Categories categories : server.getCategories()) {
            if (categories.getId().equals(categoryId)) {
                if (builder.getCurrentServer() == serverViewController.getServer()) {
                    return getNode(categories);
                }
            }
        }
        return new ArrayList<>(Arrays.asList(null, null));
    }

    private ArrayList<Object> getNode(Categories categories) {
        for (Node view : serverViewController.getCategoryBox().getChildren()) {
            if (view.getId().equals(categories.getId())) {
                return new ArrayList<>(Arrays.asList(categories, view));
            }
        }
        return new ArrayList<>(Arrays.asList(null, null));
    }

    /**
     * rename a Category and update it on the view
     */
    private void updateCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("server");
        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                findCategoryToUpdate(server, jsonData);
            }
        }
    }

    private void findCategoryToUpdate(Server server, JsonObject jsonData) {
        String categoryId = jsonData.getString("id");
        String name = jsonData.getString("name");
        for (Categories categories : server.getCategories()) {
            if (categories.getId().equals(categoryId) && !categories.getName().equals(name)) {
                categories.setName(name);
                break;
            }
        }
    }

    /**
     * adds the new channel to category for the user
     *
     * @param jsonData the message data
     */
    private void createChannel(JsonObject jsonData) {
        for (Server server : builder.getPersonalUser().getServer()) {
            findCategoryForNewChannel(server, jsonData);
        }
    }

    private void findCategoryForNewChannel(Server server, JsonObject jsonData) {
        String channelId = jsonData.getString("id");
        String channelName = jsonData.getString("name");
        String channelType = jsonData.getString("type");
        boolean channelPrivileged = jsonData.getBoolean("privileged");
        String categoryId = jsonData.getString("category");

        for (Categories cat : server.getCategories()) {
            if (cat.getId().equals(categoryId)) {
                ServerChannel newChannel = new ServerChannel().setId(channelId).setType(channelType).setName(channelName).setPrivilege(channelPrivileged);
                cat.withChannel(newChannel);
                serverViewController.refreshAllChannelLists();
                break;
            }
        }
    }

    /**
     * deletes channel from category for the user and eventually
     * get thrown out when users selected chat is the channel which will be deleted
     *
     * @param jsonData the message data
     */
    private void deleteChannel(JsonObject jsonData) {
        for (Server server : builder.getPersonalUser().getServer()) {
            findCategoryForChannelDel(server, jsonData);
        }
    }

    private void findCategoryForChannelDel(Server server, JsonObject jsonData) {
        String categoryId = jsonData.getString("category");
        for (Categories cat : server.getCategories()) {
            if (cat.getId().equals(categoryId)) {
                findChannelForDelete(server, cat, jsonData);
            }
        }
    }

    private void findChannelForDelete(Server server, Categories categories, JsonObject jsonData) {
        String channelId = jsonData.getString("id");
        String categoryId = jsonData.getString("category");
        for (ServerChannel channel : categories.getChannel()) {
            if (channel.getId().equals(channelId)) {
                // thrown out from audioChannel
                if (channel.getType().equals("audio")) {
                    if (builder.getAudioStreamClient() != null && builder.getCurrentAudioChannel() != null && builder.getCurrentAudioChannel().getId().equals(channelId)) {
                        disconnectAudioChannelNotOwner(server.getId(), categoryId, channelId);
                    }
                }
                categories.withoutChannel(channel);
                if (builder.getCurrentServer() == serverViewController.getServer()) {
                    if (serverViewController.getCurrentChannel().equals(channel) && channel.getType().equals("text")) {
                        serverViewController.throwOutUserFromChatView();
                    }
                }
                serverViewController.refreshAllChannelLists();
                return;
            }
        }
    }

    /**
     * is called when not server owner should disconnect from a audioChannel which is deleted
     */
    private void disconnectAudioChannelNotOwner(String serverId, String categoryId, String channelId) {
        serverViewController.leaveVoiceChannel(serverId, categoryId, channelId);
        builder.setCurrentAudioChannel(null);
        builder.getAudioStreamClient().disconnectStream();
        builder.setAudioStreamClient(null);
        builder.playChannelSound("left");
        if (builder.getInServerState()) {
            serverViewController.showAudioConnectedBox();
        } else {
            builder.getPrivateChatWebSocketClient().getPrivateViewController().showAudioConnectedBox();
        }
    }

    /**
     * update userList when a user joins the server
     */
    private void userArrived(JsonObject jsonData) {
        String id = jsonData.getString("id");
        String name = jsonData.getString("name");
        boolean status = jsonData.getBoolean("online");

        String description = getUserDescription(id);

        serverViewController.getServer().withUser(buildServerUser(name, id, status, description));
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.showOnlineOfflineUsers();
        }
    }

    private String getUserDescription(String id) {
        for (User u : builder.getPersonalUser().getUser()) {
            if (u.getId().equals(id)) {
                return u.getDescription();
            }
        }
        return "";
    }

    /**
     * update userList when a user exits the server
     */
    private void userExited(JsonObject jsonData) {
        String id = jsonData.getString("id");
        String name = jsonData.getString("name");
        serverViewController.getServer().withoutUser(buildServerUser(name, id, true, ""));
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.showOnlineOfflineUsers();
        }
        if (name.equals(builder.getPersonalUser().getName())) {
            showExitAlert();
        }
    }

    private void showExitAlert() {
        Platform.runLater(() -> {
            builder.getPersonalUser().withoutServer(serverViewController.getServer());
            builder.setCurrentServer(null);
            serverViewController.getHomeViewController().serverDeleted();

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle("Server left!");
            alert.setHeaderText("Server " + serverViewController.getServer().getName() + " was left!");
            alert.showAndWait();
        });
        serverViewController.getHomeViewController().stopServer(serverViewController.getServer());
    }

    /**
     * updates the channel name by change and the privileged with the privileged users from a channel by change
     */
    private void updateChannel(JsonObject jsonData) {
        String categoryId = jsonData.getString("category");
        String channelId = jsonData.getString("id");
        String channelName = jsonData.getString("name");
        String channelType = jsonData.getString("type");
        boolean channelPrivileged = jsonData.getBoolean("privileged");
        JsonArray jsonArray = jsonData.getJsonArray("members");

        Categories affectedCategory = new Categories();
        boolean hasChannel = false;
        ArrayList<User> member = getChannelMembers(jsonArray);

        for (Categories category : serverViewController.getServer().getCategories()) {
            if (category.getId().equals(categoryId)) {
                affectedCategory = category;
                hasChannel = findChannelToUpdate(jsonData, category);
            }
            // no need to search more for the channel if found
            if (hasChannel) {
                break;
            }
        }

        if (!hasChannel) {
            // add the channel for the user who has not the channel and load the messages
            ServerChannel serverChannel = new ServerChannel().setId(channelId).setType(channelType).setName(channelName)
                    .setPrivilege(channelPrivileged).withPrivilegedUsers(member);
            affectedCategory.withChannel(serverChannel);
            serverViewController.loadChannelMessages(serverChannel, response1 -> {
            });
        }
    }

    private boolean findChannelToUpdate(JsonObject jsonData, Categories category) {
        boolean hasChannel = false;
        String channelId = jsonData.getString("id");
        String channelName = jsonData.getString("name");
        boolean channelPrivileged = jsonData.getBoolean("privileged");
        JsonArray jsonArray = jsonData.getJsonArray("members");
        ArrayList<User> member = getChannelMembers(jsonArray);
        for (ServerChannel channel : category.getChannel()) {
            if (channel.getId().equals(channelId)) {
                channel.setName(channelName);
                channel.setPrivilege(channelPrivileged);
                ArrayList<User> privileged = new ArrayList<>(channel.getPrivilegedUsers());
                channel.withoutPrivilegedUsers(privileged);
                channel.withPrivilegedUsers(member);
                hasChannel = true;
            }
        }
        if (builder.getCurrentAudioChannel() != null && channelId.equals(builder.getCurrentAudioChannel().getId())) {
            showAudioBox();
        }
        return hasChannel;
    }

    private void showAudioBox() {
        builder.getPrivateChatWebSocketClient().getPrivateViewController().showAudioConnectedBox();
        serverViewController.showAudioConnectedBox();
    }

    private ArrayList<User> getChannelMembers(JsonArray jsonArray) {
        String memberId;
        ArrayList<User> member = new ArrayList<>();

        for (int j = 0; j < jsonArray.size(); j++) {
            memberId = jsonArray.getString(j);
            for (User user : serverViewController.getServer().getUser()) {
                if (user.getId().equals(memberId)) {
                    member.add(user);
                }
            }
        }
        return member;
    }

    public void setChatViewController(ChatViewController chatViewController) {
        this.chatViewController = chatViewController;
    }

    public void startNoopTimer() {
        if (this.noopTimer == null) {
            this.noopTimer = new Timer();
        }
    }
}
