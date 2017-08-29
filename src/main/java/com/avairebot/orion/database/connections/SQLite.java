package com.avairebot.orion.database.connections;

import com.avairebot.orion.contracts.database.StatementInterface;
import com.avairebot.orion.contracts.database.connections.FilenameDatabase;
import com.avairebot.orion.database.exceptions.DatabaseException;

import java.sql.*;

public class SQLite extends FilenameDatabase {

    public SQLite(String directory, String database) {
        super(directory, database.split("\\.")[0], database.split("\\.")[1]);
    }

    /**
     * Creates a new SQLite database connection.
     *
     * @param directory The folder the database is located in.
     * @param filename  The name of the database file.
     * @param extension The extension of the database file.
     */
    public SQLite(String directory, String filename, String extension) {
        super(directory, filename, extension);
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
            } catch (SQLException e) {
                String reason = "DBM - Could not establish an SQLite connection, SQLException: " + e.getMessage();

                dbm.getOrion().logger.exception(reason, e);
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
        } catch (SQLException e) {
            if ((!e.getMessage().toLowerCase().contains("locking")) && (!e.getMessage().toLowerCase().contains("locked")) && (!e.toString().contains("not return ResultSet"))) {
                dbm.getOrion().logger.exception("DBM - Error in truncate() query: ", e);
            }
        }

        return false;
    }

    @Override
    protected Statement createPreparedStatement(String query) throws SQLException {
        Statement statement = getConnection().createStatement();

        statement.setQueryTimeout(-1);
        statement.setMaxRows(-1);

        return statement;
    }
}
