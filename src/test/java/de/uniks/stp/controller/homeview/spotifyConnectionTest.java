package de.uniks.stp.controller.homeview;

import com.wrapper.spotify.requests.authorization.authorization_code.pkce.AuthorizationCodePKCERequest;
import de.uniks.stp.controller.settings.spotifyTest.ITest;
import de.uniks.stp.controller.settings.spotifyTest.TestUtil;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import org.apache.hc.core5.http.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static de.uniks.stp.controller.settings.spotifyTest.Assertions.assertHasBodyParameter;
import static de.uniks.stp.controller.settings.spotifyTest.Assertions.assertHasHeader;
import static de.uniks.stp.controller.settings.spotifyTest.ITest.*;
import static org.junit.Assert.assertEquals;

public class spotifyConnectionTest  {

    private final AuthorizationCodePKCERequest defaultRequest = SPOTIFY_API.authorizationCodePKCE(AUTHORIZATION_CODE, CODE_VERIFIER)
            .setHttpManager(
                    TestUtil.MockedHttpManager.returningJson(
                            "requests/authorization/authorization_code/AuthorizationCode.json"))
            .build();


    public spotifyConnectionTest() throws Exception {
    }
}



