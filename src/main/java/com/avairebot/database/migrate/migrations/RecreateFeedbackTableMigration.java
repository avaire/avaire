package com.avairebot.database.migrate.migrations;

import com.avairebot.Constants;
import com.avairebot.contracts.database.migrations.Migration;
import com.avairebot.database.schema.Schema;

import java.sql.SQLException;

public class RecreateFeedbackTableMigration implements Migration {

    @Override
    public String created_at() {
        return "Sat, Jun 23, 2018 4:29 PM";
    }

    @Override
    public boolean up(Schema schema) throws SQLException {
        schema.dropIfExists(Constants.FEEDBACK_TABLE_NAME);

        return schema.create(Constants.FEEDBACK_TABLE_NAME, table -> {
            table.Increments("id");
            table.String("user_id", 32);
            table.String("channel_id", 32).nullable();
            table.Text("message");
            table.Text("response").nullable();
            table.String("response_id", 32).nullable();
        });
    }

    @Override
    public boolean down(Schema schema) throws SQLException {
        // We don't really care to create the feedback table again
        // at this point, since it wasn't used anyway.
        return schema.dropIfExists(Constants.FEEDBACK_TABLE_NAME);
    }
}
