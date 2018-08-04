package com.avairebot.contracts.database.grammar;

import com.avairebot.database.DatabaseManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Grammar {

    /**
     * A list a SQL operators, this is used to compare and
     * validate operators to make sure they're valid.
     */
    protected final List<String> operators = Arrays.asList(
        "=", "<", ">", "<=", ">=", "<>", "!=",
        "like", "like binary", "not like", "between", "ilike",
        "&", "|", "^", "<<", ">>",
        "rlike", "regexp", "not regexp",
        "~", "~*", "!~*", "similar to",
        "not similar " +
            "to"
    );
    /**
     * A list of SQL order operators, this is used to compare
     * and validate operators to make sure they're valid.
     */
    protected final List<String> orderOperators = Arrays.asList("ASC", "DESC");
    protected DatabaseManager dbm;
    protected Map<String, Boolean> options;

    /**
     * The query SQL string, this string will be appended to
     * and formated by the addPart and removeLast methods.
     */
    protected String query;

    public void setDBM(DatabaseManager dbm) {
        this.dbm = dbm;
    }

    public void setOptions(Map<String, Boolean> options) {
        this.options = options;
    }

    public String getQuery() {
        return query;
    }

    /**
     * Checks to see if a string is numeric, this will help
     * determine how to format values into the query.
     *
     * @param string The string to check.
     * @return either (1) <code>TRUE</code> if the provided string is numeric
     * or (2) <code>FALSE</code> if the provided string isn't numeric
     */
    protected boolean isNumeric(String string) {
        return string.matches("[-+]?\\d*\\.?\\d+");
    }

    /**
     * Adds the given part to the query.
     *
     * @param part The string to add.
     * @return Grammar
     */
    public Grammar addPart(String part) {
        query = query.trim() + part;

        return this;
    }

    public Grammar addPart(String part, Object... params) {
        query = query.trim() + String.format(part, params);

        return this;
    }

    public Grammar addRawPart(String part) {
        query += part;

        return this;
    }

    public Grammar addRawPart(String part, Object... params) {
        query += String.format(part, params);

        return this;
    }

    /**
     * Removes the given number of characters
     * from the end of the query string.
     *
     * @param characters The amount of characters to remove.
     * @return Grammar
     */
    protected Grammar removeLast(int characters) {
        query = query.substring(0, query.length() - characters);

        return this;
    }


    /**
     * Formats a query field, splitting it up using dot-notation.
     *
     * @param field The field to format.
     * @return the formated field
     */
    protected String formatField(String field) {
        field = field.trim();

        if (field.contains(" ")) {
            String[] both = field.split(" ");

            if (both.length == 3 && both[1].equalsIgnoreCase("as")) {
                return String.format("%s AS '%s'", formatField(both[0]), both[2]);
            }
        }

        if (field.contains(".")) {
            String[] both = field.split("\\.");

            String table = both[0];
            if (both.length == 2) {
                if (both[1].trim().equals("*")) {
                    return String.format("`%s`.*", table);
                }

                return String.format("`%s`.`%s`", table, both[1]);
            }
        }

        return String.format("`%s`", field);
    }
}
