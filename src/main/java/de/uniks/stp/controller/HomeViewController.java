package de.uniks.stp.controller;

import de.uniks.stp.AlternateServerListCellFactory;
import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.subcontroller.CreateServerController;
import de.uniks.stp.model.Channel;
import de.uniks.stp.model.Server;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

import java.io.IOException;
import java.util.ArrayList;

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
    private Button logoutButton;
    private Stage stage;
    private ModelBuilder builder;
    private AlternateServerListCellFactory serverListCellFactory;
    private static Channel selectedChat;
    private PrivateViewController privateViewController;
    private ServerViewController serverController;
    private Parent privateView;

    public HomeViewController(Parent view, ModelBuilder modelBuilder) {
        this.view = view;
        this.builder = modelBuilder;
        this.restClient = new RestClient();
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
        logoutButton = (Button) view.lookup("#logoutButton");
        addServer = (Circle) view.lookup("#addServer");
        addServer.setOnMouseClicked(this::onshowCreateServer);
        serverList = (ListView<Server>) scrollPaneServerBox.getContent().lookup("#serverList");
        serverListCellFactory = new AlternateServerListCellFactory();
        serverList.setCellFactory(serverListCellFactory);
        this.serverList.setOnMouseReleased(this::onServerClicked);
        this.settingsButton.setOnAction(this::settingsButtonOnClicked);
        this.logoutButton.setOnAction(this::logoutButtonOnClicked);
        this.homeButton.setOnMouseClicked(this::homeButtonClicked);
        showPrivateView();
        showServers();
    }

    /**
     * Shows the private home view to have a private chat with other users.
     */
    private void showPrivateView() {
        try {
            if (privateView == null) {
                privateView = FXMLLoader.load(StageManager.class.getResource("PrivateView.fxml"));
                privateViewController = new PrivateViewController(privateView, builder);
                privateViewController.init();
                this.root.getChildren().clear();
                this.root.getChildren().add(privateView);
            }
            else {
                this.privateViewController.showUsers();
                this.root.getChildren().clear();
                this.root.getChildren().add(privateView);

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
        try {
            Parent root = FXMLLoader.load(StageManager.class.getResource("ServerView.fxml"));
            serverController = new ServerViewController(root, builder, builder.getCurrentServer());
            serverController.init();
            this.root.getChildren().clear();
            this.root.getChildren().add(root);
        } catch (IOException e) {
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
            Parent root = FXMLLoader.load(StageManager.class.getResource("controller/CreateServerView.fxml"));
            Scene scene = new Scene(root);
            CreateServerController createServerController = new CreateServerController(root, builder);
            createServerController.init();
            stage = new Stage();
            createServerController.showCreateServerView(this::onServerCreated);
            stage.setTitle("Create a new Server");
            stage.setScene(scene);
            stage.show();
            updateServerListColor();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the createServerStage and calls showServerView. Is
     * called after the ok button in createServer is clicked
     */
    public void onServerCreated() {
        Platform.runLater(() -> {
            stage.close();
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
            showServers();
            updateServerListColor();
            showServerView();
        });
    }

    /**
     * Sets the clicked Server as currentServer and calls showServerView.
     *
     * @param mouseEvent is called when clicked on a Server
     */
    private void onServerClicked(MouseEvent mouseEvent) {
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

    /**
     * Get Servers and show Servers
     */
    private void showServers() {
        if (!builder.getPersonalUser().getUserKey().equals("")) {
            restClient.getServers(builder.getPersonalUser().getUserKey(), response -> {
                JSONArray jsonResponse = response.getBody().getObject().getJSONArray("data");
                //List to track the online users in order to remove old users that are now offline
                ArrayList<Server> onlineServers = new ArrayList<>();
                for (int i = 0; i < jsonResponse.length(); i++) {
                    String serverName = jsonResponse.getJSONObject(i).get("name").toString();
                    String serverId = jsonResponse.getJSONObject(i).get("id").toString();
                    Server server = builder.buildServer(serverName, serverId);
                    onlineServers.add(server);
                }
                for (Server server : builder.getPersonalUser().getServer()) {
                    if (!onlineServers.contains(server)) {
                        builder.getPersonalUser().withoutServer(server);
                    }
                }
                Platform.runLater(() -> serverList.setItems(FXCollections.observableList(builder.getPersonalUser().getServer())));
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
        this.logoutButton.setOnAction(null);
        if (stage != null) {
            this.stage.close();
            stage = null;
        }
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
            if (builder.getSERVER_USER() != null) {
                if (builder.getSERVER_USER().getSession() != null) {
                    builder.getSERVER_USER().stop();
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
        JsonNode body = Unirest.post(Constants.REST_SERVER_URL + Constants.API_PREFIX + Constants.LOGOUT_PATH).header(Constants.COM_USERKEY, builder.getPersonalUser().getUserKey()).asJson().getBody();
        JSONObject result = body.getObject();
        if (result.get("status").equals("success")) {
            System.out.println(result.get("message"));
            Platform.runLater(StageManager::showLoginScreen);
        }
    }

    private void cleanup() {
        if (privateViewController != null) {
            privateViewController.stop();
            privateViewController = null;
        }
        if (serverController != null) {
            serverController.stop();
            serverController = null;
        }
    }

    public ServerViewController getServerController() {
        return serverController;
    }
}
