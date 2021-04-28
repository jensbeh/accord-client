package de.uniks.stp.net;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class RestClient {

    public static String login(String name, String password) throws IOException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", name);
        jsonBody.put("password", password);
        HttpResponse<JsonNode> response = Unirest.post("https://ac.uniks.de/api/users/login").body(jsonBody).asJson();
        JSONArray responseArray = response.getBody().getArray();
        String userKey = response.getBody().getObject().getJSONObject("data").getString("userKey");
        return userKey;
    }

    public static JSONArray getServers(String userKey) {
        JSONObject jsonBody = new JSONObject();
        HttpResponse<JsonNode> response = Unirest.get("https://ac.uniks.de/api/servers").header("userKey", userKey).asJson();
        JSONArray responseArray = response.getBody().getObject().getJSONArray("data");

        return responseArray;
    }

    public static JSONArray getUsers(String userKey) {
        JSONObject jsonBody = new JSONObject();
        HttpResponse<JsonNode> response = Unirest.get("https://ac.uniks.de/api/users").header("userKey", userKey).asJson();
        JSONArray responseArray = response.getBody().getObject().getJSONArray("data");

        return responseArray;
    }
}
