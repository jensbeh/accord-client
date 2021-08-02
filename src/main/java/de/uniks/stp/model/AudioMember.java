package de.uniks.stp.model;

import java.beans.PropertyChangeSupport;
import java.util.Objects;

public class AudioMember {
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_CHANNEL = "channel";
    protected PropertyChangeSupport listeners;
    private String id;
    private String name;
    private ServerChannel channel;

    public String getId() {
        return this.id;
    }

    public AudioMember setId(String value) {
        if (Objects.equals(value, this.id)) {
            return this;
        }

        final String oldValue = this.id;
        this.id = value;
        this.firePropertyChange(PROPERTY_ID, oldValue, value);
        return this;
    }

    public String getName() {
        return this.name;
    }

    public AudioMember setName(String value) {
        if (Objects.equals(value, this.name)) {
            return this;
        }

        final String oldValue = this.name;
        this.name = value;
        this.firePropertyChange(PROPERTY_NAME, oldValue, value);
        return this;
    }

    public ServerChannel getChannel() {
        return this.channel;
    }

    public AudioMember setChannel(ServerChannel value) {
        if (this.channel == value) {
            return this;
        }

        final ServerChannel oldValue = this.channel;
        if (this.channel != null) {
            this.channel = null;
            oldValue.withoutAudioMember(this);
        }
        this.channel = value;
        if (value != null) {
            value.withAudioMember(this);
        }
        this.firePropertyChange(PROPERTY_CHANNEL, oldValue, value);
        return this;
    }

    public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (this.listeners != null) {
            this.listeners.firePropertyChange(propertyName, oldValue, newValue);
            return true;
        }
        return false;
    }

    public PropertyChangeSupport listeners() {
        if (this.listeners == null) {
            this.listeners = new PropertyChangeSupport(this);
        }
        return this.listeners;
    }

    public void removeYou() {
        this.setChannel(null);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(' ').append(this.getId());
        result.append(' ').append(this.getName());
        return result.substring(1);
    }
}
