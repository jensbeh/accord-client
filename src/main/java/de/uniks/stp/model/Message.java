package de.uniks.stp.model;
import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Message
{
   public static final String PROPERTY_FROM = "from";
   public static final String PROPERTY_MESSAGE = "message";
   public static final String PROPERTY_TIMESTAMP = "timestamp";
   public static final String PROPERTY_CHANNEL = "channel";
   private String from;
   private String message;
   protected PropertyChangeSupport listeners;
   private int timestamp;
   private Channel channel;

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

   public int getTimestamp()
   {
      return this.timestamp;
   }

   public Message setTimestamp(int value)
   {
      if (value == this.timestamp)
      {
         return this;
      }

      final int oldValue = this.timestamp;
      this.timestamp = value;
      this.firePropertyChange(PROPERTY_TIMESTAMP, oldValue, value);
      return this;
   }

   public Message setChannel(Channel value)
   {
      if (this.channel == value)
      {
         return this;
      }

      final Channel oldValue = this.channel;
      if (this.channel != null)
      {
         this.channel = null;
         oldValue.withoutMessage(this);
      }
      this.channel = value;
      if (value != null)
      {
         value.withMessage(this);
      }
      this.firePropertyChange(PROPERTY_CHANNEL, oldValue, value);
      return this;
   }

   public Channel getChannel()
   {
      return this.channel;
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
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setChannel(null);
   }
}
