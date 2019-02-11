/*
 * Copyright (c) 2019.
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

package com.avairebot.database.transformers;

import com.avairebot.contracts.database.transformers.Transformer;
import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class PurchasesTransformer extends Transformer {

    private static final Map<String, Set<PurchasesId>> emptyPurchases = new HashMap<>();

    private Map<String, Set<PurchasesId>> purchases = emptyPurchases;

    public PurchasesTransformer(Collection collection) {
        super(null);

        if (collection != null && !collection.isEmpty()) {
            Map<String, Set<PurchasesId>> purchases = new HashMap<>();
            for (DataRow row : collection) {
                if (row == null || row.getString("type") == null || row.getString("type_id") == null) {
                    continue;
                }

                String type = row.getString("type").toLowerCase();
                int typeId = row.getInt("type_id");

                if (!purchases.containsKey(type)) {
                    purchases.put(type, new HashSet<>());
                }

                purchases.get(type).add(
                    new PurchasesId(
                        typeId,
                        row.getInt("selected", -1) == typeId
                    )
                );
            }
            this.purchases = Collections.unmodifiableMap(purchases);
        }
    }

    /**
     * Gets the purchases for the player instance, where the key
     * is the purchase type identifier, and the value is a set
     * of purchase IDs.
     *
     * @return A map of purchases for the player.
     */
    @Nonnull
    public Map<String, Set<PurchasesId>> getPurchases() {
        return purchases;
    }

    /**
     * Gets the purchases for the given purchase type.
     *
     * @param type The type of purchases that should be returned.
     * @return Possible-null, a set of purchase IDs belonging to the given type, or
     * {@code NULL} if no purchases with the given ID was found for the player.
     */
    @Nullable
    public Set<PurchasesId> getPurchasesFromType(String type) {
        return purchases.getOrDefault(type, null);
    }

    /**
     * Gets the selected purchase type ID for the give type, if the player
     * doesn't have any purchases of the given type, of if the player
     * doesn't have a purchase of the given type selected, the
     * method will return {@code NULL}.
     *
     * @param type The name of the purchase type.
     * @return Possibly-null, the selected purchase type ID, or {@code NULL}.
     */
    @Nullable
    public Integer getSelectedPurchasesForType(String type) {
        Set<PurchasesId> purchases = getPurchasesFromType(type);
        if (purchases == null) {
            return null;
        }

        for (PurchasesId item : purchases) {
            if (item.isSelected()) {
                return item.getId();
            }
        }

        return null;
    }

    /**
     * Checks if the player has a purchase of the given type and type ID.
     *
     * @param type The type that should be checked for.
     * @param id   The ID that should belong to the type.
     * @return {@code True} if the player has a purchase belonging to
     * the given type with the given ID, {@code False} otherwise.
     */
    public boolean hasPurchase(String type, int id) {
        if (!purchases.containsKey(type)) {
            return false;
        }

        for (PurchasesId item : purchases.get(type)) {
            if (item.getId() == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the purchase transformer has any purchases in it or not.
     *
     * @return {@code True} if there are purchases in the purchase transformer, {@code False} otherwise.
     */
    public boolean hasPurchases() {
        return !purchases.isEmpty();
    }

    public class PurchasesId {

        private final int id;
        private final boolean selected;

        PurchasesId(int id, boolean selected) {
            this.id = id;
            this.selected = selected;
        }

        /**
         * The ID of the purchase, this is not the incrementing purchase
         * ID in the database, but the static ID of the purchased item.
         *
         * @return The ID of the purchase.
         */
        public int getId() {
            return id;
        }

        /**
         * Determines if the purchase is selected by the player or not.
         *
         * @return {@code True} if the purchase is selected, {@code False} otherwise.
         */
        public boolean isSelected() {
            return selected;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PurchasesId) {
                return ((PurchasesId) obj).id == id;
            } else if (obj instanceof Integer) {
                return ((Integer) obj) == id;
            }
            return false;
        }
    }
}
