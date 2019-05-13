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

package com.avairebot.chat;

import com.avairebot.contracts.chat.Paginator;
import com.avairebot.language.I18n;
import net.dv8tion.jda.core.entities.Guild;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SimplePaginator<T> extends Paginator<T> {

    private static final String defaultFooterNote = "Page **{0}** out of **{1}** pages.\n`{2} [page]`";

    public SimplePaginator(Map<?, T> items, int perPage, int currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(Map<?, T> items, int perPage, String currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(Map<?, T> items, int perPage) {
        super(items, perPage, 1);
    }

    public SimplePaginator(List<T> items, int perPage, int currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(List<T> items, int perPage, String currentPage) {
        super(items, perPage, currentPage);
    }

    public SimplePaginator(List<T> items, int perPage) {
        super(items, perPage, 1);
    }

    public SimplePaginator(Iterator<T> iterator, int perPage, int currentPage) {
        super(iterator, perPage, currentPage);
    }

    public SimplePaginator(Iterator<T> iterator, int perPage) {
        this(iterator, perPage, 1);
    }

    public String generateFooter(Guild guild, String command) {
        String message = I18n.getLocale(guild).getConfig().getString(
            "pagination.simpleFooter", defaultFooterNote
        );

        return I18n.format(
            message
                .replace("\\n", "\n")
                .replace("\\t", "\t"),
            getCurrentPage(),
            getPages(),
            command
        );
    }
}
