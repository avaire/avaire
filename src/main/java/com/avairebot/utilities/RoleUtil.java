package com.avairebot.utilities;

import com.avairebot.factories.MessageFactory;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.Checks;

import java.util.List;

public class RoleUtil {

    /**
     * Gets the role from the mentioned roles list, if no roles was mentions
     * the role will be fetch from the guilds role list by name instead,
     * if no roles was found null is returned.
     *
     * @param message  The JDA message object for the current guild.
     * @param roleName The name of the role that should be fetched if no role was mentioned.
     * @return Possibly-null, if a role was mentioned the role will be returned, otherwise the role will be fetched from the guilds role list.
     */
    public static Role getRoleFromMentionsOrName(Message message, String roleName) {
        if (!message.getMentionedRoles().isEmpty()) {
            return message.getMentionedRoles().get(0);
        }

        List<Role> roles = message.getGuild().getRolesByName(roleName, true);
        return roles.isEmpty() ? null : roles.get(0);
    }

    /**
     * Get the role that is position highest in the role hierarchy for the given member.
     *
     * @param member The member whos roles should be used.
     * @return Possibly-null, if the user has any roles the role that is ranked highest in the role hierarchy will be returned.
     */
    public static Role getHighestFrom(Member member) {
        Checks.notNull(member, "Member object can not be null");

        List<Role> roles = member.getRoles();
        if (roles.isEmpty()) {
            return null;
        }

        return roles.stream().sorted((first, second) -> {
            if (first.getPosition() == second.getPosition()) {
                return 0;
            }
            return first.getPosition() > second.getPosition() ? -1 : 1;
        }).findFirst().orElseGet(null);
    }

    /**
     * Checks if the given roles hierarchy position is higher than any role given in the list.
     *
     * @param roles     The list to compare the role position to.
     * @param matchRole The role which position should be used in the comparison.
     * @return True if the given position is higher than any of the roles in the list, false otherwise.
     */
    public static boolean isRoleHierarchyHigher(List<Role> roles, Role matchRole) {
        Checks.notNull(matchRole, "Match roles can not be null");

        return isRolePositionHigher(roles, matchRole.getPosition());
    }

    /**
     * Checks if the given hierarchy position is higher than any role given in the list.
     *
     * @param roles             The list to compare the position to.
     * @param hierarchyPosition The position that should be compared with the list.
     * @return True if the given position is higher than any of the roles in the list, false otherwise.
     */
    public static boolean isRolePositionHigher(List<Role> roles, int hierarchyPosition) {
        for (Role role : roles) {
            if (role.getPosition() > hierarchyPosition) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given roles hierarchy position is lower than any role given in the list.
     *
     * @param roles     The list to compare the role position to.
     * @param matchRole The role which position should be used in the comparison.
     * @return True if the given position is lower than any of the roles in the list, false otherwise.
     */
    public static boolean isRoleHierarchyLower(List<Role> roles, Role matchRole) {
        Checks.notNull(matchRole, "Match roles can not be null");

        return isRoleHierarchyLower(roles, matchRole.getPosition());
    }

    /**
     * Checks if the given hierarchy position is lower than any role given in the list.
     *
     * @param roles             The list to compare the position to.
     * @param hierarchyPosition The position that should be compared with the list.
     * @return True if the given position is lower than any of the roles in the list, false otherwise.
     */
    public static boolean isRoleHierarchyLower(List<Role> roles, int hierarchyPosition) {
        for (Role role : roles) {
            if (role.getPosition() > hierarchyPosition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user can interact with the given role by making sure the role is
     * not higher in the role hierarchy then any role the user and the bot instance
     * for the current guild has, if the method returns true the bot should be
     * able to give the role to the users, and the given user has at least
     * one role which is in a higher position in the role hierarchy.
     *
     * @param message The JDA message object for the current guild.
     * @param role    The role that should be used in the checks.
     * @return True if both the given user and bot can interact with the role, false otherwise.
     */
    public static boolean canInteractWithRole(Message message, Role role) {
        if (RoleUtil.isRoleHierarchyHigher(message.getMember().getRoles(), role)) {
            MessageFactory.makeWarning(message,
                ":user The **:role** role is positioned higher in the hierarchy than any role you have, you can't add roles with a higher ranking than you have."
            ).set("role", role.getName()).queue();
            return false;
        }

        if (RoleUtil.isRoleHierarchyHigher(message.getGuild().getSelfMember().getRoles(), role)) {
            MessageFactory.makeWarning(message,
                ":user The **:role** role is positioned higher in the hierarchy, I can't give/remove this role from users."
            ).set("role", role.getName()).queue();
            return false;
        }

        return true;
    }

    /**
     * Checks if the given member has the given role.
     *
     * @param member The member that should check if they have the given role.
     * @param role   The role the member should have.
     * @return True if the member has the given role, false otherwise.
     */
    public static boolean hasRole(Member member, Role role) {
        for (Role memberRole : member.getRoles()) {
            if (memberRole.getId().equals(role.getId())) {
                return true;
            }
        }
        return false;
    }
}
