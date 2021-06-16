package de.uniks.stp.net;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.PrivateViewController;
import de.uniks.stp.controller.ServerViewController;
import de.uniks.stp.controller.subcontroller.ServerSettingsChannelController;
import de.uniks.stp.model.Categories;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import util.JsonUtil;
import util.SortUser;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;

public class ServerSystemWebSocket extends Endpoint {

    private Session session;
    private Timer noopTimer;
    private ModelBuilder builder;
    public static final String COM_NOOP = "noop";
    private ServerViewController serverViewController;

    public void setServerViewController(ServerViewController serverViewController) {
        this.serverViewController = serverViewController;
    }

    public ModelBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }


    public ServerSystemWebSocket(URI endpoint, String userKey) {
        this.noopTimer = new Timer();
        this.serverViewController = serverViewController;
        try {
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new CustomWebSocketConfigurator(userKey))
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientConfig, endpoint);
        } catch (Exception e) {
            System.err.println("Error during establishing websocket connection:");
        }
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
                System.out.println("##### NOOP MESSAGE FROM " + "SYSTEM" + " #####");
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
        // cancel timer
        this.noopTimer.cancel();
        // set session null
        this.session = null;
        super.onClose(session, closeReason);
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
        this.session.close();
    }

    public Session getSession() {
        return session;
    }

    public void handleMessage(JsonStructure msg) {
        System.out.println("msg: " + msg);
        JsonObject jsonMsg = JsonUtil.parse(msg.toString());
        String userAction = jsonMsg.getString("action");
        JsonObject jsonData = jsonMsg.getJsonObject("data");
        String userName = jsonData.getString("name");
        String userId = jsonData.getString("id");

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
            buildServerUser(userName, userId, true);
        }
        if (userAction.equals("userLeft")) {
            if (userName.equals(builder.getPersonalUser().getName()) && builder.getCurrentServer() == serverViewController.getServer()) {
                Platform.runLater(StageManager::showLoginScreen);
            }
            buildServerUser(userName, userId, false);
        }

        if (userAction.equals("serverDeleted")) {
            deleteServer();
        }
        if (userAction.equals("serverUpdated")) {
            updateServer(userName);
        }

        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.showOnlineOfflineUsers();
        }
    }






    /**
     * Build a serverUser with this instance of server.
     */
    private User buildServerUser(String userName, String userId, boolean online) {
        return builder.buildServerUser(serverViewController.getServer(), userName, userId, online);
    }
    
    /**
     * update server
     */
    private void updateServer(String serverName) {
        serverViewController.getServer().setName(serverName);
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            Platform.runLater(serverViewController::changeServerName);
        }
        serverViewController.getHomeViewController().showServerUpdate();
    }

    /**
     * deletes server
     */
    private void deleteServer() {
        Platform.runLater(() -> {
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
            Optional<ButtonType> result = alert.showAndWait();
        });

        serverViewController.getHomeViewController().stopServer(serverViewController.getServer());
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
                boolean found = false;
                for (Categories categories : server.getCategories()) {
                    if (categories.getId().equals(categoryId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    Categories category = new Categories().setName(name).setId(categoryId);
                    server.withCategories(category);
                    if (builder.getCurrentServer() == serverViewController.getServer()) {
                        serverViewController.generateCategoryChannelView(category);
                    }
                }
            }
        }
    }

    /**
     * deletes a category with controller and view
     */
    private void deleteCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("server");
        String categoryId = jsonData.getString("id");

        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                for (Categories categories : server.getCategories()) {
                    if (categories.getId().equals(categoryId)) {
                        server.withoutCategories(categories);
                        if (builder.getCurrentServer() == serverViewController.getServer()) {
                            for (Node view : serverViewController.getCategoryBox().getChildren()) {
                                if (view.getId().equals(categories.getId())) {
                                    Platform.runLater(() -> this.serverViewController.getCategoryBox().getChildren().remove(view));
                                    serverViewController.getCategorySubControllerList().get(categories).stop();
                                    serverViewController.getCategorySubControllerList().remove(categories);

                                    if (categories.getChannel().contains(serverViewController.getCurrentChannel()) || serverViewController.getServer().getCategories().size() == 0) {
                                        serverViewController.throwOutUserFromChatView();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * rename a Category and update it on the view
     */
    private void updateCategory(JsonObject jsonData) {
        String serverId = jsonData.getString("server");
        String categoryId = jsonData.getString("id");
        String name = jsonData.getString("name");

        for (Server server : builder.getPersonalUser().getServer()) {
            if (server.getId().equals(serverId)) {
                for (Categories categories : server.getCategories()) {
                    if (categories.getId().equals(categoryId) && !categories.getName().equals(name)) {
                        categories.setName(name);
                        break;
                    }
                }
            }
        }
    }

    /**
     * adds the new channel to category for the user
     *
     * @param jsonData the message data
     */
    private void createChannel(JsonObject jsonData) {
        String channelId = jsonData.getString("id");
        String channelName = jsonData.getString("name");
        String channelType = jsonData.getString("type");
        boolean channelPrivileged = jsonData.getBoolean("privileged");
        String categoryId = jsonData.getString("category");

        for (Server server : builder.getPersonalUser().getServer()) {
            for (Categories cat : server.getCategories()) {
                if (cat.getId().equals(categoryId)) {
                    ServerChannel newChannel = new ServerChannel().setId(channelId).setType(channelType).setName(channelName).setPrivilege(channelPrivileged);
                    cat.withChannel(newChannel);
                    if (builder.getCurrentServer() == serverViewController.getServer()) {
                        Platform.runLater(() -> ServerSettingsChannelController.loadChannels(ServerSettingsChannelController.getSelectedChannel()));
                    }
                    break;
                }
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
        String channelId = jsonData.getString("id");
        String categoryId = jsonData.getString("category");

        for (Server server : builder.getPersonalUser().getServer()) {
            for (Categories cat : server.getCategories()) {
                if (cat.getId().equals(categoryId)) {
                    for (ServerChannel channel : cat.getChannel()) {
                        if (channel.getId().equals(channelId)) {
                            cat.withoutChannel(channel);
                            if (builder.getCurrentServer() == serverViewController.getServer()) {
                                Platform.runLater(() -> ServerSettingsChannelController.loadChannels(null));
                                if (serverViewController.getCurrentChannel().equals(channel)) {
                                    serverViewController.throwOutUserFromChatView();
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * update userList when a user joins the server
     */
    private void userArrived(JsonObject jsonData) {
        String id = jsonData.getString("id");
        String name = jsonData.getString("name");
        boolean status = jsonData.getBoolean("online");

        serverViewController.getServer().withUser(buildServerUser(name, id, status));
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.showOnlineOfflineUsers();
        }
    }

    /**
     * update userList when a user exits the server
     */
    private void userExited(JsonObject jsonData) {
        String id = jsonData.getString("id");
        String name = jsonData.getString("name");
        serverViewController.getServer().withoutUser(buildServerUser(name, id, true));
        if (builder.getCurrentServer() == serverViewController.getServer()) {
            serverViewController.showOnlineOfflineUsers();
        }
        if (name.equals(builder.getPersonalUser().getName())) {
            Platform.runLater(() -> {
                builder.getPersonalUser().withoutServer(serverViewController.getServer());
                builder.setCurrentServer(null);
                serverViewController.getHomeViewController().serverDeleted();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                alert.setTitle("Server leaved!");
                alert.setHeaderText("Server " + serverViewController.getServer().getName() + " was leaved!");
                Optional<ButtonType> result = alert.showAndWait();
            });
            serverViewController.getHomeViewController().stopServer(serverViewController.getServer());
        }
    }

    /**
     * updates the channel name by change and the privileged with the privileged users from a channel by change
     */
    public void updateChannel(JsonObject jsonData) {
        String categoryId = jsonData.getString("category");
        String channelId = jsonData.getString("id");
        String channelName = jsonData.getString("name");
        String channelType = jsonData.getString("type");
        boolean channelPrivileged = jsonData.getBoolean("privileged");
        JsonArray jsonArray = jsonData.getJsonArray("members");
        String memberId = "";
        boolean hasChannel = false;
        ArrayList<User> member = new ArrayList<>();
        for (int j = 0; j < jsonArray.size(); j++) {
            memberId = jsonArray.getString(j);
            for (User user : serverViewController.getServer().getUser()) {
                if (user.getId().equals(memberId)) {
                    member.add(user);
                }
            }
        }
        
        for (Categories category : serverViewController.getServer().getCategories()) {
            if (category.getId().equals(categoryId)) {
                for (ServerChannel channel : category.getChannel()) {
                    if (channel.getId().equals(channelId)) {
                        hasChannel = true;
                        channel.setName(channelName);
                        channel.setPrivilege(channelPrivileged);
                        ArrayList<User> privileged = new ArrayList<>(channel.getPrivilegedUsers());
                        channel.withoutPrivilegedUsers(privileged);
                        channel.withPrivilegedUsers(member);
                        if (builder.getCurrentServer() == serverViewController.getServer()) {
                            Platform.runLater(() -> ServerSettingsChannelController.loadChannels(ServerSettingsChannelController.getSelectedChannel()));
                        }
                        break;
                    }
                }
                if (!hasChannel) {
                    ServerChannel newChannel = new ServerChannel().setId(channelId).setType(channelType).setName(channelName)
                            .setPrivilege(channelPrivileged).withPrivilegedUsers(member);
                    category.withChannel(newChannel);
                    if (builder.getCurrentServer() == serverViewController.getServer()) {
                        Platform.runLater(() -> ServerSettingsChannelController.loadChannels(ServerSettingsChannelController.getSelectedChannel()));
                    }
                }
            }
        }
    }


}
