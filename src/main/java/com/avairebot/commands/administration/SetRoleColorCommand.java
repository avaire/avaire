package com.avairebot.commands.administration;

import com.avairebot.AvaIre;
import com.avairebot.commands.CommandMessage;
import com.avairebot.contracts.commands.Command;
import com.avairebot.contracts.commands.CommandContext;
import com.avairebot.utilities.ColorUtil;
import com.avairebot.utilities.RandomUtil;
import com.avairebot.utilities.RoleUtil;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.impl.RoleImpl;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetRoleColorCommand extends Command
{

    public SetRoleColorCommand(AvaIre avaire)
    {
        super(avaire);
    }

    /**
     * Gets the command name, this is used in help and error
     * messages as the title as well as log messages.
     *
     * @return Never-null, the command name.
     */
    @Override
    public String getName() {
        return "Set Role Color Command";
    }

    @Override
    public List<String> getMiddleware() {
        return Arrays.asList(
            "require:user,general.administrator",
            "throttle:guild,1,5"
        );
    }


    @Override
    public String getDescription() {
        return "Takes in a role name, followed by a HEX value to\n" +
            "                set a role to a specific colour by simply knowing the HEX value. Additionally users can specify a random color.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Arrays.asList(
            "`:command <role> <color>` - Sets the role to the given color provided in hex.",
            "`:command <role> random` - Sets the role to a random color"
        );
    }

    @Override
    public List<String> getExampleUsage() {
        return Arrays.asList(
            "`:command dj #0000ff` - Sets the dj role blue.",
            "`:command @dj #0000ff` - Sets the dj role blue.",
            "`:command dj random` - Sets the dj role to a random color."
        );
    }


    /**
     * Gets am immutable list of command triggers that can be used to invoke the current
     * command, the first index in the list will be used when the `:command` placeholder
     * is used in {@link #getDescription(CommandContext)} or {@link #getUsageInstructions()} methods.
     *
     * @return An immutable list of command triggers that should invoked the command.
     */
    @Override
    public List<String> getTriggers() {
        return Collections.singletonList("setrolecolor");
    }

    /**
     * The command executor, this method is invoked by the command handler
     * and the middleware stack when a user sends a message matching the
     * commands prefix and one of its command triggers.
     *
     * @param context The command message context generated using the
     *                JDA message event that invoked the command.
     * @param args    The arguments given to the command, if no arguments was given the array will just be empty.
     * @return true on success, false on failure.
     */
    @Override
    public boolean onCommand(CommandMessage context, String[] args)
    {
        if(args.length == 0)
        {
            return sendErrorMessage(context, "errors.missingArgument", "role","color");
        }

        if(args.length == 1)
        {
            return sendErrorMessage(context, "errors.missingArgument", "color");
        }

        Role role = RoleUtil.getRoleFromMentionsOrName(context.message,args[0]);
        if(role == null)
        {
            return sendErrorMessage(context,context.i18n("noRoleFound",args[0]));
        }
        else
        {
            if(args[1].equalsIgnoreCase("random"))
            {
                Color color = RandomUtil.getRandomColor();
                RoleImpl actualRole = (RoleImpl)role;
                actualRole.getManager().setColor(color).queue();
            }
            else
            {
                try
                {
                    Color color = ColorUtil.getColorFromString(args[1]);
                    RoleImpl actualRole = (RoleImpl)role;
                    actualRole.getManager().setColor(color).queue();
                }
                catch(Throwable ex)
                {
                    return sendErrorMessage(context,context.i18n("invalidColor",args[1]));
                }
            }

        }

        return true;
    }
}
