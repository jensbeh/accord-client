package de.uniks.stp.net;

import de.uniks.stp.builder.ModelBuilder;
import kong.unirest.JsonNode;

public class updateSteamGameController implements Runnable {
    private final ModelBuilder builder;
    private boolean stop_flag = true;

    public updateSteamGameController(ModelBuilder builder) {
        this.builder = builder;
    }
    public boolean getStopFlag(){
        return stop_flag;
    }

    public void stop(){
        stop_flag=false;
    }

    public void start(){
        while (builder.isSteamShow()&&stop_flag) {
            builder.getRestClient().getCurrentGame(builder.getSteamToken(), response -> {
                JsonNode body = response.getBody();
                if (body.getObject().getJSONObject("response").getJSONArray("players").getJSONObject(0).has("gameextrainfo")) {
                    builder.getPersonalUser().setDescription("?"+body.getObject().getJSONObject("response").getJSONArray("players").getJSONObject(0).getString("gameextrainfo"));
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

    public void run() {
        new Thread(this::start);
        stop_flag=true;
    }
}

