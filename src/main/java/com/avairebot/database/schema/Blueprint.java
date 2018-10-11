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

package com.avairebot.database.schema;

import java.util.LinkedHashMap;
import java.util.Map;

public class Blueprint {

    private final String table;
    private final LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
    private String engine = DatabaseEngine.InnoDB.toString();

    /**
     * Creates a new blueprint instance for the provided table.
     *
     * @param table The table the blueprint should be created for
     */
    public Blueprint(String table) {
        this.table = table;
    }

    /**
     * Sets the database engine that should be used with the blueprint.
     *
     * @param databaseEngine The database engine that should be set
     */
    public void setEngine(DatabaseEngine databaseEngine) {
        setEngine(databaseEngine.getEngine());
    }

    /**
     * Gets the database engine used by the blueprint,
     * {@link DatabaseEngine#InnoDB} is used by default.
     *
     * @return Gets the database engine.
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Sets the database engine that should be used with the blueprint.
     *
     * @param engine The database engine that should be set
     */
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Gets the table that is used by the blueprint.
     *
     * @return the table that is used by the blueprint.
     */
    public String getTable() {
        return table;
    }

    /**
     * Gets the fields created by the blueprint.
     *
     * @return the fields created by the blueprint.
     */
    public Map<String, Field> getFields() {
        return fields;
    }

    /**
     * Creates a {@link FieldType#INTEGER} column of the given name that
     * is {@link Field#unsigned() } and {@link Field#autoIncrement() }.
     *
     * @param field The name of the column that should be created
     */
    public void Increments(String field) {
        makeField(field, FieldType.INTEGER).unsigned().autoIncrement();
    }

    /**
     * Creates a {@link FieldType#LONG} column of the given name that
     * is {@link Field#unsigned() } and {@link Field#autoIncrement() }.
     *
     * @param field The name of the column that should be created
     */
    public void BigIncrements(String field) {
        makeField(field, FieldType.LONG).unsigned().autoIncrement();
    }

    /**
     * Creates a {@link #Integer(java.lang.String, int) } with the provided name and a length of <code>17</code>.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Integer(String field) {
        return Integer(field, 17);
    }

    /**
     * Creates a {@link FieldType#INTEGER } column with the provided name and length.
     *
     * @param field  The name of the column that should be created
     * @param length The length of the column
     * @return the field instance.
     */
    public Field Integer(String field, int length) {
        return makeField(field, FieldType.INTEGER, length);
    }

    /**
     * Creates a {@link FieldType#LONG } column with the provided name and length.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Long(String field) {
        return makeField(field, FieldType.LONG);
    }

    /**
     * Creates a {@link FieldType#LONG } column with the provided name and length.
     *
     * @param field  The name of the column that should be created
     * @param length The length of the column
     * @return the field instance.
     */
    public Field Long(String field, int length) {
        return makeField(field, FieldType.LONG, length);
    }

    /**
     * Creates a {@link #Decimal(java.lang.String, int) } column with the provided name, and a length of <code>17</code>.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Decimal(String field) {
        return Decimal(field, 17);
    }

    /**
     * Creates a {@link #Decimal(java.lang.String, int, int) } column with the provided name, length and a tail of <code>2</code>.
     *
     * @param field  The name of the column that should be created
     * @param length The length of the column
     * @return the field instance.
     */
    public Field Decimal(String field, int length) {
        return Decimal(field, length, 2);
    }

    /**
     * Creates a {@link FieldType#DECIMAL } column with the provided name, length and tail.
     *
     * @param field  The name of the column that should be created
     * @param length The length of the column
     * @param tail   The tail of the column
     * @return the field instance.
     */
    public Field Decimal(String field, int length, int tail) {
        return makeField(field, FieldType.DECIMAL, length, tail);
    }

    /**
     * Creates a {@link #Double(java.lang.String, int) } column with
     * the provided name, and a length of <code>15</code>.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Double(String field) {
        return Double(field, 15);
    }

    /**
     * Creates a {@link #Double(java.lang.String, int, int) } column with
     * the provided name, length, and a tail of <code>8</code>.
     *
     * @param field  The name of the column that should be created
     * @param length The length if the column
     * @return the field instance.
     */
    public Field Double(String field, int length) {
        return Double(field, length, 8);
    }

