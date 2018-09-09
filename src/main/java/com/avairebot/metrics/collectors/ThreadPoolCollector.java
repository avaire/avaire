/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.metrics.collectors;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolCollector extends Collector {

    private final ConcurrentMap<String, ThreadPoolExecutor> pools = new ConcurrentHashMap<>();

    /**
     * Add or replace the pool with the given name.
     * <p>
     * Any references to any previous pool with this name are invalidated.
     *
     * @param poolName The name of the pool, will be the metrics label value
     * @param pool     The pool being monitored
     */
    public void addPool(String poolName, ThreadPoolExecutor pool) {
        pools.put(poolName, pool);
    }

    /**
     * Remove the pool with the given name.
     * <p>
     * Any references to the pool are invalidated.
     *
     * @param poolName pool to be removed
     * @return The previous value associated with the <tt>pool name</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    public ThreadPoolExecutor removePool(String poolName) {
        return pools.remove(poolName);
    }

    /**
     * Remove all pools.
     * <p>
     * Any references to all pools are invalidated.
     */
    public void clear() {
        pools.clear();
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<>();
        List<String> labelNames = Collections.singletonList("name");

        GaugeMetricFamily activeThreads = new GaugeMetricFamily("avaire_threadpool_active_threads_current",
            "Amount of active threads in a thread pool", labelNames);
        mfs.add(activeThreads);

        GaugeMetricFamily queueSize = new GaugeMetricFamily("avaire_threadpool_queue_size_current",
            "Size of queue of a thread pool (including scheduled tasks)", labelNames);
        mfs.add(queueSize);

        CounterMetricFamily completedTasks = new CounterMetricFamily("avaire_threadpool_completed_tasks_total",
            "Total completed tasks by a thread pool", labelNames);
        mfs.add(completedTasks);

        for (Map.Entry<String, ThreadPoolExecutor> entry : pools.entrySet()) {
            String poolName = entry.getKey();
            ThreadPoolExecutor pool = entry.getValue();
            List<String> labels = Collections.singletonList(poolName);

            activeThreads.addMetric(labels, pool.getActiveCount());
            queueSize.addMetric(labels, pool.getQueue().size());
            completedTasks.addMetric(labels, pool.getCompletedTaskCount());
        }

        return mfs;
    }
}
