/*
 * Copyright (c) 2019.
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

package com.avairebot.contracts.ai;

import com.avairebot.AvaIre;
import com.avairebot.handlers.DatabaseEventHolder;
import net.dv8tion.jda.core.entities.Message;

public interface IntelligenceService {

    /**
     * Checks if the intelligence service is enabled
     * and ready to serve requests.
     *
     * @return {@code True} if the service ready to be used, {@code False} otherwise.
     */
    boolean isEnabled();

    /**
     * Registers the service, this is called right when the
     * service is registered with the intelligence manager.
     *
     * @param avaire The main AvaIre application instance.
     */
    void registerService(AvaIre avaire);

    /**
     * Unregisters the service, this is called automatically when
     * a new service is registered to override the current set
     * service, or when the bot shuts down gracefully.
     *
     * @param avaire The main AvaIre application instance.
     */
    void unregisterService(AvaIre avaire);

    /**
     * Handles the current message as an AI request using the given
     * JDA message object instance, and the current guild and
     * player transformers for the current request.
     *
     * @param message             The JDA message instance that triggered the AI request.
     * @param databaseEventHolder The database holder for the current guild and player transformers.
     */
    void onMessage(Message message, DatabaseEventHolder databaseEventHolder);
}
