package de.uniks.stp.model;

import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class User {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_PRIVILEGED = "privileged";
    public static final String PROPERTY_SERVER = "server";
    public static final String PROPERTY_CURRENT_USER = "currentUser";
    private String name;
    private String id;
    private boolean status;
    private boolean tempUser;
    protected PropertyChangeSupport listeners;
    private List<ServerChannel> privileged;
    private List<Server> server;
    private CurrentUser currentUser;

    public String getName()
   {
      return this.name;
   }

    public User setName(String value)
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

    public String getId()
   {
      return this.id;
   }

    public User setId(String value)
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

    public boolean isStatus()
   {
      return this.status;
   }

    public User setStatus(boolean value)
   {
      if (value == this.status)
      {
         return this;
      }

      final boolean oldValue = this.status;
      this.status = value;
      this.firePropertyChange(PROPERTY_STATUS, oldValue, value);
      return this;
   }

    public List<ServerChannel> getPrivileged()
   {
      return this.privileged != null ? Collections.unmodifiableList(this.privileged) : Collections.emptyList();
   }

    public User withPrivileged(ServerChannel value)
   {
      if (this.privileged == null)
      {
         this.privileged = new ArrayList<>();
      }
      if (!this.privileged.contains(value))
      {
         this.privileged.add(value);
         value.withPrivilegedUsers(this);
         this.firePropertyChange(PROPERTY_PRIVILEGED, null, value);
      }
      return this;
   }

    public User withPrivileged(ServerChannel... value)
   {
      for (final ServerChannel item : value)
      {
         this.withPrivileged(item);
      }
      return this;
   }

    public User withPrivileged(Collection<? extends ServerChannel> value)
   {
      for (final ServerChannel item : value)
      {
         this.withPrivileged(item);
      }
      return this;
   }

    public User withoutPrivileged(ServerChannel value)
   {
      if (this.privileged != null && this.privileged.remove(value))
      {
         value.withoutPrivilegedUsers(this);
         this.firePropertyChange(PROPERTY_PRIVILEGED, value, null);
      }
      return this;
   }

    public User withoutPrivileged(ServerChannel... value)
   {
      for (final ServerChannel item : value)
      {
         this.withoutPrivileged(item);
      }
      return this;
   }

    public User withoutPrivileged(Collection<? extends ServerChannel> value)
   {
      for (final ServerChannel item : value)
      {
         this.withoutPrivileged(item);
      }
      return this;
   }

    public List<Server> getServer()
   {
      return this.server != null ? Collections.unmodifiableList(this.server) : Collections.emptyList();
   }

    public User withServer(Server value)
   {
      if (this.server == null)
      {
         this.server = new ArrayList<>();
      }
      if (!this.server.contains(value))
      {
         this.server.add(value);
         value.withUser(this);
         this.firePropertyChange(PROPERTY_SERVER, null, value);
      }
      return this;
   }

    public User withServer(Server... value)
   {
      for (final Server item : value)
      {
         this.withServer(item);
      }
      return this;
   }

    public User withServer(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withServer(item);
      }
      return this;
   }

    public User withoutServer(Server value)
   {
      if (this.server != null && this.server.remove(value))
      {
         value.withoutUser(this);
         this.firePropertyChange(PROPERTY_SERVER, value, null);
      }
      return this;
   }

    public User withoutServer(Server... value)
   {
      for (final Server item : value)
      {
         this.withoutServer(item);
      }
      return this;
   }

    public User withoutServer(Collection<? extends Server> value)
   {
      for (final Server item : value)
      {
         this.withoutServer(item);
      }
      return this;
   }

    public CurrentUser getCurrentUser()
   {
      return this.currentUser;
   }

    public User setCurrentUser(CurrentUser value)
   {
      if (this.currentUser == value)
      {
         return this;
      }

      final CurrentUser oldValue = this.currentUser;
      if (this.currentUser != null)
      {
         this.currentUser = null;
         oldValue.withoutUser(this);
      }
      this.currentUser = value;
      if (value != null)
      {
         value.withUser(this);
      }
      this.firePropertyChange(PROPERTY_CURRENT_USER, oldValue, value);
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
      result.append(' ').append(this.getId());
      return result.substring(1);
   }

    public void removeYou()
   {
      this.withoutPrivileged(new ArrayList<>(this.getPrivileged()));
      this.withoutServer(new ArrayList<>(this.getServer()));
      this.setCurrentUser(null);
   }
}
