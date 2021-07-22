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
import javafx.scene.control.ProgressBar;
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
    private Label timePlayed;
    private ProgressBar progressBar;

    private String artist;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> handle;
    private Label timeTotal;

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

    public void stopScheduler() {
        handle.cancel(true);
        scheduler.shutdown();
        updateUserDescription = null;
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
            GetInformationAboutUsersCurrentPlaybackRequest getInformationAboutUsersCurrentPlaybackRequest = spotifyApi.getInformationAboutUsersCurrentPlayback().build();
            CurrentlyPlayingContext currentlyPlayingContext = getInformationAboutUsersCurrentPlaybackRequest.execute();
            String albumLink = currentlyPlayingContext.getContext().getHref();
            String[] id = new String[0];
            if (albumLink.contains("albums/")) {
                id = albumLink.split("albums/");
            } else {
                id = albumLink.split("playlists/");
            }
            return id[1];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return "No song playing";
    }

    public Image getCurrentlyPlayingSongArtwork(String albumID) {
        try {
            GetAlbumRequest getAlbumRequest = spotifyApi.getAlbum(albumID).build();
            Album album = getAlbumRequest.execute();
            album.getArtists();
            artist = album.getArtists()[0].getName();
            Image[] images = album.getImages();
            return images[2];
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void spotifyListener(Label bandAndSong, ImageView spotifyArtwork, Label timePlayed, Label timeTotal, ProgressBar progessBar) {
        this.bandAndSong = bandAndSong;
        this.spotifyArtwork = spotifyArtwork;
        this.timeTotal = timeTotal;
        this.timePlayed = timePlayed;
        this.progressBar = progessBar;
        CurrentlyPlayingContext currentlyPlayingContext = getCurrentlyPlayingSong();
        int timeToPlayLeft = currentlyPlayingContext.getItem().getDurationMs() - currentlyPlayingContext.getProgress_ms();
        if (currentlyPlayingContext.getIs_playing()) {
            scheduler = Executors.newScheduledThreadPool(1);
            handle = scheduler.scheduleAtFixedRate(updateUserDescription, 0, 1, TimeUnit.SECONDS);

            scheduler.schedule(new Runnable() {
                public void run() {
                    handle.cancel(true);
                    scheduler.shutdown();
                    spotifyListener(bandAndSong, spotifyArtwork, timePlayed, timeTotal, progessBar);
                }
            }, timeToPlayLeft, TimeUnit.MILLISECONDS);
        }
    }

    Runnable updateUserDescription = new Runnable() {
        public void run() {
            currentSong = getCurrentlyPlayingSong();
            currentSong.getItem().getId();
            builder.getPersonalUser().setDescription(artist + " - " + currentSong.getItem().getName());
            Platform.runLater(() -> updatePersonalUser(currentSong.getProgress_ms(), currentSong.getItem().getDurationMs()));
        }
    };

    private void updatePersonalUser(double elapsed, double duration) {
        bandAndSong.setText(builder.getPersonalUser().getDescription());
        String albumID = builder.getSpotifyConnection().getCurrentlyPlayingSongAlbumID();
        com.wrapper.spotify.model_objects.specification.Image image = builder.getSpotifyConnection().getCurrentlyPlayingSongArtwork(albumID);
        javafx.scene.image.Image artwork = new javafx.scene.image.Image(image.getUrl());
        spotifyArtwork.setImage(artwork);
        formatTime((int) elapsed, (int) duration);
        double progressbarValue = (elapsed / duration);
        progressBar.setProgress(progressbarValue + 0.03);
    }

    private void formatTime(int elapsed, int duration) {
        int intElapsed = (int) Math.floor(elapsed / 1000);
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration > 0) {
            int intDuration = (int) Math.floor(duration / 1000);
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60 -
                    durationMinutes * 60;
            timePlayed.setText(String.format("%02d:%02d", elapsedMinutes, elapsedSeconds));
            timeTotal.setText(String.format("%02d:%02d", durationMinutes, durationSeconds));
        }
    }
}
