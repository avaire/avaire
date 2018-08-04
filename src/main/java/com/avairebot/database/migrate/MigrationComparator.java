package com.avairebot.database.migrate;

import com.avairebot.exceptions.InvalidFormatException;
import com.avairebot.time.Carbon;

import java.util.Comparator;

/**
 * Note to self:
 * -1 = First argument comes first in the sorted list
 * 0 = First and second are equal in the sorted list
 * 1 = First argument comes second in the sorted list
 */
public class MigrationComparator implements Comparator<MigrationContainer> {

    private final boolean orderByAsc;

    public MigrationComparator(boolean orderByAsc) {
        this.orderByAsc = orderByAsc;
    }

    @Override
    public int compare(MigrationContainer firstContainer, MigrationContainer secondContainer) {
        Carbon first, second;

        try {
            first = new Carbon(firstContainer.getMigration().created_at());
        } catch (InvalidFormatException e) {
            return 1;
        }

        try {
            second = new Carbon(secondContainer.getMigration().created_at());
        } catch (InvalidFormatException e) {
            return -1;
        }

        if (first.eq(second)) {
            return 0;
        }

        return (orderByAsc) ? ascendingOrder(first, second) : descendingOrder(first, second);
    }

    private int ascendingOrder(Carbon first, Carbon second) {
        return first.lt(second) ? -1 : 1;
    }

    private int descendingOrder(Carbon first, Carbon second) {
        return first.gt(second) ? -1 : 1;
    }
}