    /**
     * Creates a {@link FieldType#DOUBLE} with the provided length and tail.
     *
     * @param field  The name of the column that should be created
     * @param length The length if the column
     * @param tail   The tail of the column
     * @return the field instance.
     */
    public Field Double(String field, int length, int tail) {
        return makeField(field, FieldType.DOUBLE, length, tail);
    }

    /**
     * Creates a {@link #Float(java.lang.String, int) } column with
     * the provided name, and a length of <code>17</code>.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Float(String field) {
        return Float(field, 17);
    }

    /**
     * Creates a {@link FieldType#FLOAT } column with
     * the provided name and a length.
     *
     * @param field  The name of the column that should be created
     * @param length The length if the column
     * @return the field instance.
     */
    public Field Float(String field, int length) {
        return makeField(field, FieldType.FLOAT, length);
    }

    /**
     * Creates a {@link FieldType#BOOLEAN} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Boolean(String field) {
        return makeField(field, FieldType.BOOLEAN);
    }

    /**
     * Creates a {@link FieldType#DATE} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Date(String field) {
        return makeField(field, FieldType.DATE);
    }

    /**
     * Creates a {@link FieldType#DATETIME} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field DateTime(String field) {
        return makeField(field, FieldType.DATETIME);
    }

    /**
     * Creates a {@link #String(java.lang.String, int) } column with the
     * provided name, and a length of <code>256</code>.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field String(String field) {
        return String(field, 256);
    }

    /**
     * Creates a {@link FieldType#STRING} column with the provided name and length.
     *
     * @param field  The name of the column that should be created
     * @param length The length if the column
     * @return the field instance.
     */
    public Field String(String field, int length) {
        return makeField(field, FieldType.STRING, length);
    }

    /**
     * Creates a {@link FieldType#LONGTEXT} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field LongText(String field) {
        return makeField(field, FieldType.LONGTEXT);
    }

    /**
     * Creates a {@link FieldType#MEDIUMTEXT} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field MediumText(String field) {
        return makeField(field, FieldType.MEDIUMTEXT);
    }

    /**
     * Creates a {@link FieldType#SMALLTEXT} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field SmallText(String field) {
        return makeField(field, FieldType.SMALLTEXT);
    }

    /**
     * Creates a {@link FieldType#TEXT} column with the provided name.
     *
     * @param field The name of the column that should be created
     * @return the field instance.
     */
    public Field Text(String field) {
        return makeField(field, FieldType.TEXT);
    }

    /**
     * Creates two {@link FieldType#DATETIME } timestamps, one called <i>created_at</i> that
     * gets used when a row is first created, and another called <i>updated_at</i> which will
     * update every time any of the rows columns are updated/modified.
     *
     * @see #DateTime(java.lang.String)
     * @see Field#defaultValue(com.avairebot.database.schema.DefaultSQLAction)
     */
    public void Timestamps() {
        makeField("created_at", FieldType.DATETIME).defaultValue(new DefaultSQLAction("CURRENT_TIMESTAMP"));
        makeField("updated_at", FieldType.DATETIME).defaultValue(new DefaultSQLAction("CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"));
    }

    /**
     * Creates a new field object of the given type.
     *
     * @param field The name of the column that should be created
     * @param type  The type of field that should be created
     * @return the field instance.
     */
    private Field makeField(String field, FieldType type) {
        Field obj = new Field(type);

        fields.put(field, obj);

        return obj;
    }

    /**
     * Creates a new field object of the given type and length.
     *
     * @param field  The name of the column that should be created
     * @param type   The type of field that should be created
     * @param length The length of the field that should be created
     * @return the field instance.
     */
    private Field makeField(String field, FieldType type, int length) {
        Field obj = new Field(type, length);

        fields.put(field, obj);

        return obj;
    }

    /**
     * Creates a new field object of the given type, length and tail.
     *
     * @param field  The name of the column that should be created
     * @param type   The type of field that should be created
     * @param length The length of the field that should be created
     * @param tail   The tail of the field that should be created
     * @return the field instance.
     */
    private Field makeField(String field, FieldType type, int length, int tail) {
        Field obj = new Field(type, length, tail);

        fields.put(field, obj);

        return obj;
    }
}
