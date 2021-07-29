package de.uniks.stp.controller.home;

import com.github.cliftonlabs.json_simple.JsonException;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.cellfactories.ServerListCell;
import de.uniks.stp.controller.home.subcontroller.CreateJoinServerController;
import de.uniks.stp.controller.server.ServerViewController;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.ResourceManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class HomeViewController {
    private final RestClient restClient;
    private HBox root;
    private HBox homeView;
    private ScrollPane scrollPaneServerBox;
    private final Parent view;
    private ListView<Server> serverList;
    private Circle addServer;
    private Circle homeButton;
    private Circle homeCircle;
    private Button settingsButton;
    private Label homeLabel;
    private Button logoutButton;
    private Stage stage;
    private ModelBuilder builder;
    private ServerListCell serverListCellFactory;
    private PrivateViewController privateViewController;
    private Parent privateView;
    public boolean inServerChat;
    private Map<Server, Parent> serverViews;
    private Map<Server, ServerViewController> serverController;
    private CreateJoinServerController createJoinServerController;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = modelBuilder.getRestClient();
    }

    @SuppressWarnings("unchecked")
    public void init() throws IOException, URISyntaxException {
        builder.loadSettings();
        builder.setInServerState(false);
        // Load all view references
        homeView = (HBox) view.lookup("#homeView");
        root = (HBox) view.lookup("#root");
        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");
        homeCircle = (Circle) view.lookup("#homeCircle");
        homeButton = (Circle) view.lookup("#homeButton");
        settingsButton = (Button) view.lookup("#settingsButton");
        homeLabel = (Label) view.lookup("#homeLabel");
        logoutButton = (Button) view.lookup("#logoutButton");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onShowCreateServer);
        serverList = (ListView<Server>) scrollPaneServerBox.getContent().lookup("#serverList");
        serverListCellFactory = new ServerListCell();
        serverList.setCellFactory(serverListCellFactory);
        this.serverList.setOnMouseReleased(this::onServerClicked);
        this.settingsButton.setOnAction(this::settingsButtonOnClicked);
        logoutButton.setOnAction(this::logoutButtonOnClicked);
        this.homeButton.setOnMouseClicked(this::homeButtonClicked);
        serverViews = new HashMap<>();
        serverController = new HashMap<>();

        ResourceManager.extractEmojis();
        ResourceManager.copyDefaultSound(StageManager.class.getResourceAsStream("sounds/notification/default.wav"));
        try {
            builder.setBlockedUsers(ResourceManager.loadBlockedUsers(builder.getPersonalUser().getName()));
        } catch (JsonException e) {
            e.printStackTrace();
        }


        showPrivateView();
        showServers(() -> {
            for (Server server : builder.getPersonalUser().getServer()) {
                try {
                    Parent serverView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/ServerView.fxml")), StageManager.getLangBundle());
                    serverViews.put(server, serverView);
                    serverController.put(server, new ServerViewController(serverView, builder, server, getController()));
                    serverController.get(server).startController(status -> {
                        // TODO start here homeView -> from loginView this!
                        System.out.println("loaded Server " + server.getName());
                    });
                    serverController.get(server).setTheme();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Returns the current HomeViewController.
     */
    private HomeViewController getController() {
        return this;
    }

    /**
     * Updates Servers name if case the handle massage calls the changeServerName method.
     */
    public void showServerUpdate() {
        serverList.refresh();
    }

    /**
     * refreshed the serverList when a server was deleted.
     */
    public void serverDeleted() {
        builder.setInServerState(false);
        this.builder.setCurrentServer(null);
        showPrivateView();
        updateServerListColor();
        scrollPaneServerBox.setVvalue(0);
    }

    /**
     * Function to refresh the serverList.
     */
    public void refreshServerList() {
        serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer()));
        scrollPaneServerBox.setVvalue(0);
    }

    /**
     * Stops the deleted server.
     */
    public void stopServer(Server server) {
        if (builder.getUSER_CLIENT() != null) {
            if (builder.getUSER_CLIENT().getSession() != null) {
                serverController.get(server).stop();
            }
        }
        serverController.remove(server);
        serverViews.remove(server);
    }

    /**
     * Shows the private home view to have a private chat with other users.
     */
    private void showPrivateView() {
        inServerChat = false;
        try {
            if (privateView == null) {
                privateView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/homeview/PrivateView.fxml")), StageManager.getLangBundle());
                privateViewController = new PrivateViewController(privateView, builder);
                privateViewController.init();
                privateViewController.setTheme();
                this.root.getChildren().clear();
                this.root.getChildren().add(privateView);
            } else {
                this.privateViewController.showUsers();
                this.privateViewController.headsetSettings();
                this.privateViewController.showAudioConnectedBox();

                this.root.getChildren().clear();
                this.root.getChildren().add(privateView);
                if (builder.getCurrentPrivateChat() != null) {
                    this.privateViewController.MessageViews();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes the currently shown view to the Server view of the currentServer.
     * Also changes the online user list to an online and offline list of users in that server.
     */
    public void showServerView() {
        builder.setInServerState(true);
        try {
            this.root.getChildren().clear();
            this.root.getChildren().add(serverViews.get(builder.getCurrentServer()));
            this.serverController.get(builder.getCurrentServer()).startShowServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////
    // Server
    ///////////////////////////

    /**
     * Creates a createServer view in a new Stage.
     *
     * @param mouseEvent is called when clicked on the + Button.
     */
    private void onShowCreateServer(MouseEvent mouseEvent) {

        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/homeview/CreateJoinView.fxml")), StageManager.getLangBundle());
            Scene scene = new Scene(root);
            stage = new Stage();
            createJoinServerController = new CreateJoinServerController(root, builder, stage);
            createJoinServerController.init();
            createJoinServerController.setTheme();
            createJoinServerController.showCreateServerView(this::onServerCreated);
            createJoinServerController.joinNewServer(this::joinNewServer);
            stage.initOwner(homeView.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(scene);
            stage.show();
            updateServerListColor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void joinNewServer() {
        builder.setServerChatWebSocketClient(null);
        builder.setSERVER_USER(null);
        Platform.runLater(() -> {
            stage.close();
            showServers(() -> {
                for (Server server : builder.getPersonalUser().getServer()) {
                    try {
                        if (!serverController.containsKey(server)) {
                            builder.setCurrentServer(server);
                            Parent serverView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/ServerView.fxml")), StageManager.getLangBundle());
                            serverViews.put(server, serverView);
                            serverController.put(server, new ServerViewController(serverView, builder, server, getController()));
                            serverController.get(server).startController(status -> Platform.runLater(() -> {
                                updateServerListColor();
                                userArrivedNotification(server);
                                showServerView();
                            }));

                            builder.setSERVER_USER(this.serverController.get(builder.getCurrentServer()).getServerSystemWebSocket());
                            builder.setServerChatWebSocketClient(this.serverController.get(builder.getCurrentServer()).getChatWebSocketClient());

                            serverController.get(server).setTheme();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    /**
     * Closes the createServerStage and calls showServerView. Is
     * called after the ok button in createServer is clicked
     */
    public void onServerCreated() {
        if (builder.getCurrentChatViewController() != null) {
            builder.getCurrentChatViewController().stopMediaPlayers();
        }
        builder.setServerChatWebSocketClient(null);
        builder.setSERVER_USER(null);
        Platform.runLater(() -> {
            stage.close();
            showServers(() -> {
                for (Server server : builder.getPersonalUser().getServer()) {
                    try {
                        if (!serverController.containsKey(server)) {
                            builder.setCurrentServer(server);
                            Parent serverView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/ServerView.fxml")), StageManager.getLangBundle());
                            serverViews.put(server, serverView);
                            serverController.put(server, new ServerViewController(serverView, builder, server, getController()));
                            serverController.get(server).startController(status -> Platform.runLater(() -> {
                                updateServerListColor();
                                showServerView();
                            }));

                            builder.setSERVER_USER(this.serverController.get(builder.getCurrentServer()).getServerSystemWebSocket());
                            builder.setServerChatWebSocketClient(this.serverController.get(builder.getCurrentServer()).getChatWebSocketClient());

                            serverController.get(server).setTheme();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void userArrivedNotification(Server server) {
        if (builder.getServerChatWebSocketClient() != null) {
            JSONObject obj = new JSONObject().put("channel", server.getCategories().get(0).getChannel().get(0).getId()).put("message", builder.getPersonalUser().getName() + " arrived on the Server");
            try {
                builder.getServerChatWebSocketClient().sendMessage(obj.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the clicked Server as currentServer and calls showServerView.
     *
     * @param mouseEvent is called when clicked on a Server
     */
    private void onServerClicked(MouseEvent mouseEvent) {
        //stop currently playing videos
        if (builder.getCurrentChatViewController() != null) {
            builder.getCurrentChatViewController().stopMediaPlayers();
        }
        if (this.builder.getCurrentServer() != (this.serverList.getSelectionModel().getSelectedItem())) {
            builder.setServerChatWebSocketClient(null);
            builder.setSERVER_USER(null);
        }
        if (mouseEvent.getClickCount() == 1 && this.serverList.getItems().size() != 0) {
            if (this.builder.getCurrentServer() != (this.serverList.getSelectionModel().getSelectedItem())) {
                Server selectedServer = this.serverList.getSelectionModel().getSelectedItem();
                this.builder.setCurrentServer(selectedServer);
                builder.setSERVER_USER(this.serverController.get(builder.getCurrentServer()).getServerSystemWebSocket());
                builder.setServerChatWebSocketClient(this.serverController.get(builder.getCurrentServer()).getChatWebSocketClient());
                updateServerListColor();
                showServerView();
                serverController.get(builder.getCurrentServer()).setTheme();
            }
        }
    }

    /**
     * Updates the circles and change the current server or Home circle color
     */
    private void updateServerListColor() {
        if (builder.getCurrentServer() == null) {
            homeCircle.setFill(Paint.valueOf("#5a5c5e"));
        } else {
            homeCircle.setFill(Paint.valueOf("#a4a4a4"));
        }
        serverListCellFactory.setCurrentServer(builder.getCurrentServer());
        serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer()));
    }


    public interface ServerLoadedCallback {
        void onSuccess();
    }

    /**
     * Get Servers and show Servers
     */
    public void showServers(ServerLoadedCallback serverLoadedCallback) {
        if (!builder.getPersonalUser().getUserKey().equals("")) {
            restClient.getServers(builder.getPersonalUser().getUserKey(), response -> {
                JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
                for (int i = 0; i < jsonResponse.length(); i++) {
                    String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                    String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                    builder.buildServer(serverName, serverId);
                }
                Platform.runLater(() -> serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer())));
                serverLoadedCallback.onSuccess();
            });
        }
    }

    /**
     * Stop running Actions when Controller gets closed
     */
    public void stop() {
        this.addServer.setOnMouseClicked(null);
        this.homeButton.setOnMouseClicked(null);
        this.homeCircle.setOnMouseClicked(null);
        this.settingsButton.setOnAction(null);
        logoutButton.setOnAction(null);
        builder.saveSettings();
        if (stage != null) {
            this.stage.close();
            stage = null;
        }
        if (builder.getPrivateChatWebSocketClient() != null) {
            try {
                if (builder.getPrivateChatWebSocketClient().getSession() != null) {
                    builder.getPrivateChatWebSocketClient().stop();
                }
                builder.setPrivateChatWebSocketClient(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cleanup();
    }

    /**
     * Set the Builder
     *
     * @param builder is the builder to set
     */
    public void setBuilder(ModelBuilder builder) {
        this.builder = builder;
    }

    /**
     * Clicking Settings Button opens the Settings Popup
     *
     * @param actionEvent is called when clicked on the Settings Button
     */
    private void settingsButtonOnClicked(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }

    /**
     * Clicking Home Button refreshes the Online Users List
     *
     * @param mouseEvent is called when clicked on the Home Button
     */
    private void homeButtonClicked(MouseEvent mouseEvent) {
        if (builder.getCurrentChatViewController() != null) {
            builder.getCurrentChatViewController().stopMediaPlayers();
        }
        builder.setInServerState(false);
        this.builder.setCurrentServer(null);
        showPrivateView();
        updateServerListColor();
        scrollPaneServerBox.setVvalue(0);

        // start EasterEgg - Snake
        if (mouseEvent.getClickCount() == 10) {
            StageManager.showStartSnakeScreen();
        }
    }

    /**
     * Clicking Logout Button logs the currentUser out and returns to Login Screen
     *
     * @param actionEvent is called when clicked on the Logout Button
     */
    private void logoutButtonOnClicked(ActionEvent actionEvent) {
        if (builder.getCurrentChatViewController() != null) {
            builder.getCurrentChatViewController().stopMediaPlayers();
        }

        cleanup();

        restClient.logout(builder.getPersonalUser().getUserKey(), response -> {
            JSONObject result = response.getBody().getObject();
            if (result.get("status").equals("success")) {
                System.out.println(result.get("message"));
                if (builder.getCurrentPrivateChat() != null) {
                    builder.setCurrentPrivateChat(null);
                }
                Platform.runLater(StageManager::showLoginScreen);
            }
        });
    }

    private void cleanup() {
        if (privateViewController != null) {
            privateViewController.stop();
            privateViewController = null;
        }
        if (!serverController.isEmpty()) {
            for (Server server : builder.getServers()) {
                serverController.get(server).stop();
                serverController.remove(server);
            }
        }

        if (builder.getAudioStreamClient() != null) {
            builder.getAudioStreamClient().disconnectStream();
            builder.setAudioStreamClient(null);
        }
    }

    /**
     * Returns the controller of the current Server.
     */
    public ServerViewController getServerController() {
        return serverController.get(builder.getCurrentServer());
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (homeLabel != null)
            homeLabel.setText(lang.getString("label.home"));
        if (logoutButton != null)
            logoutButton.setText(lang.getString("button.logout"));

        if (createJoinServerController != null) {
            createJoinServerController.onLanguageChanged();
        }
        if (privateViewController != null) {
            privateViewController.onLanguageChanged();
        }
        if (serverController.get(builder.getCurrentServer()) != null) {
            serverController.get(builder.getCurrentServer()).onLanguageChanged();
        }
    }

    public PrivateViewController getPrivateViewController() {
        return privateViewController;
    }

    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        homeView.getStylesheets().clear();
        homeView.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/HomeView.css")).toExternalForm());
        privateViewController.setTheme();
        if (builder.getCurrentServer() != null) {
            if (serverController.size() != 0) {
                serverController.get(builder.getCurrentServer()).setTheme();
            }
        }
    }

    private void setDarkMode() {
        homeView.getStylesheets().clear();
        homeView.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/HomeView.css")).toExternalForm());
        privateViewController.setTheme();
        if (builder.getCurrentServer() != null) {
            if (serverController.size() != 0) {
                serverController.get(builder.getCurrentServer()).setTheme();
            }
        }
    }
}
