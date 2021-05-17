package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class LoginScreenController {
    private Parent root;
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
    private Boolean netConnection = false;
    private Label connectionLabel;
    private ModelBuilder builder;

    public LoginScreenController(Parent root, ModelBuilder builder) {
        this.restClient = new RestClient();
        this.root = root;
        this.builder = builder;
    }

    public void init() {
        this.usernameTextField = (TextField) root.lookup("#usernameTextfield");
        this.passwordTextField = (PasswordField) root.lookup("#passwordTextField");
        this.rememberCheckBox = (CheckBox) root.lookup("#rememberMeCheckbox");
        this.tempUserCheckBox = (CheckBox) root.lookup("#loginAsTempUser");
        this.loginButton = (Button) root.lookup("#loginButton");
        this.signInButton = (Button) root.lookup("#signinButton");
        this.settingsButton = (Button) root.lookup("#settingsButton");
        this.errorLabel = (Label) root.lookup("#errorLabel");
        this.connectionLabel = (Label) root.lookup("#connectionLabel");
        this.connectionLabel.setWrapText(true);

        //Save last username and password that wanted to be remembered in file
        File f = new File("saves/user.txt");
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
        //Buttons
        this.loginButton.setOnAction(this::loginButtonOnClick);
        this.signInButton.setOnAction(this::signInButtonOnClick);
        this.settingsButton.setOnAction(this::settingsButtonOnClick);
    }

    private void settingsButtonOnClick(ActionEvent actionEvent) {
        StageManager.showSettingsScreen();
    }

    private void signInButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        //check if username or password is missing
        if (!tempUserCheckBox.isSelected()) {
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Field is empty!");
            } else {
                //if remember me selected then username and password is saved in a user.txt
                if (rememberCheckBox.isSelected()) {
                    saveRememberMe(username, password);
                } else {
                    saveRememberMe("", "");
                }
                //signIn Post
                restClient.signIn(username, password, response -> {
                    netConnection = true;
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        //show message on screen
                        this.message = body.getObject().getString("message");
                        Platform.runLater(() -> errorLabel.setText("Sign in was a " + message));
                    } else if (status.equals("failure")) {
                        //show message on screen
                        this.message = body.getObject().getString("message");
                        Platform.runLater(() -> errorLabel.setText(message));
                    }
                });
                noConnection();
            }
        } else if (tempUserCheckBox.isSelected()) {
            errorLabel.setText("Click on Login");
        }
    }

    private void loginButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();
        if (!tempUserCheckBox.isSelected()) {
            //if remember me selected then username and password is saved in a user.txt
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Field is empty!");
            } else {
                if (rememberCheckBox.isSelected()) {
                    saveRememberMe(username, password);
                } else {
                    saveRememberMe("", "");
                }
                //login Post
                restClient.login(username, password, response -> {
                    netConnection = true;
                    JsonNode body = response.getBody();
                    String status = body.getObject().getString("status");
                    if (status.equals("success")) {
                        //build user with key
                        String userkey = body.getObject().getJSONObject("data").getString("userKey");
                        builder.buildPersonalUser(username, userkey);
                        //show message on screen
                        this.message = body.getObject().getString("status");
                        Platform.runLater(() -> errorLabel.setText("Login was a " + message));
                        Platform.runLater(StageManager::showHome);
                    } else if (status.equals("failure")) {
                        //show message on screen
                        this.message = body.getObject().getString("message");
                        Platform.runLater(() -> errorLabel.setText(message));
                    }
                });
                noConnection();
            }
        } else if (tempUserCheckBox.isSelected()) {
            saveRememberMe("", "");
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
                        errorLabel.setText("Login was a " + message);
                        usernameTextField.setText(name);
                        passwordTextField.setText(passw);
                        tempUserCheckBox.setSelected(false);
                        loginButtonOnClick(actionEvent);
                    });
                } else if (status.equals("failure")) {
                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> errorLabel.setText(message));
                }
            });
            noConnection();
        }
    }
    public void noConnection() {
        //no internet connection
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!netConnection) {
            Platform.runLater(() -> this.connectionLabel.setText("No connection - \nPlease check your connection and try again "));
        }
        netConnection = false;
    }

    public void stop() {
        this.signInButton.setOnAction(null);
        this.loginButton.setOnAction(null);
        this.settingsButton.setOnAction(null);
    }

    public void saveRememberMe(String username, String password) {
        try {
            //save username and password in text file
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream("saves/user.txt")));
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

    public String encode(String password) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(password.getBytes());
    }

    public static String decode(String str){
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(str);
        return new String(bytes);
    }
}
