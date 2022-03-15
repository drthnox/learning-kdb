package com.dg.learning.kdb.glue;

import com.exxeleron.qjava.QTable;
import com.exxeleron.qjava.QType;
import com.google.common.collect.Lists;
import cucumber.api.DataTable;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.LogManager.getLogger;

public class KdbUtils {
    private static final String IS_DOUBLE_REG_EXP = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
    private static final Pattern IS_DOUBLE_PATTERN = Pattern.compile(IS_DOUBLE_REG_EXP);
    public static Logger LOGGER = getLogger(KdbUtils.class);
    public static Map<QType, Predicate<String>> canParse = new HashMap<>();
    public static Set<String> dates = new HashSet<>();

    public static DataTable convert(QTable qTable) {
        DataTable dataTable = DataTable.create(
                Lists.newArrayList(toRows(qTable)),
                Locale.getDefault(),
                qTable.getColumns()
        );
        return dataTable;
    }

    private static List toRows(QTable qTable) {
        return Lists.newArrayList(qTable.iterator());
    }

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && (s.charAt(i) == '-' || s.charAt(i) == '+')) {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    public static boolean isLong(String s) {
        Long aLong = LongParser.tryParseLong(s);
        return aLong != null;
    }

    public static boolean isString(String s) {
        return true;
    }

    public static boolean isDate(String s) {
        return dates.contains(s);
    }

    public static boolean isDouble(String s) {
        Matcher m = IS_DOUBLE_PATTERN.matcher(s);
        return m.matches();
    }

    public static int daysInMonth(int year, int month) {
        int daysInMonth;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysInMonth = 31;
                break;
            case 2:
                if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                break;
            default:
                // returns 30 even for nonexistent months
                daysInMonth = 30;
        }
        return daysInMonth;
    }

    /**
     * Returns a free port number on localhost.
     * <p>
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
     * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
     *
     * @return a free port number on localhost
     * @throws IllegalStateException if unable to find a free port
     */
    public static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int localPort = socket.getLocalPort();
            socket.close();
            return localPort;
        } catch (Exception e) {
            LOGGER.error("Error trying to find a free port.", e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
    }


    public static class LongParser {
        // Since tryParseLong represents the value as negative during processing, we
        // counter-intuitively want to keep the sign if the result is negative and
        // negate it if it is positive.
        private static final int MULTIPLIER_FOR_NEGATIVE_RESULT = 1;
        private static final int MULTIPLIER_FOR_POSITIVE_RESULT = -1;

        private static final int FIRST_CHARACTER_POSITION = 0;
        private static final int SECOND_CHARACTER_POSITION = 1;
        private static final char NEGATIVE_SIGN_CHARACTER = '-';
        private static final char POSITIVE_SIGN_CHARACTER = '+';
        private static final int DIGIT_MAX_VALUE = 9;
        private static final int DIGIT_MIN_VALUE = 0;
        private static final char ZERO_CHARACTER = '0';
        private static final int RADIX = 10;

        /**
         * Parses a string representation of a long significantly faster than
         * <code>Long.ParseLong</code>, and avoids the noteworthy overhead of
         * throwing an exception on failure. Based on the parseInt code from
         * http://nadeausoftware.com/articles/2009/08/java_tip_how_parse_integers_quickly
         *
         * @param stringToParse The string to try to parse as a <code>long</code>.
         * @return the boxed <code>long</code> value if the string was a valid
         * representation of a long; otherwise <code>null</code>.
         */
        public static Long tryParseLong(final String stringToParse) {
            if (stringToParse == null || stringToParse.isEmpty()) {
                return null;
            }

            final int inputStringLength = stringToParse.length();
            long value = 0;

        /*
         * The absolute value of Long.MIN_VALUE is greater than the absolute
         * value of Long.MAX_VALUE, so during processing we'll use a negative
         * value, then we'll multiply it by signMultiplier before returning it.
         * This allows us to avoid a conditional add/subtract inside the loop.
         */

            int signMultiplier = MULTIPLIER_FOR_POSITIVE_RESULT;

            // Get the first character.
            char firstCharacter = stringToParse.charAt(FIRST_CHARACTER_POSITION);

            if (firstCharacter == NEGATIVE_SIGN_CHARACTER) {
                // The first character is a negative sign.
                if (inputStringLength == 1) {
                    // There are no digits.
                    // The string is not a valid representation of a long value.
                    return null;
                }

                signMultiplier = MULTIPLIER_FOR_NEGATIVE_RESULT;
            } else if (firstCharacter == POSITIVE_SIGN_CHARACTER) {
                // The first character is a positive sign.
                if (inputStringLength == 1) {
                    // There are no digits.
                    // The string is not a valid representation of a long value.
                    return null;
                }
            } else {
                // Store the (negative) digit (although we aren't sure yet if it's
                // actually a digit).
                value = -(firstCharacter - ZERO_CHARACTER);
                if (value > DIGIT_MIN_VALUE || value < -DIGIT_MAX_VALUE) {
                    // The first character is not a digit (or a negative sign).
                    // The string is not a valid representation of a long value.
                    return null;
                }
            }

            // Establish the "maximum" value (actually minimum since we're working
            // with negatives).
            final long rangeLimit = (signMultiplier == MULTIPLIER_FOR_POSITIVE_RESULT)
                    ? -Long.MAX_VALUE
                    : Long.MIN_VALUE;

            // Capture the maximum value that we can multiply by the radix without
            // overflowing.
            final long maxLongNegatedPriorToMultiplyingByRadix = rangeLimit / RADIX;

            for (int currentCharacterPosition = SECOND_CHARACTER_POSITION;
                 currentCharacterPosition < inputStringLength;
                 currentCharacterPosition++) {
                // Get the current digit (although we aren't sure yet if it's
                // actually a digit).
                long digit = stringToParse.charAt(currentCharacterPosition)
                        - ZERO_CHARACTER;

                if (digit < DIGIT_MIN_VALUE || digit > DIGIT_MAX_VALUE) {
                    // The current character is not a digit.
                    // The string is not a valid representation of a long value.
                    return null;
                }

                if (value < maxLongNegatedPriorToMultiplyingByRadix) {
                    // The value will be out of range if we multiply by the radix.
                    // The string is not a valid representation of a long value.
                    return null;
                }

                // Multiply by the radix to slide all the previously parsed digits.
                value *= RADIX;

                if (value < (rangeLimit + digit)) {
                    // The value would be out of range if we "added" the current
                    // digit.
                    return null;
                }

                // "Add" the digit to the value.
                value -= digit;
            }

            // Return the value (adjusting the sign if needed).
            return value * signMultiplier;
        }
    }
}
