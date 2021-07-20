package de.uniks.stp.controller.settings.Spotify;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.uniks.stp.builder.ModelBuilder;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SpotifyConnection {

    private String codeVerfier;
    private String codeChallenge;
    private String clientID = "f2557b7362074d3b93537b2803ef48b1";
    private String responseType = "code";
    private final ModelBuilder builder;
    private final WebView webView;
    private final Stage popUp;
    private HttpServer server;


    public SpotifyConnection(ModelBuilder builder) {
        this.builder = builder;
        webView = new WebView();
        popUp = new Stage();
        init();
    }

    public void init() {
        createHttpServer();
        webView.getEngine().load("http://localhost:8888/");

        webView.getEngine().getLoadWorker().stateProperty().addListener(this::getSpotifyToken);
        popUp.setScene(new Scene(webView));
        popUp.setTitle("Spotify Login");
        popUp.show();
    }

    private void getSpotifyToken(Observable observable) {

        if (webView.getEngine().getLocation().contains("access_token")) {
            String[] link = webView.getEngine().getLocation().split("access_token%22:%22");
            String[] link2 = link[1].split("%22,%22token_type");
            String[] link3 = link[1].split("%22refresh_token%22:%22");
            setSpotifyToken(link2[0]);
            //Platform.runLater(this::stop);
        }
    }

    private void setSpotifyToken(String spotifyToken) {
        builder.setSpotifyToken(spotifyToken);
        builder.setSpotifyShow(true);
        builder.saveSettings();
    }

    private void stop() {
        webView.getEngine().locationProperty().removeListener(this::getSpotifyToken);
        webView.getEngine().load(null);
        server.stop(0);
        server = null;
        popUp.close();
    }

    private void createHttpServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8888), 0);
            server.createContext("/", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t)  {
            try {
                URL uri = getClass().getResource("/de/uniks/stp/spotifyLogin.html");
                File file = new File(uri.toURI());
                String response = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }





}
