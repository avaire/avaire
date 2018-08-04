package com.avairebot.contracts.database.collection;

import com.avairebot.database.collection.Collection;
import com.avairebot.database.collection.DataRow;

public interface CollectionEach {

    /**
     * This is called by by the {@link Collection#each(CollectionEach)}}
     * method, used to loops through every entity in the Collection and parses the key and
     * {@link com.avairebot.database.collection.DataRow} object to the consumer.
     *
     * @param key   The key for the element
     * @param value The data row linked to the key
     */
    void forEach(int key, DataRow value);
}
