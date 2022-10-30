package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/main/com/nextlabs/util/WildcardPattern.java#1 $
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class implements text matching with wildcards.
 *
 * This is the list of supported wildcards. Quotes below are used only for
 * delimiting the actual wildcard - they are not part of the wildcard string.
 *
 * '*' matches a sequence of zero or more characters other than the separator
 * character '/'
 *
 * '**' matches a sequence of zero or more characters, including any number of
 * separator '/' characters.
 *
 * The rest of the patterns use the concept of a character class, which
 * provides a way to define related groups of characters. Character classes
 * are denoted by a single character, which may be an upper case
 * or a lower case. A lower case character represents a single-character
 * match; an upper-case character represents a sequence of one or more
 * characters.
 *
 * A character class designator is preceded  by either a question mark '?'
 * to denote the character class itself, or an exclamation point '!' to denote
 * an inversion of that character class. For example, '?d' matches any digit
 * character, while '!d' matches any non-digit character.
 *
 * This is the list of supported character classes:
 *
 * '?a' matches a single letter.
 * '?A' matches a sequence of one or more letters.
 * '!a' matches a single non-letter.
 * '!A' matches a sequence of one or more non-letters.
 *
 * '?c' matches a single character, including the separator character '/'.
 * '?C' matches a sequence of one or more characters, including the separator
 * character '/'.
 * '!c' matches a single character other than the separator character '/'.
 * '!C' matches a sequence of one or more non-separator '/' characters.
 *
 * '?d' matches a single digit.
 * '?D' matches a sequence of one or more digits.
 * '!d' matches a single non-digit.
 * '!D' matches a sequence of one or more non-digits.
 *
 * '?l' matches a single lower case letter.
 * '?L' matches a sequence of one or more lower case letters.
 * '!l' matches a single character that is not a lower case letter.
 * '!L' matches a sequence of one or more characters that are not lower case
 * letters.
 *
 * '?s' matches a single whitespace character.
 * '?S' matches a sequence of one or more whitespace characters.
 * '!s' matches a single character that is not a whitespace character.
 * '!S' matches a sequence of one or more characters that are not whitespace
 * characters.
 *
 * '?u' matches a single upper case letter.
 * '?U' matches a sequence of one or more upper case letters.
 * '!u' matches a single character that is not a upper case letter.
 * '!U' matches a sequence of one or more characters that are not upper case
 * letters.
 *
 * In case-insensitive mode character classes 'U', 'L', and 'A' are identical.
 *
 * @author Alan Morgan
 * @author Sergey Kalinichenko
 */

public class WildcardPattern {

    public static final CaseSensitivity CASE_SENSITIVE =
        CaseSensitivity.SENSITIVE;

    public static final CaseSensitivity CASE_INSENSITIVE =
        CaseSensitivity.INSENSITIVE;

    // These constants define bit patterns for each character class:
    private static final int CHAR_ANY           = 0x0001;
    private static final int CHAR_UPPER         = 0x0002;
    private static final int CHAR_LOWER         = 0x0004;
    private static final int CHAR_ALPHA         = 0x0008;
    private static final int CHAR_DIGIT         = 0x0010;
    private static final int CHAR_BLANK         = 0x0020;
    private static final int CHAR_NOT_SEPARATOR = 0x0040;
    private static final int CHAR_NOT_UPPER     = 0x0080;
    private static final int CHAR_NOT_LOWER     = 0x0100;
    private static final int CHAR_NOT_ALPHA     = 0x0200;
    private static final int CHAR_NOT_DIGIT     = 0x0400;
    private static final int CHAR_NOT_BLANK     = 0x0800;

    /**
     * The string representation of this wildcard pattern.
     */
    private final String pattern;

    /**
     * An array of bit patterns describing the character class expected
     * at a particular position.
     */
    private final int[] expected;

    /**
     * An array of flags indicating that the element at the particular position
     * is allowed to match zero or more character of its character class.
     */
    private final boolean[] zeroOrMore;

    /**
     * The characters of the normalized pattern.
     */
    private final char [] normPattern;

    /**
     * The match mode defining the case sensitivity of the matching.
     */
    private final CaseSensitivity matchMode;

