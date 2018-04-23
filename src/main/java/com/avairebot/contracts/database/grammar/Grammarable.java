package com.avairebot.contracts.database.grammar;

import com.avairebot.database.DatabaseManager;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.schema.Blueprint;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

public interface Grammarable {

    String create(DatabaseManager manager, Blueprint blueprint, @Nonnull Map<String, Boolean> options) throws SQLException;

    String delete(DatabaseManager manager, QueryBuilder query, @Nullable Map<String, Boolean> options) throws SQLException;

    String insert(DatabaseManager manager, QueryBuilder query, @Nullable Map<String, Boolean> options) throws SQLException;

    String select(DatabaseManager manager, QueryBuilder query, @Nullable Map<String, Boolean> options) throws SQLException;

    String update(DatabaseManager manager, QueryBuilder query, @Nullable Map<String, Boolean> options) throws SQLException;
}
