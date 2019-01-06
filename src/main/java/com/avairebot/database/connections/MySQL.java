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

package com.avairebot.database.connections;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.StatementInterface;
import com.avairebot.contracts.database.connections.HostnameDatabase;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.grammar.mysql.*;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.schema.Blueprint;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.Executors;

public class MySQL extends HostnameDatabase {

    /**
     * Creates a MySQL database connection instance with the parsed information,
     * the port used will default to <code>3306</code>.
     *
     * @param dbm The database manager class instance.
     */
    public MySQL(DatabaseManager dbm) {
        this(
            dbm,
            dbm.getAvaire().getConfig().getString("database.hostname"),
            3306,
            dbm.getAvaire().getConfig().getString("database.database"),
            dbm.getAvaire().getConfig().getString("database.username"),
            dbm.getAvaire().getConfig().getString("database.password")
        );
    }

    /**
     * Creates a MySQL database connection instance with the parsed information.
     *
     * @param dbm      The database manager class instance.
     * @param hostname The hostname of the MySQL database.
     * @param port     The port the connection should be opened on.
     * @param database The name of the database.
     * @param username The username for the user that should be used for the connection.
     * @param password The password for the given username.
     */
    public MySQL(DatabaseManager dbm, String hostname, int port, String database, String username, String password) {
        super(dbm, hostname, port, database, username, password);
    }

    @Override
    protected boolean initialize() {
        try {
            Class.forName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");

            return true;
        } catch (ClassNotFoundException ex) {
            AvaIre.getLogger().error("MySQL DataSource class missing.", ex);
        }

        return false;
    }

    @Override
    public boolean open() throws SQLException {
        try {
            String url = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&verifyServerCertificate=%s&useSSL=true",
                getHostname(), getPort(), getDatabase(),
                dbm.getAvaire().getConfig().getBoolean("database.verifyServerCertificate", true) ? "true" : "false"
            );

            if (initialize()) {
                connection = DriverManager.getConnection(url, getUsername(), getPassword());

                // Sets a timeout of 20 seconds(This is an extremely long time, however the default
                // is around 10 minutes so this should give some improvements with the threads
                // not being blocked for ages due to hanging database queries.
                connection.setNetworkTimeout(Executors.newCachedThreadPool(), 1000 * 20);

                return true;
            }
        } catch (SQLException ex) {
            String reason = "Could not establish a MySQL connection, SQLException: " + ex.getMessage();

            AvaIre.getLogger().error(reason, ex);
            throw new SQLException(reason);
        }

        return false;
    }

    @Override
    protected void queryValidation(StatementInterface statement) throws SQLException {
        SQLException exception;

        switch ((MySQLStatement) statement) {
            case USE:
                exception = new SQLException("Please create a new connection to use a different database.");

                AvaIre.getLogger().error("Please create a new connection to use a different database.", exception);
                throw exception;

            case PREPARE:
            case EXECUTE:
            case DEALLOCATE:
                exception = new SQLException("Please use the prepare() method to prepare a query.");

                AvaIre.getLogger().error("Please use the prepare() method to prepare a query.", exception);
                throw exception;
        }
    }

    @Override
    public StatementInterface getStatement(String query) throws SQLException {
        String[] statement = query.trim().split(" ", 2);

        try {
            return MySQLStatement.valueOf(statement[0].toUpperCase());
        } catch (IllegalArgumentException ex) {
            AvaIre.getLogger().error(String.format("Unknown statement: \"%s\"", statement[0]), ex);
        }

        return null;
    }

    @Override
    public boolean hasTable(String table) {
        try {
            DatabaseMetaData md = getConnection().getMetaData();

            try (ResultSet tables = md.getTables(null, null, table, new String[]{"TABLE"})) {
                if (tables.next()) {
                    tables.close();

                    return true;
                }
            }
        } catch (SQLException ex) {
            AvaIre.getLogger().error(String.format("Failed to check if table exists \"%s\": %s", table, ex.getMessage()), ex);
        }

        return false;
    }

    @Override
    public boolean truncate(String table) {
        try {
            if (!hasTable(table)) {
                return false;
            }

            try (Statement statement = getConnection().createStatement()) {
                statement.executeUpdate(String.format("DELETE FROM `%s`;", table));
            }

            return true;
        } catch (SQLException ex) {
            AvaIre.getLogger().error(String.format("Failed to truncate \"%s\": %s", table, ex.getMessage()), ex);
        }

        return false;
    }

    @Override
    public String select(DatabaseManager manager, QueryBuilder query, Map<String, Boolean> options) {
        return setupAndRun(new Select(), query, manager, options);
    }

    @Override
    public String create(DatabaseManager manager, Blueprint blueprint, @Nonnull Map<String, Boolean> options) {
        return setupAndRun(new Create(), blueprint, manager, options);
    }

    @Override
    public String delete(DatabaseManager manager, QueryBuilder query, Map<String, Boolean> options) {
        return setupAndRun(new Delete(), query, manager, options);
    }

    @Override
    public String insert(DatabaseManager manager, QueryBuilder query, Map<String, Boolean> options) {
        return setupAndRun(new Insert(), query, manager, options);
    }

    @Override
    public String update(DatabaseManager manager, QueryBuilder query, Map<String, Boolean> options) {
        return setupAndRun(new Update(), query, manager, options);
    }
}
