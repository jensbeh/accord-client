package de.uniks.stp.controller;

import de.uniks.stp.model.User;
import de.uniks.stp.net.RestClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.control.*;
import kong.unirest.JsonNode;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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
    private String usernameRe;
    private String passwordRe;
    private final RestClient restClient;

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

        //Save last username and password that wanted to be remembered in file
        File f = new File("saves/user.txt");
        try {
            if(f.exists() && !f.isDirectory()) {
                Scanner scanner = new Scanner(f);
                int i = 0;
                while (scanner.hasNext()) {
                    if (i == 1) {
                        passwordTextField.setText(scanner.next());
                    }
                    if (i == 0) {
                        usernameTextField.setText(scanner.next());
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
        if (usernameTextField.getText().isEmpty() || passwordTextField.getText().isEmpty()) {
            errorlabel.setText("Field is empty!");
        } else {
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();

            //if remember me selected then username and password is saved in a list
            if (rememberCheckBox.isSelected()) {
                usernameRe = username;
                passwordRe = password;
                saveRememberMe(username, password);
            }
            //signIn Post
            restClient.signIn(username, password, response -> {
                JsonNode body = response.getBody();
                String status = body.getObject().getString("status");
                if (status.equals("success")) {

                    //create new User
                    User user = new User();
                    user.setName(username);
                    user.setStatus(true);
                } else if (status.equals("failure")) {

                    // message doesn't show on screen
                    this.message = body.getObject().getString("message");
                    System.out.println(body.getObject().getString("message"));
                }
            });
            Platform.runLater(() -> errorlabel.setText("" + message));
        }
    }

    private void loginButtonOnClick(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        //if remember me selected then username and password is saved in a list
        if (rememberCheckBox.isSelected()) {
            usernameRe = username;
            passwordRe = password;
            saveRememberMe(username, password);
        }

        //login Post
        restClient.login(username, password, response -> {
            JsonNode body = response.getBody();
            String status = body.getObject().getString("status");
            if (status.equals("success")) {
                String userkey = body.getObject().getString("userKey");
            } else if (status.equals("failure")) {
                System.out.println(body.getObject().getString("message"));
            }
        });
    }

    public void stop() {
        this.signInButton.setOnAction(null);
        this.loginButton.setOnAction(null);
    }

    public void saveRememberMe(String username, String password) {
        //delete old data
        System.out.println("Hallo");
        File f = new File("saves/user.txt");
        try {
            System.out.println("Hallo1");
            if(f.exists() && !f.isDirectory()) {
                f.delete();
                System.out.println("Hallo2");
            }
        } catch (Exception e) {
            System.err.println("Error while deleting ");
            e.printStackTrace();
        }
        f = new File("saves/user.txt");
        System.out.println("Hallo3");
        if (f.exists()) {
            System.out.println("Ordner existiert.");
            System.out.println("Hallo4");
        }
        //File file = new File(System.getProperty());


        try {
            //save username and password in text file
            FileWriter fw = new FileWriter(f);
            fw.write(username);
            System.out.println(username);
            fw.write(System.getProperty("line.separator"));
            fw.write(password);
            System.out.println(password);
        } catch (Exception e) {
            System.out.println("Error while saving userdata.");
            e.printStackTrace();
        }
    }
}
