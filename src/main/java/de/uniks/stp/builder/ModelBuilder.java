package de.uniks.stp.builder;

import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelBuilder {
    private PropertyChangeSupport listeners = null;
    private ArrayList<User> onlineUsers = new ArrayList();
    private ArrayList<Server> onlineServers = new ArrayList();
    private CurrentUser personalUser;

    /////////////////////////////////////////
    //  Setter
    /////////////////////////////////////////

    public void buildPersonalUser(String name, String userKey) {
        personalUser = new CurrentUser().setName(name).setUserKey(userKey);
    }

    public User buildUser(String name, String id) {
        for (User user : personalUser.getUser()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        User newUser = new User().setName(name).setId(id);
        personalUser.withUser(newUser);
        return newUser;
    }

    public User buildUser(String name, String id, boolean online) {
        User newUser = new User();
        newUser.setName(name);
        newUser.setId(id);
        newUser.setStatus(online);

        onlineUsers.add(newUser);
        this.firePropertyChange("onlineUsers", null, onlineUsers);
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

    public ModelBuilder buildServer(Server newServer) {
        onlineServers.add(newServer);
        this.firePropertyChange("onlineServers", null, onlineServers);
        return this;
    }

    /////////////////////////////////////////
    //  remove
    /////////////////////////////////////////

    public void removeUser(User user) {
        if (onlineUsers.contains(user)) {
            ArrayList<User> oldSetupPlayer = onlineUsers;
            onlineUsers.remove(user);
            user.removeYou();
            // fire property change
            this.firePropertyChange("onlineUsers", null, user);
        }
    }

    public void clearUsers() {
        for (User user : onlineUsers) {
            user.removeYou();
        }
        onlineUsers.clear();

        // fire property change
        this.firePropertyChange("onlineUsers", null, onlineUsers);

    }

    /////////////////////////////////////////
    //  Getter
    /////////////////////////////////////////



    public List<Server> getServer()
    {
        return this.onlineServers != null ? Collections.unmodifiableList(this.onlineServers) : Collections.emptyList();
    }

    public CurrentUser getPersonalUser() {
        return personalUser;
    }

    public ArrayList<User> getUsers() {
        return onlineUsers;
    }

    /////////////////////////////////////////
    // PropertyChange
    /////////////////////////////////////////

    private boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (listeners != null) {
            listeners.firePropertyChange(propertyName, oldValue, newValue);
            return true;
        }
        return false;
    }
    public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(propertyName, listener);
        return true;
    }

    public void stop() {
        for (PropertyChangeListener listener : listeners.getPropertyChangeListeners()) {
            this.listeners.removePropertyChangeListener(listener);
        }
        this.listeners = null;
    }


}