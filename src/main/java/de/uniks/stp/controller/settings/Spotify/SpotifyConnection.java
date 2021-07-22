package de.uniks.stp.controller.settings.Spotify;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumRequest;
import com.wrapper.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;
import de.uniks.stp.builder.ModelBuilder;
import de.uniks.stp.controller.settings.ConnectionController;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SpotifyConnection {
    private String clientID = "f2557b7362074d3b93537b2803ef48b1";
    private String appID = "85a01971a347473b907d1ae06d8fad97";
    private String code = "";
    private final ModelBuilder builder;
    private WebView webView;
    private Stage popUp;
    private HttpServer server;
    private SpotifyApi spotifyApi;
    private AuthorizationCodeRequest authorizationCodeRequest;
    private AuthorizationCodeCredentials authorizationCodeCredentials;
    private AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest;
    private ConnectionController connectionController;
    private CurrentlyPlayingContext currentSong;

    private Label bandAndSong;
    private ImageView spotifyArtwork;

    public SpotifyConnection(ModelBuilder builder) {
        this.builder = builder;
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientID)
                .setClientSecret(appID)
                .setRedirectUri(URI.create("http://localhost:8888/callback/"))
                .setAccessToken(builder.getSpotifyToken())
                .setRefreshToken(builder.getSpotifyRefresh())
                .build();
        builder.setSpotifyConnection(this);
    }

    public void init(ConnectionController connectionController) {
        this.connectionController = connectionController;
        webView = new WebView();
        popUp = new Stage();
        createHttpServer();
        webView.getEngine().load("http://localhost:8888/");
        webView.getEngine().getLoadWorker().stateProperty().addListener(this::getSpotifyCode);
        popUp.setScene(new Scene(webView));
        popUp.setTitle("Spotify Login");
        popUp.show();
    }

    private void spotifyAuthentication() {
        try {
            authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            authorizationCodeCredentials = authorizationCodeRequest.execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            builder.setSpotifyToken(spotifyApi.getAccessToken());
            builder.setSpotifyRefresh(spotifyApi.getRefreshToken());
            builder.saveSettings();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void getSpotifyCode(Observable observable) {
        if (webView.getEngine().getLocation().contains("code=")) {
            String[] link = webView.getEngine().getLocation().split("code=");
            code = link[1];
            spotifyAuthentication();
            Platform.runLater(this::stop);
            connectionController.init();
        }
    }

    private void stop() {
        webView.getEngine().locationProperty().removeListener(this::getSpotifyCode);
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
        public void handle(HttpExchange t) {
            try {
                URL uri = getClass().getResource("/de/uniks/stp/spotify/spotifyLogin.html");
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

    public void refreshSpotifyToken() {
        if (builder.getSpotifyRefresh() != null) {
            try {
                authorizationCodeRefreshRequest = spotifyApi.authorizationCodeRefresh().build();
                authorizationCodeCredentials = authorizationCodeRefreshRequest.execute();
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                builder.setSpotifyToken(spotifyApi.getAccessToken());
                builder.saveSettings();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public CurrentlyPlayingContext getCurrentlyPlayingSong() {
        try {
            GetInformationAboutUsersCurrentPlaybackRequest getInformationAboutUsersCurrentPlaybackRequest = spotifyApi.getInformationAboutUsersCurrentPlayback().build();
            return getInformationAboutUsersCurrentPlaybackRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getCurrentlyPlayingSongAlbumID() {
        try {
            GetInformationAboutUsersCurrentPlaybackRequest getInformationAboutUsersCurrentPlaybackRequest  = spotifyApi.getInformationAboutUsersCurrentPlayback().build();
            CurrentlyPlayingContext currentlyPlayingContext  = getInformationAboutUsersCurrentPlaybackRequest.execute();
            String albumLink = currentlyPlayingContext.getContext().getHref();
            String[] id = albumLink.split("albums/");
            return id[1];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return "No song playing";
    }

    public Image getCurrentlyPlayingSongArtwork(String albumID) {
        try {
            GetAlbumRequest getAlbumRequest  = spotifyApi.getAlbum(albumID).build();
            Album album = getAlbumRequest.execute();
            Image[] images = album.getImages();
            return images[2];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void spotifyListener(Label bandAndSong, ImageView spotifyArtwork) {
        this.bandAndSong = bandAndSong;
        this.spotifyArtwork = spotifyArtwork;
        CurrentlyPlayingContext currentlyPlayingContext = getCurrentlyPlayingSong();
        int timeToPlayLeft = currentlyPlayingContext.getItem().getDurationMs() - currentlyPlayingContext.getProgress_ms();
        if (currentlyPlayingContext.getIs_playing()) {
            final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            final ScheduledFuture<?> handle =
                    scheduler.scheduleAtFixedRate(updateUserDescription, 0, 1, TimeUnit.SECONDS);
            scheduler.schedule(new Runnable() {
                public void run() {
                    handle.cancel(true);
                }
            }, timeToPlayLeft, TimeUnit.MILLISECONDS);
        }
    }

    final Runnable updateUserDescription = new Runnable() {
        public void run() {
            currentSong = getCurrentlyPlayingSong();
            builder.getPersonalUser().setDescription(currentSong.getItem().getName());
            Platform.runLater(() -> updatePersonalUser());
        }
    };

    private void updatePersonalUser() {
        bandAndSong.setText(builder.getPersonalUser().getDescription());
        String albumID = builder.getSpotifyConnection().getCurrentlyPlayingSongAlbumID();
        com.wrapper.spotify.model_objects.specification.Image image = builder.getSpotifyConnection().getCurrentlyPlayingSongArtwork(albumID);
        javafx.scene.image.Image artwork = new javafx.scene.image.Image(image.getUrl());
        spotifyArtwork.setImage(artwork);
    }
}
