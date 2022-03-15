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
 * Represents a q dictionary type.
 */
public final class QDictionary implements Iterable<QDictionary.KeyValuePair> {

    private final Object keys;
    private final Object values;
    private final int length;
    private final boolean areValuesArray;

    /**
     * Creates new {@link QDictionary} instance with given keys and values arrays.
     *
     * @param keys
     *            keys array
     * @param values
     *            values array
     *
     * @throws IllegalArgumentException
     */
    public QDictionary(final Object keys, final Object values) {
        if (keys == null || !keys.getClass().isArray()) {
            throw new IllegalArgumentException("Parameter: keys is not an array");
        }
        if (values == null || !(values.getClass().isArray() || values instanceof QTable)) {
            throw new IllegalArgumentException("Parameter: values is not an array nor table");
        }

        length = Array.getLength(keys);
        if ((values.getClass().isArray() && length != Array.getLength(values)) || (values instanceof QTable && length != ((QTable) values).getRowsCount())) {
            throw new IllegalArgumentException("Keys and values cannot have different length");
        }

        this.keys = keys;
        this.values = values;
        this.areValuesArray = values.getClass().isArray();
    }

    /**
     * Returns array containing list of keys stored in the dictionary.
     *
     * @return an array of keys
     */
    public Object getKeys() {
        return keys;
    }

    /**
     * Returns array containing list of values stored in the dictionary.
     *
     * @return an array of values
     */
    public Object getValues() {
        return values;
    }

    /**
     * Gets a number of entries in {@link QDictionary}.
     *
     * @return a number of entries
     */
    public int size() {
        return length;
    }

    /**
     * <p>
     * Returns an iterator over a key/value pairs stored in the dictionary.
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

            private KeyValuePair view = new KeyValuePair(index);

            public boolean hasNext() {
                return index < length;
            }

            public KeyValuePair next() {
                if (hasNext()) {
                    view.setIndex(index++);
                    return view;
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
     * Returns a String that represents the current {@link QDictionary}.
     *
     * @return a String representation of the {@link QDictionary}
     * @see Object#toString()
     */
    @Override
    public String toString() {
        if (areValuesArray) {
            return "QDictionary: " + Utils.arrayToString(keys) + "!" + Utils.arrayToString(values);
        } else {
            return "QDictionary: " + Utils.arrayToString(keys) + "!" + values.toString();
        }
    }

    /**
     * Indicates whether some other object is "equal to" this dictionary. {@link QDictionary} objects are considered
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

        if (!(obj instanceof QDictionary)) {
            return false;
        }

        final QDictionary d = (QDictionary) obj;
        if (areValuesArray) {
            return Utils.deepArraysEquals(keys, d.keys) && Utils.deepArraysEquals(values, d.values);
        } else {
            return Utils.deepArraysEquals(keys, d.keys) && values.equals(d.values);
        }
    }

    /**
     * Returns a hash code value for this {@link QDictionary}.
     *
     * @return a hash code value for this object
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 31 * Utils.arrayHashCode(keys) + Utils.arrayHashCode(values);
    }

    /**
     * Defines a key/value pair that can be retrieved.
     */
    public class KeyValuePair {

        private int index;

        KeyValuePair(final int index) {
            setIndex(index);
        }

        /**
         * Returns index of the view.
         *
         * @return {@link int}
         */
        public int getIndex() {
            return index;
        }

        /**
         * Moves the view to new index.
         *
         * @param index
         *            the index to set
         */
        public void setIndex(final int index) {
            if (index < 0 || index > length) {
                throw new IndexOutOfBoundsException();
            }
            this.index = index;
        }

        /**
         * Returns key from given pair.
         *
         * @return key
         */
        public Object getKey() {
            return Array.get(keys, index);
        }

        /**
         * Returns value from given pair.
         *
         * @return value
         */
        public Object getValue() {
            if (areValuesArray) {
                return Array.get(values, index);
            } else {
                return ((QTable) values).get(index);
            }
        }
    }
}
