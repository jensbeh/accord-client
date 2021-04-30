package de.uniks.stp.builder;

import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.util.ArrayList;

public class ModelBuilder {
    private ArrayList<User> users = new ArrayList();
    private ArrayList<Server> servers = new ArrayList();
    private User personalUser;

    public void buildPersonalUser(String name, String userKey) {
        personalUser = new User().setName(name).setUserKey(userKey);
    }

    public void buildUser(String name) {
        users.add(new User().setName(name));
    }

    public void buildServer(String name, String id) {
        servers.add(new Server().setName(name).setId(id));
    }

    public void withServer(Server newServer) {
        servers.add(newServer);
    }

    public User getPersonalUser() {
        return personalUser;
    }

}