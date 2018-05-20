package com.avairebot.metrics.filters;

import com.avairebot.AvaIre;
import spark.Filter;
import spark.Request;
import spark.Response;

import static spark.Spark.halt;

public class AreWeReadyYetFilter implements Filter {

    private final AvaIre avaire;

    public AreWeReadyYetFilter(AvaIre avaire) {
        this.avaire = avaire;
    }

    @Override
    public void handle(Request request, Response response) throws Exception {
        if (avaire.getShardManager() == null) {
            halt(503, "Application is still starting up, try again later.");
        }
    }
}
