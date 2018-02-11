package com.avairebot.contracts.shard;

import net.dv8tion.jda.core.JDA;

public interface EntityGenerator {

    long generateEntity(JDA shard);
}
