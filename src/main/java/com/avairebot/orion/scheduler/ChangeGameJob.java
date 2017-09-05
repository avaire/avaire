package com.avairebot.orion.scheduler;

import com.avairebot.orion.Orion;
import com.avairebot.orion.contracts.scheduler.Job;
import net.dv8tion.jda.core.entities.Game;

public class ChangeGameJob extends Job {

    private int index = 0;

    public ChangeGameJob(Orion orion) {
        super(orion);
    }

    @Override
    public void run() {
        if (orion.config.getPlaying().size() <= index) {
            index = 0;
        }

        orion.getJDA().getPresence().setGame(
                Game.of(orion.config.getPlaying().get(index++))
        );
    }
}
