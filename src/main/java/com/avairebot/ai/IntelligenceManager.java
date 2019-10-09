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

package com.avairebot.ai;

import com.avairebot.AvaIre;
import com.avairebot.contracts.ai.IntelligenceService;
import com.avairebot.handlers.DatabaseEventHolder;
import com.avairebot.metrics.Metrics;
import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class IntelligenceManager {

    private static final Logger log = LoggerFactory.getLogger(IntelligenceService.class);

    private final AvaIre avaire;
    private IntelligenceService service;

    /**
     * Creates a new intelligence manager instance using
     * the given AvaIre application instance.
     *
     * @param avaire The main AvaIre application instance.
     */
    public IntelligenceManager(AvaIre avaire) {
        this.avaire = avaire;

        service = null;
    }

    /**
     * Checks if the there is an AI service registered, and that
     * the service is enabled and ready to serve requests.
     *
     * @return {@code True} if the registered AI service is ready to serve requests,
     *         {@code False} otherwise.
     */
    public boolean isEnabled() {
        return service != null && service.isEnabled();
    }

    /**
     * Registers the given service, if a service is already register,
     * the service will first be unregistered, and then the new
     * service will be registered in its place.
     * <p>
     * A service can also be unregistered to disable the AI completely by calling
     * the {@link #unregisterService() unregister serivce} method.
     *
     * @param service The AI service that should be used to handle AI requests.
     */
    public void registerService(@Nonnull IntelligenceService service) {
        if (this.service != null) {
            try {
                this.service.unregisterService(avaire);
            } catch (Exception e) {
                log.warn("The {} AI service threw an exception while being unregistered: {}",
                    this.service.getClass().getSimpleName(), e.getMessage(), e
                );
            }
        }

        service.registerService(avaire);

        this.service = service;
    }

    /**
     * Unregisters the AI service that is currently
     * being used to serve AI requests.
     */
    public void unregisterService() {
        if (service == null) {
            return;
        }

        try {
            service.unregisterService(avaire);
        } catch (Exception e) {
            log.warn("The {} AI service threw an exception while being unregistered: {}",
                this.service.getClass().getSimpleName(), e.getMessage(), e
            );
        }

        service = null;
    }

    /**
     * Gets the AI service that are currently registered,
     * the service may not be enabled yet.
     *
     * @return Possibly-null, or the service that is registered.
     */
    public IntelligenceService getService() {
        return service;
    }

    /**
     * Sends an AI request message to the service using the give JDA
     * message object instance, and the database event holders for
     * the event that triggered the AI requests.
     * <p>
     * The database event holders will hold the guild and player
     * database transformers for the current request.
     *
     * @param message             The JDA message instance that triggered the AI request.
     * @param databaseEventHolder The database holder for the current guild and player transformers.
     */
    public void handleRequest(@Nonnull Message message, @Nonnull DatabaseEventHolder databaseEventHolder) {
        if (service == null || !isEnabled()) {
            return;
        }

        Metrics.aiRequestsReceived.inc();

        service.onMessage(message, databaseEventHolder);
    }
}
