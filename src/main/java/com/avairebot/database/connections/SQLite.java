package com.avairebot.database.connections;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.StatementInterface;
import com.avairebot.contracts.database.connections.FilenameDatabase;
import com.avairebot.database.exceptions.DatabaseException;

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
        Statement statement = getConnection().createStatement();

        statement.setQueryTimeout(-1);
        statement.setMaxRows(-1);

        return statement;
    }
}
