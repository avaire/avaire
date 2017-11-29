package com.avairebot.orion.contracts.reflection;

import com.avairebot.orion.Orion;

public abstract class Reflectionable {

    /**
     * The Orion class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final Orion orion;

    public Reflectionable(Orion orion) {
        this.orion = orion;
    }
}
