package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import util.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.Scanner;

public class LoginScreenController {
    private Parent root;
    private static TextField usernameTextField;
    private static PasswordField passwordTextField;
    private static CheckBox rememberCheckBox;
    private static CheckBox tempUserCheckBox;
    private static Button loginButton;
    private static Button signInButton;
    private Button settingsButton;
    private static Label errorLabel;
    private String message;
    private final RestClient restClient;
    private Boolean netConnection = false;
    private static Label connectionLabel;
    private ModelBuilder builder;
    private static String error;
    private static String connectionError;
    public static boolean noConnectionTest;

    public LoginScreenController(Parent root, ModelBuilder builder) {
        this.restClient = new RestClient();
        this.root = root;
        this.builder = builder;
    }

    public void init() {
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

        //Get last username and password that wanted to be remembered in file
        setup();

        //Buttons
        this.loginButton.setOnAction(this::loginButtonOnClick);
        this.signInButton.setOnAction(this::signInButtonOnClick);
        this.settingsButton.setOnAction(this::settingsButtonOnClick);
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }

    /**
     * sign in
     */
    private void signInButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        //check if username or password is missing
        if (!tempUserCheckBox.isSelected()) {
            if (username.isEmpty() || password.isEmpty()) {
                setError("error.field_is_empty");
            } else {
                //if remember me selected then username and password is saved in a user.txt
                if (rememberCheckBox.isSelected()) {
                    saveRememberMe(username, password);
                } else {
                    saveRememberMe("", "");
                }
                //signIn Post
                if (!noConnectionTest) {
                    restClient.signIn(username, password, response -> {
                        netConnection = true;
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
                noConnection();
            }
        } else if (tempUserCheckBox.isSelected()) {
            setError("error.click_on_login");
        }
    }

    /**
     * login
     */
    private void loginButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        if (!tempUserCheckBox.isSelected()) {
            //if remember me selected then username and password is saved in a user.txt
            if (username.isEmpty() || password.isEmpty()) {
                setError("error.field_is_empty");
            } else {
                if (rememberCheckBox.isSelected()) {
                    saveRememberMe(username, password);
                } else {
                    saveRememberMe("", "");
                }
                //login Post
                if (!noConnectionTest) {
                    restClient.login(username, password, response -> {
                        netConnection = true;
                        JsonNode body = response.getBody();
                        String status = body.getObject().getString("status");
                        if (status.equals("success")) {
                            //build user with key
                            String userkey = body.getObject().getJSONObject("data").getString("userKey");
                            builder.buildPersonalUser(username, password, userkey);
                            //show message on screen
                            this.message = body.getObject().getString("status");
                            Platform.runLater(() -> setError("error.login_success"));
                            Platform.runLater(StageManager::showHome); //TODO load here server, then showHome
                        } else if (status.equals("failure")) {
                            //show message on screen
                            this.message = body.getObject().getString("message");
                            if (message.equals("Invalid credentials")) {
                                Platform.runLater(() -> setError("error.invalid_credentials"));
                            } else {
                                Platform.runLater(() -> setError("error.login_failure"));
                            }
                        }
                    });
                }
                noConnection();
            }
        } else if (tempUserCheckBox.isSelected()) {
            saveRememberMe("", "");
            if (!noConnectionTest) {
                restClient.loginTemp(response -> {
                    netConnection = true;
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        //get name and password from server
                        String name = body.getObject().getJSONObject("data").getString("name");
                        String passw = body.getObject().getJSONObject("data").getString("password");
                        //show message on screen
                        this.message = body.getObject().getString("status");
                        //fill in username and password and login of tempUser
                        Platform.runLater(() -> {
                            setError("error.login_success");
                            usernameTextField.setText(name);
                            passwordTextField.setText(passw);
                            tempUserCheckBox.setSelected(false);
                            loginButtonOnClick(actionEvent);
                        });
                    } else if (status.equals("failure")) {
                        //show message on screen
                        this.message = body.getObject().getString("status");
                        Platform.runLater(() -> setError("error.login_failure"));
                    }
                });
            }
            noConnection();
        }
    }

    /**
     * Change Text on no connection Label
     */
    public void noConnection() {
        //no internet connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!netConnection) {
            Platform.runLater(() -> setConnectionError("error.login_no_connection"));
        }
        netConnection = false;
    }

    public void stop() {
        this.signInButton.setOnAction(null);
        this.loginButton.setOnAction(null);
        this.settingsButton.setOnAction(null);
    }

    /**
     * save username and password in text file
     */
    public void saveRememberMe(String username, String password) {
        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;
        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(path_to_config + Constants.USERDATA_FILE)));
            out.write(username);
            out.newLine();
            String encodedPassword = encode(password);
            out.write(encodedPassword);
            out.close();
        } catch (Exception e) {
            System.out.println("Error while saving userdata.");
            e.printStackTrace();
        }
    }

    /**
     * First check if there is a userData file already in user local directory - if not, create
     */
    public static void setup() {
        AppDirs appDirs = AppDirsFactory.getInstance();
        Constants.APPDIR_ACCORD_PATH = appDirs.getUserConfigDir("Accord", null, null);

        String path_to_config = Constants.APPDIR_ACCORD_PATH + Constants.CONFIG_PATH;
        File f = new File(path_to_config + Constants.USERDATA_FILE);
        try {
            if (f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNextLine()) {
                    if (i == 0) {
                        usernameTextField.setText(scanner.nextLine());
                    }
                    if (i == 1) {
                        passwordTextField.setText(decode(scanner.nextLine()));
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Error while reading!");
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
    public static String decode(String str) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(str);
        return new String(bytes);
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public static void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
        usernameTextField.setPromptText(lang.getString("textfield.prompt_username"));
        passwordTextField.setPromptText(lang.getString("textfield.prompt_password"));
        rememberCheckBox.setText(lang.getString("checkbox.remember_me"));
        tempUserCheckBox.setText(lang.getString("checkbox.login_temp_user"));
        loginButton.setText(lang.getString("button.login"));
        signInButton.setText(lang.getString("button.signin"));

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
        ResourceBundle lang = StageManager.getLangBundle();
        error = errorMsg;
        errorLabel.setText(lang.getString(error));
    }

    /**
     * set the connection error text in label placeholder
     *
     * @param connectionErrorMsg the connection error text
     */
    private void setConnectionError(String connectionErrorMsg) {
        ResourceBundle lang = StageManager.getLangBundle();
        connectionError = connectionErrorMsg;
        connectionLabel.setText(lang.getString(connectionError));
    }
}
