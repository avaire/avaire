/*
 * Copyright (c) 2018.
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

package com.avairebot.contracts.database.transformers;

import com.avairebot.contracts.config.serialization.ConfigurationSerializable;
import com.avairebot.contracts.debug.Evalable;
import com.avairebot.database.collection.DataRow;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class Transformer extends Evalable implements ConfigurationSerializable {

    /**
     * The main data object, used for retrieving
     * all the data for the transformer.
     */
    protected DataRow data;

    /**
     * Determines if the transformer has data or not.
     */
    protected boolean hasData;

    /**
     * Determines if the transformer has been checked if it has any data or not.
     */
    private boolean hasBeenChecked;

    /**
     * Creates a new transformer instance using
     * the given data row object.
     *
     * @param data The data row object that should be used
     *             for creating the transformer instance.
     */
    public Transformer(DataRow data) {
        this.data = data;
        this.hasBeenChecked = false;
    }

    /**
     * Gets the raw data row object instance for the transformer, or the data has been
     * reset since creating the transformer, the raw data may be {@code NULL}.
     *
     * @return The raw data row object instance, or {@code NULL}.
     */
    @Nullable
    @SuppressWarnings("unused")
    public DataRow getRawData() {
        return data;
    }

    /**
     * Checks if the transformer instance has data or not.
     *
     * @return {@code True} if the transformer has data, {@code False} otherwise.
     */
    public final boolean hasData() {
        if (!hasBeenChecked) {
            hasData = checkIfTransformerHasData();
            hasBeenChecked = true;
        }
        return hasData;
    }

    /**
     * Checks if the transformer has any data.
     *
     * @return {@code True} if the transformer has data, {@code False} otherwise.
     */
    protected boolean checkIfTransformerHasData() {
        return data != null;
    }

    @Override
    public String toString() {
        if (data != null) {
            return data.toString();
        }
        return super.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        return data.getRaw();
    }

    /**
     * Resets the raw data row instance, setting it to {@code NULL}.
     */
    protected void reset() {
        this.data = null;
    }
}
