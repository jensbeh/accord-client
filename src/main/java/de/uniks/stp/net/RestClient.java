package de.uniks.stp.net;

import kong.unirest.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class RestClient {

    public static String login(String name, String password) throws IOException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", name);
        jsonBody.put("password", password);
        HttpResponse<JsonNode> response = Unirest.post("https://ac.uniks.de/api/users/login").body(jsonBody).asJson();
        return response.getBody().getObject().getJSONObject("data").getString("userKey");
    }

    public static JSONArray getServers(String userKey) {
        HttpResponse<JsonNode> response = Unirest.get("https://ac.uniks.de/api/servers").header("userKey", userKey).asJson();
        return response.getBody().getObject().getJSONArray("data");
    }

//    public static void postServer(String userKey, String serverName, Callback<JsonNode> callback) {
//        JSONObject jsonBody = new JSONObject();
//        jsonBody.put("name", serverName);
//        HttpRequest<?> postRequest = Unirest.post("https://ac.uniks.de/api/servers").body(jsonBody).header("userKey",userKey);
//        sendRequest(postRequest, callback);
//    }

    public static void getServerUsers(String serverId, String userKey, Callback<JsonNode> callback){
        String url = "https://ac.uniks.de/api/servers/" + serverId;
        HttpRequest<?> postRequest = Unirest.get(url).header("userKey",userKey);
        sendRequest(postRequest, callback);
    }

    public static JsonNode postServer(String userKey, String serverName) {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("name", serverName);
        HttpResponse<JsonNode> response = Unirest.post("https://ac.uniks.de/api/servers").body(jsonBody).header("userKey",userKey).asJson();
        return response.getBody();
    }

    public static JSONArray getUsers(String userKey) {
        HttpResponse<JsonNode> response = Unirest.get("https://ac.uniks.de/api/users").header("userKey", userKey).asJson();
        return response.getBody().getObject().getJSONArray("data");
    }

    private static void sendRequest(HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }
}
