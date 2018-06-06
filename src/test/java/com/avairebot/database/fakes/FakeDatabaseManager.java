package com.avairebot.database.fakes;

import com.avairebot.contracts.database.Database;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.exceptions.DatabaseException;

import java.sql.SQLException;

public class FakeDatabaseManager extends DatabaseManager {

    public FakeDatabaseManager() {
        super(null);
    }

    @Override
    public Database getConnection() throws SQLException, DatabaseException {
        return new FakeMySQLConnection();
    }
}
