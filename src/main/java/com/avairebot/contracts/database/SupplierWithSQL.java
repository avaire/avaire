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

package com.avairebot.contracts.database;

import java.sql.SQLException;

@FunctionalInterface
public interface SupplierWithSQL<R> {

    /**
     * Returns the result of the supplier with the given result type, or throws
     * a {@link SQLException SQL exception} if something went wrong.
     *
     * @return The result of the supplier.
     * @throws SQLException If an SQL exception is thrown within the
     *                      supplier, it will be passed down the stack.
     */
    R get() throws SQLException;
}
