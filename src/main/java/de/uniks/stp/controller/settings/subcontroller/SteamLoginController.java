package de.uniks.stp.controller.settings.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import kong.unirest.JsonNode;

public class SteamLoginController {
    private ModelBuilder builder;
    private WebView webView;
    private Stage popUp;

    public SteamLoginController(ModelBuilder builder) {
        this.builder = builder;
        webView = new WebView();
        popUp = new Stage();
    }

    public void init() {
        webView.getEngine().load("https://steamcommunity.com/login/home/?goto=");
        webView.getEngine().locationProperty().addListener(this::getSteam64ID);
        popUp.setScene(new Scene(webView));
        popUp.setTitle("Steam Login");
        popUp.show();
    }

    private void getSteam64ID(Observable observable) {
        String[] link = webView.getEngine().getLocation().split("/");
        if (!link[link.length - 1].equals("goto")) {
            String selector = link[link.length - 2];
            if (selector.equals("id")) {   // https://steamcommunity.com/id/VanityID/
                builder.getRestClient().resolveVanityID(link[link.length - 1], response -> {
                    JsonNode body = response.getBody();
                    int status = body.getObject().getJSONObject("response").getInt("success");
                    if (status == 1) {
                        setSteam64ID(body.getObject().getJSONObject("response").getString("steamid"));
                        stop();
                    }
                });
            } else if (selector.equals("profiles")) { // https://steamcommunity.com/profiles/steam64ID/
                setSteam64ID(link[link.length - 1]);
                stop();
            }
        }
    }

    private void setSteam64ID(String steam64ID) {
        builder.setSteamToken(steam64ID);
        builder.saveSettings();
    }

    private void stop() {
        webView.getEngine().locationProperty().removeListener(this::getSteam64ID);
        webView.getEngine().load(null);
        Platform.runLater(() -> popUp.close());
    }
}
