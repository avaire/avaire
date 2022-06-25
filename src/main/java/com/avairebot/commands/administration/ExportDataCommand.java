package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.Constants;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandGroup;
import com.avairebot.contracts.commands.CommandGroups;
import com.avairebot.contracts.database.Database;
import com.avairebot.database.collection.Collection;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportDataCommand extends Command {

    public ExportDataCommand(AvaIre avaire) {
        super(avaire, false);
    }

    @Override
    public String getName() {
        return "Export Data Command";
    }

    @Override
    public String getDescription() {
        return "Exports the bots server data so you can download it and use it in self-hosted versions of the bot, this will export the servers guild, logs, mutes, player XP, and reaction role data in JSON, CSV, and SQL formats.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Exports all the bots server data.");
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("export-data");
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "require:bot,text.attach_files",
            "throttle:guild,1,30"
        );
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.MISCELLANEOUS);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (ZipOutputStream zipStream = new ZipOutputStream(stream)) {
                try {
                    this.addGuildTableData(context, zipStream);
                    this.addExperiencesTableData(context, zipStream);
                    this.addReactionRolesTableData(context, zipStream);
                    this.addMutesTableData(context, zipStream);
                    this.addLogsTableData(context, zipStream);
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                }
            }

            context.makeInfo(String.join(" ", Arrays.asList(
                "Data exported successfully, you can download the zip file attached below to",
                "see the servers guild, logs, mutes, players, and reaction role data in",
                "JSON, CSV, and SQL formats."
            )))
                .setTitle("Exported AvaIre Server Data")
                .setTimestamp(Instant.now())
                .requestedBy(context.getAuthor())
                .queue(message -> {
                    context.getChannel().sendFile(
                        stream.toByteArray(),
                        "exported-avaire-guild-data-" + context.getGuild().getId() + ".zip"
                    ).queue();
                });

            return true;
        } catch (IOException e) {
            return sendErrorMessage(context, "Failed to build and send the export file, error: " + e.getMessage());
        }
    }

    private void addGuildTableData(CommandMessage context, ZipOutputStream stream) throws SQLException, IOException {
        final List<String> exportedKeys = Arrays.asList(
            "id", "type", "owner", "name", "icon", "points", "local", "level_channel", "music_channel_text", "music_channel_voice",
            "music_messages", "autorole", "modlog", "modlog_case", "mute_role", "levels", "level_alerts", "level_roles",
            "level_exempt_channels", "level_exempt_roles", "channels", "channels_data", "roles_data", "claimable_roles",
            "prefixes", "aliases", "modules", "dj_level", "dj_role", "default_volume", "partner", "hierarchy",
            "level_modifier", "created_at", "updated_at", "leftguild_at"
        );

        Collection guildData = avaire.getDatabase().newQueryBuilder(Constants.GUILD_TABLE_NAME)
            .where("id", context.getGuild().getId())
            .get();

        addToZipFile(stream, "json/guild.json", guildData.toJson().getBytes());
        addToZipFile(stream, "csv/guild.csv", convertCollectionToCSV(guildData, exportedKeys));
        addToZipFile(stream, "sql/guild.sql", convertCollectionToSQL(guildData, exportedKeys, Constants.GUILD_TABLE_NAME));
    }

    private void addExperiencesTableData(CommandMessage context, ZipOutputStream stream) throws SQLException, IOException {
        final List<String> exportedKeys = Arrays.asList(
            "user_id", "guild_id", "username", "discriminator", "avatar", "experience",
            "global_experience", "active", "created_at", "updated_at"
        );

        Collection playerData = avaire.getDatabase().newQueryBuilder(Constants.PLAYER_EXPERIENCE_TABLE_NAME)
            .where("guild_id", context.getGuild().getId())
            .get();

        addToZipFile(stream, "json/players.json", playerData.toJson().getBytes());
        addToZipFile(stream, "csv/players.csv", convertCollectionToCSV(playerData, exportedKeys));
        addToZipFile(stream, "sql/players.sql", convertCollectionToSQL(playerData, exportedKeys, Constants.PLAYER_EXPERIENCE_TABLE_NAME));
    }

    private void addReactionRolesTableData(CommandMessage context, ZipOutputStream stream) throws SQLException, IOException {
        final List<String> exportedKeys = Arrays.asList(
            "guild_id", "channel_id", "message_id", "snippet", "roles"
        );

        Collection reactionRoles = avaire.getDatabase().newQueryBuilder(Constants.REACTION_ROLES_TABLE_NAME)
            .where("guild_id", context.getGuild().getId())
            .get();

        addToZipFile(stream, "json/reaction-roles.json", reactionRoles.toJson().getBytes());
        addToZipFile(stream, "csv/reaction-roles.csv", convertCollectionToCSV(reactionRoles, exportedKeys));
        addToZipFile(stream, "sql/reaction-roles.sql", convertCollectionToSQL(reactionRoles, exportedKeys, Constants.REACTION_ROLES_TABLE_NAME));
    }

    private void addMutesTableData(CommandMessage context, ZipOutputStream stream) throws SQLException, IOException {
        final List<String> exportedKeys = Arrays.asList(
            "guild_id", "modlog_id", "expires_in", "created_at", "updated_at"
        );

        Collection mutes = avaire.getDatabase().newQueryBuilder(Constants.MUTE_TABLE_NAME)
            .where("guild_id", context.getGuild().getId())
            .get();

        addToZipFile(stream, "json/mutes.json", mutes.toJson().getBytes());
        addToZipFile(stream, "csv/mutes.csv", convertCollectionToCSV(mutes, exportedKeys));
        addToZipFile(stream, "sql/mutes.sql", convertCollectionToSQL(mutes, exportedKeys, Constants.MUTE_TABLE_NAME));
    }

    private void addLogsTableData(CommandMessage context, ZipOutputStream stream) throws SQLException, IOException {
        final List<String> exportedKeys = Arrays.asList(
            "type", "modlogCase", "guild_id", "user_id", "target_id", "message_id",
            "reason", "created_at", "updated_at", "pardon"
        );

        Collection logs = avaire.getDatabase().newQueryBuilder(Constants.LOG_TABLE_NAME)
            .where("guild_id", context.getGuild().getId())
            .get();

        addToZipFile(stream, "json/logs.json", logs.toJson().getBytes());
        addToZipFile(stream, "csv/logs.csv", convertCollectionToCSV(logs, exportedKeys));
        addToZipFile(stream, "sql/logs.sql", convertCollectionToSQL(logs, exportedKeys, Constants.LOG_TABLE_NAME));
    }

    /**
     * Adds a new item to the zip file given the current zip output stream.
     *
     * @param stream   The Zip output stream the file should be added to
     * @param fileName The name of the file that should be added
     * @param data     The byte array of data that should be associated with the newly created file
     * @throws IOException
     */
    private void addToZipFile(ZipOutputStream stream, String fileName, byte[] data) throws IOException {
        stream.putNextEntry(new ZipEntry(fileName));
        stream.write(data);
        stream.closeEntry();
    }

    /**
     * Converts the given collection to a list of comma separated items.
     *
     * @param collection   The collection of data that should be converted to CSV
     * @param exportedKeys The keys that should be exported from the data row of each collection item
     * @return A byte array of all the CSV strings
     */
    private byte[] convertCollectionToCSV(Collection collection, List<String> exportedKeys) {
        if (collection.isEmpty()) {
            return new byte[0];
        }

        final String lastKey = exportedKeys.get(exportedKeys.size() - 1);
        StringBuilder csv = new StringBuilder();

        // Adds the headers for the CSV file
        csv.append(String.join(",", exportedKeys));
        csv.append("\n");

        collection.each((key, dataRow) -> {
            for (String dataKey : exportedKeys) {
                csv.append(dataRow.get(dataKey));

                if (!dataKey.equals(lastKey)) {
                    csv.append(",");
                }
            }

            csv.append("\n");
        });

        return csv.toString().getBytes();
    }

    /**
     * Converts the given collection to a list of SQL insert statements.
     *
     * @param collection   The collection of data that should be converted to SQL
     * @param exportedKeys The keys that should be exported from the data row of each collection item
     * @param tableName    The name of the table the queries should be built for
     * @return A byte array of all the SQL statements
     */
    private byte[] convertCollectionToSQL(Collection collection, List<String> exportedKeys, String tableName) {
        if (collection.isEmpty()) {
            return new byte[0];
        }

        StringBuilder sql = new StringBuilder();

        collection.each((key, dataRow) -> {
            LinkedHashMap<String, Object> items = new LinkedHashMap<>();

            for (String dataKey : exportedKeys) {
                Object data = dataRow.getRaw().get(dataKey);

                if (data instanceof String) {
                    String sqlStr = "RAW:" + data.toString();

                    sqlStr = sqlStr.replaceAll("'", "\\'");

                    data = "'" + sqlStr + "'";
                }

                items.put(dataKey, data);
            }

            sql.append(
                avaire.getDatabase().newQueryBuilder()
                    .table(tableName)
                    .forcefullySetItems(items)
                    .toSQL(Database.QueryType.INSERT)
            ).append("\n");
        });

        return sql.toString().getBytes();
    }
}
