package de.uniks.stp.builder;

import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.util.ArrayList;

public class ModelBuilder {
    private ArrayList<User> onlineUsers = new ArrayList();
    private ArrayList<Server> servers = new ArrayList();
    private User personalUser;

    public void buildPersonalUser(String name, String userKey, Boolean tempUser) {
        personalUser = new User().setName(name).setUserKey(userKey);
        personalUser.setTempUser(tempUser);
    }

    public void buildUser(String name) {
        User user = new User().setName(name);
        onlineUsers.add(user);
    }

    public void buildServer(String name, String id) {
        servers.add(new Server().setName(name).setId(id));
    }

    public User getPersonalUser() {
        return personalUser;
    }
}
