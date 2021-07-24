package de.uniks.stp.net;

import de.uniks.stp.builder.ModelBuilder;
import kong.unirest.JsonNode;

public class updateSteamGameController implements Runnable {
    private final ModelBuilder builder;

    public updateSteamGameController(ModelBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() {
        while (!builder.getSteamToken().equals("")) {
            if (builder.isSteamShow()) {
                builder.getRestClient().getCurrentGame(builder.getSteamToken(), response -> {
                    JsonNode body = response.getBody();
                    if (body.getObject().getJSONObject("response").getJSONArray("players").getJSONObject(0).has("gameextrainfo")) {
                        builder.getPersonalUser().setDescription(body.getObject().getJSONObject("response").getJSONArray("players").getJSONObject(0).getString("gameextrainfo"));
                    } else {
                        builder.getPersonalUser().setDescription("");
                    }
                });
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

