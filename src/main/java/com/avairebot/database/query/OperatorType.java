package com.avairebot.database.query;

public enum OperatorType {
    
    /**
     * The common SQL <code>AND</code> operator.
     */
    AND("AND"),
    /**
     * The common SQL <code>OR</code> operator.
     */
    OR("OR");

    /**
     * The operators string value.
     */
    private final String operator;

    /**
     * Creates a new operator type with the provided operator string value.
     *
     * @param operator The operator string value to use
     */
    private OperatorType(String operator) {
        this.operator = operator;
    }

    /**
     * Gets the operator string value.
     *
     * @return the operator string value.
     */
    public String getOperator() {
        return operator;
    }
}
