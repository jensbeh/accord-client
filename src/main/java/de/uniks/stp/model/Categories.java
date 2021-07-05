package de.uniks.stp.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class Categories {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_SERVER = "server";
    public static final String PROPERTY_CHANNEL = "channel";
    private String name;
    private String id;
    protected PropertyChangeSupport listeners;
    private Server server;
    private List<ServerChannel> channel;

    public String getName()
   {
      return this.name;
   }

    public Categories setName(String value)
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

    public Categories setId(String value)
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

    public Server getServer()
   {
      return this.server;
   }

    public Categories setServer(Server value)
   {
      if (this.server == value)
      {
         return this;
      }

      final Server oldValue = this.server;
      if (this.server != null)
      {
         this.server = null;
         oldValue.withoutCategories(this);
      }
      this.server = value;
      if (value != null)
      {
         value.withCategories(this);
      }
      this.firePropertyChange(PROPERTY_SERVER, oldValue, value);
      return this;
   }

    public List<ServerChannel> getChannel()
   {
      return this.channel != null ? Collections.unmodifiableList(this.channel) : Collections.emptyList();
   }

    public Categories withChannel(ServerChannel value)
   {
      if (this.channel == null)
      {
         this.channel = new ArrayList<>();
      }
      if (!this.channel.contains(value))
      {
         this.channel.add(value);
         value.setCategories(this);
         this.firePropertyChange(PROPERTY_CHANNEL, null, value);
      }
      return this;
   }

    public Categories withChannel(ServerChannel... value)
   {
      for (final ServerChannel item : value)
      {
         this.withChannel(item);
      }
      return this;
   }

    public Categories withChannel(Collection<? extends ServerChannel> value)
   {
      for (final ServerChannel item : value)
      {
         this.withChannel(item);
      }
      return this;
   }

    public Categories withoutChannel(ServerChannel value)
   {
      if (this.channel != null && this.channel.remove(value))
      {
         value.setCategories(null);
         this.firePropertyChange(PROPERTY_CHANNEL, value, null);
      }
      return this;
   }

    public Categories withoutChannel(ServerChannel... value)
   {
      for (final ServerChannel item : value)
      {
         this.withoutChannel(item);
      }
      return this;
   }

    public Categories withoutChannel(Collection<? extends ServerChannel> value)
   {
      for (final ServerChannel item : value)
      {
         this.withoutChannel(item);
      }
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

    public boolean addPropertyChangeListener(PropertyChangeListener listener) {
        // No fulib
        if (this.listeners == null) {
            this.listeners = new PropertyChangeSupport(this);
        }
        this.listeners.addPropertyChangeListener(listener);
        return true;
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

    public boolean removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        // No fulib
        if (this.listeners != null) {
            this.listeners.removePropertyChangeListener(propertyName, listener);
        }
        return true;
    }

    public void removeYou()
   {
      this.setServer(null);
      this.withoutChannel(new ArrayList<>(this.getChannel()));
   }
}