    /**
     * Compiles a string representation of the pattern into a case-insensitive
     * <code>WildcardPattern</code>.
     *
     * @param pattern the string representation of the pattern to be compiled.
     * @return the <code>WildcardPattern</code> representing the pattern.
     */
    public static WildcardPattern compile(String pattern) {
        return new WildcardPattern(pattern, CaseSensitivity.INSENSITIVE);
    }

    /**
     * Compiles a string representation of the pattern into
     * a <code>WildcardPattern</code> with the defined case sensitivity.
     *
     * @param pattern the string representation of the pattern to be compiled.
     * @param comparisonType the flag defining the way the characters
     * are to be compared.
     * @return the <code>WildcardPattern</code> representing the pattern.
     */
    public static WildcardPattern compile(
        String pattern
    ,   CaseSensitivity comparisonType
    ) {
        return new WildcardPattern(pattern, comparisonType);
    }

    /**
     * Creates a <code>WildcardPattern</code> from the specified pattern.
     *
     * @param pattern the string representation of the pattern to be compiled.
     * @param comparisonType the flag defining the way the characters
     * are to be compared.
     */
    private WildcardPattern(String pattern, CaseSensitivity comparisonType) {
        if (comparisonType == null) {
            throw new NullPointerException("comparisonType");
        }
        if ( pattern == null ) {
            throw new NullPointerException("pattern");
        }

        matchMode = comparisonType;

        this.pattern = pattern;

        int patternLength = pattern.length();
        int patternLast = patternLength-1;

        List<Integer> expected = new ArrayList<Integer>();
        List<Boolean> zeroOrMore = new ArrayList<Boolean>();
        StringBuffer normPattern = new StringBuffer();

        for (int i = 0 ; i != patternLength ; i++) {
            char ch = pattern.charAt(i);
            if ((ch == '?' || ch == '!') && i != patternLast) {
                char next = pattern.charAt(++i);
                int charClass;
                switch(next) {
                    case 'U':
                    case 'u':
                        charClass = (ch=='?') ? CHAR_UPPER : CHAR_NOT_UPPER;
                        break;
                    case 'L':
                    case 'l':
                        charClass = (ch=='?') ? CHAR_LOWER : CHAR_NOT_LOWER;
                        break;
                    case 'S':
                    case 's':
                        charClass = (ch=='?') ? CHAR_BLANK : CHAR_NOT_BLANK;
                        break;
                    case 'A':
                    case 'a':
                        charClass = (ch=='?') ? CHAR_ALPHA : CHAR_NOT_ALPHA;
                        break;
                    case 'D':
                    case 'd':
                        charClass = (ch=='?') ? CHAR_DIGIT : CHAR_NOT_DIGIT;
                        break;
                    case 'C':
                    case 'c':
                        charClass = (ch=='?') ? CHAR_NOT_SEPARATOR : CHAR_ANY;
                        break;
                    default:
                        // Unknown character classes are treated as a single
                        // character equal to the character itself.
                        charClass = 0;
                        break;
                }
                if (charClass != 0 && Character.isUpperCase(next)) {
                    expected.add(charClass);
                    zeroOrMore.add(true);
                    normPattern.append(ch);
                }
                expected.add(charClass);
                zeroOrMore.add(false);
                normPattern.append(comparisonType.normalize(next));
            } else if (ch == '*') {
                boolean multistar = false;
                while (i != patternLast && pattern.charAt(i+1) == '*') {
                    i++;
                    multistar = true;
                }
                normPattern.append(multistar ? '|' : '*');
                expected.add(multistar ? CHAR_ANY : CHAR_NOT_SEPARATOR);
                zeroOrMore.add(true);
            } else {
                normPattern.append(comparisonType.normalize(ch));
                expected.add(0);
                zeroOrMore.add(false);
            }
        }
        this.normPattern = normPattern.toString().toCharArray();

        this.expected = new int[expected.size()];
        this.zeroOrMore = new boolean[zeroOrMore.size()];

        for (int i = 0 ; i != expected.size() ; i++) {
            this.expected[i] = expected.get(i);
            this.zeroOrMore[i] = zeroOrMore.get(i);
        }
    }

