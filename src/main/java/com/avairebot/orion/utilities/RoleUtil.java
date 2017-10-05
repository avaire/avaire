package com.avairebot.orion.utilities;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.Checks;

import java.util.List;

public class RoleUtil {

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
        }).findFirst().get();
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
}
