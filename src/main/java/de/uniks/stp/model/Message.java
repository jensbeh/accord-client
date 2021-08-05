package de.uniks.stp.model;

import java.beans.PropertyChangeSupport;
import java.util.Objects;

public class Message {
    public static final String PROPERTY_FROM = "from";
    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_TIMESTAMP = "timestamp";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_SERVER_CHANNEL = "serverChannel";
    public static final String PROPERTY_PRIVATE_CHAT = "privateChat";
    protected PropertyChangeSupport listeners;
    private String from;
    private String message;
    private long timestamp;
    private String id;
    private ServerChannel serverChannel;
    private PrivateChat privateChat;

    public String getFrom()
   {
      return this.from;
   }

    public Message setFrom(String value)
   {
      if (Objects.equals(value, this.from))
      {
         return this;
      }

      final String oldValue = this.from;
      this.from = value;
      this.firePropertyChange(PROPERTY_FROM, oldValue, value);
      return this;
   }

    public String getMessage()
   {
      return this.message;
   }

    public Message setMessage(String value)
   {
      if (Objects.equals(value, this.message))
      {
         return this;
      }

      final String oldValue = this.message;
      this.message = value;
      this.firePropertyChange(PROPERTY_MESSAGE, oldValue, value);
      return this;
   }

    public long getTimestamp()
   {
      return this.timestamp;
   }

    public Message setTimestamp(long value)
   {
      if (value == this.timestamp)
      {
         return this;
      }

      final long oldValue = this.timestamp;
      this.timestamp = value;
      this.firePropertyChange(PROPERTY_TIMESTAMP, oldValue, value);
      return this;
   }

    public String getId()
   {
      return this.id;
   }

    public Message setId(String value)
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

    public ServerChannel getServerChannel()
   {
      return this.serverChannel;
   }

    public Message setServerChannel(ServerChannel value)
   {
      if (this.serverChannel == value)
      {
         return this;
      }

      final ServerChannel oldValue = this.serverChannel;
      if (this.serverChannel != null)
      {
         this.serverChannel = null;
         oldValue.withoutMessage(this);
      }
      this.serverChannel = value;
      if (value != null)
      {
         value.withMessage(this);
      }
      this.firePropertyChange(PROPERTY_SERVER_CHANNEL, oldValue, value);
      return this;
   }

    public PrivateChat getPrivateChat()
   {
      return this.privateChat;
   }

    public Message setPrivateChat(PrivateChat value)
   {
      if (this.privateChat == value)
      {
         return this;
      }

      final PrivateChat oldValue = this.privateChat;
      if (this.privateChat != null)
      {
         this.privateChat = null;
         oldValue.withoutMessage(this);
      }
      this.privateChat = value;
      if (value != null)
      {
         value.withMessage(this);
      }
      this.firePropertyChange(PROPERTY_PRIVATE_CHAT, oldValue, value);
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
      result.append(' ').append(this.getFrom());
      result.append(' ').append(this.getMessage());
      result.append(' ').append(this.getId());
      return result.substring(1);
   }

    public void removeYou()
   {
      this.setServerChannel(null);
      this.setPrivateChat(null);
   }
}
