package com.avairebot.orion.contracts.metrics;

import com.avairebot.orion.metrics.Metrics;
import spark.Route;

public abstract class SparkRoute implements Route {

    protected final Metrics metrics;

    public SparkRoute(Metrics metrics) {
        this.metrics = metrics;
    }
}
