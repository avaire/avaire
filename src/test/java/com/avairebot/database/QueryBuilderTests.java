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

package com.avairebot.database;

import com.avairebot.BaseTest;
import com.avairebot.database.fakes.FakeDatabaseManager;
import com.avairebot.database.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QueryBuilderTests extends BaseTest {

    private FakeDatabaseManager dbm;

    @Before
    public void setUp() throws Exception {
        dbm = new FakeDatabaseManager();
    }

    @Test
    public void testTableIsSetInTheQuery() {
        assertEquals("SELECT * FROM `test`;", makeQuery().toSQL());
    }

    @Test
    public void testWhereCauseWithNoComparatorIsSetInTheQueue() {
        assertEquals(
            "SELECT * FROM `test` WHERE `something` = 'something else';",
            makeQuery().where("something", "something else").toSQL()
        );
    }

    @Test
    public void testWhereCauseWithLessThanComparatorIsSetInTheQueue() {
        assertEquals(
            "SELECT * FROM `test` WHERE `something` < 5;",
            makeQuery().where("something", "<", 5).toSQL()
        );
    }

    @Test
    public void testWhereCauseWithGreaterThanComparatorIsSetInTheQueue() {
        assertEquals(
            "SELECT * FROM `test` WHERE `something` > 49;",
            makeQuery().where("something", ">", 49).toSQL()
        );
    }

    @Test
    public void testNestedWhereClausesAreBuildCorrectly() {
        assertEquals(
            "SELECT * FROM `test` WHERE `test` = 'thing' AND (`role` = 'admin' AND `permission_level` > 9001);",
            makeQuery().where("test", "thing")
                .where(builder -> {
                    builder.where("role", "admin")
                        .andWhere("permission_level", ">", 9001);
                }).toSQL()
        );
    }

    @Test
    public void testAndOrWhereClausesAreBuildCorrectly() {
        assertEquals(
            "SELECT * FROM `test` WHERE `test` = 'thing' OR `thing` = 'test';",
            makeQuery().where("test", "thing")
                .orWhere("thing", "test")
                .toSQL()
        );

        assertEquals(
            "SELECT * FROM `test` WHERE `test` = 'thing' AND `thing` = 'test';",
            makeQuery().where("test", "thing")
                .andWhere("thing", "test")
                .toSQL()
        );
    }

    private QueryBuilder makeQuery() {
        return dbm.newQueryBuilder("test");
    }
}
