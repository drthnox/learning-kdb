/**
 * Copyright (c) 2011-2014 Exxeleron GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.exxeleron.qjava;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Represents a q table type.
 */
public final class QTable implements Iterable<QTable.Row>, Table {

    private final String[] columns;
    private final Object[] data;
    private final int rowsCount;
    private final Map<String, Integer> columnsMap;

    /**
     * Initializes a new instance of the {@link QTable} with specified column names and data matrix.
     *
     * @param columns
     *            column names
     * @param data
     *            data matrix
     *
     * @throws IllegalArgumentException
     */
    public QTable(final String[] columns, final Object[] data) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("Columns array cannot be null or 0-length");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data matrix cannot be null or 0-length");
        }
        if (columns.length != data.length) {
            throw new IllegalArgumentException("Columns array and data matrix cannot have different length");
        }
        for (final Object col : data) {
            if (col == null || !col.getClass().isArray()) {
                throw new IllegalArgumentException("Non array column found in data matrix");
            }
        }

        this.columnsMap = new HashMap<String, Integer>();
        for (int i = 0; i < columns.length; i++) {
            this.columnsMap.put(columns[i], i);
        }

        this.columns = columns;
        this.data = data;
        this.rowsCount = Array.getLength(data[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#size()
     */
    public int getRowsCount() {
        return rowsCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#getColumnsCount()
     */
    public int getColumnsCount() {
        return columns.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#getColumnIndex(java.lang.String)
     */
    public int getColumnIndex(final String column) {
        return columnsMap.get(column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#hasColumn(java.lang.String)
     */
    public boolean hasColumn(final String column) {
        return columnsMap.containsKey(column);
    }

    /**
     * Gets an array of columns in current {@link QTable}.
     *
     * @return an array of columns
     */
    public String[] getColumns() {
        return columns;
    }

    /**
     * Gets a data matrix in current {@link QTable}.
     *
     * @return an array of arrays with internal representation of data
     */
    public Object[] getData() {
        return data;
    }

    /**
     * Gets a row of data from current {@link QTable}.
     *
     * @param index
     *            0 based row index
     * @return Row object representing a row in current {@link QTable}
     */
    public Row get(final int index) {
        return new Row(index);
    }

    /**
     * Returns a String that represents the current {@link QTable}.
     *
     * @return a String representation of the {@link QTable}
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "QTable: " + Utils.arrayToString(columns) + "!" + Utils.arrayToString(data);
    }

    /**
     * Indicates whether some other object is "equal to" this table. {@link QTable} objects are considered equal if the
     * columns and data matrix are equal for both instances.
     *
     * @return <code>true</code> if this object is the same as the obj argument, <code>false</code> otherwise.
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof QTable)) {
            return false;
        }

        final QTable t = (QTable) obj;
        return Utils.deepArraysEquals(columns, t.columns) && Utils.deepArraysEquals(data, t.data);
    }

    /**
     * Returns a hash code value for this {@link QTable}.
     *
     * @return a hash code value for this object
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * Utils.arrayHashCode(columns) + Utils.arrayHashCode(data);
    }

    /**
     * <p>
     * Returns an iterator over rows stored in the table. Iterator uses same {@link Row} instance as a moving window
     * over the table data.
     * </p>
     *
     * <p>
     * Note that the iterator returned by this method will throw an {@link UnsupportedOperationException} in response to
     * its <code>remove</code> method.
     * </p>
     *
     * @see Iterable#iterator()
     */
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {

            private int index = 0;

            private Row row = new Row(index);

            public boolean hasNext() {
                return index < rowsCount;
            }

            public Row next() {
                if (hasNext()) {
                    row.setRowIndex(index++);
                    return row;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Represents view at single row in a table.
     */
    public class Row implements Iterable<Object> {

        private int rowIndex;

        /**
         * Creates row view for row with given index.
         *
         * @param rowIndex
         *            index of the row
         */
        Row(final int rowIndex) {
            setRowIndex(rowIndex);
        }

        /**
         * Returns index of the row.
         *
         * @return {@link int}
         */
        public int getRowIndex() {
            return rowIndex;
        }

        /**
         * Moves the row view to new index.
         *
         * @param rowIndex
         *            the rowIndex to set
         */
        public void setRowIndex(final int rowIndex) {
            if (rowIndex < 0 || rowIndex > rowsCount) {
                throw new IndexOutOfBoundsException();
            }
            this.rowIndex = rowIndex;
        }

        /**
         * Creates a copy of entire row and returns it as an {@link Object} array.
         *
         * @return {@link Object}[] with copy of entire row
         */
        public Object[] toArray() {
            final int length = getLength();
            final Object[] row = new Object[length];

            for (int i = 0; i < length; i++) {
                row[i] = get(i);
            }

            return row;
        }

        /**
         * Returns number of columns in the current {@link QTable}.
         *
         * @return number of columns
         */
        public int getLength() {
            return columns.length;
        }

        /**
         * Gets an object stored under specific index.
         *
         * @param index
         *            0 based index
         * @return object
         */
        public Object get(final int index) {
            return Array.get(data[index], rowIndex);
        }

        /**
         * Sets an object stored under specific index.
         *
         * @param index
         *            0 based index
         * @param value
         *            value to be set
         */
        public void set(final int index, final Object value) {
            Array.set(data[index], rowIndex, value);
        }

        @Override
        public String toString() {
            return Utils.arrayToString(columns) + "!" + Utils.arrayToString(toArray());
        }

        /**
         * <p>
         * Returns an iterator over columns in a particular row in the table.
         * </p>
         *
         * <p>
         * Note that the iterator returned by this method will throw an {@link UnsupportedOperationException} in
         * response to its <code>remove</code> method.
         * </p>
         *
         * @see Iterable#iterator()
         */
        public Iterator<Object> iterator() {
            return new Iterator<Object>() {

                int index = 0;

                public boolean hasNext() {
                    return index < getLength();
                }

                public Object next() {
                    if (hasNext()) {
                        return Array.get(data[index++], rowIndex);
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }

}
