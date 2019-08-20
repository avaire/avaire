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

package com.avairebot.database.grammar.sqlite;

import com.avairebot.contracts.database.grammar.CreateGrammar;
import com.avairebot.database.schema.Blueprint;
import com.avairebot.database.schema.Field;
import com.avairebot.database.schema.FieldType;

public class Create extends CreateGrammar {

    @Override
    public String format(Blueprint blueprint) {
        buildTable(blueprint);

        buildFields(blueprint);

        return finalize(blueprint);
    }

    @Override
    protected String finalize(Blueprint blueprint) {
        addPart(");");

        return query;
    }

    private void buildTable(Blueprint blueprint) {
        if (!options.getOrDefault("ignoreExistingTable", Boolean.FALSE)) {
            addPart(" IF NOT EXISTS ");
        }

        addPart(" %s (", formatField(blueprint.getTable()));
    }

    private void buildFields(Blueprint blueprint) {
        String fields = "";
        String primary = "";

        for (String name : blueprint.getFields().keySet()) {
            Field field = blueprint.getFields().get(name);
            FieldType type = field.getType();

            String line = String.format("%s %s", formatField(name), getFieldTypeName(type));

            if (type.requireArguments()) {
                if (type.getArguments() == 2) {
                    line += String.format("(%s, %s)", field.getLength(), field.getTail());
                } else {
                    line += String.format("(%s)", field.getLength());
                }
            }

            String nullable = field.isNullable() ? " NULL" : " NOT NULL";
            String defaultString = " ";

            if (field.getDefaultValue() != null) {
                defaultString = " DEFAULT ";

                if (field.getDefaultValue().toUpperCase().equals("NULL")) {
                    defaultString += "NULL";
                } else if (field.isDefaultSQLAction()) {
                    if (!field.getDefaultValue().toLowerCase().contains("on")) {
                        defaultString += field.getDefaultValue();
                    } else {
                        defaultString = " DEFAULT NULL ";
                        nullable = " NULL ";
                    }
                } else {
                    defaultString += String.format("'%s'", field.getDefaultValue().replace("'", "\'"));
                }

                line += defaultString;
            }

            if (field.isAutoIncrement()) {
                nullable = "";
                defaultString += "PRIMARY KEY";
            }

            fields += line + nullable + defaultString + ", ";
        }

        if (primary.length() > 0) {
            fields += String.format("PRIMARY KEY (%s), ", primary.substring(0, primary.length() - 2));
        }

        addPart(fields.substring(0, fields.length() - 2));
    }

    private String getFieldTypeName(FieldType type) {
        switch (type) {
            case LONG:
            case INTEGER:
                return "INTEGER";

            case STRING:
            case LONGTEXT:
            case SMALLTEXT:
            case MEDIUMTEXT:
                return "TEXT";

            case DATE:
            case BOOLEAN:
            case DECIMAL:
            case DATETIME:
                return "NUMERIC";

            case FLOAT:
            case DOUBLE:
                return "REAL";

            default:
                return type.getName();
        }
    }
}
