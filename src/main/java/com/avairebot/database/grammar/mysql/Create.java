package com.avairebot.database.grammar.mysql;

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
        if (blueprint.getEngine() == null) {
            addPart(");");
        } else {
            addPart(") ENGINE = %s;", blueprint.getEngine());
        }

        return query;
    }

    private void buildTable(Blueprint blueprint) {
        if (!options.getOrDefault("ignoreExistingTable", Boolean.FALSE)) {
            addPart(" IF NOT EXISTS ");
        }

        addPart(" %s (", formatField(blueprint.getTable()));
    }

    private void buildFields(Blueprint blueprint) {
        StringBuilder fields = new StringBuilder();
        StringBuilder primary = new StringBuilder();

        for (String name : blueprint.getFields().keySet()) {
            Field field = blueprint.getFields().get(name);
            FieldType type = field.getType();

            String line = String.format("%s %s", formatField(name), type.getName());

            if (type.requireArguments()) {
                if (type.getArguments() == 2) {
                    line += String.format("(%s, %s)", field.getLength(), field.getTail());
                } else {
                    line += String.format("(%s)", field.getLength());
                }
            }

            if (field.isUnsigned()) {
                line += " UNSIGNED";
            }

            line += field.isNullable() ? " NULL" : " NOT NULL";

            if (field.getDefaultValue() != null) {
                StringBuilder defaultString = new StringBuilder(" DEFAULT ");

                if (field.getDefaultValue().equalsIgnoreCase("NULL")) {
                    defaultString.append("NULL");
                } else if (field.isDefaultSQLAction()) {
                    defaultString.append(field.getDefaultValue());
                } else {
                    defaultString.append(String.format("'%s'", field.getDefaultValue().replace("'", "\'")));
                }

                line += defaultString;
            }

            if (field.isAutoIncrement()) {
                primary.append(formatField(name)).append(", ");

                line += " AUTO_INCREMENT";
            }

            fields.append(line).append(", ");
        }

        if (primary.length() > 0) {
            fields.append(String.format("PRIMARY KEY (%s), ", primary.substring(0, primary.length() - 2)));
        }

        addPart(fields.substring(0, fields.length() - 2));
    }
}
