package de.uniks.stp.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class User
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_USER_KEY = "userKey";
   public static final String PROPERTY_STATUS = "status";
   public static final String PROPERTY_SERVER = "server";
   public static final String PROPERTY_PRIVATE_CHAT = "privateChat";
   private String name;
   private String id;
   private String userKey;
   private boolean status;
   private List<Server> server;
   protected PropertyChangeSupport listeners;
   private List<Channel> privateChat;

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

   public String getUserKey()
   {
      return this.userKey;
   }

   public User setUserKey(String value)
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

   public List<Channel> getPrivateChat()
   {
      return this.privateChat != null ? Collections.unmodifiableList(this.privateChat) : Collections.emptyList();
   }

   public User withPrivateChat(Channel value)
   {
      if (this.privateChat == null)
      {
         this.privateChat = new ArrayList<>();
      }
      if (!this.privateChat.contains(value))
      {
         this.privateChat.add(value);
         value.setUser(this);
         this.firePropertyChange(PROPERTY_PRIVATE_CHAT, null, value);
      }
      return this;
   }

   public User withPrivateChat(Channel... value)
   {
      for (final Channel item : value)
      {
         this.withPrivateChat(item);
      }
      return this;
   }

   public User withPrivateChat(Collection<? extends Channel> value)
   {
      for (final Channel item : value)
      {
         this.withPrivateChat(item);
      }
      return this;
   }

   public User withoutPrivateChat(Channel value)
   {
      if (this.privateChat != null && this.privateChat.remove(value))
      {
         value.setUser(null);
         this.firePropertyChange(PROPERTY_PRIVATE_CHAT, value, null);
      }
      return this;
   }

   public User withoutPrivateChat(Channel... value)
   {
      for (final Channel item : value)
      {
         this.withoutPrivateChat(item);
      }
      return this;
   }

   public User withoutPrivateChat(Collection<? extends Channel> value)
   {
      for (final Channel item : value)
      {
         this.withoutPrivateChat(item);
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
      result.append(' ').append(this.getUserKey());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutServer(new ArrayList<>(this.getServer()));
      this.withoutPrivateChat(new ArrayList<>(this.getPrivateChat()));
   }
}
