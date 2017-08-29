package com.avairebot.orion.database.schema;

import com.avairebot.orion.database.DatabaseManager;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Schema {

    /**
     * The DBM main instance.
     */
    private final DatabaseManager dbm;

    /**
     * Creates a new Schematic instance for the provided DBM instance.
     *
     * @param dbm The DMM instance the schema instance should be created for
     */
    public Schema(DatabaseManager dbm) {
        this.dbm = dbm;
    }

    /**
     * Checks if the default connection has the provided table name.
     *
     * @param table The table to check if exists
     * @return <code>TRUE</code> if the table exists, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean hasTable(String table) throws SQLException {
        return dbm.getConnection().hasTable(table);
    }

    /**
     * Checks if the default connection has the provided column for the given table.
     *
     * @param table  The table to use
     * @param column The column to check if exists
     * @return <code>TRUE</code> if the column exists, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean hasColumn(String table, String column) throws SQLException {
        return getMetaData().getColumns(null, null, table, column).next();
    }

    /**
     * Drops the provided table, if the table doesn't exist an exception will be thrown.
     *
     * @param table The table that should be dropped
     * @return <code>TRUE</code> if the table was dropped successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean drop(String table) throws SQLException {
        return alterQuery(format("DROP TABLE `%s`;", table));
    }

    /**
     * Drops the provided table if it exists.
     *
     * @param table The table that should be dropped
     * @return <code>TRUE</code> if the table was dropped successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean dropIfExists(String table) throws SQLException {
        return alterQuery(format("DROP TABLE IF EXISTS `%s`;", table));
    }


    /**
     * Renames the provided table to the new name using the database prefix, if
     * the table doesn't exist an exception will be thrown.
     * <p>
     * <Strong>Note:</strong> Both names are affected by the database prefix.
     *
     * @param from The tables current name
     * @param to   The name the table should be renamed to
     * @return <code>TRUE</code> if the table was renamed successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean rename(String from, String to) throws SQLException {
        return alterQuery(format("ALTER TABLE `%s` RENAME `%s`;", from, to));
    }

    /**
     * Renames the provided table to the new name if it exists, if the table doesn't exist nothing will happen.
     * <p>
     * <Strong>Note:</strong> If <code>FALSE</code> is parsed to the ignoreDatabasePrefix
     * parameter, both names are affected by the database prefix.
     *
     * @param from The tables current name
     * @param to   The name the table should be renamed to
     * @return <code>TRUE</code> if the table was renamed successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean renameIfExists(String from, String to) throws SQLException {
        return hasTable(from) && rename(from, to);

    }

    /**
     * Creates an alter query statement for the default database connection, and then executes
     * the provided query using the {@link Statement#execute(java.lang.String) } method.
     *
     * @param query The query that should be executed
     * @return <code>TRUE</code> if the query affected any rows/tables/columns successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    private boolean alterQuery(String query) throws SQLException {
        return !dbm.getConnection().getConnection().createStatement().execute(query);
    }

    /**
     * Formats the provided string using the varargs items object array and
     * the {@link String#format(java.lang.String, java.lang.Object...) } method.
     *
     * @param query The string to format
     * @param items The varargs items to format the string with
     * @return The formatted string.
     */
    private String format(String query, Object... items) {
        return String.format(query, items);
    }

    /**
     * Gets the database meta data object from the default database connection.
     *
     * @return The database meta data object.
     * @throws SQLException
     */
    private DatabaseMetaData getMetaData() throws SQLException {
        return dbm.getConnection().getConnection().getMetaData();
    }
}