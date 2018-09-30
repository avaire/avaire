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

public class Field {

    private final FieldType type;
    private final int length;
    private final int tail;
    private boolean nullable = false;
    private boolean unsigned = false;
    private boolean autoIncrement = false;
    private boolean defaultIsSQLAction = false;
    private String defaultValue = null;

    public Field(FieldType type) {
        this.type = type;
        this.length = -1;
        this.tail = -1;
    }

    public Field(FieldType type, int length) {
        this.type = type;
        this.length = length;
        this.tail = -1;
    }

    public Field(FieldType type, int length, int tail) {
        this.type = type;
        this.length = length;
        this.tail = tail;
    }

    public FieldType getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getTail() {
        return tail;
    }

    public Field nullable() {
        nullable = true;

        return this;
    }

    public boolean isNullable() {
        return nullable;
    }

    public Field unsigned() {
        unsigned = true;

        return this;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public Field autoIncrement() {
        autoIncrement = true;

        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Field defaultValue(int value) {
        defaultValue = String.valueOf(value);

        return this;
    }

    public Field defaultValue(long value) {
        defaultValue = String.valueOf(value);

        return this;
    }

    public Field defaultValue(double value) {
        defaultValue = String.valueOf(value);

        return this;
    }

    public Field defaultValue(boolean value) {
        if (value) {
            return defaultValue(new DefaultSQLAction("true"));
        }

        return defaultValue(new DefaultSQLAction("false"));
    }

    public Field defaultValue(String value) {
        if (value == null) {
            return setDefaultToNull();
        }

        defaultValue = value;

        return this;
    }

    public Field defaultValue(Object value) {
        if (value == null) {
            return setDefaultToNull();
        }

        defaultValue = String.valueOf(value);

        return this;
    }

    public Field defaultValue(DefaultSQLAction action) {
        defaultIsSQLAction = true;

        defaultValue = action.getAction();

        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isDefaultSQLAction() {
        return defaultIsSQLAction;
    }

    private Field setDefaultToNull() {
        defaultValue = "NULL";

        return this;
    }
}
