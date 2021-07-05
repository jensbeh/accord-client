package de.uniks.stp.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;

public class PrivateChat {
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_UNREAD_MESSAGES_COUNTER = "unreadMessagesCounter";
    public static final String PROPERTY_CURRENT_USER = "currentUser";
    public static final String PROPERTY_MESSAGE = "message";
    private String name;
    private String id;
    private int unreadMessagesCounter;
    protected PropertyChangeSupport listeners;
    private CurrentUser currentUser;
    private List<Message> message;

    public String getName()
   {
      return this.name;
   }

    public PrivateChat setName(String value)
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

    public PrivateChat setId(String value)
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

    public int getUnreadMessagesCounter()
   {
      return this.unreadMessagesCounter;
   }

    public PrivateChat setUnreadMessagesCounter(int value)
   {
      if (value == this.unreadMessagesCounter)
      {
         return this;
      }

      final int oldValue = this.unreadMessagesCounter;
      this.unreadMessagesCounter = value;
      this.firePropertyChange(PROPERTY_UNREAD_MESSAGES_COUNTER, oldValue, value);
      return this;
   }

    public CurrentUser getCurrentUser()
   {
      return this.currentUser;
   }

    public PrivateChat setCurrentUser(CurrentUser value)
   {
      if (this.currentUser == value)
      {
         return this;
      }

      final CurrentUser oldValue = this.currentUser;
      if (this.currentUser != null)
      {
         this.currentUser = null;
         oldValue.withoutPrivateChat(this);
      }
      this.currentUser = value;
      if (value != null)
      {
         value.withPrivateChat(this);
      }
      this.firePropertyChange(PROPERTY_CURRENT_USER, oldValue, value);
      return this;
   }

    public List<Message> getMessage()
   {
      return this.message != null ? Collections.unmodifiableList(this.message) : Collections.emptyList();
   }

    public PrivateChat withMessage(Message value)
   {
      if (this.message == null)
      {
         this.message = new ArrayList<>();
      }
      if (!this.message.contains(value))
      {
         this.message.add(value);
         value.setPrivateChat(this);
         this.firePropertyChange(PROPERTY_MESSAGE, null, value);
      }
      return this;
   }

    public PrivateChat withMessage(Message... value)
   {
      for (final Message item : value)
      {
         this.withMessage(item);
      }
      return this;
   }

    public PrivateChat withMessage(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withMessage(item);
      }
      return this;
   }

    public PrivateChat withoutMessage(Message value)
   {
      if (this.message != null && this.message.remove(value))
      {
         value.setPrivateChat(null);
         this.firePropertyChange(PROPERTY_MESSAGE, value, null);
      }
      return this;
   }

    public PrivateChat withoutMessage(Message... value)
   {
      for (final Message item : value)
      {
         this.withoutMessage(item);
      }
      return this;
   }

    public PrivateChat withoutMessage(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withoutMessage(item);
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
      this.setCurrentUser(null);
      this.withoutMessage(new ArrayList<>(this.getMessage()));
   }
}
