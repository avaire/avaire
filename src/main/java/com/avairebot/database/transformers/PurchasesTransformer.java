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

    private static final Map<String, Set<Integer>> emptyPurchases = new HashMap<>();

    private Map<String, Set<Integer>> purchases = emptyPurchases;

    public PurchasesTransformer(Collection collection) {
        super(null);

        if (collection != null && !collection.isEmpty()) {
            Map<String, Set<Integer>> purchases = new HashMap<>();
            for (DataRow row : collection) {
                if (row == null || row.getString("type") == null || row.getString("type_id") == null) {
                    continue;
                }

                String type = row.getString("type").toLowerCase();
                int typeId = row.getInt("type_id");

                if (!purchases.containsKey(type)) {
                    purchases.put(type, new HashSet<>());
                }

                purchases.get(type).add(typeId);
            }
            this.purchases = Collections.unmodifiableMap(purchases);
        }
    }

    @Nonnull
    public Map<String, Set<Integer>> getPurchases() {
        return purchases;
    }

    @Nullable
    public Set<Integer> getPurchasesFromType(String type) {
        return purchases.getOrDefault(type, null);
    }

    public boolean hasPuraches(String type, int id) {
        return purchases.containsKey(type)
            && purchases.get(type).contains(id);
    }
}
