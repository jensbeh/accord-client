package de.uniks.stp.net;

import kong.unirest.Callback;
import kong.unirest.HttpRequest;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.JSONObject;

public class RestClient {

    public void signIn(String username, String password, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("password", password).accumulate("name",username);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post("https://ac.uniks.de/api/users").body(body);
        sendRequest(request, callback);
    }

    public void login(String username, String password, Callback<JsonNode> callback) {
        JSONObject jsonObj = new JSONObject().accumulate("password", password).accumulate("name",username);
        String body = JSONObject.valueToString(jsonObj);
        HttpRequest<?> request = Unirest.post("https://ac.uniks.de/api/users/login").body(body);
        sendRequest(request, callback);
    }

    private void sendRequest (HttpRequest<?> req, Callback<JsonNode> callback) {
        new Thread(() -> req.asJsonAsync(callback)).start();
    }
}
