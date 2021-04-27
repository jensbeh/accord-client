package de.uniks.stp.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Server
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_ID = "id";
   public static final String PROPERTY_OWNER = "owner";
   public static final String PROPERTY_CATEGORIES = "categories";
   public static final String PROPERTY_USER = "user";
   private String name;
   private String id;
   private String owner;
   private List<Categories> categories;
   private List<User> user;
   protected PropertyChangeSupport listeners;

   public String getName()
   {
      return this.name;
   }

   public Server setName(String value)
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

   public Server setId(String value)
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

   public String getOwner()
   {
      return this.owner;
   }

   public Server setOwner(String value)
   {
      if (Objects.equals(value, this.owner))
      {
         return this;
      }

      final String oldValue = this.owner;
      this.owner = value;
      this.firePropertyChange(PROPERTY_OWNER, oldValue, value);
      return this;
   }

   public List<Categories> getCategories()
   {
      return this.categories != null ? Collections.unmodifiableList(this.categories) : Collections.emptyList();
   }

   public Server withCategories(Categories value)
   {
      if (this.categories == null)
      {
         this.categories = new ArrayList<>();
      }
      if (!this.categories.contains(value))
      {
         this.categories.add(value);
         value.setServer(this);
         this.firePropertyChange(PROPERTY_CATEGORIES, null, value);
      }
      return this;
   }

   public Server withCategories(Categories... value)
   {
      for (final Categories item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

   public Server withCategories(Collection<? extends Categories> value)
   {
      for (final Categories item : value)
      {
         this.withCategories(item);
      }
      return this;
   }

   public Server withoutCategories(Categories value)
   {
      if (this.categories != null && this.categories.remove(value))
      {
         value.setServer(null);
         this.firePropertyChange(PROPERTY_CATEGORIES, value, null);
      }
      return this;
   }

   public Server withoutCategories(Categories... value)
   {
      for (final Categories item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

   public Server withoutCategories(Collection<? extends Categories> value)
   {
      for (final Categories item : value)
      {
         this.withoutCategories(item);
      }
      return this;
   }

   public List<User> getUser()
   {
      return this.user != null ? Collections.unmodifiableList(this.user) : Collections.emptyList();
   }

   public Server withUser(User value)
   {
      if (this.user == null)
      {
         this.user = new ArrayList<>();
      }
      if (!this.user.contains(value))
      {
         this.user.add(value);
         value.withServer(this);
         this.firePropertyChange(PROPERTY_USER, null, value);
      }
      return this;
   }

   public Server withUser(User... value)
   {
      for (final User item : value)
      {
         this.withUser(item);
      }
      return this;
   }

   public Server withUser(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withUser(item);
      }
      return this;
   }

   public Server withoutUser(User value)
   {
      if (this.user != null && this.user.remove(value))
      {
         value.withoutServer(this);
         this.firePropertyChange(PROPERTY_USER, value, null);
      }
      return this;
   }

   public Server withoutUser(User... value)
   {
      for (final User item : value)
      {
         this.withoutUser(item);
      }
      return this;
   }

   public Server withoutUser(Collection<? extends User> value)
   {
      for (final User item : value)
      {
         this.withoutUser(item);
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
      result.append(' ').append(this.getOwner());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutCategories(new ArrayList<>(this.getCategories()));
      this.withoutUser(new ArrayList<>(this.getUser()));
   }
}