    /**
     * Determines if the given string matches the pattern,
     * using the specified case-sensitivity rules.
     *
     * @param pattern the pattern against which to match the string.
     * @param str the string to match against the pattern.
     * @param comparisonType a <code>CaseSensitivity</code> constant
     * specifying if the match needs to be case-sensitive or not.
     * @return true if the string matches the pattern using the specified
     * case-sensitivity rules; false otherwise.
     */
    public static boolean isMatch(
        String pattern
    ,   String str
    ,   CaseSensitivity comparisonType) {
        return WildcardPattern.compile(pattern, comparisonType).isMatch(str);
    }

    /**
     * Determines if the given string matches the pattern case-insensitively.
     *
     * @param pattern the pattern against which to match the string.
     * @param str the string to match against the pattern.
     * @return true if the string matches the pattern case-insensitively;
     * false otherwise.
     */
    public static boolean isMatch(String pattern, String str) {
        return WildcardPattern.compile(pattern).isMatch(str);
    }

    /**
     * Given a string and a wildcard pattern with ?, *, and **,
     * returns true when the string matches the pattern, and false
     * when the string does not match the pattern.  Matching is
     * case-sensitive.
     *
     * @param pattern the pattern with ?, *, and **
     * @param str the string to be matched.
     * @return true when the string matches the pattern, and false
     * when the string does not match the pattern.
     */
    public boolean isMatch(String str) {
        if ( str == null ) {
            throw new NullPointerException("string");
        }

        int patternLength = normPattern.length;

        if (patternLength == 0) {
            return str.length() == 0;
        }

        boolean[][] state = new boolean[2][patternLength + 1];
        int pos = 1;
        state[0][0] = true;
        for (int i = 0 ; i != patternLength && zeroOrMore[i] ; i++) {
            state[0][pos++] = true;
        }
        char[] strCA = str.toCharArray();
        for (int i = 0; i != strCA.length; i++) {
            char c = strCA[i];
            int charClass = matchMode.characterClass(c);
            if ( c != '|' ) {
                // Process a regular character
                for (int j = 0; j < patternLength; j++) {
                    if (state[0][j]) {
                        boolean match;
                        if (expected[j] != 0) {
                            match = (charClass & expected[j]) != 0;
                        } else {
                            match = matchMode.compare(normPattern[j], c);
                        }
                        if (match) {
                            pos = (zeroOrMore[j]) ? j : j + 1;
                            state[1][pos++] = true;

                            if (pos <= patternLength) {
                                if (zeroOrMore[pos-1]) {
                                    state[1][pos++] = true;
                                }
                            }
                        }
                    }
                }
            } else {
                boolean seenTrue = false;
                for ( int j = patternLength ; j >= 0 ; j-- ) {
                    state[1][j] = seenTrue ? state[0][j] : true;
                    seenTrue |= state[0][j];
                }
            }
            boolean haveTrue = false;
            for (int j = 0; j != state[0].length; j++) {
                haveTrue |= (state[0][j] = state[1][j]);
                state[1][j] = false;
            }
            // If the entire state is false, we will not get a match
            if ( !haveTrue ) {
                return false;
            }
        }
        return state[0][patternLength];
    }

