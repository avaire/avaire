package com.avairebot.orion.database.collection;

import com.avairebot.orion.exceptions.InvalidFormatException;
import com.avairebot.orion.time.Carbon;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataRow {

    private final Map<String, Object> items;

    /**
     * Creates a new data row object from the provided data row.
     *
     * @param row The row to generate the data row from.
     */
    public DataRow(DataRow row) {
        this.items = new HashMap<>();

        for (Map.Entry<String, Object> item : row.items.entrySet()) {
            this.items.put(item.getKey(), item.getValue());
        }
    }

    /**
     * Creates a new data row object from a map of data.
     *
     * @param items The map to generate the data row from.
     */
    public DataRow(Map<String, Object> items) {
        this.items = items;
    }

    /**
     * Gets a object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>NULL</code> if the index doesn't exists.
     */
    public Object get(String name) {
        return get(name, null);
    }

    /**
     * Gets a object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public Object get(String name, Object def) {
        if (has(name)) {
            return items.get(name);
        }

        return def;
    }

    /**
     * Gets a boolean object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>FALSE</code> if the index doesn't exists.
     */
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * Gets a boolean object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public boolean getBoolean(String name, boolean def) {
        Object value = get(name, def);

        if (isString(value)) {
            String str = String.valueOf(value);

            return isEqual(str, "1", "true");
        }

        return (boolean) value;
    }

    /**
     * Gets a double object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>0.0D</code> if the index doesn't exists.
     */
    public double getDouble(String name) {
        return getDouble(name, 0.0D);
    }

    /**
     * Gets a double object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public double getDouble(String name, double def) {
        Object value = get(name, def);

        if (isString(value)) {
            String str = String.valueOf(value);

            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException ex) {
                return def;
            }
        }

        switch (getType(value)) {
            case "Integer":
                value = ((Integer) value).doubleValue();
                break;

            case "Long":
                value = ((Long) value).doubleValue();
                break;

            case "Float":
                value = ((Float) value).doubleValue();
                break;
        }

        try {
            return (double) value;
        } catch (ClassCastException ex) {
            return def;
        }
    }

    /**
     * Gets a integer object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>0</code> if the index doesn't exists.
     */
    public int getInt(String name) {
        return getInt(name, 0);
    }

    /**
     * Gets a integer object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public int getInt(String name, int def) {
        Object value = get(name, def);

        if (isString(value)) {
            String str = String.valueOf(value);

            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                return def;
            }
        }

        switch (getType(value)) {
            case "Double":
                value = ((Double) value).intValue();
                break;

            case "Long":
                value = ((Long) value).intValue();
                break;

            case "Float":
                value = ((Float) value).intValue();
                break;
        }

        try {
            return (int) value;
        } catch (ClassCastException ex) {
            return def;
        }
    }

    /**
     * Gets a long object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>0L</code> if the index doesn't exists.
     */
    public long getLong(String name) {
        return getLong(name, 0L);
    }

    /**
     * Gets a long object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public long getLong(String name, long def) {
        Object value = get(name, def);

        if (isString(value)) {
            String str = String.valueOf(value);

            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ex) {
                return def;
            }
        }

        switch (getType(value)) {
            case "Double":
                value = ((Double) value).longValue();
                break;

            case "Integer":
                value = ((Integer) value).longValue();
                break;

            case "Float":
                value = ((Float) value).longValue();
                break;
        }

        try {
            return (long) value;
        } catch (ClassCastException ex) {
            return def;
        }
    }

    /**
     * Gets a float object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>0F</code> if the index doesn't exists.
     */
    public float getFloat(String name) {
        return getFloat(name, 0F);
    }

    /**
     * Gets a float object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public float getFloat(String name, float def) {
        Object value = get(name, def);

        if (isString(value)) {
            String str = String.valueOf(value);

            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException ex) {
                return def;
            }
        }

        switch (getType(value)) {
            case "Double":
                value = ((Double) value).floatValue();
                break;

            case "Integer":
                value = ((Integer) value).floatValue();
                break;

            case "Long":
                value = ((Long) value).floatValue();
                break;
        }

        try {
            return (float) value;
        } catch (ClassCastException ex) {
            return def;
        }
    }

    /**
     * Gets a string object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>NULL</code> if the index doesn't exists.
     */
    public String getString(String name) {
        return getString(name, null);
    }

    /**
     * Gets a string object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public String getString(String name, String def) {
        Object value = get(name, def);

        return String.valueOf(value);
    }

    /**
     * Gets a carbon timestamp object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @return either (1) The value of the index given,
     * or (2) <code>NULL</code> if the index doesn't exists.
     */
    public Carbon getTimestamp(String name) {
        return getTimestamp(name, null);
    }

    /**
     * Gets a carbon timestamp object from the data rows item list.
     *
     * @param name The index(name) to get.
     * @param def  The default vault to return if the index doesn't exists.
     * @return either (1) The value of the index given,
     * or (2) the default value given.
     */
    public Carbon getTimestamp(String name, Carbon def) {
        try {
            String time = getString(name);

            return new Carbon(time);
        } catch (InvalidFormatException ex) {
            return def;
        }
    }

    /**
     * Checks to see if the given index exists in the data rows list of items.
     *
     * @param name The index(name) to check if exists.
     * @return true if the index exists, otherwise it will return false.
     */
    public boolean has(String name) {
        return items.containsKey(name);
    }

    /**
     * Gets all the keys from the data row.
     *
     * @return All the keys from the data row.
     */
    public Set<String> keySet() {
        return items.keySet();
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
        Gson gson = new Gson();

        return gson.toJson(items);
    }

    private boolean isString(Object name) {
        return getType(name).equalsIgnoreCase("string");
    }

    private String getType(Object name) {
        return name.getClass().getSimpleName();
    }

    private boolean isEqual(String name, String... items) {
        for (String item : items) {
            if (name.equalsIgnoreCase(item)) {
                return true;
            }
        }

        return false;
    }
}
