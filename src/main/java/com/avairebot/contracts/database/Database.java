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

import com.avairebot.contracts.database.grammar.AlterGrammar;
import com.avairebot.contracts.database.grammar.Grammarable;
import com.avairebot.contracts.database.grammar.TableGrammar;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.schema.Blueprint;
import com.avairebot.metrics.Metrics;
import com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.WillNotClose;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Database implements DatabaseConnection, Grammarable {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    /**
     * The main database manage instance, used to communicate
     * with the rest of the application.
     */
    protected DatabaseManager dbm = null;

    /**
     * Represents our prepared query statements and their statement
     * type, allowing us to quickly render and compile statements.
     */
    protected Map<PreparedStatement, StatementInterface> preparedStatements = new HashMap<>();

    /**
     * Represents our current database connection, this is
     * used to send queries to the database, as well as
     * fetch, and persist data.
     */
    protected Connection connection;

    /**
     * Represents the last connection state the
     * last time the connection was checked.
     */
    private boolean lastState;

    /**
     * Represents the last time in milliseconds the state of the connection
     * was checked, if the connection was checked less then a few seconds
     * before, the last state will be returned instead, preventing
     * checking the actually database by querying it every time.
     */
    private long lastChecked;

    /**
     * Sets the Database Manager instance to the database.
     *
     * @param dbm The database manager class instance.
     */
    public Database(DatabaseManager dbm) {
        this.dbm = dbm;

        lastState = false;
        lastChecked = 0L;
    }

    /**
     * Initialize the database abstraction, this should be
     * called by the open method if it's necessary.
     *
     * @return either (1) <code>TRUE</code> if the initialization didn't throw any errors or exceptions
     * or (2) <code>FALSE</code> if something happened during the initialization
     */
    protected abstract boolean initialize();

    /**
     * Checks a statement for faults, issues, overlaps,
     * deprecated calls and other issues.
     *
     * @param paramStatement The statement to check.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    protected abstract void queryValidation(StatementInterface paramStatement) throws SQLException;

    /**
     * Attempts to close the database connection.
     *
     * @return either (1) <code>TRUE</code> if the database connection was closed successfully
     * or (2) <code>FALSE</code> if the connection is already close, or an exception was thrown
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public final boolean close() throws SQLException {
        if (connection == null) {
            log.warn("Could not close connection, it is null.");
            return false;
        }

        try {
            connection.close();
            lastState = false;
            lastChecked = 0L;

            return true;
        } catch (SQLException e) {
            log.warn("Could not close connection, SQLException: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Returns the current database connection, if the connection is not open/active, it
     * will attempt to open the connection for you.
     *
     * @return the database connection
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public synchronized Connection getConnection() throws SQLException {
        if (!isOpen()) {
            open();
            lastState = true;
        }

        return connection;
    }

    /**
     * Checks to see if the database connection is still valid.
     *
     * @return either (1) <code>TRUE</code> if the database connection is open and valid
     * or (2) <code>FALSE</code> if the database connection is closed
     */
    public final boolean isOpen() {
        return isOpen(2);
    }

    /**
     * Checks to see if the database connection is still valid.
     *
     * @param seconds The amount of time to wait for the connection for.
     * @return either (1) <code>TRUE</code> if the database connection is open and valid
     * or (2) <code>FALSE</code> if the database connection is closed
     */
    public final synchronized boolean isOpen(int seconds) {
        if (connection != null) {
            // Returns the last state if the connection was checked less than three seconds ago.
            if (System.currentTimeMillis() - 5000 < lastChecked) {
                return lastState;
            }

            try {
                if (connection.isClosed()) {
                    return false;
                }

                lastState = connection.isValid(seconds);
                lastChecked = System.currentTimeMillis() - (lastState ? 0L : 250L);

                return lastState;
            } catch (SQLException e) {
                if (e instanceof MySQLNonTransientConnectionException) {
                    log.warn("Failed to check if the database connection is open due to a non transient connection exception!", e);
                }
                // If the exception type is anything else, we just ignore it.
            }
        }

        return false;
    }

    /**
     * Queries the database with the given query, the
     * query should be a <code>SELECT</code> query.
     *
     * @param query The query to run.
     * @return the current result as a <code>ResultSet</code> object or
     * <code>null</code> if the result is an update count or there are no more results
     * @throws SQLException if a database access error occurs or this method is called on a
     *                      closed <code>Statement</code>
     */
    @Nullable
    @WillCloseWhenClosed
    public final ResultSet query(String query) throws SQLException {
        return handleQuery(() -> {
            queryValidation(getStatement(query));

            Statement statement = createPreparedStatement(query);
            statement.closeOnCompletion();

            if (statement.execute(query)) {
                return statement.getResultSet();
            }
            throw new SQLException("The query failed to execute successfully: " + query);
        });
    }

    /**
     * Queries the database with the given query, the
     * query should be a <code>SELECT</code> query.
     *
     * @param query The query to run.
     * @return the current result as a <code>ResultSet</code> object or
     * <code>null</code> if the result is an update count or there are no more results
     * @throws SQLException if a database access error occurs or this method is called on a
     *                      closed <code>Statement</code>
     */
    @Nullable
    @WillCloseWhenClosed
    public final ResultSet query(QueryBuilder query) throws SQLException {
        return query(query.toSQL());
    }

    /**
     * Queries the database with the given prepared statement.
     *
     * @param query The prepared statement to run.
     * @return the current result as a <code>ResultSet</code> object or
     * <code>null</code> if the result is an update count or there are no more results
     * @throws SQLException if a database access error occurs or this method is called on a
     *                      closed <code>Statement</code>
     */
    @Nullable
    @WillNotClose
    public final ResultSet query(PreparedStatement query) throws SQLException {
        ResultSet output = query(query, preparedStatements.get(query));

        preparedStatements.remove(query);

        return output;
    }

    /**
     * Queries the database with the given prepared statement.
     *
     * @param query     The prepared statement to run.
     * @param statement The query statement.
     * @return the current result as a <code>ResultSet</code> object or
     * <code>null</code> if the result is an update count or there are no more results
     * @throws SQLException if a database access error occurs or this method is called on a
     *                      closed <code>Statement</code>
     */
    @Nullable
    @WillNotClose
    public final ResultSet query(PreparedStatement query, StatementInterface statement) throws SQLException {
        return handleQuery(() -> {
            queryValidation(statement);

            if (query.execute()) {
                return query.getResultSet();
            }
            throw new SQLException("The query failed to execute successfully: " + query);
        });
    }

    /**
     * Prepares a query as a prepared statement before executing it.
     *
     * @param query The query to prepare.
     * @return The JDBC prepared statement object for the given query.
     * @throws SQLException if a database access error occurs or this method is called on a
     *                      closed <code>Statement</code>
     */
    @WillNotClose
    public final Statement prepare(String query) throws SQLException {
        StatementInterface statement = getStatement(query);
        Statement ps = createPreparedStatement(query);

        if (ps instanceof PreparedStatement) {
            preparedStatements.put((PreparedStatement) ps, statement);
        }

        return ps;
    }

    /**
     * Executes the provided SQL statement as a <code>PreparedStatement</code> object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing, such as a DDL statement.
     * <p>
     * After the query has been executed, all the auto-generated keys created as a result of executing the
     * provided query will be retrieved. If the <code>Statement</code> object did not generate any keys, an
     * empty <code>List</code> object is returned.
     * <p>
     * <B>Note:</B>If the columns which represent the auto-generated keys were not specified,
     * the JDBC driver implementation will determine the columns which best represent the auto-generated keys.
     *
     * @param query The query to run
     * @return the list of auto-generated keys
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    @WillClose
    public final List<Long> insert(String query) throws SQLException {
        List<Long> keys = new ArrayList<>();

        try (PreparedStatement pstmt = createPreparedStatement(query, 1)) {
            ResultSet key = pstmt.getGeneratedKeys();
            if (key.next()) {
                keys.add(key.getLong(1));
            }

            return keys;
        }
    }

    /**
     * Executes the provided SQL statement as a <code>PreparedStatement</code> object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing, such as a DDL statement.
     * <p>
     * After the query has been executed, all the auto-generated keys created as a result of executing the
     * provided query will be retrieved. If the <code>Statement</code> object did not generate any keys, an
     * empty <code>List</code> object is returned.
     * <p>
     * <B>Note:</B>If the columns which represent the auto-generated keys were not specified,
     * the JDBC driver implementation will determine the columns which best represent the auto-generated keys.
     *
     * @param query The query to run
     * @return the list of auto-generated keys
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    @WillNotClose
    public final ArrayList<Long> insert(PreparedStatement query) throws SQLException {
        preparedStatements.remove(query);

        ArrayList<Long> keys = new ArrayList<>();
        ResultSet key = query.getGeneratedKeys();
        if (key.next()) {
            keys.add(key.getLong(1));
        }

        return keys;
    }

    @Nullable
    private ResultSet handleQuery(SupplierWithSQL<ResultSet> callback) throws SQLException {
        try {
            return callback.get();
        } catch (MySQLNonTransientConnectionException e) {
            if (e.getMessage().contains("connection closed")) {
                log.error("Attempted to run a query after the connection was closed, closing and re-opening the connection.", e);

                // The connection should already be closed, we're just forcefully close the
                // connection here so that the database manage can see that connection is
                // closed, and so the connection can be reopened on the next request.
                close();
            }
            return null;
        }
    }

    protected Statement createPreparedStatement(String query) throws SQLException {
        Metrics.databaseQueries.labels(query.split(" ")[0].toUpperCase()).inc();

        return getConnection().prepareStatement(query);
    }

    private PreparedStatement createPreparedStatement(String query, int autoGeneratedKeys) throws SQLException {
        Metrics.databaseQueries.labels(query.split(" ")[0].toUpperCase()).inc();

        return getConnection().prepareStatement(query, autoGeneratedKeys);
    }

    protected String setupAndRun(TableGrammar grammar, QueryBuilder builder, DatabaseManager manager, Map<String, Boolean> options) {
        grammar.setDBM(manager);
        grammar.setOptions(options);

        return grammar.format(builder);
    }

    protected String setupAndRun(AlterGrammar grammar, Blueprint blueprint, DatabaseManager manager, Map<String, Boolean> options) {
        grammar.setDBM(manager);
        grammar.setOptions(options);

        return grammar.format(blueprint);
    }

    public enum QueryType {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        CREATE
    }
}
