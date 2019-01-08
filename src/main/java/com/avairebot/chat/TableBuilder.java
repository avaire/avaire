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

package com.avairebot.chat;

import spark.utils.Assert;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * A table builder for displaying key-based content in a pretty format to users,
 * the class is a modified version of the code provided by Naval Kishore.
 * <p>
 * Original code: https://stackoverflow.com/a/41406399
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class TableBuilder {

    private List<String> headers;
    private List<List<String>> content;

    private int paddingSize = 2;
    private int rowPadding = 1;
    private String newLineCharacter = "\n";
    private String jointTableSymbol = "+";
    private String verticalSplitSymbol = "|";
    private String horizontalSplitSymbol = "-";

    /**
     * Sets the headers that should be used for the table.
     *
     * @param headers The set of headers that should be used for the table.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setHeaders(@Nonnull Set<String> headers) {
        return setHeaders(new ArrayList<>(headers));
    }

    /**
     * Sets the headers that should be used for the table.
     *
     * @param headers The list of headers that should be used for the table.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setHeaders(@Nonnull List<String> headers) {
        this.headers = headers;

        return this;
    }

    /**
     * Sets the content that should be used for the table, this is the body itself for
     * the table, where each item in the list is a row, and each row has all the
     * keys corresponding to the {@link #setHeaders(List) headers}.
     *
     * @param content The set of content that should be used for the table.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setContent(@Nonnull List<List<String>> content) {
        this.content = content;

        return this;
    }

    /**
     * Sets the amount of lines that should be used as padding
     * between the rows, by default this is {@code 1}.
     *
     * @param rowPadding The amount of rows that should be padded between the rows.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setRowPadding(int rowPadding) {
        this.rowPadding = rowPadding;

        return this;
    }

    /**
     * Sets the amount of space that should be padded around any of the values and
     * the {@link #setVerticalSplitSymbol(String) vertical split wall},
     * by default this is set to {@code 2}.
     *
     * @param paddingSize he amount of space that should be padded around any value as a minimum.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setPaddingSize(int paddingSize) {
        this.paddingSize = Math.max(paddingSize, 0);

        return this;
    }

    /**
     * Sets the new line character used for creating a new line in
     * the table, by default this is set to {@code \n}.
     *
     * @param newLineCharacter The new line character that should be used.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setNewLineCharacter(@Nonnull String newLineCharacter) {
        this.newLineCharacter = newLineCharacter;

        return this;
    }

    /**
     * Sets the joint table symbol, this is used between two headers
     * or columns, by default this is set to {@code +}.
     *
     * @param jointTableSymbol The joint table symbol that should be used.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setJointTableSymbol(@Nonnull String jointTableSymbol) {
        this.jointTableSymbol = jointTableSymbol;

        return this;
    }

    /**
     * Sets the vertical split symbol, this is used to create the vertical lines between
     * the header keys and the row values, by default this is set to {@code |}
     *
     * @param verticalSplitSymbol The vertical split symbol that should be used.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setVerticalSplitSymbol(@Nonnull String verticalSplitSymbol) {
        this.verticalSplitSymbol = verticalSplitSymbol;

        return this;
    }

    /**
     * Sets the horizontal split symbol, this is used to create the lines above and below the
     * header, as well as the bottom of the table, by default this is set to {@code -}.
     *
     * @param horizontalSplitSymbol The horizontal split symbol that should be used.
     * @return The {@link TableBuilder table builder} class instance.
     */
    public TableBuilder setHorizontalSplitSymbol(@Nonnull String horizontalSplitSymbol) {
        this.horizontalSplitSymbol = horizontalSplitSymbol;

        return this;
    }

    /**
     * Builds the table headers and content, and
     * returns the end result as a string.
     *
     * @return The table built using the table builder instance.
     */
    public String build() {
        Assert.notNull(headers, "The table header can not be null.");
        Assert.notNull(content, "The table content can not be null.");

        StringBuilder stringBuilder = new StringBuilder();

        int rowHeight = rowPadding > 0 ? rowPadding : 1;
        Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidthOfTable(headers, content);

        stringBuilder.append(newLineCharacter);
        stringBuilder.append(newLineCharacter);
        createRowLine(stringBuilder, headers.size(), columnMaxWidthMapping);
        stringBuilder.append(newLineCharacter);

        for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {
            fillCell(stringBuilder, headers.get(headerIndex), headerIndex, columnMaxWidthMapping);
        }

        stringBuilder.append(newLineCharacter);
        createRowLine(stringBuilder, headers.size(), columnMaxWidthMapping);

        for (List<String> row : content) {
            for (int i = 0; i < rowHeight; i++) {
                stringBuilder.append(newLineCharacter);
            }

            for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {
                fillCell(stringBuilder, row.get(cellIndex), cellIndex, columnMaxWidthMapping);
            }

        }

        stringBuilder.append(newLineCharacter);
        createRowLine(stringBuilder, headers.size(), columnMaxWidthMapping);
        stringBuilder.append(newLineCharacter);
        stringBuilder.append(newLineCharacter);

        return stringBuilder.toString();
    }

    private void fillSpace(StringBuilder stringBuilder, int length) {
        for (int i = 0; i < length; i++) {
            stringBuilder.append(" ");
        }
    }

    private void createRowLine(StringBuilder builder, int headersListSize, Map<Integer, Integer> columnMaxWidthMapping) {
        for (int i = 0; i < headersListSize; i++) {
            if (i == 0) {
                builder.append(jointTableSymbol);
            }

            for (int j = 0; j < columnMaxWidthMapping.get(i) + paddingSize * 2; j++) {
                builder.append(horizontalSplitSymbol);
            }
            builder.append(jointTableSymbol);
        }
    }

    private Map<Integer, Integer> getMaximumWidthOfTable(List<String> headersList, List<List<String>> rowsList) {
        Map<Integer, Integer> columnMaxWidthMapping = new HashMap<>();

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            columnMaxWidthMapping.put(columnIndex, 0);
        }

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            if (headersList.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
                columnMaxWidthMapping.put(columnIndex, headersList.get(columnIndex).length());
            }
        }

        for (List<String> row : rowsList) {
            for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {
                if (row.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
                    columnMaxWidthMapping.put(columnIndex, row.get(columnIndex).length());
                }
            }
        }

        for (int columnIndex = 0; columnIndex < headersList.size(); columnIndex++) {
            if (columnMaxWidthMapping.get(columnIndex) % 2 != 0) {
                columnMaxWidthMapping.put(columnIndex, columnMaxWidthMapping.get(columnIndex) + 1);
            }
        }

        return columnMaxWidthMapping;
    }

    private int getOptimumCellPadding(int cellIndex, int datalength, Map<Integer, Integer> columnMaxWidthMapping, int cellPaddingSize) {
        if (datalength % 2 != 0) {
            datalength++;
        }

        if (datalength < columnMaxWidthMapping.get(cellIndex)) {
            cellPaddingSize = cellPaddingSize + (columnMaxWidthMapping.get(cellIndex) - datalength) / 2;
        }

        return cellPaddingSize;
    }

    private void fillCell(StringBuilder stringBuilder, String cell, int cellIndex, Map<Integer, Integer> columnMaxWidthMapping) {
        int cellPaddingSize = getOptimumCellPadding(cellIndex, cell.length(), columnMaxWidthMapping, paddingSize);

        if (cellIndex == 0) {
            stringBuilder.append(verticalSplitSymbol);
        }

        fillSpace(stringBuilder, cellPaddingSize);
        stringBuilder.append(cell);
        if (cell.length() % 2 != 0) {
            stringBuilder.append(" ");
        }

        fillSpace(stringBuilder, cellPaddingSize);
        stringBuilder.append(verticalSplitSymbol);
    }
}
