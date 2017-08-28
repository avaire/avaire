package com.avairebot.orion.contracts.database;

import com.avairebot.orion.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDatabase implements DatabaseInterface {

    protected DatabaseManager dbm = null;

    /**
     * Represents our prepared query statements and their statement
     * type, allowing us to quickly render and compile statements.
     */
    protected Map<PreparedStatement, StatementInterface> preparedStatements = new HashMap();

    /**
     * Represents our current database connection, this is
     * used to send queries to the database, as well as
     * fetch, and persist data.
     */
    protected Connection connection;

    /**
     * Represents a unix timestamp of the last time we
     * communicated with the database.
     */
    protected int lastUpdate;

    /**
     * Sets the Database Manager instance to the database.
     *
     * @param dbm The database manager class instance.
     */
    public void setDatabaseManager(DatabaseManager dbm) {
        this.dbm = dbm;
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
            dbm.getOrion().logger.warning("Database - Could not close connection, it is null.");
            return false;
        }

        try {
            connection.close();

            return true;
        } catch (SQLException e) {
            dbm.getOrion().logger.warning("Database - Could not close connection, SQLException: %s", e.getMessage());
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
    public Connection getConnection() throws SQLException {
        if (!isOpen()) {
            open();
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
        return isOpen(1);
    }

    /**
     * Checks to see if the database connection is still valid.
     *
     * @param seconds The amount of time to wait for the connection for.
     * @return either (1) <code>TRUE</code> if the database connection is open and valid
     * or (2) <code>FALSE</code> if the database connection is closed
     */
    public final boolean isOpen(int seconds) {
        if (connection != null) {
            try {
                if (connection.isValid(seconds)) {
                    return true;
                }
            } catch (SQLException e) {
            }
        }

        return false;
    }

    /**
     * Get the unix timestamp of the last time the class
     * communicated with the database.
     *
     * @return the last updated timestamp
     */
    public final int getLastUpdateCount() {
        return lastUpdate;
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
    public final synchronized ResultSet query(String query) throws SQLException {
        queryValidation(getStatement(query));

        Statement statement = createPreparedStatement(query);

        if (statement.execute(query)) {
            return statement.getResultSet();
        }

        return getConnection().createStatement().executeQuery("SELECT " + (lastUpdate = statement.getUpdateCount()));
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
    public final synchronized ResultSet query(PreparedStatement query, StatementInterface statement) throws SQLException {
        queryValidation(statement);

        if (query.execute()) {
            return query.getResultSet();
        }

        return getConnection().createStatement().executeQuery("SELECT " + (lastUpdate = query.getUpdateCount()));
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
    public final synchronized ResultSet query(PreparedStatement query) throws SQLException {
        ResultSet output = query(query, (StatementInterface) preparedStatements.get(query));

        preparedStatements.remove(query);

        return output;
    }

    /**
     * Prepares a query as a prepared statement before executing it.
     *
     * @param query The query to prepare.
     * @return the current result as a <code>ResultSet</code> object or
     * <code>null</code> if the result is an update count or there are no more results
     * @throws SQLException if a database access error occurs or this method is called on a
     *                      closed <code>Statement</code>
     */
    public final synchronized Statement prepare(String query) throws SQLException {
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
    public final synchronized ArrayList<Long> insert(String query) throws SQLException {
        ArrayList<Long> keys = new ArrayList();

        PreparedStatement pstmt = createPreparedStatement(query, 1);
        lastUpdate = pstmt.executeUpdate();

        ResultSet key = pstmt.getGeneratedKeys();
        if (key.next()) {
            keys.add(key.getLong(1));
        }

        return keys;
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
    public final synchronized ArrayList<Long> insert(PreparedStatement query) throws SQLException {
        lastUpdate = query.executeUpdate();
        preparedStatements.remove(query);

        ArrayList<Long> keys = new ArrayList();
        ResultSet key = query.getGeneratedKeys();
        if (key.next()) {
            keys.add(key.getLong(1));
        }

        return keys;
    }

    protected Statement createPreparedStatement(String query) throws SQLException {
        return getConnection().prepareStatement(query);
    }

    private PreparedStatement createPreparedStatement(String query, int autoGeneratedKeys) throws SQLException {
        return getConnection().prepareStatement(query, autoGeneratedKeys);
    }
}
