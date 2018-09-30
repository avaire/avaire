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

package com.avairebot.database.connections;

import com.avairebot.contracts.database.StatementInterface;

public enum MySQLStatement implements StatementInterface {

    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    DO("DO"),
    REPLACE("REPLACE"),
    LOAD("LOAD"),
    HANDLER("HANDLER"),
    CALL("CALL"),
    CREATE("CREATE"),
    ALTER("ALTER"),
    DROP("DROP"),
    TRUNCATE("TRUNCATE"),
    RENAME("RENAME"),
    START("START"),
    COMMIT("COMMIT"),
    SAVEPOINT("SAVEPOINT"),
    ROLLBACK("ROLLBACK"),
    RELEASE("RELEASE"),
    LOCK("LOCK"),
    UNLOCK("UNLOCK"),
    PREPARE("PREPARE"),
    EXECUTE("EXECUTE"),
    DEALLOCATE("DEALLOCATE"),
    SET("SET"),
    SHOW("SHOW"),
    DESCRIBE("DESCRIBE"),
    EXPLAIN("EXPLAIN"),
    HELP("HELP"),
    USE("USE");

    private final String string;

    MySQLStatement(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
