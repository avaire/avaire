package com.avairebot.orion.contracts.scheduler;

import com.avairebot.orion.Orion;

import java.util.concurrent.TimeUnit;

public abstract class Job implements Runnable {

    protected final Orion orion;
    private final long delay;
    private final long period;
    private final TimeUnit unit;

    public Job(Orion orion) {
        this(orion, 0, 1);
    }

    public Job(Orion orion, long delay, long period) {
        this(orion, delay, period, TimeUnit.MINUTES);
    }

    public Job(Orion orion, long delay, long period, TimeUnit unit) {
        this.orion = orion;
        this.delay = delay;
        this.period = period;
        this.unit = unit;
    }

    public long getDelay() {
        return delay;
    }

    public long getPeriod() {
        return period;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
