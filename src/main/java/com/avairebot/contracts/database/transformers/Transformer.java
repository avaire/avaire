package com.avairebot.contracts.database.transformers;

import com.avairebot.contracts.config.serialization.ConfigurationSerializable;
import com.avairebot.database.collection.DataRow;

import java.util.Map;

public abstract class Transformer implements ConfigurationSerializable {

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
        return data.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        return data.getRaw();
    }

    protected void reset() {
        this.data = null;
    }
}
