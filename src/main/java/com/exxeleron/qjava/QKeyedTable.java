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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a q keyed table type.
 */
public class QKeyedTable implements Iterable<QKeyedTable.KeyValuePair>, Table {

    private final QTable keys;
    private final QTable values;
    private final int length;

    /**
     * Creates new {@link QKeyedTable} instance with given keys and values arrays.
     *
     * @param keys
     *            keys {@link QTable}
     * @param values
     *            values {@link QTable}
     *
     * @throws IllegalArgumentException
     */
    public QKeyedTable(final QTable keys, final QTable values) {
        length = keys.getRowsCount();
        if (length != values.getRowsCount()) {
            throw new IllegalArgumentException("Keys and value arrays cannot have different length");
        }

        this.keys = keys;
        this.values = values;
    }

    /**
     * Returns {@link QTable} containing table keys.
     *
     * @return table of keys
     */
    public QTable getKeys() {
        return keys;
    }

    /**
     * Returns {@link QTable} containing table values.
     *
     * @return table of values
     */
    public QTable getValues() {
        return values;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#size()
     */
    public int getRowsCount() {
        return keys.getRowsCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#getColumnsCount()
     */
    public int getColumnsCount() {
        return keys.getColumnsCount() + values.getColumnsCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#hasColumn(java.lang.String)
     */
    public boolean hasColumn(final String column) {
        return keys.hasColumn(column) || values.hasColumn(column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.exxeleron.qjava.Table#getColumnIndex(java.lang.String)
     */
    public int getColumnIndex(final String column) {
        if (keys.hasColumn(column)) {
            return keys.getColumnIndex(column);
        } else {
            return keys.getColumnsCount() + values.getColumnIndex(column);
        }
    }

    /**
     * <p>
     * Returns an iterator over a key/value pairs stored in the keyed table.
     * </p>
     *
     * <p>
     * Note that the iterator returned by this method will throw an {@link UnsupportedOperationException} in response to
     * its <code>remove</code> method.
     * </p>
     *
     * @see Iterable#iterator()
     */
    public Iterator<KeyValuePair> iterator() {
        return new Iterator<KeyValuePair>() {

            private int index = 0;

            private KeyValuePair row = new KeyValuePair(0);

            public boolean hasNext() {
                return index < length;
            }

            public KeyValuePair next() {
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
     * Returns a String that represents the current {@link QKeyedTable}.
     *
     * @return a String representation of the {@link QKeyedTable}
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return "QKeyedTable: " + keys + "|" + values;
    }

    /**
     * Indicates whether some other object is "equal to" this keyed table. {@link QKeyedTable} objects are considered
     * equal if the keys and values lists are equal for both instances.
     *
     * @return <code>true</code> if this object is the same as the obj argument, <code>false</code> otherwise.
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof QKeyedTable)) {
            return false;
        }

        final QKeyedTable kt = (QKeyedTable) obj;
        return keys.equals(kt.getKeys()) && values.equals(kt.getValues());
    }

    /**
     * Returns a hash code value for this {@link QKeyedTable}.
     *
     * @return a hash code value for this object
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * keys.hashCode() + values.hashCode();
    }

    /**
     * Defines a key/value pair that can be retrieved.
     */
    public class KeyValuePair {

        private int index;

        KeyValuePair(final int index) {
            this.index = index;
        }

        /**
         * Returns index of the row.
         *
         * @return {@link int}
         */
        public int getRowIndex() {
            return index;
        }

        /**
         * Moves the row view to new index.
         *
         * @param index
         *            the index to set
         */
        public void setRowIndex(final int index) {
            if (index < 0 || index > getRowsCount()) {
                throw new IndexOutOfBoundsException();
            }
            this.index = index;
        }

        /**
         * Returns key from given pair.
         *
         * @return key
         */
        public QTable.Row getKey() {
            return keys.get(index);
        }

        /**
         * Returns value from given pair.
         *
         * @return value
         */
        public QTable.Row getValue() {
            return values.get(index);
        }
    }

}
