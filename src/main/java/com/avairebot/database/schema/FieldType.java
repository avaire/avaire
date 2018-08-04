package com.avairebot.database.schema;

public enum FieldType {

    INTEGER("INT", true, 1, false),
    DECIMAL("DECIMAL", true, 2, true),
    DOUBLE("DOUBLE", true, 1, true),
    FLOAT("FLOAT", true, 1, true),
    LONG("BIGINT", true, 1, false),
    BOOLEAN("BOOLEAN", false, 0, false),
    DATE("DATE", false, 0, false),
    DATETIME("DATETIME", false, 0, false),
    STRING("VARCHAR", true, 1, true),
    LONGTEXT("LONGTEXT", false, 0, false),
    MEDIUMTEXT("MEDIUMTEXT", false, 0, false),
    SMALLTEXT("TINYTEXT", false, 0, false),
    TEXT("TEXT", false, 0, false);

    private final String name;
    private final boolean arguments;
    private final int argumentAmount;
    private final boolean requiredArguments;

    private FieldType(String name, boolean arguments, int argumentAmount, boolean requiredArguments) {
        this.name = name;
        this.arguments = arguments;
        this.argumentAmount = argumentAmount;
        this.requiredArguments = requiredArguments;
    }

    public String getName() {
        return name;
    }

    public boolean hasArguments() {
        return arguments;
    }

    public int getArguments() {
        return argumentAmount;
    }

    public boolean requireArguments() {
        return requiredArguments;
    }
}
