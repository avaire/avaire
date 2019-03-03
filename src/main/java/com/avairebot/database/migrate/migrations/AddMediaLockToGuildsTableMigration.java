package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.migrate.Migrations;
import com.avairebot.database.schema.Schema;
import com.avairebot.time.Carbon;

import java.sql.SQLException;

public class AddMediaLockToGuildsTableMigration implements Migration
{
    /**
     * Gets the time the migration was created at, this is used to order
     * migrations, making sure migrations are rolled out to the
     * database and back in the right order.
     * <p>
     * The time format can be any of the supported carbon time formats.
     *
     * @return the carbon time string
     * @see Carbon
     */
    @Override
    public String created_at()
    {
        return "2019-03-02 00:15:36";
    }

    /**
     * Attempts to migrate the database, this is automatically executed from the
     * {@link Migrations#up() migrate up} method.
     *
     * @param schema the database schematic instance
     * @return the result of the schematic instance call
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */
    @Override
    public boolean up(Schema schema) throws SQLException
    {
        if (schema.hasColumn(Constants.GUILD_TABLE_NAME, "mediaonly")) {
            return true;
        }
        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` ADD `mediaonly` TINYINT(1) NOT NULL DEFAULT '0';",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }

    /**
     * Attempts to rollback the migrations from the database, this is automatically executed from the
     * {@link Migrations#down() down()} and
     * {@link Migrations#rollback(int) rollback(int)} method.
     *
     * @param schema the database schematic instance
     * @return the result of the schematic instance call
     * @throws SQLException if a database access error occurs,
     *                      this method is called on a closed <code>Statement</code>, the given
     *                      SQL statement produces anything other than a single
     *                      <code>ResultSet</code> object, the method is called on a
     *                      <code>PreparedStatement</code> or <code>CallableStatement</code>
     */

    @Override
    public boolean down(Schema schema) throws SQLException {
        if (!schema.hasColumn(Constants.GUILD_TABLE_NAME, "mediaonly") ){
            return true;
        }

        schema.getDbm().queryUpdate(String.format(
            "ALTER TABLE `%s` DROP `mediaonly`;",
            Constants.GUILD_TABLE_NAME
        ));

        return true;
    }
}
