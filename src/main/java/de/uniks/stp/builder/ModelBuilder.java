package de.uniks.stp.builder;

import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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