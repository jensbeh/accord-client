package de.uniks.stp.net;

import kong.unirest.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class RestClient {

    public void signIn(String username, String password, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("password", password).accumulate("name",username);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post("https://ac.uniks.de/api/users").body(body);
        sendRequest(request, callback);
    }

    public void login(String username, String password, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("name",username).accumulate("password", password);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post("https://ac.uniks.de/api/users/login").body(body);
        sendRequest(request, callback);
    }

    public void loginTemp(Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.post("https://ac.uniks.de/api/users/temp");
        sendRequest(request, callback);
    }

    public void getServers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get("https://ac.uniks.de/api/servers").header("userKey", userKey);
        sendRequest(request, callback);
    }

    public void getUsers(String userKey, Callback<JsonNode> callback) {
        HttpRequest<?> request = Unirest.get("https://ac.uniks.de/api/users").header("userKey", userKey);
        sendRequest(request, callback);
    }

    private void sendRequest (HttpRequest<?> req, Callback<JsonNode> callback) {
        req.asJsonAsync(callback);
    }
}
