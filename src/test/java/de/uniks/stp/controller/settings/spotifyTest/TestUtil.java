package de.uniks.stp.controller.settings.spotifyTest;

import com.wrapper.spotify.IHttpManager;
import com.wrapper.spotify.SpotifyApi;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {

    private static final String FIXTURE_DIR = "src/test/resources";

    private static String readTestData(String fileName) throws IOException {
        return readFromFile(new File(FIXTURE_DIR, fileName));
    }

    private static String readFromFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            out.append(line);
        }

        in.close();

        return out.toString();
    }


    public static class MockedHttpManager {

        public static IHttpManager returningJson(String jsonFixtureFileName) throws Exception {

            // Mocked HTTP Manager to get predictable responses
            final IHttpManager mockedHttpManager = mock(IHttpManager.class);
            String fixture = null;

            if (jsonFixtureFileName != null) {
                fixture = readTestData(jsonFixtureFileName);
            }

            when(mockedHttpManager.get(any(URI.class), any(Header[].class))).thenReturn(fixture);
            when(mockedHttpManager.post(any(URI.class), any(Header[].class), any(HttpEntity.class))).thenReturn(fixture);
            when(mockedHttpManager.put(any(URI.class), any(Header[].class), any(HttpEntity.class))).thenReturn(fixture);
            when(mockedHttpManager.delete(any(URI.class), any(Header[].class), any(HttpEntity.class))).thenReturn(fixture);

            return mockedHttpManager;
        }
    }
}