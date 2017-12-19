package com.avairebot.orion.commands.utility;

import com.avairebot.orion.AppInfo;
import com.avairebot.orion.Orion;
import com.avairebot.orion.cache.CacheType;
import com.avairebot.orion.chat.PlaceholderMessage;
import com.avairebot.orion.contracts.commands.Command;
import com.avairebot.orion.factories.MessageFactory;
import com.avairebot.orion.utilities.NumberUtil;
import net.dv8tion.jda.core.entities.Message;
import org.jsoup.Jsoup;

import java.util.Collections;
import java.util.List;

public class VersionCommand extends Command {

    public VersionCommand(Orion orion) {
        super(orion);
    }

    @Override
    public String getName() {
        return "Version Command";
    }

    @Override
    public String getDescription() {
        return "Displays the current version of the bot and how many versions behind it is (if any).";
    }

    @Override
    public List<String> getUsageInstructions() {
        return null;
    }

    @Override
    public String getExampleUsage() {
        return null;
    }

    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("version");
    }

    @Override
    public List<String> getMiddleware() {
        return Collections.singletonList(
            "throttle:channel,1,5"
        );
    }

    @Override
    public boolean onCommand(Message message, String[] args) {
        SemanticVersion latestVersion = getLatestVersion();
        if (latestVersion == null) {
            return sendErrorMessage(message, "Failed to fetch the latest version of Orion, try again later.");
        }

        String template = String.join("\n",
            "I am currently `:difference` :type versions behind!",
            "",
            ":message",
            "",
            "You can get the latest version of me on github at [avaire/orion](https://github.com/avaire/orion)"
        );

        PlaceholderMessage versionMessage = null;
        SemanticVersion currentVersion = new SemanticVersion(AppInfo.getAppInfo().VERSION);
        if (latestVersion.major > currentVersion.major) {
            versionMessage = MessageFactory.makeError(message, template)
                .set("message", "Major version updates are total reworks of the bot and how it works or a large compilation of " +
                    "minor changes to the source code, it is highly recommended that you update on major version " +
                    "changes since older versions will not be supported for bug fixes or updates.")
                .set("difference", latestVersion.major - currentVersion.major)
                .set("type", "Major");
        } else if (latestVersion.minor > currentVersion.minor) {
            versionMessage = MessageFactory.makeWarning(message, template)
                .set("message", "Minor version updates are new additions, features and reworks of the existing codebase, it is " +
                    "recommended that you update on minor version changes to keep up with the new features.")
                .set("difference", latestVersion.minor - currentVersion.minor)
                .set("type", "Minor");
        } else if (latestVersion.patch > currentVersion.patch) {
            versionMessage = MessageFactory.makeInfo(message, template)
                .set("message", "Patch version updates are bug fixes, refactoring of existing code and very minor changes that " +
                    "wont affect other things in the code base, it is recommended that you update on patch version " +
                    "changes to keep up with the bug fixes and patches.")
                .set("difference", latestVersion.patch - currentVersion.patch)
                .set("type", "Patch");
        }

        if (versionMessage == null) {
            versionMessage = MessageFactory.makeSuccess(message, "You\'re using the latest version of Orion!");
        }

        versionMessage
            .setTitle("v" + AppInfo.getAppInfo().VERSION)
            .setFooter("The latest version of AvaIre is v" + latestVersion)
            .queue();

        return true;
    }

    private SemanticVersion getLatestVersion() {
        Object version = orion.getCache().getAdapter(CacheType.FILE).remember("github.version", 1800, () -> {
            return Jsoup.connect("https://raw.githubusercontent.com/avaire/orion/master/build.gradle")
                .execute().body().split("version = '")[1].split("'")[0];
        });

        if (version == null) {
            return null;
        }
        return new SemanticVersion(version.toString());
    }

    class SemanticVersion {

        private int major;
        private int minor;
        private int patch;

        SemanticVersion(String version) {
            String[] split = version.split("\\.");

            if (split.length > 0) {
                major = NumberUtil.parseInt(split[0]);
            }
            if (split.length > 1) {
                minor = NumberUtil.parseInt(split[1]);
            }
            if (split.length > 2) {
                patch = NumberUtil.parseInt(split[2]);
            }
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch;
        }
    }
}
