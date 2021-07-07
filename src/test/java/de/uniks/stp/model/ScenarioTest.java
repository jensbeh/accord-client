package de.uniks.stp.model;

import org.junit.Assert;
import org.junit.Test;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

public class ScenarioTest {

    @Test
    public void audioMemberTest() {
        AudioMember user = new AudioMember();
        ServerChannel channel = new ServerChannel().setId("123").setName("Test");
        user.setName(user.getName()).setId(user.getId());

        user.setChannel(channel).setId(channel.getId()).setName(channel.getName());
        Assert.assertNotNull(user.getChannel());

        PropertyChangeSupport listener = user.listeners();
        Assert.assertNotNull(listener);

        user.firePropertyChange("name", channel, channel);
        Assert.assertEquals(user.toString(), "123 Test");

        user.removeYou();
    }

    @Test
    public void currentUserTest() {
        CurrentUser user = new CurrentUser();
        user.setName(user.getName()).setUserKey(user.getUserKey()).setPassword(user.getPassword()).setId(user.getId());
        ServerChannel channel1 = new ServerChannel().setId("123").setName("Test1");
        ServerChannel channel2 = new ServerChannel().setId("234").setName("Test2");
        ServerChannel channel3 = new ServerChannel().setId("345").setName("Test3");
        PrivateChat chat1 = new PrivateChat().setId("123").setName("Test1");
        PrivateChat chat2 = new PrivateChat().setId("234").setName("Test2");
        Server server1 = new Server().setId("123").setName("Test1").setOwner(user.toString()).setCurrentUser(user);
        Server server2 = new Server().setId("234").setName("Test2").setOwner(user.toString()).setCurrentUser(user);
        User user1 = new User().setId("123").setName("Test1").setStatus(true).setCurrentUser(user);
        User user2 = new User().setId("234").setName("Test2").setStatus(true).setCurrentUser(user);

        user.withChannel(channel1, channel2);
        Assert.assertEquals(user.getChannel().size(), 2);

        user.withoutChannel(channel1, channel2);
        Assert.assertEquals(user.getChannel().size(), 0);

        ArrayList<ServerChannel> channels = new ArrayList<>();
        channels.add(channel1);
        channels.add(channel2);
        channels.add(channel3);
        user.withChannel(channels);
        user.withoutChannel(channels);
        Assert.assertEquals(user.getChannel().size(), 0);

        ArrayList<PrivateChat> chats = new ArrayList<>();
        chats.add(chat1);
        chats.add(chat2);
        user.withPrivateChat(chats);
        user.withoutPrivateChat(chats);
        Assert.assertEquals(user.getChannel().size(), 0);

        user.withPrivateChat(chat1, chat2);
        Assert.assertEquals(user.getPrivateChat().size(), 2);

        user.withoutPrivateChat(chat1, chat2);
        Assert.assertEquals(user.getPrivateChat().size(), 0);

        user.withServer(server1, server2);
        Assert.assertEquals(user.getServer().size(), 2);

        user.withoutServer(server1, server2);
        Assert.assertEquals(user.getServer().size(), 0);

        ArrayList<Server> servers = new ArrayList<>();
        servers.add(server1);
        servers.add(server2);
        user.withServer(servers);
        user.withoutServer(servers);
        Assert.assertEquals(user.getServer().size(), 0);

        user.withUser(user1, user2);
        Assert.assertEquals(user.getUser().size(), 2);

        user.withoutUser(user1, user2);
        Assert.assertEquals(user.getUser().size(), 0);

        ArrayList<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        user.withUser(users);
        user.withoutUser(users);
        Assert.assertEquals(user.getUser().size(), 0);

        PropertyChangeSupport listener = user.listeners();
        Assert.assertNotNull(listener);

        user.firePropertyChange("name", channel1, channel1);

        user.removeYou();
    }

