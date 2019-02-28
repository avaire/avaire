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

package com.avairebot.scheduler.tasks;

import com.avairebot.AvaIre;
import com.avairebot.chat.MessageType;
import com.avairebot.chat.PlaceholderMessage;
import com.avairebot.contracts.scheduler.Task;
import com.avairebot.factories.MessageFactory;
import com.avairebot.factories.RequestFactory;
import com.avairebot.language.I18n;
import com.avairebot.requests.Response;
import com.avairebot.requests.service.WeatherService;
import com.avairebot.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DrainWeatherQueueTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(DrainVoteQueueTask.class);
    private static final String directions[] = {"North", "North East", "East", "South East", "South", "South West", "West", "North West", "North"};
    private static final DelayQueue<WeatherEntity> queue = new DelayQueue<>();
    private static long lastCheck = -1;

    /**
     * Queues the given weather entity, requesting the weather data for
     * the users query and then sending the result back to the user.
     *
     * @param entity The weather entity that should be added to the queue.
     */
    public static void queueWeather(@Nonnull WeatherEntity entity) {
        if (queue.contains(entity)) {
            return;
        }

        long diff = 0;
        if (lastCheck > System.currentTimeMillis()) {
            diff = lastCheck - System.currentTimeMillis();
            entity.setDuration(diff + WeatherEntity.defaultDuration);
        }
        lastCheck = System.currentTimeMillis() + diff + WeatherEntity.defaultDuration;

        queue.put(entity);
    }

    @Override
    public void handle(AvaIre avaire) {
        if (queue.isEmpty()) {
            return;
        }

        WeatherEntity entity = queue.poll();
        if (entity == null) {
            return;
        }

        String apiToken = avaire.getConfig().getString("apiKeys.openWeatherMap");
        if (apiToken == null || apiToken.trim().length() == 0) {
            return;
        }

        log.debug("Loading weather data by {} with the query: {}", entity.userId, entity.query);

        RequestFactory.makeGET("http://api.openweathermap.org/data/2.5/weather")
            .addParameter("appid", avaire.getConfig().getString("apiKeys.openWeatherMap"))
            .addParameter("units", "metric")
            .addParameter("q", String.join(" ", entity.query))
            .send((Consumer<Response>) response -> accept(avaire, response, entity));
    }

    private void accept(AvaIre avaire, Response response, WeatherEntity entity) {
        TextChannel textChannel = avaire.getShardManager().getTextChannelById(entity.channelId);
        if (textChannel == null || !textChannel.canTalk()) {
            return;
        }

        I18n.getString(textChannel.getGuild(), "");

        if (response.getResponse().code() != 200) {
            MessageFactory.makeEmbeddedMessage(textChannel, MessageType.WARNING)
                .setDescription(I18n.getString(textChannel.getGuild(), "utility.WeatherCommand.noCityFound"))
                .set("name", entity.query)
                .queue();
            return;
        }

        WeatherService service = (WeatherService) response.toService(WeatherService.class);

        Member member = textChannel.getGuild().getMemberById(entity.userId);
        if (member == null) {
            return;
        }

        PlaceholderMessage message = MessageFactory.makeEmbeddedMessage(textChannel)
            .requestedBy(member)
            .setTitle(I18n.getString(textChannel.getGuild(), "utility.WeatherCommand.fields.weatherFor",
                service.getName(), service.getLocation().getCountry()
            ))
            .addField(
                I18n.getString(textChannel.getGuild(), "utility.WeatherCommand.fields.country"),
                I18n.format(":flag_{0}:",
                    service.getLocation().getCountry().toLowerCase()
                ), true
            )
            .addField(
                I18n.getString(textChannel.getGuild(), "utility.WeatherCommand.fields.temperature"),
                I18n.format("{0}°C | {1}°F",
                    NumberUtil.formatNicelyWithDecimals(service.getTemperature().getTemp()),
                    NumberUtil.formatNicelyWithDecimals((service.getTemperature().getTemp() * (9D / 5D)) + 32)
                ), true
            )
            .addField(
                I18n.getString(textChannel.getGuild(), "utility.WeatherCommand.fields.windDirection"),
                directions[(int) Math.round(((service.getWind().getDeg() % 360) / 45))],
                true
            )
            .addField(
                I18n.getString(textChannel.getGuild(), "utility.WeatherCommand.fields.windSpeed"),
                I18n.format("{0} kph | {1} mph",
                    NumberUtil.formatNicelyWithDecimals(service.getWind().getSpeed()),
                    NumberUtil.formatNicelyWithDecimals(service.getWind().getSpeed() / 1.609D)
                ), true
            );

        if (!service.getWeather().isEmpty()) {
            message.setThumbnail(service.getWeather().get(0).getIconUrl());
        }

        message.queue();
    }

    public static class WeatherEntity implements Delayed {

        /**
         * The default time in milliseconds that should be between weather checks
         * that are sent to the open weather API, their ratelimit allows one
         * request a second, or sixty requests every minute.
         */
        static final long defaultDuration = 1000;

        private final long userId;
        private final long channelId;
        private final String query;
        private long duration;

        public WeatherEntity(long userId, long channelId, @Nonnull String query) {
            this.userId = userId;
            this.channelId = channelId;
            this.query = query;
            setDuration(defaultDuration);
        }

        /**
         * Sets the duration in milliseconds for when the weather entity can be consumed
         * by the {@link DrainWeatherQueueTask drain weather queue task}, the duration
         * will be added to the current time in milliseconds.
         *
         * @param duration The time in milliseconds before the vote entity can be consumed.
         */
        public void setDuration(long duration) {
            this.duration = System.currentTimeMillis() + duration;
        }

        @Override
        public long getDelay(@Nonnull TimeUnit unit) {
            return unit.convert(duration - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof WeatherEntity && userId == ((WeatherEntity) obj).userId;
        }

        @Override
        public int compareTo(@Nonnull Delayed obj) {
            return (int) (this.duration - ((WeatherEntity) obj).duration);
        }

        @Override
        public String toString() {
            return "WeatherEntity [duration=" + duration + ", userId=" + userId + "]";
        }
    }
}
