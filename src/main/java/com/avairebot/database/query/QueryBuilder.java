/*
 * Copyright (c) 2018.
 *
 * This file is part of AvaIre.
 *
 * AvaIre is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AvaIre is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AvaIre.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avairebot.database.query;

import com.avairebot.contracts.database.Database;
import com.avairebot.contracts.database.Database.QueryType;
import com.avairebot.contracts.database.QueryClause;
import com.avairebot.contracts.database.query.ChangeableClosure;
import com.avairebot.contracts.database.query.ClauseConsumer;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.collection.Collection;
import com.avairebot.scheduler.ScheduleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.util.*;

public final class QueryBuilder {

    private static final Logger log = LoggerFactory.getLogger(QueryBuilder.class);

    /**
     * The instance of the {@link DatabaseManager}.
     */
    private final DatabaseManager dbm;

    /**
     * The list of {@link QueryOrder} clauses that should be used by the generator.
     */
    private final List<QueryOrder> order = new ArrayList<>();

    /**
     * The list of {@link Clause} clauses that should be used by the generator.
     */
    private final List<QueryClause> wheres = new ArrayList<>();

    /**
     * The list of {@link String} clauses that should be used by the generator.
     */
    private final List<String> columns = new ArrayList<>();

    /**
     * The list of {@link JoinClause} objects that should be used by the generator.
     */
    private final List<JoinClause> joins = new ArrayList<>();

    /**
     * The list of {@link Map} objects that should be used by the generator, containing
     * the column name as the key, and value for the column as a map value.
     */
    private final List<Map<String, Object>> items = new ArrayList<>();

    /**
     * The query type that's being preformed.
     */
    private QueryType type;

    /**
     * The table the query builder to used.
     */
    private String table = null;

    /**
     * The amount of rows to take(LIMIT), if the value is set
     * to -1 it should be ignored by the grammar generator.
     */
    private int take = -1;

    /**
     * The amount of rows to skip(OFFSET), if the value is set
     * to -1 it should be ignored by the grammar generator.
     */
    private int skip = -1;

    /**
     * Determines if the query should be executed async or not, if the query is
     * executed async it will not return any results, or throw any errors.
     */
    private boolean async = false;

    /**
     * Creates a new Query Builder instance.
     *
     * @param dbm The database manager instance
     */
    public QueryBuilder(DatabaseManager dbm) {
        this.dbm = dbm;

        table(table);
    }

    /**
     * Creates a new Query Builder instance for the provided table.
     * <p>
     * This calls the {@link #table(java.lang.String) } method behind the scene.
     *
     * @param dbm   The database manager instance
     * @param table The table the query builder should be generated for
     */
    public QueryBuilder(DatabaseManager dbm, String table) {
        this.dbm = dbm;

        table(table);
    }

    /**
     * Sets the table that the query builder should be using, and sets the select state to ALL.
     * This is the same as calling {@link #selectAll() } and {@link #from(java.lang.String) } in one go.
     *
     * @param table The table the query builder should be using
     * @return the query builder instance.
     */
    public QueryBuilder table(String table) {
        return selectAll().from(table);
    }

    /**
     * Sets the table the query should be generated for.
     *
     * @param table The table the query should be generated for
     * @return the query builder instance.
     */
    public QueryBuilder from(String table) {
        this.table = table;

        return this;
    }

    /**
     * Gets the table the query should be generated for.
     *
     * @return the table the query should be generated for.
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the select state to ALL using the star(*) symbol.
     *
     * @return the query builder instance.
     */
    public QueryBuilder selectAll() {
        return select("*");
    }

    /**
     * Selects the provided columns from the varargs columns object, columns
     * parsed will automatically be formatted to SQL fields using the grave
     * accent character(`), using the keyword <code>AS</code> will use the
     * SQL AS to rename the output the collection will end up using.
     * <p>
     * Example calling: <code>select("username as name", "email", ...)</code>
     * <p>
     * Would re-name the username field in the table to name in the collection object.
     *
     * @param columns The varargs list of columns that should be selected.
     * @return the query builder instance.
     */
    public QueryBuilder select(String... columns) {
        type = QueryType.SELECT;

        for (String column : columns) {
            addColumn(column);
        }

        return this;
    }

    /**
     * Creates a raw select statement, allowing you to parse in raw SQL that
     * the generator won't modify or affect in any way.
     *
     * @param select The raw SQL select statement.
     * @return the query builder instance.
     */
    public QueryBuilder selectRaw(String select) {
        type = QueryType.SELECT;

        columns.clear();
        columns.add("RAW:" + select.trim());

        return this;
    }

    /**
     * Adds a column that should be selected.
     *
     * @param column The column that should be selected
     */
    protected void addColumn(String column) {
        if (!column.equals("*")) {
            columns.remove("*");

            if (!columns.contains(column)) {
                columns.add(column);
            }

            return;
        }

        columns.clear();
        columns.add("*");
    }

    /**
     * Gets the columns that should be selected in a {@link #get() } request.
     *
     * @return the columns that should be selected.
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Sets the amount of rows to skip using the SQL <code>OFFSET</code>.
     *
     * @param skip The amount of rows to skip
     * @return the query builder instance.
     */
    public QueryBuilder skip(int skip) {
        this.skip = Math.max(skip, 0);

        return this;
    }

    /**
     * Removes the skip(SQL <code>OFFSET</code>) from the query builder.
     *
     * @return the query builder instance.
     */
    public QueryBuilder removeSkip() {
        this.skip = -1;

        return this;
    }

    /**
     * Gets the amount to skip(SQL <code>OFFSET</code>) in the SQL query.
     *
     * @return the amount to skip.
     */
    public int getSkip() {
        return skip;
    }

    /**
     * Sets the amount of rows to take using the SQL <code>LIMIT</code>.
     *
     * @param take The amount of rows to take
     * @return the query builder instance.
     */
    public QueryBuilder take(int take) {
        this.take = Math.max(take, 0);

        return this;
    }

    /**
     * Removes the take(SQL <code>LIMIT</code>) from the query builder.
     *
     * @return the query builder instance.
     */
    public QueryBuilder removeTake() {
        this.take = -1;

        return this;
    }

    /**
     * Gets the amount to take(SQL <code>LIMIT</code>) in the SQL query.
     *
     * @return the amount to take.
     */
    public int getTake() {
        return take;
    }

    /**
     * Creates a SQL WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return the query builder instance.
     */
    public QueryBuilder where(String column, Object value) {
        return where(column, "=", value);
    }

    /**
     * Creates a SQL WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return the query builder instance.
     */
    public QueryBuilder where(String column, String operator, Object value) {
        wheres.add(new Clause(column, operator, value));

        return this;
    }

    public QueryBuilder where(ClauseConsumer consumer) {
        NestedClause clause = new NestedClause(dbm);

        consumer.build(clause);

        wheres.add(clause);

        return this;
    }

    /**
     * Creates a SQL AND WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return the query builder instance.
     */
    public QueryBuilder andWhere(String column, Object value) {
        return andWhere(column, "=", value);
    }

    /**
     * Creates a SQL AND WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return the query builder instance.
     */
    public QueryBuilder andWhere(String column, String operator, Object value) {
        wheres.add(new Clause(column, operator, value, OperatorType.AND));

        return this;
    }

    public QueryBuilder andWhere(ClauseConsumer consumer) {
        NestedClause clause = new NestedClause(dbm, OperatorType.AND);

        consumer.build(clause);

        wheres.add(clause);

        return this;
    }

    /**
     * Creates a SQL OR WHERE clause with an equal operator.
     *
     * @param column The column to use in the clause
     * @param value  The value to compare the column to
     * @return the query builder instance.
     */
    public QueryBuilder orWhere(String column, Object value) {
        return orWhere(column, "=", value);
    }

    /**
     * Creates a SQL OR WHERE clause with the provided operator.
     *
     * @param column   The column to use in the clause
     * @param operator The operator to compare with
     * @param value    The value to compare the column to
     * @return the query builder instance.
     */
    public QueryBuilder orWhere(String column, String operator, Object value) {
        wheres.add(new Clause(column, operator, value, OperatorType.OR));

        return this;
    }

    public QueryBuilder orWhere(ClauseConsumer consumer) {
        NestedClause clause = new NestedClause(dbm, OperatorType.OR);

        consumer.build(clause);

        wheres.add(clause);

        return this;
    }

    /**
     * Gets the list where clauses that should be generated.
     *
     * @return the list of where clauses that should be generated.
     */
    public List<QueryClause> getWhereClauses() {
        return wheres;
    }

    /**
     * Creates a SQL ORDER BY clause, ordering by ascending order.
     *
     * @param field The field the query should be ordered by
     * @return the query builder instance.
     */
    public QueryBuilder orderBy(String field) {
        return orderBy(field, "ASC");
    }

    /**
     * Creates a SQL ORDER BY clause, ordering by the provided type.
     *
     * @param field The field the query should be ordered by
     * @param type  The type to order the query by
     * @return the query builder instance.
     */
    public QueryBuilder orderBy(String field, String type) {
        order.add(new QueryOrder(field, type));

        return this;
    }

    /**
     * Creates a random query order statement.
     *
     * @return the query builder instance.
     */
    public QueryBuilder inRandomOrder() {
        order.add(new QueryOrder("RAND()", null, true));

        return this;
    }

    /**
     * Gets the list of order clauses that should be used in the generated query.
     *
     * @return the list of order clauses.
     */
    public List<QueryOrder> getOrder() {
        return order;
    }

    /**
     * Creates a JOIN clause on the provided table of the given type, once the join clause
     * has been created, a {@link JoinClause} object will be returned to help specify
     * what the clause should be bound to.
     *
     * @param table The table the join clause should be used on
     * @param type  The type of the join clause
     * @return the join clause that was created.
     */
    public JoinClause join(String table, String type) {
        JoinClause join = new JoinClause(type, table);

        joins.add(join);

        return join;
    }

    /**
     * Creates a LEFT JOIN clause on the provided table, once the join clause has been
     * created, a {@link JoinClause} object will be returned to help specify
     * what the clause should be bound to.
     *
     * @param table The table the join clause should be used on
     * @return the join clause that was created.
     */
    public JoinClause leftJoin(String table) {
        return join(table, "left");
    }

    /**
     * Creates a LEFT JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder leftJoin(String table, String one, String two) {
        JoinClause join = leftJoin(table);

        join.on(one, two);

        return this;
    }

    /**
     * Creates a LEFT JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder leftJoin(String table, String one, String operator, String two) {
        JoinClause join = leftJoin(table);

        join.on(one, operator, two);

        return this;
    }

    /**
     * Creates a RIGHT JOIN clause on the provided table, once the join clause has been
     * created, a {@link JoinClause} object will be returned to help specify
     * what the clause should be bound to.
     *
     * @param table The table the join clause should be used on
     * @return the join clause that was created.
     */
    public JoinClause rightJoin(String table) {
        return join(table, "right");
    }

    /**
     * Creates a RIGHT JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder rightJoin(String table, String one, String two) {
        JoinClause join = rightJoin(table);

        join.on(one, two);

        return this;
    }

    /**
     * Creates a RIGHT JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder rightJoin(String table, String one, String operator, String two) {
        JoinClause join = rightJoin(table);

        join.on(one, operator, two);

        return this;
    }

    /**
     * Creates a INNER JOIN clause on the provided table, once the join clause has been
     * created, a {@link JoinClause} object will be returned to help specify
     * what the clause should be bound to.
     *
     * @param table The table the join clause should be used on
     * @return the join clause that was created.
     */
    public JoinClause innerJoin(String table) {
        return join(table, "inner");
    }

    /**
     * Creates a INNER JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder innerJoin(String table, String one, String two) {
        JoinClause join = innerJoin(table);

        join.on(one, two);

        return this;
    }

    /**
     * Creates a INNER JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder innerJoin(String table, String one, String operator, String two) {
        JoinClause join = innerJoin(table);

        join.on(one, operator, two);

        return this;
    }

    /**
     * Creates a OUTER JOIN clause on the provided table, once the join clause has been
     * created, a {@link JoinClause} object will be returned to help specify
     * what the clause should be bound to.
     *
     * @param table The table the join clause should be used on
     * @return the join clause that was created.
     */
    public JoinClause outerJoin(String table) {
        return join(table, "outer");
    }

    /**
     * Creates a OUTER JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder outerJoin(String table, String one, String two) {
        JoinClause join = outerJoin(table);

        join.on(one, two);

        return this;
    }

    /**
     * Creates a OUTER JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder outerJoin(String table, String one, String operator, String two) {
        JoinClause join = outerJoin(table);

        join.on(one, operator, two);

        return this;
    }

    /**
     * Creates a FULL JOIN clause on the provided table, once the join clause has been
     * created, a {@link JoinClause} object will be returned to help specify
     * what the clause should be bound to.
     *
     * @param table The table the join clause should be used on
     * @return the join clause that was created.
     */
    public JoinClause fullJoin(String table) {
        return join(table, "full");
    }

    /**
     * Creates a FULL JOIN clause on the provided table, using the equal operator.
     *
     * @param table The table the join clause should be used on
     * @param one   The first field to bind on
     * @param two   The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder fullJoin(String table, String one, String two) {
        JoinClause join = fullJoin(table);

        join.on(one, two);

        return this;
    }

    /**
     * Creates a FULL JOIN clause on the provided table, using the provided operator.
     *
     * @param table    The table the join clause should be used on
     * @param one      The first field to bind on
     * @param operator The operator to compare with
     * @param two      The second field to bind on
     * @return the query builder instance.
     */
    public QueryBuilder fullJoin(String table, String one, String operator, String two) {
        JoinClause join = fullJoin(table);

        join.on(one, operator, two);

        return this;
    }

    /**
     * Gets a list of all the JOIN clauses.
     *
     * @return a list of all the JOIN clauses.
     */
    public List<JoinClause> getJoins() {
        return joins;
    }

    /**
     * Sets the async status for the query, if async is set to true the query will be executed on a separate
     * thread, only <code>update</code>, <code>insert</code>, and <code>delete</code> queries can be
     * executed as async, when async is enabled for a query it will not return any response or
     * throw any exceptions that can be catched.
     *
     * @param async The query async value.
     * @return the query builder instance.
     */
    public QueryBuilder useAsync(boolean async) {
        this.async = async;

        return this;
    }

    /**
     * Creates the grammar instance and builds the SQL query, if an error occurs
     * while building the query <code>NULL</code> will be returned instead.
     *
     * @return either (1) the generated SQL query
     * or (2) <code>NULL</code> if an error occurred.
     */
    public String toSQL() {
        try {
            switch (type) {
                case SELECT:
                    return dbm.getConnection().select(dbm, this, null);
                case INSERT:
                    return dbm.getConnection().insert(dbm, this, null);
                case UPDATE:
                    return dbm.getConnection().update(dbm, this, null);
                case DELETE:
                    return dbm.getConnection().delete(dbm, this, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Runs the {@link Database#query(String)} method with the generated query.
     *
     * @return a <code>Collection</code> object that contains the data produced
     * by the given query; never <code>null</code>@exception
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public Collection get() throws SQLException {
        String query = toSQL();

        log.debug("QueryBuilder#get() was called with the following SQL query.\nSQL: " + query);
        MDC.put("query", query);

        // Note: When parsing the result to a collection, we can't use the DBM query method since it auto closes the result,
        // and the collection still needs to communicated with the result set to get meta data so it can build the keysets
        // for the column names, this isn't possible if we close the result before parsing it to the collection, instead
        // we use the direct connection and have the collection close the connection instead.
        return new Collection(dbm.getConnection().query(query));
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryUpdate(QueryBuilder)} method with
     * the current instance of the query builder, and the given items from the changeable closure.
     *
     * @param closure The changeable closure that should be run.
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * or (2) 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public int update(ChangeableClosure closure) throws SQLException {
        type = QueryType.UPDATE;

        ChangeableStatement statement = new ChangeableStatement(this);
        closure.run(statement);

        this.items.addAll(Collections.singletonList(statement.getItems()));

        if (!async) {
            return dbm.queryUpdate(this);
        }

        ScheduleHandler.getScheduler().submit(() -> {
            try {
                dbm.queryUpdate(this);
            } catch (SQLException e) {
                log.error("Error thrown during async update query: " + toSQL(), e);
            }
        });

        return 0;
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryUpdate(QueryBuilder)}
     * method with the current instance of the query builder.
     *
     * @param arrays The list of items that should be updated
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * or (2) 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public int update(List<String>... arrays) throws SQLException {
        return update(buildMapFromArrays(arrays));
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryUpdate(QueryBuilder)}
     * method with the current instance of the query builder.
     *
     * @param items The map of items that should be updated
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * or (2) 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public int update(Map<String, Object>... items) throws SQLException {
        type = QueryType.UPDATE;

        this.items.addAll(Arrays.asList(items));

        if (!async) {
            return dbm.queryUpdate(this);
        }

        ScheduleHandler.getScheduler().submit(() -> {
            try {
                dbm.queryUpdate(this);
            } catch (SQLException e) {
                log.error("Error thrown during async update query: " + toSQL(), e);
            }
        });

        return 0;
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryInsert(QueryBuilder)} method with
     * the current instance of the query builder, and the given items from the changeable closure.
     *
     * @param closure The changeable closure that should be run.
     * @return a Collection of the generated IDs.
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public Collection insert(ChangeableClosure closure) throws SQLException {
        type = QueryType.INSERT;

        ChangeableStatement statement = new ChangeableStatement(this);
        closure.run(statement);

        this.items.addAll(Collections.singletonList(statement.getItems()));

        if (!async) {
            return runInsertQuery();
        }

        ScheduleHandler.getScheduler().submit(() -> {
            try {
                dbm.queryInsert(this);
            } catch (SQLException e) {
                log.error("Error thrown during async insert query: " + toSQL(), e);
            }
        });

        return new Collection();
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryInsert(QueryBuilder) }
     * method with the current instance of the query builder.
     *
     * @param arrays The list of items that should be inserted
     * @return a Collection
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public Collection insert(List<String>... arrays) throws SQLException {
        return insert(buildMapFromArrays(arrays));
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryInsert(QueryBuilder)}
     * method with the current instance of the query builder.
     *
     * @param items The map of items that should be inserted
     * @return a Collection of the generated IDs.
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public Collection insert(Map<String, Object>... items) throws SQLException {
        type = QueryType.INSERT;

        this.items.addAll(Arrays.asList(items));

        if (!async) {
            return runInsertQuery();
        }

        ScheduleHandler.getScheduler().submit(() -> {
            try {
                dbm.queryInsert(this);
            } catch (SQLException e) {
                log.error("Error thrown during async insert query: " + toSQL(), e);
            }
        });

        return new Collection();
    }

    /**
     * Runs the {@link com.avairebot.database.DatabaseManager#queryUpdate(QueryBuilder) }
     * method with the current instance of the query builder.
     *
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements
     * or (2) 0 for SQL statements that return nothing
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    public int delete() throws SQLException {
        type = QueryType.DELETE;

        if (!async) {
            return dbm.queryUpdate(this);
        }

        ScheduleHandler.getScheduler().submit(() -> {
            try {
                dbm.queryUpdate(this);
            } catch (SQLException e) {
                log.error("Error thrown during async delete query: " + toSQL(), e);
            }
        });

        return 0;
    }

    /**
     * Builds a Map from a List object.
     *
     * @param arrays The list to build the map from
     * @return The map that was build from the list.
     */
    private Map<String, Object> buildMapFromArrays(List<String>... arrays) {
        Map<String, Object> map = new HashMap<>();

        for (List<String> array : arrays) {
            if (array.size() != 2) {
                continue;
            }

            map.put(array.get(0), array.get(1));
        }

        return map;
    }

    /**
     * Runs the insert query and builds a collection of IDs for
     * all the new rows that was created by the query.
     *
     * @return The collection of IDs for the created rows.
     * @throws SQLException if a database access error occurs;
     *                      this method is called on a closed  <code>PreparedStatement</code>
     *                      or the SQL statement returns a <code>ResultSet</code> object
     */
    private Collection runInsertQuery() throws SQLException {
        Set<Integer> keys = dbm.queryInsert(this);
        List<Map<String, Object>> collectionItems = new ArrayList<>();

        for (int id : keys) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", id);
            collectionItems.add(row);
        }

        return new Collection(collectionItems);
    }

    /**
     * Gets the list of item maps for the query builder.
     *
     * @return the list of item maps for the query builder.
     */
    public List<Map<String, Object>> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return toSQL();
    }
}
