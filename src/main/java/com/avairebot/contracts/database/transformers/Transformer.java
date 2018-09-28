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

import java.util.Map;

public abstract class Transformer extends Evalable implements ConfigurationSerializable {

    protected DataRow data;
    protected boolean hasData;

    public Transformer(DataRow data) {
        this.data = data;
        hasData = (data != null);
    }

    public DataRow getRawData() {
        return data;
    }

    public boolean hasData() {
        return hasData;
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

    protected void reset() {
        this.data = null;
    }
}