    @Test
    public void categoriesTest() {
        Categories category = new Categories();
        category.setId(category.getId()).setServer(category.getServer()).setName(category.getName());
        ServerChannel channel1 = new ServerChannel().setId("123").setName("Test1");
        ServerChannel channel2 = new ServerChannel().setId("234").setName("Test2");
        ServerChannel channel3 = new ServerChannel().setId("345").setName("Test3");

        category.withChannel(channel1, channel2);
        Assert.assertEquals(category.getChannel().size(), 2);

        category.withoutChannel(channel1, channel2);
        Assert.assertEquals(category.getChannel().size(), 0);

        ArrayList<ServerChannel> channels = new ArrayList<>();
        channels.add(channel1);
        channels.add(channel2);
        channels.add(channel3);
        category.withChannel(channels);
        category.withoutChannel(channels);
        Assert.assertEquals(category.getChannel().size(), 0);

        PropertyChangeSupport listener = category.listeners();
        Assert.assertNotNull(listener);

        category.listeners = null;
        category.addPropertyChangeListener(null);
        category.removePropertyChangeListener(null);

        category.removeYou();
    }

    @Test
    public void messageTest() {
        Message message = new Message();
        message.setId(message.getId()).setFrom(message.getFrom())
                .setMessage(message.getMessage()).setTimestamp(message.getTimestamp());
        ServerChannel channel1 = new ServerChannel().setId("123").setName("Test1");
        PrivateChat chat1 = new PrivateChat().setId("123").setName("Test1");
        PrivateChat chat2 = new PrivateChat().setId("234").setName("Test2");

        message.setPrivateChat(chat1);
        message.setPrivateChat(chat2);
        Assert.assertNotNull(message.getPrivateChat());

        PropertyChangeSupport listener = message.listeners();
        Assert.assertNotNull(listener);

        message.firePropertyChange("Channel", channel1, channel1);
    }

    @Test
    public void privateChatTest() {
        PrivateChat chat = new PrivateChat().setName("Test").setId("123");
        chat.setId(chat.getId()).setName(chat.getName()).setCurrentUser(chat.getCurrentUser())
                .setUnreadMessagesCounter(chat.getUnreadMessagesCounter());
        Message message1 = new Message().setId("123").setMessage("Test1");
        Message message2 = new Message().setId("123").setMessage("Test1");

        chat.withMessage(message1, message2);
        Assert.assertEquals(chat.getMessage().size(), 2);

        chat.withoutMessage(message1, message2);
        Assert.assertEquals(chat.getMessage().size(), 0);

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);
        chat.withMessage(messages);
        chat.withoutMessage(messages);
        Assert.assertEquals(chat.getMessage().size(), 0);

        PropertyChangeSupport listener = chat.listeners();
        Assert.assertNotNull(listener);

        chat.firePropertyChange("name", message1, message1);
        chat.listeners = null;
        chat.addPropertyChangeListener("chat", null);
        chat.removePropertyChangeListener(null);

        Assert.assertEquals(chat.toString(), "Test 123");

