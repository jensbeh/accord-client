package de.uniks.stp.builder;

import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.ServerChannel;
import de.uniks.stp.model.User;
import de.uniks.stp.net.*;

import java.util.Collections;
import java.util.List;

public class ModelBuilder {
    private Server currentServer;
    private CurrentUser personalUser;
    private ServerChannel currentServerChannel;

    private ServerSystemWebSocket serverSystemWebSocket;
    private PrivateSystemWebSocketClient USER_CLIENT;
    private PrivateChatWebSocket privateChatWebSocketCLient;
    private ServerChatWebSocket serverChatWebSocketClient;

    private RestClient restClient;
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


    public ServerSystemWebSocket getServerSystemWebSocket() {
        return serverSystemWebSocket;
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

    public PrivateChatWebSocket getPrivateChatWebSocketCLient() {
        return privateChatWebSocketCLient;
    }

    public void setPrivateChatWebSocketCLient(PrivateChatWebSocket privateChatWebSocketCLient) {
        this.privateChatWebSocketCLient = privateChatWebSocketCLient;
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
}