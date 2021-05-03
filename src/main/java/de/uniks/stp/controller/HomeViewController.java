package de.uniks.stp.controller;

import com.sun.javafx.scene.control.LabeledText;
import de.uniks.stp.AlternateChannelListCellFactory;
import de.uniks.stp.ServerEditor;
import de.uniks.stp.StageManager;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Message;
import de.uniks.stp.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class HomeViewController {

    private ServerEditor editor;
    private User currentUser;
    private Parent view;
    private VBox userProfile;
    private ScrollPane userPane, chatpane;
    private List<User> dummylist = new ArrayList<>();
    private ListView<Channel> chatlist;
    private ObservableList<Channel> channels;
    private Channel selectedChat;
    private VBox messages;
    private ScrollPane chatscrollpane;


    public HomeViewController(Parent view, ServerEditor serverEditor) {
        this.currentUser = new User().setName("Helmut").setStatus(true).setUserKey("wildesWiesel");
        this.view = view;
        this.editor = serverEditor;
    }

    public void init() {
        // Load all view references
        dummylist.add(new User().setName("Maurice").setStatus(true));
        dummylist.add(new User().setName("Pascal").setStatus(true));
        dummylist.add(new User().setName("Simon").setStatus(true));
        dummylist.add(new User().setName("Alexander").setStatus(true));
        dummylist.add(new User().setName("Albert").setStatus(true));
        dummylist.add(new User().setName("Clemens").setStatus(true));
        dummylist.add(new User().setName("Sebastian").setStatus(true));
        dummylist.add(new User().setName("Julian").setStatus(true));
        dummylist.add(new User().setName("Paul").setStatus(true));
        dummylist.add(new User().setName("Daniel").setStatus(true));
        dummylist.add(new User().setName("Stefan").setStatus(true));
        dummylist.add(new User().setName("Markus").setStatus(true));
        dummylist.add(new User().setName("Angelo").setStatus(true));
        dummylist.add(new User().setName("GabeN").setStatus(true));
        dummylist.add(new User().setName("Jens").setStatus(true));
        dummylist.add(new User().setName("Arnold").setStatus(true));
        dummylist.add(new User().setName("Christian").setStatus(true));
        dummylist.add(new User().setName("Mehmet").setStatus(true));
        userPane = (ScrollPane) view.lookup("#userscrollpane");
        chatpane = (ScrollPane) view.lookup("#chatpane");
        userProfile = (VBox) userPane.getContent().lookup("#singleuser");
        UserProfileViews();
        chatscrollpane = (ScrollPane) view.lookup("#chatscrollpane");
        messages = (VBox) chatscrollpane.getContent().lookup("#messages");

        chatlist = (ListView<Channel>) chatpane.getContent().lookup("#list");
        chatlist.setCellFactory(new AlternateChannelListCellFactory());
        this.chatlist.setOnMouseReleased(this::onChatlistClicked);


        channels = FXCollections.observableArrayList();
        this.chatlist.setItems(channels);
        channels.add(new Channel().setName("Simon").withMessage(new Message().setMessage("Hallo").setFrom("Simon"),new Message().setMessage("Moin").setFrom("Pascal")));

        this.userProfile.setOnMouseReleased(this::onUserProfileClicked);
    }

    public void stop() {
        this.userProfile.setOnMouseReleased(null);
    }

    private void UserProfileViews() {
        this.userProfile.getChildren().clear();
        for (User user : this.dummylist) {
            try {
                Parent view = FXMLLoader.load(StageManager.class.getResource("controller/UserProfileView.fxml"));
                UserProfileController userProfileController = new UserProfileController(user, view, editor);
                userProfileController.init();

                this.userProfile.getChildren().add(view);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onUserProfileClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            boolean flag = true;
            String name;
            EventTarget tmp = mouseEvent.getTarget();
            Label test;
            if (tmp.getClass().equals(Circle.class)) {
                tmp = ((Circle) tmp).getParent();
            }
            if (!tmp.getClass().equals(Label.class) & !tmp.getClass().equals(HBox.class)) {
                tmp = ((LabeledText) tmp).getParent();
            }
            test = (Label) ((Parent) tmp).lookup("#username");
            name = test.getText();
            for (Channel channel : channels) {
                if (channel.getName().equals(name)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                channels.add(new Channel().setName(test.getText()));
            }
        }
    }

    private void onChatlistClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            selectedChat = this.chatlist.getSelectionModel().getSelectedItem();
            MessageViews();
        }
    }

    private void MessageViews() {
        this.messages.getChildren().clear();
        for (Message msg : this.selectedChat.getMessage()) {
            try {
                Parent view = FXMLLoader.load(StageManager.class.getResource("controller/Message.fxml"));
                MessageController messageController = new MessageController(msg, view, editor);
                messageController.init();

                this.messages.getChildren().add(view);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
