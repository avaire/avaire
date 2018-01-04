package com.avairebot.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.cache.CacheType;
import com.avairebot.contracts.scheduler.Job;
import com.avairebot.factories.RequestFactory;
import com.avairebot.requests.Response;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GithubChangesJob extends Job {

    public GithubChangesJob(AvaIre avaire) {
        super(avaire, 30, 75, TimeUnit.MINUTES);

        if (!avaire.getCache().getAdapter(CacheType.FILE).has("github.commits")) {
            run();
        }
    }

    @Override
    public void run() {
        RequestFactory.makeGET("https://api.github.com/repos/avaire/avaire/commits")
            .send((Consumer<Response>) response -> {
                List service = (List) response.toService(List.class);

                avaire.getCache().getAdapter(CacheType.FILE).forever("github.commits", service);
            });
    }
}
