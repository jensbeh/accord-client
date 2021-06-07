package de.uniks.stp.model;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class ServerChannel
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_UNREAD_MESSAGES_COUNTER = "unreadMessagesCounter";
   public static final String PROPERTY_PRIVILEGE = "privilege";
   public static final String PROPERTY_TYPE = "type";
   public static final String PROPERTY_CATEGORIES = "categories";
   public static final String PROPERTY_PRIVILEGED_USERS = "privilegedUsers";
   public static final String PROPERTY_CURRENT_USER = "currentUser";
   public static final String PROPERTY_MESSAGE = "message";
   private String name;
   private String id;
   private int unreadMessagesCounter;
   private boolean privilege;
   private String type;
   private Categories categories;
   private List<User> privilegedUsers;
   private CurrentUser currentUser;
   private List<Message> message;
   protected PropertyChangeSupport listeners;

   public String getName()
   {
      return this.name;
   }

   public ServerChannel setName(String value)
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

   public ServerChannel setId(String value)
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

   public ServerChannel setUnreadMessagesCounter(int value)
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

   public boolean isPrivilege()
   {
      return this.privilege;
   }

   public ServerChannel setPrivilege(boolean value)
   {
      if (value == this.privilege)
      {
         return this;
      }

      final boolean oldValue = this.privilege;
      this.privilege = value;
      this.firePropertyChange(PROPERTY_PRIVILEGE, oldValue, value);
      return this;
   }

   public String getType()
   {
      return this.type;
   }

   public ServerChannel setType(String value)
   {
      if (Objects.equals(value, this.type))
      {
         return this;
      }

      final String oldValue = this.type;
      this.type = value;
      this.firePropertyChange(PROPERTY_TYPE, oldValue, value);
      return this;
   }

   public Categories getCategories()
   {
      return this.categories;
   }

   public ServerChannel setCategories(Categories value)
   {
      if (this.categories == value)
      {
         return this;
      }

      final Categories oldValue = this.categories;
      if (this.categories != null)
      {
         this.categories = null;
         oldValue.withoutChannel(this);
      }
      this.categories = value;
      if (value != null)
      {
         value.withChannel(this);
      }
      this.firePropertyChange(PROPERTY_CATEGORIES, oldValue, value);
      return this;
   }

   public List<User> getPrivilegedUsers()
   {
      return this.privilegedUsers != null ? Collections.unmodifiableList(this.privilegedUsers) : Collections.emptyList();
   }

   public ServerChannel withPrivilegedUsers(User value)
   {
      if (this.privilegedUsers == null)
      {
         this.privilegedUsers = new ArrayList<>();
      }
      if (!this.privilegedUsers.contains(value))
      {
         this.privilegedUsers.add(value);
         value.withPrivileged(this);
         this.firePropertyChange(PROPERTY_PRIVILEGED_USERS, null, value);
      }
      return this;
   }

   public ServerChannel withPrivilegedUsers(User... value)
   {
      for (final User item : value)
      {
         this.withPrivilegedUsers(item);
      }
      return this;
   }

   public ServerChannel withPrivilegedUsers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withPrivilegedUsers(item);
      }
      return this;
   }

   public ServerChannel withoutPrivilegedUsers(User value)
   {
      if (this.privilegedUsers != null && this.privilegedUsers.remove(value))
      {
         value.withoutPrivileged(this);
         this.firePropertyChange(PROPERTY_PRIVILEGED_USERS, value, null);
      }
      return this;
   }

   public ServerChannel withoutPrivilegedUsers(User... value)
   {
      for (final User item : value)
      {
         this.withoutPrivilegedUsers(item);
      }
      return this;
   }

   public ServerChannel withoutPrivilegedUsers(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutPrivilegedUsers(item);
      }
      return this;
   }

   public CurrentUser getCurrentUser()
   {
      return this.currentUser;
   }

   public ServerChannel setCurrentUser(CurrentUser value)
   {
      if (this.currentUser == value)
      {
         return this;
      }

      final CurrentUser oldValue = this.currentUser;
      if (this.currentUser != null)
      {
         this.currentUser = null;
         oldValue.withoutChannel(this);
      }
      this.currentUser = value;
      if (value != null)
      {
         value.withChannel(this);
      }
      this.firePropertyChange(PROPERTY_CURRENT_USER, oldValue, value);
      return this;
   }

   public List<Message> getMessage()
   {
      return this.message != null ? Collections.unmodifiableList(this.message) : Collections.emptyList();
   }

   public ServerChannel withMessage(Message value)
   {
      if (this.message == null)
      {
         this.message = new ArrayList<>();
      }
      if (!this.message.contains(value))
      {
         this.message.add(value);
         value.setChannel(this);
         this.firePropertyChange(PROPERTY_MESSAGE, null, value);
      }
      return this;
   }

   public ServerChannel withMessage(Message... value)
   {
      for (final Message item : value)
      {
         this.withMessage(item);
      }
      return this;
   }

   public ServerChannel withMessage(Collection<? extends Message> value)
   {
      for (final Message item : value)
      {
         this.withMessage(item);
      }
      return this;
   }

   public ServerChannel withoutMessage(Message value)
   {
      if (this.message != null && this.message.remove(value))
      {
         value.setChannel(null);
         this.firePropertyChange(PROPERTY_MESSAGE, value, null);
      }
      return this;
   }

   public ServerChannel withoutMessage(Message... value)
   {
      for (final Message item : value)
      {
         this.withoutMessage(item);
      }
      return this;
   }

   public ServerChannel withoutMessage(Collection<? extends Message> value)
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
      result.append(' ').append(this.getType());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setCategories(null);
      this.withoutPrivilegedUsers(new ArrayList<>(this.getPrivilegedUsers()));
      this.setCurrentUser(null);
      this.withoutMessage(new ArrayList<>(this.getMessage()));
   }
}
