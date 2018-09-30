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

package com.avairebot.contracts.debug;

import lavalink.client.player.event.AudioEventAdapterWrapped;
import org.json.JSONObject;

import java.lang.reflect.Field;

@SuppressWarnings("Duplicates")
public class EvalAudioEventWrapper extends AudioEventAdapterWrapped {

    /**
     * Creates a {@link JSONObject JSON object} out of all the declared fields of the class
     * instance, if one of the classes extends from the {@link Evalable Evalable class},
     * the {@link #toEvalableString()} method will be called for that object and
     * added onto the main {@link JSONObject JSON object}.
     *
     * @return The {@link JSONObject JSON object} with all the names and values
     * of properties for the current class instance.
     */
    @SuppressWarnings({"WeakerAccess", "JavaDoc"})
    public final JSONObject toEvalableString() {
        JSONObject json = new JSONObject();

        for (Field field : getClass().getDeclaredFields()) {
            json.put(field.getName(), getValueFromField(field));
        }

        return json;
    }

    /**
     * Gets the value for the given {@link Field field}, if the {@link Field fields}
     * value is another instance of an {@link Evalable evalable} object, the
     * {@link #toEvalableString()} method will be called for that object.
     *
     * @param field The field that the value should be retried from.
     * @return The value of the given field, or {@link JSONObject#NULL null}
     * on errors or if the value was <code>null</code>.
     */
    private Object getValueFromField(Field field) {
        try {
            field.setAccessible(true);

            Object value = field.get(this);
            if (value == null) {
                return JSONObject.NULL;
            }

            if (value instanceof Evalable) {
                return ((Evalable) value).toEvalableString();
            }

            if (value instanceof EvalAudioEventWrapper) {
                return ((EvalAudioEventWrapper) value).toEvalableString();
            }

            return value;
        } catch (IllegalAccessException e) {
            return JSONObject.NULL;
        }
    }

    @Override
    public String toString() {
        return toEvalableString().toString();
    }
}
