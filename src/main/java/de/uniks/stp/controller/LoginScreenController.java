package de.uniks.stp.controller;

import de.uniks.stp.StageManager;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;
import java.io.*;
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
    private Label connectionLabel;
    private ModelBuilder builder;
    private Boolean temp = false;

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
            if(f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNextLine()) {
                    if (i == 0) {
                        usernameTextField.setText(scanner.nextLine());
                    }
                    if (i == 1) {
                        passwordTextField.setText(scanner.nextLine());
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
                if (rememberCheckBox.isSelected() && !temp) {
                    saveRememberMe(username, password);
                } else {
                    saveRememberMe("", "");
                }
                //login Post
                restClient.login(username, password, response -> {
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
            }
        } else if (tempUserCheckBox.isSelected()){
            saveRememberMe("", "");
            restClient.loginTemp(response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    //get name and password from server
                    String name = body.getObject().getJSONObject("data").getString("name");
                    String passw = body.getObject().getJSONObject("data").getString("password");
                    //show message on screen
                    this.message = body.getObject().getString("status");
                    //fill in username and password and login of tempUser
                    Platform.runLater(()-> {
                        errorLabel.setText("Login was a " + message);
                        usernameTextField.setText(name);
                        passwordTextField.setText(passw);
                        tempUserCheckBox.setSelected(false);
                        loginTempUser(name, passw);
                    });
                } else if (status.equals("failure")) {
                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> errorLabel.setText(message));
                }
            });

        }
    }

    private void loginTempUser(String username, String password) {
        //login Post
        restClient.login(username, password, response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                //build tempUser with key
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
                            new FileOutputStream( "saves/user.txt" )));
            out.write(username);
            out.newLine();
            out.write(password);
            out.close();
        } catch (Exception e) {
            System.out.println("Error while saving userdata.");
            e.printStackTrace();
        }
    }
}
