package com.avairebot.contracts.reflection;

import com.avairebot.AvaIre;

public abstract class Reflectionable implements Reflectional {
    
    /**
     * The AvaIre class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final AvaIre avaire;

    public Reflectionable(AvaIre avaire) {
        this.avaire = avaire;
    }
}
