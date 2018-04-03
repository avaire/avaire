package com.avairebot.contracts.database.grammar;

import com.avairebot.contracts.database.Database;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.schema.Blueprint;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class GrammarParser {
    protected Map<String, Boolean> options = new HashMap<>();

    public String parse(DatabaseManager manager, QueryBuilder query) throws SQLException {
        return null;
    }

    public String parse(DatabaseManager manager, Blueprint blueprint) throws SQLException {
        return null;
    }

    public void setOption(String option, boolean value) {
        this.options.put(option, value);
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

    protected enum ConnectionType {
        MySQL,
        SQLite;

        public static ConnectionType getType(Database connection) {
            if (connection instanceof com.avairebot.database.connections.MySQL) {
                return MySQL;
            }

            if (connection instanceof com.avairebot.database.connections.SQLite) {
                return SQLite;
            }

            return null;
        }
    }
}
