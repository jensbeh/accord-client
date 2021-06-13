package de.uniks.stp.controller;

import de.uniks.stp.AlternateServerListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.CreateServerController;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import util.Constants;

import javax.websocket.CloseReason;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class HomeViewController {
    private final RestClient restClient;
    private HBox root;
    private VBox serverBox;
    private ScrollPane scrollPaneServerBox;
    private Parent view;
    private ListView<Server> serverList;
    private Circle addServer;
    private Circle homeButton;
    private Circle homeCircle;
    private Button settingsButton;
    private static Label homeLabel;
    private static Button logoutButton;
    private static Stage stage;
    private static String stageTitleName;
    private ModelBuilder builder;
    private AlternateServerListCellFactory serverListCellFactory;
    private PrivateViewController privateViewController;
    private Parent privateView;
    public static boolean inServerChat = false;
    private Map<Server, Parent> serverViews;
    private Map<Server, ServerViewController> serverController;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = modelBuilder.getRestClient();
    }

    @SuppressWarnings("unchecked")
    public void init() {
        // Load all view references
        root = (HBox) view.lookup("#root");
        scrollPaneServerBox = (ScrollPane) view.lookup("#scrollPaneServerBox");
        homeCircle = (Circle) view.lookup("#homeCircle");
        homeButton = (Circle) view.lookup("#homeButton");
        serverBox = (VBox) scrollPaneServerBox.getContent().lookup("#serverBox");
        settingsButton = (Button) view.lookup("#settingsButton");
        homeLabel = (Label) view.lookup("#homeLabel");
        logoutButton = (Button) view.lookup("#logoutButton");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onshowCreateServer);
        serverList = (ListView<Server>) scrollPaneServerBox.getContent().lookup("#serverList");
        serverListCellFactory = new AlternateServerListCellFactory();
        serverList.setCellFactory(serverListCellFactory);
        this.serverList.setOnMouseReleased(this::onServerClicked);
        this.settingsButton.setOnAction(this::settingsButtonOnClicked);
        logoutButton.setOnAction(this::logoutButtonOnClicked);
        this.homeButton.setOnMouseClicked(this::homeButtonClicked);
        serverViews = new HashMap<>();
        serverController = new HashMap<>();

        showPrivateView();
        showServers(new ServerLoadedCallback() {
            @Override
            public void onSuccess() {
                for (Server server : builder.getPersonalUser().getServer()) {
                    try {
                        Parent serverView = FXMLLoader.load(StageManager.class.getResource("ServerView.fxml"), StageManager.getLangBundle());
                        serverViews.put(server, serverView);
                        serverController.put(server, new ServerViewController(serverView, builder, server, getController()));
                        serverController.get(server).startController(new ServerViewController.ServerReadyCallback() {
                            @Override
                            public void onSuccess(String status) {
                                // TODO start here homeView -> from loginView this!
                                System.out.println("loaded Server " + server.getName());
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
        try {
            if (builder.getUSER_CLIENT() != null) {
                if (builder.getUSER_CLIENT().getSession() != null) {
                    builder.getUSER_CLIENT().stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.builder.setCurrentServer(null);
        showPrivateView();
        updateServerListColor();
    }

    /**
     * Function to refresh the serverList.
     */
    public void refreshServerList() {
        serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer()));
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
                privateView = FXMLLoader.load(StageManager.class.getResource("PrivateView.fxml"), StageManager.getLangBundle());
                privateViewController = new PrivateViewController(privateView, builder);
                privateViewController.init();
                this.root.getChildren().clear();
                this.root.getChildren().add(privateView);
            } else {
                this.privateViewController.showUsers();
                this.root.getChildren().clear();
                this.root.getChildren().add(privateView);
                if (PrivateViewController.getSelectedChat() != null) {
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
        inServerChat = true;
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
    private void onshowCreateServer(MouseEvent mouseEvent) {

        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/CreateServerView.fxml"), StageManager.getLangBundle());
            Scene scene = new Scene(root);
            CreateServerController createServerController = new CreateServerController(root, builder);
            createServerController.init();
            stage = new Stage();
            createServerController.showCreateServerView(this::onServerCreated);
            createServerController.joinNewServer(this::joinNewServer);
            setStageTitle("window_title_create_new_server");
            stage.setScene(scene);
            stage.show();
            updateServerListColor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void joinNewServer() {
        Platform.runLater(() -> {
            stage.close();
            showServers(new ServerLoadedCallback() {
                @Override
                public void onSuccess() {
                    for (Server server : builder.getPersonalUser().getServer()) {
                        try {
                            if (!serverController.containsKey(server)) {
                                builder.setCurrentServer(server);
                                Parent serverView = FXMLLoader.load(StageManager.class.getResource("ServerView.fxml"), StageManager.getLangBundle());
                                serverViews.put(server, serverView);
                                serverController.put(server, new ServerViewController(serverView, builder, server, getController()));
                                serverController.get(server).startController(new ServerViewController.ServerReadyCallback() {
                                    @Override
                                    public void onSuccess(String status) {
                                        Platform.runLater(() -> {
                                            updateServerListColor();
                                            showServerView();
                                        });
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
        Platform.runLater(() -> {
            stage.close();
            try {
                if (builder.getUSER_CLIENT() != null) {
                    if (builder.getUSER_CLIENT().getSession() != null) {
                        builder.getUSER_CLIENT().stop();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            showServers(new ServerLoadedCallback() {
                @Override
                public void onSuccess() {
                    for (Server server : builder.getPersonalUser().getServer()) {
                        try {
                            if (!serverController.containsKey(server)) {
                                builder.setCurrentServer(server);
                                Parent serverView = FXMLLoader.load(StageManager.class.getResource("ServerView.fxml"), StageManager.getLangBundle());
                                serverViews.put(server, serverView);
                                serverController.put(server, new ServerViewController(serverView, builder, server, getController()));
                                serverController.get(server).startController(new ServerViewController.ServerReadyCallback() {
                                    @Override
                                    public void onSuccess(String status) {
                                        Platform.runLater(() -> {
                                            updateServerListColor();
                                            showServerView();
                                        });
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        });
    }

    /**
     * Sets the clicked Server as currentServer and calls showServerView.
     *
     * @param mouseEvent is called when clicked on a Server
     */
    private void onServerClicked(MouseEvent mouseEvent) {
        try {
            if (builder.getUSER_CLIENT() != null) {
                if (builder.getUSER_CLIENT().getSession() != null) {
                    builder.getUSER_CLIENT().stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mouseEvent.getClickCount() == 1 && this.serverList.getItems().size() != 0) {
            if (this.builder.getCurrentServer() != (this.serverList.getSelectionModel().getSelectedItem())) {
                Server selectedServer = this.serverList.getSelectionModel().getSelectedItem();
                this.builder.setCurrentServer(selectedServer);
                updateServerListColor();
                showServerView();
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
        if (stage != null) {
            this.stage.close();
            stage = null;
        }
        try {
            if (builder.getUSER_CLIENT() != null) {
                if (builder.getUSER_CLIENT().getSession() != null) {
                    builder.getUSER_CLIENT().stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (builder.getPrivateChatWebSocketCLient() != null) {
            try {
                if (builder.getPrivateChatWebSocketCLient().getSession() != null) {
                    builder.getPrivateChatWebSocketCLient().stop();
                }
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
        try {
            if (builder.getUSER_CLIENT() != null) {
                if (builder.getUSER_CLIENT().getSession() != null) {
                    builder.getUSER_CLIENT().stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.builder.setCurrentServer(null);
        showPrivateView();
        updateServerListColor();
        if (mouseEvent.getClickCount() == 15) {
            StageManager.showStartSnakeScreen();
        }
    }

    /**
     * Clicking Logout Button logs the currentUser out and returns to Login Screen
     *
     * @param actionEvent is called when clicked on the Logout Button
     */
    private void logoutButtonOnClicked(ActionEvent actionEvent) {
        try {
            if (builder.getSERVER_USER() != null) {
                if (builder.getSERVER_USER().getSession() != null) {
                    builder.getSERVER_USER().stop();
                }
            }
            if (builder.getUSER_CLIENT() != null) {
                if (builder.getUSER_CLIENT().getSession() != null) {
                    builder.getUSER_CLIENT().stop();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        restClient.logout(builder.getPersonalUser().getUserKey(), response -> {
            JSONObject result = response.getBody().getObject();
            if (result.get("status").equals("success")) {
                System.out.println(result.get("message"));
                if (PrivateViewController.getSelectedChat() != null) {
                    PrivateViewController.setSelectedChat(null);
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
        for (Server server : builder.getServers()) {
            if (builder.getUSER_CLIENT() != null) {
                if (builder.getUSER_CLIENT().getSession() != null) {
                    serverController.get(server).stop();
                }
            }
            serverController.remove(server);
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
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        if (homeLabel != null)
            homeLabel.setText(lang.getString("label.home"));

        if (logoutButton != null)
            logoutButton.setText(lang.getString("button.logout"));

        if (stageTitleName != null && !stageTitleName.equals("") && stage != null) {
            stage.setTitle(lang.getString(stageTitleName));
        }
        CreateServerController.onLanguageChanged();
        PrivateViewController.onLanguageChanged();
        ServerViewController.onLanguageChanged();
    }

    public static void setStageTitle(String name) {
        ResourceBundle lang = StageManager.getLangBundle();
        stageTitleName = name;
        stage.setTitle(lang.getString(stageTitleName));
    }
}
