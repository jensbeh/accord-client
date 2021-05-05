package de.uniks.stp.controller;

import de.uniks.stp.model.RootDataModel;
import de.uniks.stp.model.User;
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
    private Label errorlabel;
    private String message;
    private final RestClient restClient;
    private User user;
    private Boolean netConnection = false;
    private Label connectionLabel;

    public LoginScreenController(Parent root) {
        this.restClient = new RestClient();
        this.root = root;
    }

    public void init() {
        this.usernameTextField = (TextField) root.lookup("#usernameTextfield");
        this.passwordTextField = (PasswordField) root.lookup("#passwordTextField");
        this.rememberCheckBox = (CheckBox) root.lookup("#rememberMeCheckbox");
        this.tempUserCheckBox = (CheckBox) root.lookup("#loginAsTempUser");
        this.loginButton = (Button) root.lookup("#loginButton");
        this.signInButton = (Button) root.lookup("#signinButton");
        this.errorlabel = (Label) root.lookup("#errorLabel");
        this.connectionLabel = (Label) root.lookup("#connectionLabel");
        this.connectionLabel.setWrapText(true);

        //Save last username and password that wanted to be remembered in file
        File f = new File("saves/user.txt");
        try {
            if(f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNext()) {
                    if (i == 0) {
                        usernameTextField.setText(scanner.next());
                    }
                    if (i == 1) {
                        passwordTextField.setText(scanner.next());
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
    }

    private void signInButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        //check if username or password is missing
        if (username.isEmpty() || password.isEmpty()) {
            errorlabel.setText("Field is empty!");
        } else {

            //if remember me selected then username and password is saved in a list
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

                    //create new User
                    user = new User();
                    user.setName(username);
                    user.setStatus(true);

                    //show message on screen
                    this.message = body.getObject().getString("message");
                    Platform.runLater(() -> errorlabel.setText(message));

                } else if (status.equals("failure")) {

                    //show message on screen
                    this.message = body.getObject().getString("message");
                    Platform.runLater(() -> errorlabel.setText(message));
                }
            });
            //login Post
            restClient.login(username, password, response -> {
                netConnection = true;
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {
                    String userkey = body.getObject().getJSONObject("data").getString("userKey");
                    RootDataModel.setKey(userkey); //for test
                    user.setUserKey(userkey);

                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> errorlabel.setText(message));
                } else if (status.equals("failure")) {
                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> errorlabel.setText(message));
                }
            });
        }
        if (!netConnection) {
            Platform.runLater(() -> this.connectionLabel.setText("No internet connection - \nPlease check your connection and try again "));
        }
    }

    private void loginButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        if (!tempUserCheckBox.isSelected()) {
            //if remember me selected then username and password is saved in a listi
            if (username.isEmpty() || password.isEmpty()) {
                errorlabel.setText("Field is empty!");
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
                        String userkey = body.getObject().getJSONObject("data").getString("userKey");
                        RootDataModel.setKey(userkey); //for test

                        //show message on screen
                        this.message = body.getObject().getString("status");
                        Platform.runLater(() -> errorlabel.setText(message));
                        Platform.runLater(() -> connectionLabel.setText(""));
                    } else if (status.equals("failure")) {

                        //show message on screen
                        this.message = body.getObject().getString("message");
                        Platform.runLater(() -> errorlabel.setText(message));
                    }
                });
            }
        } else if (tempUserCheckBox.isSelected()){
            restClient.loginTemp(response -> {
                netConnection = true;
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {

                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> errorlabel.setText(message));
                } else if (status.equals("failure")) {

                    //show message on screen
                    this.message = body.getObject().getString("status");
                    Platform.runLater(() -> errorlabel.setText(message));
                }
            });
        }
        if (!netConnection) {
            Platform.runLater(() -> this.connectionLabel.setText("No internet connection - \nPlease check your connection and try again "));
        }
    }

    public void stop() {
        this.signInButton.setOnAction(null);
        this.loginButton.setOnAction(null);
    }

    public void saveRememberMe(String username, String password) {
        File f = new File("saves/user.txt");
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
