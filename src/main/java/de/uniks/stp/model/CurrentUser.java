package de.uniks.stp.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class CurrentUser {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_USER_KEY = "userKey";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_SERVER = "server";
    public static final String PROPERTY_PRIVATE_CHAT = "privateChat";
    public static final String PROPERTY_CHANNEL = "channel";
    public static final String PROPERTY_DESCRIPTION = "description";
    protected PropertyChangeSupport listeners;
    private String name;
    private String userKey;
    private String password;
    private String id;
    private List<User> user;
    private List<Server> server;
    private List<PrivateChat> privateChat;
    private List<ServerChannel> channel;
    private String description;

    public String getName()
   {
      return this.name;
   }

    public CurrentUser setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_NAME, oldValue, value);
      return this;
   }

    public String getUserKey()
   {
      return this.userKey;
   }

    public CurrentUser setUserKey(String value)
   {
      if (Objects.equals(value, this.userKey))
      {
         return this;
      }

      final String oldValue = this.userKey;
      this.userKey = value;
      this.firePropertyChange(PROPERTY_USER_KEY, oldValue, value);
      return this;
   }

    public String getPassword()
   {
      return this.password;
   }

    public CurrentUser setPassword(String value)
   {
      if (Objects.equals(value, this.password))
      {
         return this;
      }

      final String oldValue = this.password;
      this.password = value;
      this.firePropertyChange(PROPERTY_PASSWORD, oldValue, value);
      return this;
   }

    public String getId()
   {
      return this.id;
   }

    public CurrentUser setId(String value)
   {
      if (Objects.equals(value, this.id))
      {
         return this;
      }

      final String oldValue = this.id;
      this.id = value;
      this.firePropertyChange(PROPERTY_ID, oldValue, value);
      return this;
   }

    public List<User> getUser()
   {
      return this.user != null ? Collections.unmodifiableList(this.user) : Collections.emptyList();
   }

    public CurrentUser withUser(User value)
   {
      if (this.user == null)
      {
         this.user = new ArrayList<>();
      }
      if (!this.user.contains(value))
      {
         this.user.add(value);
         value.setCurrentUser(this);
         this.firePropertyChange(PROPERTY_USER, null, value);
      }
      return this;
   }

    public CurrentUser withUser(User... value)
   {
      for (final User item : value)
      {
         this.withUser(item);
      }
      return this;
   }

    public CurrentUser withUser(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withUser(item);
      }
      return this;
   }

    public CurrentUser withoutUser(User value)
   {
      if (this.user != null && this.user.remove(value))
      {
         value.setCurrentUser(null);
         this.firePropertyChange(PROPERTY_USER, value, null);
      }
      return this;
   }

    public CurrentUser withoutUser(User... value)
   {
      for (final User item : value)
      {
         this.withoutUser(item);
      }
      return this;
   }

    public CurrentUser withoutUser(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutUser(item);
      }
      return this;
   }

    public List<Server> getServer()
   {
      return this.server != null ? Collections.unmodifiableList(this.server) : Collections.emptyList();
   }

    public CurrentUser withServer(Server value)
   {
      if (this.server == null)
      {
         this.server = new ArrayList<>();
      }
      if (!this.server.contains(value))
      {
         this.server.add(value);
         value.setCurrentUser(this);
         this.firePropertyChange(PROPERTY_SERVER, null, value);
      }
      return this;
   }

    public CurrentUser withServer(Server... value)
   {
      for (final Server item : value)
      {
         this.withServer(item);
      }
      return this;
   }

    public CurrentUser withServer(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withServer(item);
      }
      return this;
   }

    public CurrentUser withoutServer(Server value)
   {
      if (this.server != null && this.server.remove(value))
      {
         value.setCurrentUser(null);
         this.firePropertyChange(PROPERTY_SERVER, value, null);
      }
      return this;
   }

    public CurrentUser withoutServer(Server... value)
   {
      for (final Server item : value)
      {
         this.withoutServer(item);
      }
      return this;
   }

    public CurrentUser withoutServer(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withoutServer(item);
      }
      return this;
   }

    public List<PrivateChat> getPrivateChat()
   {
      return this.privateChat != null ? Collections.unmodifiableList(this.privateChat) : Collections.emptyList();
   }

    public CurrentUser withPrivateChat(PrivateChat value)
   {
      if (this.privateChat == null)
      {
         this.privateChat = new ArrayList<>();
      }
      if (!this.privateChat.contains(value))
      {
         this.privateChat.add(value);
         value.setCurrentUser(this);
         this.firePropertyChange(PROPERTY_PRIVATE_CHAT, null, value);
      }
      return this;
   }

    public CurrentUser withPrivateChat(PrivateChat... value)
   {
      for (final PrivateChat item : value)
      {
         this.withPrivateChat(item);
      }
      return this;
   }

    public CurrentUser withPrivateChat(Collection<? extends PrivateChat> value)
   {
      for (final PrivateChat item : value)
      {
         this.withPrivateChat(item);
      }
      return this;
   }

    public CurrentUser withoutPrivateChat(PrivateChat value)
   {
      if (this.privateChat != null && this.privateChat.remove(value))
      {
         value.setCurrentUser(null);
         this.firePropertyChange(PROPERTY_PRIVATE_CHAT, value, null);
      }
      return this;
   }

    public CurrentUser withoutPrivateChat(PrivateChat... value)
   {
      for (final PrivateChat item : value)
      {
         this.withoutPrivateChat(item);
      }
      return this;
   }

    public CurrentUser withoutPrivateChat(Collection<? extends PrivateChat> value)
   {
      for (final PrivateChat item : value)
      {
         this.withoutPrivateChat(item);
      }
      return this;
   }

    public List<ServerChannel> getChannel()
   {
      return this.channel != null ? Collections.unmodifiableList(this.channel) : Collections.emptyList();
   }

    public CurrentUser withChannel(ServerChannel value)
   {
      if (this.channel == null)
      {
         this.channel = new ArrayList<>();
      }
      if (!this.channel.contains(value))
      {
         this.channel.add(value);
         value.setCurrentUser(this);
         this.firePropertyChange(PROPERTY_CHANNEL, null, value);
      }
      return this;
   }

    public CurrentUser withChannel(ServerChannel... value)
   {
      for (final ServerChannel item : value)
      {
         this.withChannel(item);
      }
      return this;
   }

    public CurrentUser withChannel(Collection<? extends ServerChannel> value)
   {
      for (final ServerChannel item : value)
      {
         this.withChannel(item);
      }
      return this;
   }

    public CurrentUser withoutChannel(ServerChannel value)
   {
      if (this.channel != null && this.channel.remove(value))
      {
         value.setCurrentUser(null);
         this.firePropertyChange(PROPERTY_CHANNEL, value, null);
      }
      return this;
   }

    public CurrentUser withoutChannel(ServerChannel... value)
   {
      for (final ServerChannel item : value)
      {
         this.withoutChannel(item);
      }
      return this;
   }

    public CurrentUser withoutChannel(Collection<? extends ServerChannel> value)
   {
      for (final ServerChannel item : value)
      {
         this.withoutChannel(item);
      }
      return this;
   }

    public String getDescription()
   {
      return this.description;
   }

    public CurrentUser setDescription(String value)
   {
      if (Objects.equals(value, this.description))
      {
         return this;
      }

      final String oldValue = this.description;
      this.description = value;
      this.firePropertyChange(PROPERTY_DESCRIPTION, oldValue, value);
      return this;
   }

    public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

    public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

    @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getName());
      result.append(' ').append(this.getUserKey());
      result.append(' ').append(this.getPassword());
      result.append(' ').append(this.getId());
      result.append(' ').append(this.getDescription());
      return result.substring(1);
   }

    public void removeYou()
   {
      this.withoutUser(new ArrayList<>(this.getUser()));
      this.withoutServer(new ArrayList<>(this.getServer()));
      this.withoutPrivateChat(new ArrayList<>(this.getPrivateChat()));
      this.withoutChannel(new ArrayList<>(this.getChannel()));
   }

    public boolean addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // No fulib
        if (this.listeners == null) {
            this.listeners = new PropertyChangeSupport(this);
        }
        this.listeners.addPropertyChangeListener(propertyName, listener);
        return true;
    }

    public boolean removePropertyChangeListener(PropertyChangeListener listener) {
        // No fulib
        if (this.listeners != null) {
            this.listeners.removePropertyChangeListener(listener);
        }
        return true;
    }
}