        chat.removeYou();
    }

    @Test
    public void serverTest() {
        Server server = new Server();
        server.setId(server.getId()).setOwner(server.getOwner()).setName(server.getName()).setCurrentUser(server.getCurrentUser());
        User user1 = new User().setId("123").setName("Test1").setStatus(true);
        User user2 = new User().setId("234").setName("Test2").setStatus(true);
        Categories category1 = new Categories().setId("123").setName("Test1");
        Categories category2 = new Categories().setId("234").setName("Test2");

        server.withUser(user1, user2);
        Assert.assertEquals(server.getUser().size(), 2);

        server.withoutUser(user1, user2);
        Assert.assertEquals(server.getUser().size(), 0);

        ArrayList<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        server.withUser(users);
        server.withoutUser(users);
        Assert.assertEquals(server.getUser().size(), 0);

        server.withCategories(category1, category2);
        Assert.assertEquals(server.getCategories().size(), 2);

        server.withoutCategories(category1, category2);
        Assert.assertEquals(server.getCategories().size(), 0);

        ArrayList<Categories> categories = new ArrayList<>();
        categories.add(category1);
        categories.add(category2);
        server.withCategories(categories);
        server.withoutCategories(categories);
        Assert.assertEquals(server.getCategories().size(), 0);

        server.addPropertyChangeListener("chat", null);
        server.removePropertyChangeListener(null);
        server.listeners = null;
        server.addPropertyChangeListener(null);
        server.removePropertyChangeListener("chat", null);

        server.listeners = null;
        server.firePropertyChange("name", user1, user2);
        PropertyChangeSupport listener = server.listeners();
        Assert.assertNotNull(listener);

        server.removeCategories();
        Assert.assertEquals(server.getCategories().size(), 0);
        server.removeYou();
    }

    @Test
    public void serverChannelTest() {
        ServerChannel channel = new ServerChannel();
        channel.setId(channel.getId()).setType(channel.getType()).setName(channel.getName())
                .setPrivilege(channel.isPrivilege()).setUnreadMessagesCounter(channel.getUnreadMessagesCounter())
                .setCurrentUser(channel.getCurrentUser()).setCategories(channel.getCategories());
        AudioMember user1 = new AudioMember().setId("123").setName("Test1");
        AudioMember user2 = new AudioMember().setId("234").setName("Test2");
        Message msg1 = new Message().setId("123").setMessage("Test1");
        Message msg2 = new Message().setId("123").setMessage("Test1");
        User pUser1 = new User().setId("123").setName("Test1").setStatus(true);
        User pUser2 = new User().setId("234").setName("Test2").setStatus(true);

        channel.withAudioMember(user1, user2);
        Assert.assertEquals(channel.getAudioMember().size(), 2);

        channel.withoutAudioMember(user1, user2);
        Assert.assertEquals(channel.getAudioMember().size(), 0);

        ArrayList<AudioMember> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        channel.withAudioMember(users);
        channel.withoutAudioMember(users);
        Assert.assertEquals(channel.getAudioMember().size(), 0);

        channel.withMessage(msg1, msg2);
        Assert.assertEquals(channel.getMessage().size(), 2);

        channel.withoutMessage(msg1, msg2);
        Assert.assertEquals(channel.getMessage().size(), 0);

        ArrayList<Message> messages = new ArrayList<>();
        messages.add(msg1);
        messages.add(msg2);
        channel.withMessage(messages);
        channel.withoutMessage(messages);
        Assert.assertEquals(channel.getMessage().size(), 0);

        channel.withPrivilegedUsers(pUser1, pUser2);
        Assert.assertEquals(channel.getPrivilegedUsers().size(), 2);

        channel.withoutPrivilegedUsers(pUser1, pUser2);
        Assert.assertEquals(channel.getPrivilegedUsers().size(), 0);

        ArrayList<User> privileged = new ArrayList<>();
        privileged.add(pUser1);
        privileged.add(pUser2);
        channel.withPrivilegedUsers(privileged);
        channel.withoutPrivilegedUsers(privileged);
        Assert.assertEquals(channel.getPrivilegedUsers().size(), 0);

        PropertyChangeSupport listener = channel.listeners();
        Assert.assertNotNull(listener);

        channel.removeYou();
    }

    @Test
    public void userTest() {
        User user = new User().setName("Test").setId("123");
        user.setId(user.getId()).setName(user.getName()).setStatus(true).setCurrentUser(user.getCurrentUser());
        Server server1 = new Server().setId("123").setName("Test1").setOwner(user.toString());
        Server server2 = new Server().setId("234").setName("Test2").setOwner(user.toString());
        ServerChannel channel1 = new ServerChannel().setName("Test1").setId("123").setPrivilege(false);
        ServerChannel channel2 = new ServerChannel().setName("Test1").setId("123").setPrivilege(false);

        user.withServer(server1, server2);
        Assert.assertEquals(user.getServer().size(), 2);

        user.withoutServer(server1, server2);
        Assert.assertEquals(user.getServer().size(), 0);

        ArrayList<Server> servers = new ArrayList<>();
        servers.add(server1);
        servers.add(server2);
        user.withServer(servers);
        user.withoutServer(servers);
        Assert.assertEquals(user.getServer().size(), 0);

        user.withPrivileged(channel1, channel2);
        Assert.assertEquals(user.getPrivileged().size(), 2);

        user.withoutPrivileged(channel1, channel2);
        Assert.assertEquals(user.getPrivileged().size(), 0);

        ArrayList<ServerChannel> privileged = new ArrayList<>();
        privileged.add(channel1);
        privileged.add(channel2);
        user.withPrivileged(privileged);
        user.withoutPrivileged(privileged);
        Assert.assertEquals(user.getPrivileged().size(), 0);

        PropertyChangeSupport listener = user.listeners();
        Assert.assertNotNull(listener);

        user.firePropertyChange("server", server1, server1);

        Assert.assertEquals(user.toString(), "Test 123");

        user.removeYou();
    }
}
