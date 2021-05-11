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
   public static final String PROPERTY_STATUS = "status";
   public static final String PROPERTY_SERVER = "server";
   public static final String PROPERTY_CURRENT_USER = "currentUser";
   private String name;
   private String id;
   private boolean status;
   private boolean tempUser;
   private List<Server> server;
   protected PropertyChangeSupport listeners;
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
      this.withoutServer(new ArrayList<>(this.getServer()));
      this.setCurrentUser(null);
   }
}
