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

package com.avairebot.database.migrate;


import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.DatabaseManager;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;
import com.avairebot.database.query.QueryBuilder;
import com.avairebot.database.schema.Blueprint;
import com.avairebot.database.schema.DefaultSQLAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

public class Migrations {

    private static final Logger log = LoggerFactory.getLogger(Migrations.class);

    private final DatabaseManager dbm;
    private final List<MigrationContainer> migrations;

    private final String migrationTableName = "avaire_migrations";
    private boolean ranSetup = false;

    public Migrations(DatabaseManager dbm) {
        this.dbm = dbm;

        this.migrations = new ArrayList<>();
    }

    /**
     * Registers a list of migrations to the migration containers, the migrations
     * will be used in {@link #up() up()}, {@link #down() down()} and {@link #rollback(int) rollback(int)}
     * <p>
     * All migrations must follow the {@link com.avairebot.contracts.database.migrations.Migration Migration contract}.
     *
     * @param migration the list of migrations that should be registered
     * @see com.avairebot.contracts.database.migrations.Migration
     */
    public void register(Migration... migration) {
        ENTIRE_LOOP:
        for (Migration migrate : migration) {
            for (MigrationContainer container : migrations) {
                if (container.match(migrate)) {
                    container.setMigration(migrate);

                    continue ENTIRE_LOOP;
                }
            }

            migrations.add(new MigrationContainer(migrate));
        }
    }

    /**
     * Checks all the migrations in the container against the migrations in the database, filters
     * out the migrations that have already been run before and then runs the new migrations
     * against the default database connection.
     * <p>
     * If the DBM migrations table isn't found in the database, the table will be created.
     *
     * @return either (1) true if at-least one migration was migrated to the database successfully
     * or (2) false if nothing was migrated to the database.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean up() throws SQLException {
        checkAndRunMigrationSetup();
        updateBatchForLocalMigrations();

        boolean ranMigrations = false;
        for (MigrationContainer migration : getOrderedMigrations(true)) {
            if (migration.getBatch() == 1) {
                continue;
            }

            migration.getMigration().up(dbm.getSchema());
            updateRemoteMigrationBatchValue(migration, 1);

            log.info("Created \"{}\"", migration.getName());

            ranMigrations = true;
        }

        if (!ranMigrations) {
            log.info("There were nothing to migrate");
        }

        return ranMigrations;
    }

    /**
     * Checks all the migrations in the container against the migrations in the database, filters
     * out the migrations that haven't been executed before and then rolls back all the
     * existing migrations from default database connection.
     * <p>
     * If the DBM migrations table isn't found in the database, the table will be created.
     *
     * @return either (1) true if at-least one migration was rolled back from the database successfully
     * or (2) false if nothing was rolled back from the database.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean down() throws SQLException {
        checkAndRunMigrationSetup();
        updateBatchForLocalMigrations();

        boolean ranMigrations = false;
        for (MigrationContainer migration : getOrderedMigrations(false)) {
            if (migration.getBatch() != 1) {
                continue;
            }

            migration.getMigration().down(dbm.getSchema());
            updateRemoteMigrationBatchValue(migration, 0);

            log.info("Rolled back \"{}\"", migration.getName());

            ranMigrations = true;
        }

        if (!ranMigrations) {
            log.info("There were nothing to rollback");
        }

        return ranMigrations;
    }

    /**
     * Checks all the migrations in the container against the migrations in the database, filters
     * out the migrations that haven't been executed before and then rolls back all the
     * existing migrations from default database connection.
     * <p>
     * If the DBM migrations table isn't found in the database, the table will be created.
     *
     * @param steps the amount of steps to rollback
     * @return either (1) true if at-least one migration was rolled back from the database successfully
     * or (2) false if nothing was rolled back from the database.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean rollback(int steps) throws SQLException {
        checkAndRunMigrationSetup();
        updateBatchForLocalMigrations();

        int ran = 0;
        boolean ranMigrations = false;
        for (MigrationContainer migration : getOrderedMigrations(false)) {
            if (migration.getBatch() != 1) {
                continue;
            }

            if (ran++ >= steps) {
                break;
            }

            migration.getMigration().down(dbm.getSchema());
            updateRemoteMigrationBatchValue(migration, 0);

            log.info("Rolled back \"{}\"", migration.getName());

            ranMigrations = true;
        }

        if (!ranMigrations) {
            log.info("There were nothing to rollback");
        }

        return ranMigrations;
    }

    /**
     * Reruns the given migration class by first rolling back the migration changes,
     * and then re-running the up method to re-apply the migration.
     *
     * @param migration The migration that should be re-run.
     * @return {@code True} if the migrations ran successfully.
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    public boolean rerun(Migration migration) throws SQLException {
        MigrationContainer container = new MigrationContainer(migration);

        for (MigrationContainer migrationContainer : migrations) {
            if (migrationContainer.match(migration)) {
                container.setBatch(migrationContainer.getBatch());
                break;
            }
        }

        migration.down(dbm.getSchema());
        updateRemoteMigrationBatchValue(container, 0);
        log.info("Rolled back \"{}\"", container.getName());

        migration.up(dbm.getSchema());
        updateRemoteMigrationBatchValue(container, 1);
        log.info("Created \"{}\"", container.getName());

        return true;
    }

    /**
     * Gets a ordered list of the migration containers.
     *
     * @param orderByAsc determines if the list should be ordered ascending or descendingly
     * @return the ordered migration list
     */
    public List<MigrationContainer> getOrderedMigrations(boolean orderByAsc) {
        List<MigrationContainer> orderedMigrations = new ArrayList<>(migrations);

        Collections.sort(orderedMigrations, new MigrationComparator(orderByAsc));

        return orderedMigrations;
    }

    public List<MigrationContainer> getMigrations() {
        return migrations;
    }

    private void checkAndRunMigrationSetup() throws SQLException {
        if (ranSetup) {
            return;
        }

        runMigrationSetup();
        ranSetup = true;
    }

    private void runMigrationSetup() throws SQLException {
        boolean created = dbm.getSchema().createIfNotExists(migrationTableName, (Blueprint table) -> {
            table.Increments("id");
            table.String("name");
            table.Boolean("batch");
            table.DateTime("migration_time").defaultValue(new DefaultSQLAction("CURRENT_TIMESTAMP"));
        });

        if (created) {
            log.info("Migration table created successfully");
        }
    }

    private void updateBatchForLocalMigrations() throws SQLException {
        Collection results = dbm.query(makeQuery());
        Map<String, Integer> batchMigrations = new HashMap<>();

        for (DataRow row : results) {
            batchMigrations.put(row.getString("name"), row.getInt("batch"));
        }

        migrations.stream().filter((container) -> (batchMigrations.containsKey(container.getName()))).forEach((container) -> {
            container.setBatch(batchMigrations.get(container.getName()));
        });
    }

    private void updateRemoteMigrationBatchValue(MigrationContainer migration, int batch) throws SQLException {
        // If the migration has ran before, but was rolled back(down), this will update the existing row
        if (migration.getBatch() != -1) {
            makeQuery().where("name", migration.getName())
                .update(statement -> statement.set("batch", batch));
        } // If the migration has never run before, this will create a new row
        else {
            makeQuery().insert(statement -> {
                statement.set("name", migration.getName());
                statement.set("batch", batch);
            });
        }

        migration.setBatch(batch);
    }

    private QueryBuilder makeQuery() {
        return dbm.newQueryBuilder(migrationTableName);
    }
}
