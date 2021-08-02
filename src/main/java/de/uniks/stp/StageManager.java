package de.uniks.stp;

import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.home.HomeViewController;
import de.uniks.stp.controller.login.LoginViewController;
import de.uniks.stp.controller.server.subcontroller.InviteUsersController;
import de.uniks.stp.controller.server.subcontroller.serversettings.ServerSettingsController;
import de.uniks.stp.controller.settings.SettingsController;
import de.uniks.stp.controller.settings.Spotify.SpotifyConnection;
import de.uniks.stp.controller.snake.SnakeGameController;
import de.uniks.stp.controller.snake.StartSnakeController;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.Constants;
import de.uniks.stp.util.LinePoolService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kong.unirest.Unirest;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;


public class StageManager extends Application {
    private static RestClient restClient;
    private static ModelBuilder builder;
    private static Stage stage;
    private static Stage subStage;
    private static HomeViewController homeViewController;
    private static LoginViewController loginViewController;
    private static SettingsController settingsController;
    private static Scene scene;
    private static ResourceBundle langBundle;
    private static String stageTitleName;
    private static String subStageTitleName;
    private static ServerSettingsController serverSettingsController;
    private static InviteUsersController inviteUsersController;
    private static StartSnakeController startSnakeController;
    private static SnakeGameController snakeGameController;
    private static SpotifyConnection spotifyConnection;

