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

public enum FieldType {

    INTEGER("INT", true, 1, false),
    DECIMAL("DECIMAL", true, 2, true),
    DOUBLE("DOUBLE", true, 1, true),
    FLOAT("FLOAT", true, 1, true),
    LONG("BIGINT", true, 1, false),
    BOOLEAN("BOOLEAN", false, 0, false),
    DATE("DATE", false, 0, false),
    DATETIME("DATETIME", false, 0, false),
    STRING("VARCHAR", true, 1, true),
    LONGTEXT("LONGTEXT", false, 0, false),
    MEDIUMTEXT("MEDIUMTEXT", false, 0, false),
    SMALLTEXT("TINYTEXT", false, 0, false),
    TEXT("TEXT", false, 0, false);

    private final String name;
    private final boolean arguments;
    private final int argumentAmount;
    private final boolean requiredArguments;

    private FieldType(String name, boolean arguments, int argumentAmount, boolean requiredArguments) {
        this.name = name;
        this.arguments = arguments;
        this.argumentAmount = argumentAmount;
        this.requiredArguments = requiredArguments;
    }

    public String getName() {
        return name;
    }

    public boolean hasArguments() {
        return arguments;
    }

    public int getArguments() {
        return argumentAmount;
    }

    public boolean requireArguments() {
        return requiredArguments;
    }
}
