package de.uniks.stp.net;

import de.uniks.stp.builder.ModelBuilder;
import kong.unirest.JsonNode;

public class updateSteamGameController implements Runnable {
    private final ModelBuilder builder;

    public updateSteamGameController(ModelBuilder builder) {
        this.builder = builder;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while (builder.isSteamShow() && builder.isSteamRun()) {
            builder.getRestClient().getCurrentGame(builder.getSteamToken(), response -> {
                JsonNode body = response.getBody();
                if (body.getObject().getJSONObject("response").getJSONArray("players").getJSONObject(0).has("gameextrainfo")) {
                    builder.getPersonalUser().setDescription("?" + body.getObject().getJSONObject("response").getJSONArray("players").getJSONObject(0).getString("gameextrainfo"));
                } else {
                    builder.getPersonalUser().setDescription("?");
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