    @Override
    public void start(Stage primaryStage) {
        if (builder == null) {
            builder = new ModelBuilder();
        }
        if (restClient == null) {
            restClient = new RestClient();
        }
        langBundle = ResourceBundle.getBundle("de/uniks/stp/LangBundle");

        loadAppDir();
        languageSetup();

        LinePoolService linePoolService = new LinePoolService();
        linePoolService.init();
        builder.setLinePoolService(linePoolService);
        builder.loadSettings();

        // start application
        stage = primaryStage;
        stage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));
        stage.initStyle(StageStyle.TRANSPARENT);

        showLoginScreen();
        primaryStage.show();
    }

    private void loadAppDir() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);
    }

    /**
     * First check if there is a settings file already in user local directory - if not, create
     */
    private void languageSetup() {
        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;

        Properties prop = new Properties();
        File file = new File(path_to_config + Constants.SETTINGS_FILE);
        File dir = new File(path_to_config);
        if (!file.exists()) {
            try {
                dir.mkdirs();
                if (file.createNewFile()) {
                    FileOutputStream op = new FileOutputStream(path_to_config + Constants.SETTINGS_FILE);
                    prop.setProperty("LANGUAGE", "en");
                    prop.store(op, null);
                }
            } catch (Exception e) {
                System.out.println(e + "");
                e.printStackTrace();
            }
        }

        // load language from Settings
        prop = new Properties();
        try {
            String PATH_FILE_SETTINGS = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH + Constants.SETTINGS_FILE;
            FileInputStream ip = new FileInputStream(PATH_FILE_SETTINGS);
            prop.load(ip);
            Locale currentLocale = new Locale(prop.getProperty("LANGUAGE"));
            Locale.setDefault(currentLocale);
            resetLangBundle();
        } catch (Exception e) {
            System.err.println(e + "");
            e.printStackTrace();
        }
    }

    public static void setBuilder(ModelBuilder newBuilder) {
        builder = newBuilder;
    }

    public static void showLoginScreen() {
        cleanup();
        //show login screen
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/loginview/LoginScreenView.fxml")), getLangBundle());
            scene = new Scene(root);
            builder.setRestClient(restClient);
            builder.loadSettings();
            loginViewController = new LoginViewController(root, builder);
            loginViewController.init(stage);
            loginViewController.setTheme();
            setStageTitle("window_title_login");
            stage.setResizable(false);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setMinHeight(scene.getHeight());
            stage.setMinWidth(scene.getWidth());
        } catch (Exception e) {
            System.err.println("Error on showing LoginScreen");
            e.printStackTrace();
        }
    }

    public static void showHome() {
        cleanup();
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/homeview/HomeView.fxml")), getLangBundle());
            scene.setRoot(root);
            homeViewController = new HomeViewController(root, builder);
            builder.setHomeViewController(homeViewController);
            homeViewController.init(stage);
            homeViewController.setTheme();
            setStageTitle("window_title_home");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.sizeToScene();
            stage.setMinHeight(625);
            stage.setMinWidth(1020);
            stage.setOnCloseRequest(event -> stopAll());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws IOException {
        try {
            super.stop();
        } catch (Exception e) {
            System.err.println("Error while shutdown");
            e.printStackTrace();
        }
        cleanup();
    }


    private static void cleanup() {
        // call cascading stop
        if (loginViewController != null) {
            loginViewController.stop();
            loginViewController = null;
        }
        if (homeViewController != null) {
            homeViewController.stop();
            homeViewController = null;
        }
        if (builder.getSpotifyConnection() != null) {
            builder.getSpotifyConnection().stopPersonalScheduler();
            builder.getSpotifyConnection().stopDescriptionScheduler();
        }
    }

    private static void stopAll() {
        //automatic logout if application is closed
        if (!Objects.isNull(builder.getPersonalUser())) {
            String userKey = builder.getPersonalUser().getUserKey();
            if (userKey != null && !userKey.isEmpty()) {
                cleanup();
                Unirest.post("https://ac.uniks.de/api/users/logout").header("userKey", userKey).asJson().getBody();
                System.out.println("Logged out");
            }
        }
        Unirest.shutDown();
    }

    public static void showSettingsScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/settings/Settings.fxml")), getLangBundle());
            Scene scene = new Scene(root);

            // init controller
            settingsController = new SettingsController(builder, root);
            settingsController.init();
            settingsController.setTheme();

            subStage = new Stage();
            subStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            setSubStageTitle("window_title_settings");
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if (settingsController != null) {
                    settingsController.stop();
                    settingsController = null;
                }
            });
            subStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing Setting Screen");
            e.printStackTrace();
        }
    }

    public static void showServerSettingsScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/serversettings/ServerSettings.fxml")), getLangBundle());
            Scene scene = new Scene(root);

            // init controller
            serverSettingsController = new ServerSettingsController(root, builder, builder.getCurrentServer());
            serverSettingsController.init();
            serverSettingsController.setTheme();

            //setting stage settings
            subStage = new Stage();
            subStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            setSubStageTitle("window_title_serverSettings");
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if (serverSettingsController != null) {
                    serverSettingsController.stop();
                    serverSettingsController = null;
                }
            });
            subStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing ServerSetting Screen");
            e.printStackTrace();
        }
    }


    public static void showInviteUsersScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/serverview/invite users/inviteUsers.fxml")), getLangBundle());
            Scene scene = new Scene(root);

            // init controller
            inviteUsersController = new InviteUsersController(root, builder, builder.getCurrentServer());
            inviteUsersController.init();
            inviteUsersController.setTheme();

            subStage = new Stage();
            subStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            setSubStageTitle("window_title_inviteUsers");
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if (inviteUsersController != null) {
                    inviteUsersController.stop();
                    inviteUsersController = null;
                }
            });
            subStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing Setting Screen");
            e.printStackTrace();
        }
    }

    public static void showStartSnakeScreen() {
        try {
            // load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/snake/view/startSnakeView.fxml")), getLangBundle());
            Scene scene = new Scene(root);

            // init controller
            startSnakeController = new StartSnakeController(root, builder);
            startSnakeController.init();
            startSnakeController.setTheme();

            //start snake stage
            subStage = new Stage();
            subStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            subStage.setTitle("Snake");
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.setAlwaysOnTop(true);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if (startSnakeController != null) {
                    startSnakeController.stop();
                    startSnakeController = null;
                }
            });
            subStage.show();
        } catch (Exception e) {
            System.err.println("Error on showing Start Snake Screen");
            e.printStackTrace();
        }
    }

    public static void snakeScreen() {
        try {
            subStage.close();
            if (startSnakeController != null) {
                startSnakeController.stop();
                startSnakeController = null;
            }

            // load view
            Parent root = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/snake/view/snakeGameView.fxml")), getLangBundle());
            Scene scene = new Scene(root);

            // init controller
            snakeGameController = new SnakeGameController(scene, root, builder);
            snakeGameController.init();
            snakeGameController.setTheme();

            //start snake game stage
            subStage = new Stage();
            subStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream("icons/AccordIcon.png"))));

            subStage.setTitle("Snake");
            subStage.setResizable(false);
            subStage.setScene(scene);
            subStage.sizeToScene();
            subStage.centerOnScreen();
            subStage.initOwner(stage);
            subStage.initModality(Modality.WINDOW_MODAL);
            subStage.setOnCloseRequest(event -> {
                if (snakeGameController != null) {
                    snakeGameController.stop();
                    snakeGameController = null;
                }
            });
            subStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public SnakeGameController getSnakeGameController() {
        return snakeGameController;
    }

    public static void setRestClient(RestClient rest) {
        restClient = rest;
    }

    public ModelBuilder getBuilder() {
        return builder;
    }

    public HomeViewController getHomeViewController() {
        return homeViewController;
    }

    public LoginViewController getLoginViewController() {
        return loginViewController;
    }

    public static ResourceBundle getLangBundle() {
        return langBundle;
    }

    public static void setStageTitle(String name) {
        stageTitleName = name;
        stage.setTitle(getLangBundle().getString(stageTitleName));
    }

    public static void setSubStageTitle(String name) {
        subStageTitleName = name;
        subStage.setTitle(getLangBundle().getString(subStageTitleName));
    }

    public static void resetLangBundle() {
        langBundle = ResourceBundle.getBundle("de/uniks/stp/LangBundle");
    }

    /**
     * when language changed call every controller with view onLanguageChanged
     */
    public static void onLanguageChanged() {
        resetLangBundle();

        // Titles
        if (stageTitleName != null && !stageTitleName.equals("")) {
            stage.setTitle(getLangBundle().getString(stageTitleName));
        }
        if (subStageTitleName != null && !subStageTitleName.equals("")) {
            subStage.setTitle(getLangBundle().getString(subStageTitleName));
        }

        settingsController.onLanguageChanged();

        if (loginViewController != null) {
            loginViewController.onLanguageChanged();
        }
        if (homeViewController != null) {
            homeViewController.onLanguageChanged();
        }
        if (inviteUsersController != null) {
            inviteUsersController.onLanguageChanged();
        }
    }


    public static void setTheme() {
        if (homeViewController != null) {
            homeViewController.setTheme();
        }
        if (loginViewController != null) {
            loginViewController.setTheme();
        }
        if (settingsController != null) {
            settingsController.setTheme();
        }
    }
}
