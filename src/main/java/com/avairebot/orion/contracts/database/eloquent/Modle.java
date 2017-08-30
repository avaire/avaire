package com.avairebot.orion.contracts.database.eloquent;

import com.avairebot.orion.contracts.eloquent.QueryScope;
import com.avairebot.orion.database.DatabaseManager;
import com.avairebot.orion.database.collection.Collection;
import com.avairebot.orion.database.query.QueryBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Modle extends DefaultFields {

    protected final QueryBuilder builder;

    public Modle(DatabaseManager dbm) {
        builder = new QueryBuilder(dbm, table());
    }

    @Override
    public Modle instance() {
        return this;
    }

    public Collection find(int id) throws SQLException {
        builder.table(table()).where(primaryKey(), id);

        return get();
    }

    public Collection all() throws SQLException {
        builder.table(table()).selectAll();

        return get();
    }

    public Modle select(String... columns) {
        builder.select(columns);

        return this;
    }

    public Modle where(String column, Object field) {
        return where(column, "=", field);
    }

    public Modle where(String column, String identifier, Object field) {
        builder.where(column, identifier, field);

        return this;
    }

    public Modle andWhere(String column, Object field) {
        return andWhere(column, "=", field);
    }

    public Modle andWhere(String column, String identifier, Object field) {
        builder.andWhere(column, identifier, field);

        return this;
    }

    public Modle orWhere(String column, Object field) {
        return orWhere(column, "=", field);
    }

    public Modle orWhere(String column, String identifier, Object field) {
        builder.orWhere(column, identifier, field);

        return this;
    }

    public Modle leftJoin(String table, String one, String two) {
        builder.leftJoin(table, one, two);

        return this;
    }

    public Modle leftJoin(String table, String one, String identifier, String two) {
        builder.leftJoin(table, one, identifier, two);

        return this;
    }

    public Modle rightJoin(String table, String one, String two) {
        builder.rightJoin(table, one, two);

        return this;
    }

    public Modle rightJoin(String table, String one, String identifier, String two) {
        builder.rightJoin(table, one, identifier, two);

        return this;
    }

    public Modle innerJoin(String table, String one, String two) {
        builder.innerJoin(table, one, two);

        return this;
    }

    public Modle innerJoin(String table, String one, String identifier, String two) {
        builder.innerJoin(table, one, identifier, two);

        return this;
    }

    public Modle outerJoin(String table, String one, String two) {
        builder.outerJoin(table, one, two);

        return this;
    }

    public Modle outerJoin(String table, String one, String identifier, String two) {
        builder.outerJoin(table, one, identifier, two);

        return this;
    }

    public Modle with(String... fields) {
        List<String> methods = new ArrayList<>();
        methods.addAll(Arrays.asList(fields));

        Class<? extends Modle> iClass = instance().getClass();

        for (Method method : iClass.getMethods()) {
            if (!methods.contains(method.getName())) {
                continue;
            }

            if (method.getAnnotations().length == 0) {
                continue;
            }

            boolean isQueryHandler = false;
            for (Annotation an : method.getAnnotations()) {
                if (an.annotationType().getName().equals(QueryScope.class.getName())) {
                    isQueryHandler = true;
                    break;
                }
            }

            if (isQueryHandler) {
                try {
                    method.setAccessible(true);
                    method.invoke(instance(), builder);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Modle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return this;
    }

    public Collection get() throws SQLException {
        return builder.get();
    }

    @Override
    public String toString() {
        return builder.toSQL();
    }
}
