package com.avairebot.contracts.database.transformers;

import com.avairebot.database.collection.DataRow;

public abstract class Transformer {

    protected final DataRow data;
    protected final boolean hasData;

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
}
