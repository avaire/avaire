package com.avairebot.orion.database;

import com.avairebot.orion.Orion;
import com.avairebot.orion.config.MainConfiguration;
import com.avairebot.orion.contracts.database.AbstractDatabase;
import com.avairebot.orion.database.connections.MySQL;
import com.avairebot.orion.database.exceptions.DatabaseException;

import java.sql.SQLException;

public class DatabaseManager {

    private final Orion orion;

    private AbstractDatabase connection = null;

    public DatabaseManager(Orion orion) {
        this.orion = orion;
    }

    public Orion getOrion() {
        return orion;
    }

    public AbstractDatabase getConnection() throws SQLException, DatabaseException {
        if (connection == null) {
            MainConfiguration.Database configDatabase = orion.config.getDatabase();

            switch (configDatabase.getType().toLowerCase()) {
                case "mysql":
                    connection = new MySQL(
                            configDatabase.getHostname(),
                            configDatabase.getDatabase(),
                            configDatabase.getUsername(),
                            configDatabase.getPassword()
                    );
                    break;

                default:
                    throw new DatabaseException("Invalid database type given, failed to create a new database connection.");
            }

            connection.setDatabaseManager(this);
        }

        if (connection.isOpen()) {
            return connection;
        }

        if (!connection.open()) {
            throw new DatabaseException("Failed to connect to the database.");
        }

        return connection;
    }
}
