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
