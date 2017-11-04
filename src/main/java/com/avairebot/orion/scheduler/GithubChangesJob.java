package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.contracts.scheduler.Job;
import com.avairebot.orion.factories.RequestFactory;
import com.avairebot.orion.requests.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GithubChangesJob extends Job {

    public GithubChangesJob(Orion orion) {
        super(orion, 30, 75, TimeUnit.MINUTES);

        if (!orion.getCache().getAdapter(CacheType.FILE).has("github.commits")) {
            run();
        }
    }

    @Override
    public void run() {
        RequestFactory.makeGET("https://api.github.com/repos/avaire/orion/commits")
            .send((Consumer<Response>) response -> {
                List service = (List) response.toService(List.class);

                orion.getCache().getAdapter(CacheType.FILE).forever("github.commits", service);
            });
    }
}
