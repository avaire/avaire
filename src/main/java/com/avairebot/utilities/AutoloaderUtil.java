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

package com.avairebot.utilities;

import com.avairebot.AvaIre;
import com.avairebot.contracts.reflection.Reflectional;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.Consumer;

public class AutoloaderUtil {

    private static final Logger log = LoggerFactory.getLogger(AutoloaderUtil.class);

    /**
     * Loads all the classes in the given package path that implements the
     * {@link Reflectional reflectional interface}, all classes found
     * will be instantiated with the AvaIre instance passed to the
     * class constructor, and the class will then be sent to
     * the consumer callback so it can be used.
     *
     * @param path     The package path that should be autoloaded.
     * @param callback The consumer used to register the loaded class.
     */
    public static void load(String path, Consumer<Reflectional> callback) {
        load(path, callback, true);
    }

    /**
     * Loads all the classes in the given package path that implements the
     * {@link Reflectional reflectional interface}, all classes found
     * will be instantiated and passed to the class constructor,
     * and the class will then be sent to the consumer
     * callback so it can be used.
     * <p>
     * If the {@code parseAvaIreInstance} argument is set to true, the AvaIre
     * application instance will be parsed to the constructor of the class,
     * however if it is set false then the class will be instantiated
     * without any arguments parsed to the its constructor.
     *
     * @param path                The package path that should be autoloaded.
     * @param callback            The consumer used to register the loaded class.
     * @param parseAvaIreInstance Determines if the AvaIre instance should be passed
     *                            to the loaded class constructor.
     */
    public static void load(String path, Consumer<Reflectional> callback, boolean parseAvaIreInstance) {
        Set<Class<? extends Reflectional>> types = new Reflections(path).getSubTypesOf(Reflectional.class);

        for (Class<? extends Reflectional> reflectionClass : types) {
            if (reflectionClass.getPackage().getName().contains("contracts")) {
                continue;
            }

            try {
                if (parseAvaIreInstance) {
                    Class[] arguments = new Class[1];
                    arguments[0] = AvaIre.class;

                    //noinspection JavaReflectionMemberAccess
                    callback.accept(reflectionClass.getDeclaredConstructor(arguments).newInstance(
                        AvaIre.getInstance()
                    ));
                } else {
                    callback.accept(reflectionClass.getDeclaredConstructor().newInstance());
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Failed to create a new instance of package {}", reflectionClass.getName(), e);
            }
        }
    }
}
