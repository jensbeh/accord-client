package de.uniks.stp.controller.settings.subcontroller;

import de.uniks.stp.builder.ModelBuilder;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import kong.unirest.JsonNode;

public class SteamLoginController {
    private final ModelBuilder builder;
    private WebView webView;
    private Stage popUp;
    private Runnable refreshConnectionView;

    public SteamLoginController(ModelBuilder builder) {
        this.builder = builder;
    }

    public void init() {
        webView = new WebView();
        popUp = new Stage();
        webView.getEngine().load("https://steamcommunity.com/login/home/?goto=");
        webView.getEngine().locationProperty().addListener(this::getSteam64ID);
        popUp.setScene(new Scene(webView));
        popUp.setTitle("Steam Login");
        popUp.show();
    }

    private void getSteam64ID(Observable observable) {
        String[] link = webView.getEngine().getLocation().split("/");
        if (link.length > 1 && !link[link.length - 1].equals("goto")) {
            String selector = link[link.length - 2];
            if (selector.equals("id")) {   // https://steamcommunity.com/id/VanityID/
                builder.getRestClient().resolveVanityID(link[link.length - 1], response -> {
                    JsonNode body = response.getBody();
                    int status = body.getObject().getJSONObject("response").getInt("success");
                    if (status == 1) {
                        setSteam64ID(body.getObject().getJSONObject("response").getString("steamid"));
                    }
                });
            } else if (selector.equals("profiles")) { // https://steamcommunity.com/profiles/steam64ID/
                setSteam64ID(link[link.length - 1]);
            }
        }
    }

    private void setSteam64ID(String steam64ID) {
        Platform.runLater(popUp::close);
        builder.setSteamToken(steam64ID);
        builder.saveSettings();
        webView.getEngine().getLoadWorker().cancel();
        webView.getEngine().locationProperty().removeListener(this::getSteam64ID);
        webView = null;
        popUp = null;
        refreshConnectionView.run();
        Platform.runLater(builder::getGame);
    }

    public void refresh(Runnable refresh){
        refreshConnectionView = refresh;
    }
}
