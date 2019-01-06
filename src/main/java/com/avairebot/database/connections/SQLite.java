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
import com.avairebot.contracts.database.connections.FilenameDatabase;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.exceptions.DatabaseException;
import com.avairebot.database.grammar.sqlite.*;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.schema.Blueprint;
import com.avairebot.metrics.Metrics;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.Arrays;
import java.util.Map;

public class SQLite extends FilenameDatabase {

    /**
     * Creates a SQLite database connection instance with the parsed information.
     *
     * @param dbm The database manager class instance.
     */
    public SQLite(DatabaseManager dbm) {
        this(dbm, dbm.getAvaire().getConfig().getString("database.filename", "database.sqlite"));
    }

    /**
     * Creates a SQLite database connection instance with the parsed information.
     *
     * @param dbm      The database manager class instance.
     * @param filename The filename of the database.
     */
    public SQLite(DatabaseManager dbm, String filename) {
        super(dbm);

        if (filename.equals(":memory:")) {
            this.setFilename(null);
            return;
        }

        String[] parts = filename.split("\\.");
        String extension = parts[parts.length - 1];

        filename = String.join(".", Arrays.copyOf(parts, parts.length - 1));

        this.setFile(".", filename, extension);
    }

    @Override
    protected boolean initialize() {
        try {
            Class.forName("org.sqlite.JDBC");

            return true;
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Class not found while initializing", e);
        }
    }

    @Override
    public boolean open() throws SQLException {
        if (initialize()) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + (getFile() == null ? ":memory:" : getFile().getAbsolutePath()));

                return true;
            } catch (SQLException ex) {
                String reason = "DBM - Could not establish an SQLite connection, SQLException: " + ex.getMessage();

                AvaIre.getLogger().error(reason, ex);
                throw new SQLException(reason);
            }
        }

        return false;
    }

    @Override
    protected void queryValidation(StatementInterface paramStatement) throws SQLException {
        // This does nothing for SQLite
    }

    @Override
    public Connection getConnection() throws SQLException {
        open();

        return connection;
    }

    @Override
    public StatementInterface getStatement(String query) throws SQLException {
        String[] statement = query.trim().split(" ", 2);

        try {
            return SQLiteStatement.valueOf(statement[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SQLException(String.format("Unknown statement: \"%s\".", statement[0]));
        }
    }

    @Override
    public boolean hasTable(String table) {
        try {
            DatabaseMetaData md = getConnection().getMetaData();

            try (ResultSet tables = md.getTables(null, null, table, null)) {
                if (tables.next()) {
                    tables.close();

                    return true;
                }
            }

            return false;
        } catch (SQLException ignored) {
            return false;
        }
    }

    @Override
    public boolean truncate(String table) {
        try {
            if (!hasTable(table)) {
                return false;
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeQuery(String.format("DELETE FROM `%s`;", table));
            }

            return true;
        } catch (SQLException ex) {
            if ((!ex.getMessage().toLowerCase().contains("locking")) && (!ex.getMessage().toLowerCase().contains("locked")) && (!ex.toString().contains("not return ResultSet"))) {
                AvaIre.getLogger().error(String.format("Failed to truncate \"%s\": %s", table, ex.getMessage()), ex);
            }
        }

        return false;
    }

    @Override
    protected Statement createPreparedStatement(String query) throws SQLException {
        Metrics.databaseQueries.labels(query.split(" ")[0].toUpperCase()).inc();

        Statement statement = getConnection().createStatement();

        statement.setQueryTimeout(5);
        statement.setMaxRows(25000);

        return statement;
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
