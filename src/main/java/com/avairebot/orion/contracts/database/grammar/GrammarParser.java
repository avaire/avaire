package com.avairebot.orion.contracts.database.grammar;

import com.avairebot.orion.contracts.database.Database;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.connections.MySQL;
import com.avairebot.orion.database.connections.SQLite;
import com.avairebot.orion.database.query.QueryBuilder;
import com.avairebot.orion.database.schema.Blueprint;

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
            if (connection instanceof MySQL) {
                return MySQL;
            }

            if (connection instanceof SQLite) {
                return MySQL;
            }

            return null;
        }
    }
}
