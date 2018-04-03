package com.avairebot.database.grammar;

import com.avairebot.contracts.database.grammar.GrammarParser;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.query.QueryBuilder;

import java.sql.SQLException;

public class UpdateParser extends GrammarParser {

    @Override
    public String parse(DatabaseManager manager, QueryBuilder query) throws SQLException {
        switch (ConnectionType.getType(manager.getConnection())) {
            case MySQL:
                return setupAndRun(new com.avairebot.database.grammar.mysql.Update(), query, manager, options);

            case SQLite:
                return setupAndRun(new com.avairebot.database.grammar.sqlite.Update(), query, manager, options);

            default:
                return null;
        }
    }
}
