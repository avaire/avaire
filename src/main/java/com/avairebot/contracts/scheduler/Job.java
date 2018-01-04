package com.avairebot.contracts.scheduler;

import com.avairebot.AvaIre;
import com.avairebot.contracts.reflection.Reflectionable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class Job extends Reflectionable implements Runnable {

    /**
     * The amount of time the job should be delayed before starting,
     * the {@link #unit} of time is determined by {@link #unit}.
     */
    private final long delay;

    /**
     * The amount of time in between each time the job should be executed.
     */
    private final long period;

    /**
     * The unit of time that the job should be scaled after.
     *
     * @see TimeUnit
     */
    private final TimeUnit unit;

    /**
     * The unique ID for the current job.
     */
    private final String unique;

    /**
     * Instantiates the job instance with the given AvaIre application instance, with
     * a delay of 0, period of 1, and time unit of {@link TimeUnit#MINUTES}.
     *
     * @param avaire The AvaIre application instance.
     */
    public Job(AvaIre avaire) {
        this(avaire, 0);
    }

    /**
     * Instantiates the job instance with the given AvaIre application instance and delay,
     * setting the default period to 1 along with a time unit of {@link TimeUnit#MINUTES}.
     *
     * @param avaire The AvaIre application instance.
     * @param delay  The delay before the command should be executed for the first time.
     */
    public Job(AvaIre avaire, long delay) {
        this(avaire, delay, 1);
    }

    /**
     * Instantiates the job instance with the given AvaIre application instance, delay,
     * and period, setting the default time unit to {@link TimeUnit#MINUTES}.
     *
     * @param avaire The AvaIre application instance.
     * @param delay  The delay before the command should be executed for the first time.
     * @param period The time in between executions after the job has already run once.
     */
    public Job(AvaIre avaire, long delay, long period) {
        this(avaire, delay, period, TimeUnit.MINUTES);
    }

    /**
     * Instantiates the job instance with the given AvaIre application
     * instance, delay, and, period and time unit.
     *
     * @param avaire The AvaIre application instance.
     * @param delay  The delay before the command should be executed for the first time.
     * @param period The time in between executions after the job has already run once.
     * @param unit   The unit of time the job should measure the delay and periods in.
     */
    public Job(AvaIre avaire, long delay, long period, TimeUnit unit) {
        super(avaire);

        this.delay = delay;
        this.period = period;
        this.unit = unit;

        this.unique = Long.toHexString(System.nanoTime());
    }

    /**
     * Gets the job delay.
     *
     * @return The job delay.
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Gets the job period.
     *
     * @return The job period.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Gets the time unit.
     *
     * @return The time unit.
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Gets the unique id for the current job.
     *
     * @return The unique id for the current job.
     */
    public String getUniqueId() {
        return Integer.toHexString(hashCode()) + unique;
    }

    @Override
    public int hashCode() {
        return Objects.hash(avaire, delay, period, unit, unique);
    }
}
