package de.uniks.stp.controller.settings.Spotify;

import com.sun.net.httpserver.HttpServer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SpotifyConnection {
    private String clientID = "f2557b7362074d3b93537b2803ef48b1";
    private String appID = "85a01971a347473b907d1ae06d8fad97";
    private String codeVerifier = "";
    private String codeChallenge = "";
    private String code = "";
    private final ModelBuilder builder;
    private WebView webView;
    private Stage popUp;
    private HttpServer server;
    private SpotifyApi spotifyApi;
    private AuthorizationCodePKCERequest authorizationCodePKCERequest;
    private AuthorizationCodeCredentials authorizationCodeCredentials;
    private AuthorizationCodePKCERefreshRequest authorizationCodePKCERefreshRequest;
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
        createCodeVerifier();
        createCodeChallenge();
        webView.getEngine().load(createSpotifyAuthenticationURLPKCE());
        webView.getEngine().getLoadWorker().stateProperty().addListener(this::getSpotifyCode);
        popUp.setScene(new Scene(webView));
        popUp.setTitle("Spotify Login");
        popUp.show();
    }

    private void createCodeChallenge() {
        byte[] codeChallengeHash = DigestUtils.sha256(codeVerifier);
        Base64.Encoder encoder = Base64.getUrlEncoder();
        String codeChallengeBase64 = encoder.encodeToString(codeChallengeHash);
        codeChallenge = codeChallengeBase64.substring(0, codeChallengeBase64.length() - 1);
    }

    private void createCodeVerifier() {
        codeVerifier = RandomStringUtils.random(80, 0, 0, true, true, null, new SecureRandom());
    }

    private String createSpotifyAuthenticationURLPKCE() {
        String url = "https://accounts.spotify.com/authorize";
        url += "?client_id=" + clientID;
        url += "&response_type=code";
        url += "&redirect_uri=http://localhost:8888/callback/";
        url += "&code_challenge_method=S256";
        url += "&code_challenge=" + codeChallenge;
        url += "&scope=user-read-currently-playing user-read-playback-position user-read-playback-state";
        return url;
    }

    private void spotifyAuthentication() {
        try {
            authorizationCodePKCERequest = spotifyApi.authorizationCodePKCE(code, codeVerifier).build();
            authorizationCodeCredentials = authorizationCodePKCERequest.execute();
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
        if (handle != null) {
            handle.cancel(true);
            scheduler.shutdownNow();
        }
    }

    private void createHttpServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8888), 0);
            server.createContext("/");
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshSpotifyToken() {
        if (builder.getSpotifyRefresh() != null) {
            try {
                authorizationCodePKCERefreshRequest = spotifyApi.authorizationCodePKCERefresh().build();
                authorizationCodeCredentials = authorizationCodePKCERefreshRequest.execute();
                spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
                spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
                builder.setSpotifyToken(spotifyApi.getAccessToken());
                builder.setSpotifyRefresh(spotifyApi.getRefreshToken());
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
