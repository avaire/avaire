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
