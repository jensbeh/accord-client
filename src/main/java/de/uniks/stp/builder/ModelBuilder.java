package de.uniks.stp.builder;

import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelBuilder {
    private ArrayList<User> onlineUsers = new ArrayList();
    private ArrayList<Server> servers = new ArrayList();
    private User personalUser;

    public void buildPersonalUser(String name, String userKey) {
        personalUser = new User().setName(name).setUserKey(userKey);
    }

    public void buildUser(String name) {
        User user = new User().setName(name);
        onlineUsers.add(user);
    }

    public void buildTempUser(String name) {
        personalUser = new User().setName(name).setUserKey("");
    }

    public User buildUser(String name, String id, String online) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setId(id);
        if (online.equals("true")) {
            newUser.setStatus(true);
        }
        onlineUsers.add(newUser);
        return newUser;
    }

    public void buildServer(String name, String id) {
        servers.add(new Server().setName(name).setId(id));
    }

    public ModelBuilder withServer(Server newServer) {
        servers.add(newServer);
        return this;
    }

    public List<Server> getServer()
    {
        return this.servers != null ? Collections.unmodifiableList(this.servers) : Collections.emptyList();
    }

    public User getPersonalUser() {
        return personalUser;
    }
}