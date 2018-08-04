package com.avairebot.database.schema;

public enum DatabaseEngine {

    MyISAM("MyISAM"),
    InnoDB("InnoDB"),
    MERGE("MERGE"),
    MEMORY("MEMORY"),
    BDB("BDB"),
    EXAMPLE("EXAMPLE"),
    FEDERATED("FEDERATED"),
    ARCHIVE("ARCHIVE"),
    CSV("CSV"),
    BLACKHOLE("BLACKHOLE");

    private final String engine;

    DatabaseEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Gets the database engine string value.
     *
     * @return the database engines string value.
     */
    public String getEngine() {
        return engine;
    }
}
