package de.uniks.stp.controller.login;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import de.uniks.stp.util.Constants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;
import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;

public class LoginViewController {
    private final Parent root;
    private TextField usernameTextField;
    private PasswordField passwordTextField;
    private CheckBox rememberCheckBox;
    private CheckBox tempUserCheckBox;
    private Button loginButton;
    private Button signInButton;
    private Button settingsButton;
    private Label errorLabel;
    private String message;
    private final RestClient restClient;
    private Label connectionLabel;
    private final ModelBuilder builder;
    private String error;
    private String connectionError;
    public boolean noConnectionTest;

    public LoginViewController(Parent root, ModelBuilder builder) {
        this.restClient = builder.getRestClient();
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
        StageManager.showSettingsScreen();
    }

    /**
     * sign in
     */
    private void signInButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        try {
            //check if username or password is missing
            if (!tempUserCheckBox.isSelected()) {
                if (username.isEmpty() || password.isEmpty()) {
                    setError("error.field_is_empty");
                } else {
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
                            if (status.equals("success")) {
                                //build user with key
                                String userKey = body.getObject().getJSONObject("data").getString("userKey");
                                builder.buildPersonalUser(username, password, userKey);
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
                }
            } else if (tempUserCheckBox.isSelected()) {
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
                                if (statusLogin.equals("success")) {
                                    //build user with key
                                    String userKey = bodyLogin.getObject().getJSONObject("data").getString("userKey");
                                    builder.buildPersonalUser(name, pass, userKey);
                                    //show message on screen
                                    this.message = bodyLogin.getObject().getString("status");
                                    Platform.runLater(() -> setError("error.login_success"));
                                    Platform.runLater(StageManager::showHome); //TODO load here server, then showHome
                                } else if (statusLogin.equals("failure")) {
                                    //show message on screen
                                    this.message = bodyLogin.getObject().getString("message");
                                    if (message.equals("Invalid credentials")) {
                                        Platform.runLater(() -> setError("error.invalid_credentials"));
                                    } else {
                                        Platform.runLater(() -> setError("error.login_failure"));
                                    }
                                }
                            });
                        } else if (status.equals("failure")) {
                            //show message on screen
                            this.message = body.getObject().getString("status");
                            Platform.runLater(() -> setError("error.login_failure"));
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().equals("java.net.NoRouteToHostException: No route to host: connect")) {
                setConnectionError("error.login_no_connection");
            }
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
            System.out.println("Error while saving userdata.");
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
                    if (i == 2) {
                        rememberCheckBox.setSelected(Boolean.parseBoolean(scanner.nextLine()));
                    }
                    if (i == 3) {
                        tempUserCheckBox.setSelected(Boolean.parseBoolean(scanner.nextLine()));
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
    public String decode(String str) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(str);
        return new String(bytes);
    }

    /**
     * when language changed reset labels and texts with correct language
     */
    public void onLanguageChanged() {
        ResourceBundle lang = StageManager.getLangBundle();
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
    }

    private void setDarkMode() {
        root.getStylesheets().clear();
        root.getStylesheets().add(Objects.requireNonNull(StageManager.class.getResource("styles/themes/dark/Login.css")).toExternalForm());
    }

    public void setNoConnectionTest(boolean noConnectionTestState) {
        this.noConnectionTest = noConnectionTestState;
    }
}
