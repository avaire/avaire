package com.avairebot.scheduler.jobs;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GithubChangesJob extends Job {

    private final String cacheToken = "github.commits";

    public GithubChangesJob(AvaIre avaire) {
        super(avaire, 90, 90, TimeUnit.MINUTES);

        if (!avaire.getCache().getAdapter(CacheType.FILE).has(cacheToken)) {
            run();
        }
    }

    @Override
    public void run() {
        handleTask((Task) avaire -> {
            RequestFactory.makeGET("https://api.github.com/repos/avaire/avaire/commits")
                .send((Consumer<Response>) response -> {
                    List service = (List) response.toService(List.class);

                    avaire.getCache().getAdapter(CacheType.FILE).forever(cacheToken, service);
                });
        });
    }
}
