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

package com.avairebot.database.collection;

import com.avairebot.AvaIre;
import com.avairebot.contracts.database.collection.CollectionEach;
import com.avairebot.utilities.RandomUtil;
import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Collection implements Cloneable, Iterable<DataRow> {

    private final HashMap<String, String> keys;
    private final List<DataRow> items;

    /**
     * Creates an empty collection.
     */
    public Collection() {
        this.keys = new HashMap<>();
        this.items = new ArrayList<>();
    }

    /**
     * Creates a new Collection object from the provided collection
     * instance, this is the same as calling the {@link #copy() } method.
     *
     * @param instance The collection to copy
     */
    public Collection(@Nonnull Collection instance) {
        this.keys = new HashMap<>();
        this.items = new ArrayList<>();

        for (DataRow row : instance.all()) {
            items.add(new DataRow(row));
        }
    }

    /**
     * Creates a new Collection object from a multidimensional map.
     *
     * @param items the map of items to create the collection from
     */
    public Collection(@Nonnull List<Map<String, Object>> items) {
        this.keys = new HashMap<>();
        this.items = new ArrayList<>();

        for (Map<String, Object> row : items) {
            row.keySet().stream().filter((key) -> (!keys.containsKey(key))).forEach((key) -> {
                keys.put(key, row.get(key).getClass().getTypeName());
            });

            this.items.add(new DataRow(row));
        }
    }

    /**
     * Creates a new Collection instance, allowing you to the loop
     * and fetch data from a ResultSet object a lot easier.
     *
     * @param result The ResultSet to generate the collection from.
     * @throws SQLException if a database access error occurs,
     *                      this exception is thrown if the collection was unable to read
     *                      form the database <code>ResultSet</code> object, or if the object
     *                      didn't return a valid response.
     */
    public Collection(@Nullable ResultSet result) throws SQLException {
        this.keys = new HashMap<>();
        this.items = new ArrayList<>();

        if (result == null) {
            return;
        }

        ResultSetMetaData meta = result.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            keys.put(meta.getColumnLabel(i), meta.getColumnClassName(i));
        }

        while (result.next()) {
            Map<String, Object> array = new HashMap<>();

            for (String key : keys.keySet()) {
                array.put(key, result.getString(key));
            }

            items.add(new DataRow(array));
        }

        if (!result.isClosed()) {
            result.close();
        }
    }

    /**
     * Gets all the <code>DataRow</code> items from the collection.
     *
     * @return All the <code>DataRow</code> items from the collection.
     */
    public List<DataRow> all() {
        return items;
    }

    /**
     * Calculates the average for the first key field.
     *
     * @return the average for the first key field.
     */
    public double avg() {
        return avg(keys.keySet().iterator().next());
    }

    /**
     * Calculates the average for a field
     *
     * @param field the field to calculated the average of
     * @return the average for the provided field
     */
    public double avg(String field) {
        if (isEmpty() || !keys.containsKey(field)) {
            return 0;
        }

        BigDecimal decimal = new BigDecimal(0);

        for (DataRow row : items) {
            Object obj = row.get(field);

            switch (obj.getClass().getTypeName()) {
                case "java.lang.Double":
                    decimal = decimal.add(new BigDecimal((Double) obj));
                    break;

                case "java.lang.Long":
                    decimal = decimal.add(new BigDecimal((Long) obj));
                    break;

                case "java.lang.Integer":
                    decimal = decimal.add(new BigDecimal((Integer) obj));
                    break;

                case "java.lang.Float":
                    decimal = decimal.add(new BigDecimal((Float) obj));
                    break;
            }
        }

        return decimal.divide(new BigDecimal(items.size())).doubleValue();
    }

    /**
     * Breaks the collection into multiple, smaller lists of the given size.
     *
     * @param size the size to chunk the collection down to
     * @return the chunked down collection
     */
    public List<Collection> chunk(int size) {
        List<Collection> chunk = new ArrayList<>();

        int index = 0, counter = 0;
        for (DataRow row : items) {
            if (counter++ >= size) {
                index++;
                counter = 0;
            }

            try {
                Collection get = chunk.get(index);

                get.add(row);
            } catch (IndexOutOfBoundsException e) {
                Collection collection = new Collection();

                collection.add(row);

                chunk.add(index, collection);
            }
        }

        return chunk;
    }

    /**
     * Checks every value stored in the collection and compares
     * it to see if it matches the provided item.
     *
     * @param item The item to compare the collection with
     * @return <code>true</code> if this collection contains the provided elements.
     */
    public boolean contains(Object item) {
        return items.stream().anyMatch((row)
            -> (row.keySet().stream().anyMatch((key)
            -> (row.get(key).equals(item)))));
    }

    /**
     * Creates a copy of the current collections instance.
     *
     * @return the new collection.
     */
    public Collection copy() {
        return new Collection(this);
    }

    /**
     * Loops through every entity in the Collection and parses the key and
     * {@link com.avairebot.database.collection.DataRow} object to the consumer.
     *
     * @param comparator The collection consumer to use.
     * @return the collection instance.
     */
    public Collection each(CollectionEach comparator) {
        ListIterator<DataRow> iterator = items.listIterator();

        while (iterator.hasNext()) {
            comparator.forEach(iterator.nextIndex(), iterator.next());
        }

        return this;
    }

    /**
     * Gets the first index of the collection.
     *
     * @return either (1) The first <code>DataRow</code> object, generated from the <code>ResultSet</code> object,
     * or (2) <code>NULL</code> if the collection doesn't have any items.
     */
    public DataRow first() {
        if (items.isEmpty()) {
            return null;
        }

        return items.get(0);
    }

    /**
     * Gets the result of the provided index.
     *
     * @param index the index to get from the collection
     * @return the DataRow object in the provided index
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public DataRow get(int index) {
        return items.get(index);
    }

    /**
     * Gets all the keys from the <code>ResultSet</code> object in the form of a
     * <code>HashMap</code>, where the key is the database table column
     * name, and the value is the database column type.
     *
     * @return A map of all the database keys.
     */
    public HashMap<String, String> getKeys() {
        return keys;
    }

    /**
     * Get all the data row items.
     *
     * @return The data row items.
     */
    public List<DataRow> getItems() {
        return items;
    }

    /**
     * Checks to see the collection contains the provided field.
     *
     * @param field the field to check if exists
     * @return ture if the field exists in the collection, false if it doesn't
     */
    public boolean has(String field) {
        return keys.containsKey(field);
    }

    /**
     * Returns <code>true</code> if this collection contains no elements.
     *
     * @return <code>true</code> if this collection contains no elements
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Gets the last DataRow of the collection.
     *
     * @return the last DataRow of the collection
     */
    public DataRow last() {
        if (isEmpty()) {
            return null;
        }

        return items.get(items.size() - 1);
    }

    /**
     * Gets the max/highest integer value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the highest value from the provided field
     * or (2) <code>Integer.MIN_VALUE</code>
     */
    public int maxInt(String field) {
        if (!has(field)) {
            return Integer.MIN_VALUE;
        }

        int max = Integer.MIN_VALUE;
        for (DataRow row : items) {
            int x = row.getInt(field);

            if (max < x) {
                max = x;
            }
        }

        return max;
    }

    /**
     * Gets the max/highest long value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the highest value from the provided field
     * or (2) <code>Long.MIN_VALUE</code>
     */
    public long maxLong(String field) {
        if (!has(field)) {
            return Long.MIN_VALUE;
        }

        long max = Long.MIN_VALUE;
        for (DataRow row : items) {
            long x = row.getLong(field);

            if (max < x) {
                max = x;
            }
        }

        return max;
    }

    /**
     * Gets the max/highest double value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the highest value from the provided field
     * or (2) <code>Double.MIN_VALUE</code>
     */
    public double maxDouble(String field) {
        if (!has(field)) {
            return Double.MIN_VALUE;
        }

        double max = Double.MIN_VALUE;
        for (DataRow row : items) {
            double x = row.getDouble(field);

            if (max < x) {
                max = x;
            }
        }

        return max;
    }

    /**
     * Gets the max/highest float value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the highest value from the provided field
     * or (2) <code>Float.MIN_VALUE</code>
     */
    public float maxFloat(String field) {
        if (!has(field)) {
            return Float.MIN_VALUE;
        }

        float max = Float.MIN_VALUE;
        for (DataRow row : items) {
            float x = row.getFloat(field);

            if (max < x) {
                max = x;
            }
        }

        return max;
    }

    /**
     * Gets the min/lowest integer value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the lowest value from the provided field
     * or (2) <code>Integer.MAX_VALUE</code>
     */
    public int minInt(String field) {
        if (!has(field)) {
            return Integer.MAX_VALUE;
        }

        int min = Integer.MAX_VALUE;
        for (DataRow row : items) {
            int x = row.getInt(field);

            if (min > x) {
                min = x;
            }
        }

        return min;
    }

    /**
     * Gets the min/lowest long value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the lowest value from the provided field
     * or (2) <code>Long.MAX_VALUE</code>
     */
    public long minLong(String field) {
        if (!has(field)) {
            return Long.MAX_VALUE;
        }

        long min = Long.MAX_VALUE;
        for (DataRow row : items) {
            long x = row.getLong(field);

            if (min > x) {
                min = x;
            }
        }

        return min;
    }

    /**
     * Gets the min/lowest double value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the lowest value from the provided field
     * or (2) <code>Double.MAX_VALUE</code>
     */
    public double minDouble(String field) {
        if (!has(field)) {
            return Double.MAX_VALUE;
        }

        double min = Double.MAX_VALUE;
        for (DataRow row : items) {
            double x = row.getDouble(field);

            if (min > x) {
                min = x;
            }
        }

        return min;
    }

    /**
     * Gets the min/lowest float value from the provided field.
     *
     * @param field the field to use
     * @return either (1) the lowest value from the provided field
     * or (2) <code>Float.MAX_VALUE</code>
     */
    public float minFloat(String field) {
        if (!has(field)) {
            return Float.MAX_VALUE;
        }

        float min = Float.MAX_VALUE;
        for (DataRow row : items) {
            float x = row.getFloat(field);

            if (min > x) {
                min = x;
            }
        }

        return min;
    }

    /**
     * Removes and returns the last item of the collection.
     *
     * @return the last item of the collection
     */
    public DataRow pop() {
        if (isEmpty()) {
            return null;
        }

        return items.remove(items.size() - 1);
    }

    /**
     * Gets a RANDOM item from the collection
     *
     * @return a RANDOM item from the collection
     */
    public DataRow random() {
        if (isEmpty()) {
            return null;
        }

        return items.get(RandomUtil.getInteger(items.size()));
    }

    /**
     * Reverses the order of items in the collection.
     *
     * @return the reversed collection.
     */
    public Collection reverse() {
        Collections.reverse(items);

        return this;
    }

    /**
     * Search the collection where the field is equal to the value.
     *
     * @param field the field to check
     * @param value the value to use
     * @return either (1) the index of the item that matches the search
     * or (2) -1
     */
    public int search(String field, Object value) {
        if (isEmpty() || !has(field)) {
            return -1;
        }

        String rValue = value.toString();

        for (int index = 0; index < items.size(); index++) {
            DataRow row = get(index);

            if (row.getString(field).equals(rValue)) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Removes and returns the first element in the collection.
     *
     * @return The first element in the collection.
     */
    public DataRow shift() {
        try {
            return items.remove(0);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Randomly shuffles the items in the collection.
     *
     * @return the newly shuffled collection.
     */
    public Collection shuffle() {
        Collections.shuffle(items, new SecureRandom());

        return this;
    }

    /**
     * Sorts the collection according to the order induced by the specified
     * comparator. All elements in the list must be <i>mutually comparable</i>
     * using the specified comparator.
     *
     * @param comparator The comparator to use to sort the collection.
     * @return the collection instance.
     */
    public Collection sort(Comparator<DataRow> comparator) {
        items.sort(comparator);

        return this;
    }

    /**
     * Sorts the collection in an ascending order using the provided field, if
     * the field is not found the collection will be returned in its current state.
     *
     * @param field The field that should be used to sort the collection.
     * @return the collection instance.
     */
    public Collection sortBy(String field) {
        if (!has(field)) {
            return this;
        }

        return sort(Comparator.comparingInt(row -> row.get(field).hashCode()));
    }

    /**
     * Sorts the collection in an descending order using the provided field, if
     * the field is not found the collection will be returned in its current state.
     *
     * @param field The field that should be used to sort the collection.
     * @return the collection instance.
     */
    public Collection sortByDesc(String field) {
        if (!has(field)) {
            return this;
        }

        return sort((DataRow first, DataRow second) -> second.get(field).hashCode() - first.get(field).hashCode());
    }

    /**
     * Gets the total number of items in the collection.
     *
     * @return the total number of items in the collection
     */
    public int size() {
        return items.size();
    }

    /**
     * Calculates the sum of a list of integers.
     *
     * @param field the field to calculated the sum of
     * @return the sum for the provided field
     */
    public long sumInt(String field) {
        long sum = 0;

        if (!has(field)) {
            return sum;
        }

        for (DataRow row : items) {
            sum += row.getInt(field);
        }

        return sum;
    }

    /**
     * Takes the provided number of items from the collection and returns a new collection.
     *
     * @param amount The amount of items to take from the original collection
     * @return a new collection with the provided number of items from the original collection.
     */
    public Collection take(int amount) {
        Collection collection = new Collection();
        Iterator<DataRow> iterator = items.iterator();

        int index = 0;
        while (iterator.hasNext()) {
            DataRow next = iterator.next();

            if (index++ >= amount) {
                break;
            }

            collection.add(new DataRow(next));
            iterator.remove();
        }

        return collection;
    }

    /**
     * Gets all the data rows where the field equals the value, this uses strict
     * comparisons to match the values, use the {@link #whereLoose(String, Object) whereLosse}
     * method to filter using "losse" comparisons.
     *
     * @param field The field that should be matched
     * @param value The value that should match the field
     * @return a list of data row objects that match the where clause
     */
    public List<DataRow> where(String field, Object value) {
        if (isEmpty() || !has(field)) {
            return new ArrayList<>();
        }

        String rValue = value.toString();
        List<DataRow> rows = new ArrayList<>();

        items.stream()
            .filter((row) -> (row.getString(field).equals(rValue)))
            .forEach(rows::add);

        return rows;
    }

    /**
     * Gets all the data rows where the field equals the value, this uses a loose
     * comparisons to match the values, use the {@link #where(String, Object) where}
     * method to filter using "strict" comparisons.
     *
     * @param field The field that should be matched
     * @param value The value that should match the field
     * @return a list of data row objects that match the where clause
     */
    public List<DataRow> whereLoose(String field, Object value) {
        if (isEmpty() || !has(field)) {
            return new ArrayList<>();
        }

        String rValue = value.toString();
        List<DataRow> rows = new ArrayList<>();

        items.stream()
            .filter((row) -> (row.getString(field).equalsIgnoreCase(rValue)))
            .forEach(rows::add);

        return rows;
    }

    @Override
    public String toString() {
        return toJson();
    }

    /**
     * Converts the collection to a JSON string using {@link Gson}.
     *
     * @return the JSON collection string
     */
    public String toJson() {
        return AvaIre.gson.toJson(items);
    }

    @Nonnull
    @Override
    public Iterator<DataRow> iterator() {
        return new CollectionIterator();
    }

    private void add(DataRow row) {
        this.items.add(new DataRow(row));
    }

    private class CollectionIterator implements Iterator<DataRow> {

        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < Collection.this.items.size();
        }

        @Override
        public DataRow next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return Collection.this.items.get(cursor++);
        }
    }
}
