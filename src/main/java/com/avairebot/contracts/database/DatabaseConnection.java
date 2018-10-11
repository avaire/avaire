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

package com.avairebot.contracts.database;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public interface DatabaseConnection {

    /**
     * Attempts to open the database connection to the database
     * type, this will return true if it manages to connect
     * to the database, and false otherwise.
     *
     * @return true if the database connection is open, false otherwise.
     * @throws SQLException        if a database access error occurs or the url is {@code null}
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value specified by the {@code setLoginTimeout} method
     *                             has been exceeded and has at least tried to cancel the
     *                             current database connection attempt
     */
    boolean open() throws SQLException;

    /**
     * Attempts to get the database statement from the query.
     *
     * @param query The query to check.
     * @return The implementation of the statement contract.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    StatementInterface getStatement(String query) throws SQLException;

    /**
     * Attempts to find out if the parsed string is a table.
     *
     * @param table The table name to check.
     * @return true if the table exists, false otherwise.
     */
    boolean hasTable(String table);

    /**
     * Attempts to truncate the given table, this will delete
     * every record in the table and reset it completely.
     *
     * @param table The table name to truncate.
     * @return true if the table was successfully reset, false otherwise.
     */
    boolean truncate(String table);
}
