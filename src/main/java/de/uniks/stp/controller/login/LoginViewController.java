package de.uniks.stp.controller.login;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.titlebar.TitleBarController;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.Constants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import kong.unirest.JsonNode;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.*;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginViewController {
    private final Parent root;
    private final RestClient restClient;
    private final ModelBuilder builder;
    public boolean noConnectionTest;
    private TextField usernameTextField;
    private PasswordField passwordTextField;
    private CheckBox rememberCheckBox;
    private CheckBox tempUserCheckBox;
    private Button loginButton;
    private Button signInButton;
    private Button settingsButton;
    private Label errorLabel;
    private String message;
    private Label connectionLabel;
    private String error;
    private String connectionError;
    private TitleBarController titleBarController;

    public LoginViewController(Parent root, ModelBuilder builder) {
        this.restClient = builder.getRestClient();
        this.root = root;
        this.builder = builder;
    }

    public void init(Stage stage) {
        // create titleBar
        HBox titleBarBox = (HBox) root.lookup("#titleBarBox");
        Parent titleBarView = null;
        try {
            titleBarView = FXMLLoader.load(Objects.requireNonNull(StageManager.class.getResource("controller/titlebar/TitleBarView.fxml")), builder.getStageManager().getLangBundle());
        } catch (IOException e) {
            e.printStackTrace();
        }
        titleBarBox.getChildren().add(titleBarView);
        titleBarController = new TitleBarController(stage, titleBarView, builder);
        titleBarController.init();
        titleBarController.setTheme();
        titleBarController.setMaximizable(false);
        titleBarController.setTitle("Accord");
        stage.setTitle("Accord");

        usernameTextField = (TextField) root.lookup("#usernameTextfield");
        passwordTextField = (PasswordField) root.lookup("#passwordTextField");
        rememberCheckBox = (CheckBox) root.lookup("#rememberMeCheckbox");
        tempUserCheckBox = (CheckBox) root.lookup("#loginAsTempUser");
        loginButton = (Button) root.lookup("#loginButton");
        signInButton = (Button) root.lookup("#signinButton");
        this.settingsButton = (Button) root.lookup("#settingsButton");
        errorLabel = (Label) root.lookup("#errorLabel");
        connectionLabel = (Label) root.lookup("#connectionLabel");
        connectionLabel.setWrapText(true);
        //clear error message
        error = "";

        //Get last username and password that wanted to be remembered in file
        setup();

        //Buttons
        loginButton.setOnAction(this::loginButtonOnClick);
        signInButton.setOnAction(this::signInButtonOnClick);
        this.settingsButton.setOnAction(this::settingsButtonOnClick);
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        Platform.runLater(() -> builder.getStageManager().showSettingsScreen());
    }

    /**
     * sign in
     */
    private void signInButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        Matcher whiteSpaceMatcher = Pattern.compile("^( )*$").matcher(username);

        try {
            //check if username or password is missing
            if (!tempUserCheckBox.isSelected()) {
                if (username.isEmpty() || password.isEmpty() || whiteSpaceMatcher.find()) {
                    setError("error.field_is_empty");
                } else {
                    signIn(username, password);
                }
            } else if (tempUserCheckBox.isSelected()) {
                setError("error.click_on_login");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host: connect")) {
                setConnectionError("error.create_server_no_connection");
            }
        }
    }

    private void signIn(String username, String password) {
        //if remember me selected then username and password is saved in a user.txt
        if (rememberCheckBox.isSelected()) {
            saveRememberMe(username, password, true, false);
        } else {
            saveRememberMe("", "", false, false);
        }
        //signIn Post
        if (!noConnectionTest) {
            restClient.signIn(username, password, response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    //show message on screen
                    this.message = body.getObject().getString("message");
                    Platform.runLater(() -> setError("error.sign_in_success"));
                } else if (status.equals("failure")) {
                    //show message on screen
                    this.message = body.getObject().getString("message");
                    if (message.equals("Name already taken")) {
                        Platform.runLater(() -> setError("error.name_already_taken"));
                    } else {
                        Platform.runLater(() -> setError("error.sign_in_failure"));
                    }
                }
            });
        }
    }

    /**
     * login
     */
    private void loginButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        try {
            if (!tempUserCheckBox.isSelected()) {
                //if remember me selected then username and password is saved in a user.txt
                if (username.isEmpty() || password.isEmpty()) {
                    setError("error.field_is_empty");
                } else {
                    loginUser(username, password);
                }
            } else if (tempUserCheckBox.isSelected()) {
                loginTempUser();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host: connect")) {
                setConnectionError("error.login_no_connection");
            }
        }
    }

    private void loginTempUser() {
        saveRememberMe("", "", false, true);
        if (!noConnectionTest) {
            restClient.loginTemp(response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    //get name and password from server
                    String name = body.getObject().getJSONObject("data").getString("name");
                    String pass = body.getObject().getJSONObject("data").getString("password");
                    //show message on screen
                    this.message = body.getObject().getString("status");
                    //fill in username and password and login of tempUser
                    Platform.runLater(() -> {
                        setError("error.login_success");
                        usernameTextField.setText(name);
                        passwordTextField.setText(pass);
                    });
                    if (rememberCheckBox.isSelected()) {
                        saveRememberMe(name, pass, true, true);
                    } else {
                        saveRememberMe("", "", false, true);
                    }
                    //login Post
                    restClient.login(name, pass, responseLogin -> {
                        JsonNode bodyLogin = responseLogin.getBody();
                        String statusLogin = bodyLogin.getObject().getString("status");
                        test(statusLogin, bodyLogin, name, pass);
                    });
                } else if (status.equals("failure")) {
                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> setError("error.login_failure"));
                }
            });
        }
    }

    private void test(String status, JsonNode body, String userName, String password) {
        if (status.equals("success")) {
            //build user with key
            String userKey = body.getObject().getJSONObject("data").getString("userKey");
            builder.buildPersonalUser(userName, password, userKey);
            //show message on screen
            this.message = body.getObject().getString("status");
            Platform.runLater(() -> setError("error.login_success"));
            Platform.runLater(() -> builder.getStageManager().showHome()); //TODO load here server, then showHome
        } else if (status.equals("failure")) {
            //show message on screen
            this.message = body.getObject().getString("message");
            if (message.equals("Invalid credentials")) {
                Platform.runLater(() -> setError("error.invalid_credentials"));
            } else {
                Platform.runLater(() -> setError("error.login_failure"));
            }
        }
    }


    private void loginUser(String username, String password) {
        if (rememberCheckBox.isSelected()) {
            saveRememberMe(username, password, true, false);
        } else {
            saveRememberMe("", "", false, false);
        }
        //login Post
        if (!noConnectionTest) {
            restClient.login(username, password, response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                test(status, body, username, password);
            });
        }
    }

    public void stop() {
        signInButton.setOnAction(null);
        loginButton.setOnAction(null);
        this.settingsButton.setOnAction(null);
    }

    /**
     * save username and password in text file
     */
    public void saveRememberMe(String username, String password, Boolean rememberMe, Boolean tempCheckBox) {
        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;
        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(path_to_config + Constants.USERDATA_FILE)));
            out.write(username);
            out.newLine();
            String encodedPassword = encode(password);
            out.write(encodedPassword);
            out.newLine();
            out.write(rememberMe.toString());
            out.newLine();
            out.write(tempCheckBox.toString());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * First check if there is a userData file already in user local directory - if not, create
     */
    public void setup() {
        if (!builder.getLoadUserData()) {
            return;
        }

        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);

        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;
        File f = new File(path_to_config + Constants.USERDATA_FILE);
        if (f.exists() && !f.isDirectory()) {
            setupUI(f);
        }
    }

    private void setupUI(File f) {
        try {
            Scanner scanner = new Scanner(f);
            int i = 0;
            while (scanner.hasNextLine()) {
                if (i == 0) {
                    usernameTextField.setText(scanner.nextLine());
                }
                if (i == 1) {
                    passwordTextField.setText(decode(scanner.nextLine()));
                }
                if (i == 2) {
                    rememberCheckBox.setSelected(Boolean.parseBoolean(scanner.nextLine()));
                }
                if (i == 3) {
                    tempUserCheckBox.setSelected(Boolean.parseBoolean(scanner.nextLine()));
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * encode password
     */
    public String encode(String password) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(password.getBytes());
    }

    /**
     * decode password
     */
    public String decode(String str) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(str);
        return new String(bytes);
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        usernameTextField.setPromptText(lang.getString("textField.prompt_username"));
        passwordTextField.setPromptText(lang.getString("textField.prompt_password"));
        rememberCheckBox.setText(lang.getString("checkbox.remember_me"));
        tempUserCheckBox.setText(lang.getString("checkbox.login_temp_user"));
        loginButton.setText(lang.getString("button.login"));
        signInButton.setText(lang.getString("button.signIn"));

        if (error != null && !error.equals("")) {
            errorLabel.setText(lang.getString(error));
        }

        if (connectionError != null && !connectionError.equals("")) {
            connectionLabel.setText(lang.getString(connectionError));
        }
    }

    /**
     * set the error text in label placeholder
     *
     * @param errorMsg the error text
     */
    private void setError(String errorMsg) {
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        error = errorMsg;
        errorLabel.setText(lang.getString(error));
    }

    /**
     * set the connection error text in label placeholder
     *
     * @param connectionErrorMsg the connection error text
     */
    private void setConnectionError(String connectionErrorMsg) {
        ResourceBundle lang = builder.getStageManager().getLangBundle();
        connectionError = connectionErrorMsg;
        connectionLabel.setText(lang.getString(connectionError));
    }


    public void setTheme() {
        if (builder.getTheme().equals("Bright")) {
            setWhiteMode();
        } else {
            setDarkMode();
        }
    }

    private void setWhiteMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/bright/Login.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Login.css")).toExternalForm());
        if (titleBarController != null) {
            titleBarController.setTheme();
        }
    }

    public void setNoConnectionTest(boolean noConnectionTestState) {
        this.noConnectionTest = noConnectionTestState;
    }

    public Parent getLoginView() {
        return this.root;
    }
}
