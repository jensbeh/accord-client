package de.uniks.stp.controller.settings.spotifyTest;

import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;


public interface ITest<T> {

    SpotifyApi SPOTIFY_API = new SpotifyApi.Builder()
            .setClientId("default")
            .setClientSecret("zudknyqbh3wunbhcvg9uyvo7uwzeu6nne")
            .setRedirectUri(SpotifyHttpManager.makeUri("https://example.com/spotify-redirect"))
            .setAccessToken("taHZ2SdB-bPA3FsK3D7ZN5npZS47cMy-IEySVEGttOhXmqaVAIo0ESvTCLjLBifhHOHOIuhFUKPW1WMDP7w6dj3MAZdWT8CLI2MkZaXbYLTeoDvXesf2eeiLYPBGdx8tIwQJKgV8XdnzH_DONk")
            .setRefreshToken("b0KuPuLw77Z0hQhCsK-GTHoEx_kethtn357V7iqwEpCTIsLgqbBC_vQBTGC6M5rINl0FrqHK-D3cbOsMOlfyVKuQPvpyGcLcxAoLOTpYXc28nVwB7iBq2oKj9G9lHkFOUKn")
            .build();

    String AUTHORIZATION_CODE = "c-oGaPdYJF3tu3oUZRUiBHWQvm4oHnBrsxfHackYzzomKJiy5te1k04LJdr6XxjACe9TonpJR8NPOQ3o5btASx_oMw4trmXLYdkda77wY0NJ9Scl69lKvGiOfdnRi5Q0IbBu185Y0TZgyUJz3Auqqv-Wk7zjRke4DzqYEc3ucyUBOq08j5223te-G2K72aL9PxgVJaEHBbLvhdJscCy-zcyU29EZoNlG_E5";
    String CODE_VERIFIER = "NlJx4kD4opk4HY7zBM6WfUHxX7HoF8A2TUhOIPGA74w";
    String ADDITIONAL_TYPES = "track,episode";
    String ID_ALBUM = "5zT1JLIj9E57p3e1rFm9Uq";
    String ID_TRACK = "01iyCAUm8EvOFqVWYJ3dVX";
    CountryCode MARKET = CountryCode.SE;

}