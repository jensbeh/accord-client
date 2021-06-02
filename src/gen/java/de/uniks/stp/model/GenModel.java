package de.uniks.stp.model;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.Type;
import org.fulib.classmodel.Clazz;

public class GenModel implements ClassModelDecorator {
    @Override
    public void decorate(ClassModelManager mm) {
        Clazz server = mm.haveClass("Server");
        mm.haveAttribute(server, "name", Type.STRING);
        mm.haveAttribute(server, "id", Type.STRING);
        mm.haveAttribute(server, "owner", Type.STRING);

        Clazz categories = mm.haveClass("Categories");
        mm.haveAttribute(categories, "name", Type.STRING);
        mm.haveAttribute(categories, "id", Type.STRING);

        mm.associate(server, "categories", 3, categories, "server", 1);

        Clazz channel = mm.haveClass("Channel");
        mm.haveAttribute(channel, "name", Type.STRING);
        mm.haveAttribute(channel, "id", Type.STRING);
        mm.haveAttribute(channel, "unreadMessagesCounter", Type.INT);
        mm.haveAttribute(channel, "privilege", Type.BOOLEAN);
        mm.haveAttribute(channel, "type", Type.STRING);

        mm.associate(categories, "channel", 3, channel, "categories", 1);

        Clazz user = mm.haveClass("User");
        mm.haveAttribute(user, "name", Type.STRING);
        mm.haveAttribute(user, "id", Type.STRING);
        mm.haveAttribute(user, "status", Type.BOOLEAN);

        mm.associate(server, "user", 3, user, "server", 3);

        Clazz currentUser = mm.haveClass("CurrentUser");
        mm.haveAttribute(currentUser, "name", Type.STRING);
        mm.haveAttribute(currentUser, "userKey", Type.STRING);

        mm.associate(currentUser, "user", 3, user, "currentUser", 1);
        mm.associate(currentUser, "server", 3, server, "currentUser", 1);
        mm.associate(currentUser, "privateChat", 3, channel, "currentUser", 1);


        Clazz msg = mm.haveClass("Message");
        mm.haveAttribute(msg, "from", Type.STRING);
        mm.haveAttribute(msg, "timestamp", Type.INT);
        mm.haveAttribute(msg, "message", Type.STRING);

        mm.associate(channel, "message", 3, msg, "channel", 1);
    }
}

