package com.avairebot.database.schema;

import com.avairebot.AvaIre;
import com.avairebot.Statistics;
import com.avairebot.contracts.database.schema.DatabaseClosure;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.grammar.CreateParser;
import com.avairebot.database.query.QueryType;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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

    public DatabaseManager getDbm() {
        return dbm;
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
        Statistics.addQueries();

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
        Statistics.addQueries();

        return getMetaData().getColumns(null, null, table, column).next();
    }

    /**
     * Creates a new table using the {@link DatabaseClosure} and {@link Blueprint} classes.
     *
     * @param table   The table that should be created
     * @param closure The database closure that creates the blueprint
     * @return <code>TRUE</code> if the table was created successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean create(String table, DatabaseClosure closure) throws SQLException {
        Blueprint blueprint = createAndRunBlueprint(table, closure);
        CreateParser grammar = createGrammar(true);
        Statistics.addQueries();

        String query = grammar.parse(dbm, blueprint);
        Statement stmt = dbm.getConnection().prepare(query);

        if (stmt instanceof PreparedStatement) {
            return !((PreparedStatement) stmt).execute();
        }

        return !stmt.execute(query);
    }

    /**
     * Creates a new table if it doesn't exists using the {@link DatabaseClosure} and {@link Blueprint} classes.
     *
     * @param table   The table that should be created
     * @param closure The database closure that creates the blueprint
     * @return <code>TRUE</code> if the table was created successfully, <code>FALSE</code> otherwise.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean createIfNotExists(String table, DatabaseClosure closure) throws SQLException {
        if (dbm.getConnection().hasTable(table)) {
            return false;
        }

        Blueprint blueprint = createAndRunBlueprint(table, closure);
        CreateParser grammar = createGrammar(false);
        Statistics.addQueries();

        String query = grammar.parse(dbm, blueprint);
        Statement stmt = dbm.getConnection().prepare(query);

        if (stmt instanceof PreparedStatement) {
            return !((PreparedStatement) stmt).execute();
        }

        return !stmt.execute(query);
    }

    /**
     * Creates and runs a blueprint for the provided closure for the given table.
     *
     * @param table   The table the blueprint should be created for
     * @param closure The closure that should run the blueprint
     * @return The blueprint that was created.
     */
    private Blueprint createAndRunBlueprint(String table, DatabaseClosure closure) {
        Blueprint blueprint = new Blueprint(table);

        closure.run(blueprint);

        return blueprint;
    }

    /**
     * Creates a {@link QueryType#CREATE} grammar instance with the provided settings.
     *
     * @param shouldIgnoreExistingTable Determines if the grammar instance should ignore existing tables
     * @return The {@link QueryType#CREATE} grammar instance.
     */
    private CreateParser createGrammar(boolean shouldIgnoreExistingTable) throws SQLException {
        try {
            CreateParser grammar = (CreateParser) QueryType.CREATE.getGrammar().newInstance();
            grammar.setOption("ignoreExistingTable", shouldIgnoreExistingTable);

            return grammar;
        } catch (InstantiationException ex) {
            AvaIre.getLogger().error("Invalid grammar object parsed, failed to create a new instance!", ex);
        } catch (IllegalAccessException ex) {
            AvaIre.getLogger().error("An attempt was made to create a grammar instance on an object that is not accessible!", ex);
        }

        throw new SQLException("Failed to create grammar instance for query building");
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
        if (!hasTable(from)) {
            return false;
        }

        return rename(from, to);
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
        Statistics.addQueries();
        Statement stmt = dbm.getConnection().getConnection().createStatement();

        return !stmt.execute(query);
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
