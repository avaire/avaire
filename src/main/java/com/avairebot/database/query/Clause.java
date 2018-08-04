package com.avairebot.database.query;

import com.avairebot.contracts.database.QueryClause;

public class Clause implements QueryClause {

    /**
     * The first field for the clause.
     */
    private final String one;

    /**
     * The clause identifier that should be used when
     * comparing field {@link #one} and {@link #two}.
     */
    private final String identifier;

    /**
     * The second field for the clause.
     */
    private final Object two;

    /**
     * The order operator that should be appended to the end of the clause.
     */
    private OperatorType order;

    /**
     * Creates a new clause with a <code>WHERE</code> order.
     *
     * @param one        The first field to compare with
     * @param identifier The identifier to use when comparing the fields
     * @param two        The second field to compare with
     */
    public Clause(String one, String identifier, Object two) {
        this.one = one;
        this.identifier = identifier;
        this.two = two;

        this.order = OperatorType.AND;
    }

    /**
     * Creates a new clause with the provided order.
     *
     * @param one        The first field to compare with
     * @param identifier The identifier to use when comparing the fields
     * @param two        The second field to compare with
     * @param order      The order to append to the clause
     */
    public Clause(String one, String identifier, Object two, OperatorType order) {
        this.one = one;
        this.identifier = identifier;
        this.two = two;

        this.order = order;
    }

    /**
     * Gets the first field for the clause.
     *
     * @return the first field for the clause.
     */
    public String getOne() {
        return one;
    }

    /**
     * Gets the clause identifier that should be used when
     * comparing field {@link #one} and {@link #two}.
     *
     * @return the clause identifier that should be used.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the second field for the clause.
     *
     * @return the second field for the clause.
     */
    public Object getTwo() {
        return two;
    }

    /**
     * Gets the order operator that should be appended
     * to the end of the clause.
     *
     * @return the operator type used for the where clause.
     */
    public OperatorType getOrder() {
        return order;
    }

    /**
     * Sets the order operator that should be appended to the end of the clause.
     *
     * @param order The order to set
     */
    public void setOrder(OperatorType order) {
        this.order = order;
    }
}