    /**
     * Calculates the value of the character class.
     *
     * @param ch the character the class of which is to be calculated.
     * @return the class of the character represented as a bitmask.
     */
    private static int calculateCharacterClass(char ch) {
        int res = CHAR_ANY;
        if (Character.isUpperCase(ch)) {
            res |= CHAR_UPPER;
        } else {
            res |= CHAR_NOT_UPPER;
        }
        if (Character.isLowerCase(ch)) {
            res |= CHAR_LOWER;
        } else {
            res |= CHAR_NOT_LOWER;
        }
        if (Character.isLetter(ch)) {
            res |= CHAR_ALPHA;
        } else {
            res |= CHAR_NOT_ALPHA;
        }
        if (Character.isDigit(ch)) {
            res |= CHAR_DIGIT;
        } else {
            res |= CHAR_NOT_DIGIT;
        }
        if (Character.isWhitespace(ch)) {
            res |= CHAR_BLANK;
        } else {
            res |= CHAR_NOT_BLANK;
        }
        if (ch != '/') {
            res |= CHAR_NOT_SEPARATOR;
        }
        return res;
    }
    /**
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WildcardPattern) {
            WildcardPattern other = (WildcardPattern)obj;
            return pattern.equals(other.pattern)
                && matchMode == other.matchMode;
        } else {
            return false;
        }
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return pattern.hashCode() ^ matchMode.hashCode();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return matchMode.toString(pattern);
    }

    /**
     * This enumeration provides values for case-sensitive
     * and case-insensitive matching options.
     */
    public enum CaseSensitivity {
        /**
         * This enumeration value defines case-sensitive matching.
         */
        SENSITIVE {
            /**
             * This is the lookup table of character class bit patterns.
             */
            private int[] classLookup = new int[256];

            private final Map<Character,Integer> dynamicLookup =
                Collections.synchronizedMap(new HashMap<Character,Integer>());

            {
                for (int i = 0 ; i != 256 ; i++) {
                    classLookup[i] = calculateCharacterClass((char)i);
                }
            }

            @Override
            boolean compare(char a, char b) {
                return a == b;
            }

            @Override
            char normalize(char c) {
                return c;
            }

            @Override
            int characterClass(char c) {
                if (c < 256) {
                    return classLookup[c];
                } else {
                    Integer res = dynamicLookup.get(c);
                    if (res == null) {
                        int r = calculateCharacterClass(c);
                        dynamicLookup.put(c, r);
                        return r;
                    } else {
                        return res;
                    }
                }
            }

            @Override
            String toString(String pattern) {
                return "[CS:\""+pattern+"\"]";
            }

        },

        /**
         * This enumeration value defines case-insensitive matching.
         */
        INSENSITIVE {

            /**
             * This is the lookup table of character class bit patterns.
             */
            private int[] classLookup = new int[256];

            private final Map<Character,Integer> dynamicLookup =
                Collections.synchronizedMap(new HashMap<Character,Integer>());

            {
                for (int i = 0 ; i != 256 ; i++) {
                    char l = (char)Character.toLowerCase(i);
                    char u = (char)Character.toUpperCase(i);
                    classLookup[i] = calculateCharacterClass(l)
                                       | calculateCharacterClass(u);
                }
            }

            // toLowerCase can be quite expensive.  Let's keep a table around
            // so that the common European languages will work faster.
            private char[] lowerCaseMapping = new char[256];

            {
                for (int i = 0; i < 256; i++) {
                    lowerCaseMapping[(char)i] = Character.toLowerCase((char)i);
                }
            }

            @Override
            boolean compare(char a, char b) {
                if (a < 256 && b < 256) {
                    return a == lowerCaseMapping[b];
                } else {
                    // Why do both toUpper and toLower? Blame classical
                    // Georgian. Classical Georgian had an upper case,
                    // but modern usage frowns upon it. toLower will convert
                    // both upper and lower to lower case, but toUpper
                    // will leave the characters alone.  Neato!
                    return a == Character.toLowerCase(b) ||
                           Character.toUpperCase(a)==Character.toUpperCase(b);
                }
            }

            @Override
            char normalize(char c) {
                return Character.toLowerCase(c);
            }

            @Override
            int characterClass(char c) {
                if (c < 256) {
                    return classLookup[c];
                } else {
                    Integer res = dynamicLookup.get(c);
                    if (res == null) {
                        char l = Character.toLowerCase(c);
                        char u = Character.toUpperCase(c);
                        int r = calculateCharacterClass(l)
                              | calculateCharacterClass(u);
                        dynamicLookup.put(c, r);
                        return r;
                    } else {
                        return res;
                    }
                }
            }

            @Override
            String toString(String pattern) {
                return "[\""+pattern+"\"]";
            }

        };

        /**
         * Compares two characters according to case sensitivity rules.
         *
         * @param a the first character to compare.
         * @param b the second character to compare.
         * @return the result of comparing a and b.
         */
        abstract boolean compare(char a, char b);

        /**
         * Normalizes the character for the pattern.
         *
         * @param c the character to normalize.
         * @return the normalized character.
         */
        abstract char normalize(char c);

        /**
         * Determines the character class of the given character.
         *
         * @param c the character the class of which is to be determined.
         * @return the character class of the given character.
         */
        abstract int characterClass(char c);

        /**
         * Produces a string representation of the specified pattern.
         *
         * @param pattern the pattern the string representation of which
         * needs to be produced.
         * @return a string representation of the specified pattern.
         */
        abstract String toString(String pattern);

    }

}
