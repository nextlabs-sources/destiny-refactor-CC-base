/**
 * Created on Dec 11, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * San Mateo CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.framework.utils;

import java.util.Arrays;

import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.utils.StringUtils;

/**
 * @author sergey
 * This is the test suite for string utilities.
 */
public class StringUtilsTest extends BaseDestinyTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SetUtilsTest.class);
    }

    public void testNoWildcards() {
        match("abc123", "abc123", true);
        match("abc123", "axc123", false);
        match("", "", true);
        match("", "a", false);
        match("a", "", false);
    }

    public void testSingleCharacterWildcard() {
        match("abc1?c3", "abc123", true);
        match("abc123", "abc1?3", false);
    }

    public void testMulticharacterNoSeparator() {
        match("*", "", true);
        match("abc1*3", "abc123", true);
        match("abc123", "axc1*3", false);
        match("abc1*3", "abc12222222222222223", true);
        match("abc1*3", "abc13", true);
        match("abc1*3", "abc122222/222222223", false);
    }

    public void testMulticharacterWithSeparator() {
        match("**", "", true);
        match("abc1**3", "abc123", true);
        match("abc123", "axc1**3", false);
        match("abc1**3", "abc12222222222222223", true);
        match("abc1**3", "abc13", true);
        match("abc1**3", "abc122222/222222223", true);
    }

    public void testSpecialString() {
        match("**", "|", true);
        match("*", "abc123|", true);
        match("abc123*", "a|bc", true);
        match("abcdef", "abc|", true);
        match("ab", "abc|", false);
    }

    public void testEscapedWildcard() {
        match("?", "?", true);
        match("!", "!", true);
        match("A?", "A?", true);
        match("A!", "A!", true);
        match("??abc", "?abc", true);
        match("!!abc", "!abc", true);
        match("!?abc", "?abc", true);
        match("?!abc", "!abc", true);
        match("http://www.cnn.com/index.html??a#b!!c", "http://www.cnn.com/index.html?a#b!c", true);
    }

    public void testMixedWildcardsPositive() {
        match("?u?l?a?d?s", "Abc0 ", true);
        match("?u?l?a?d?s", "AB_0 ", false);
        match("?u?l?a?d?s", "Ab00 ", false);
        match("?u?l?a?d?s", "Ab_# ", false);
        match("?u?l?a?d?s", "#b_0 ", false);
        match("?u?l?a?d?s", "", false);
        match("?U?L?A?D?S", "AAAbbbcxx0098098 \t\t", true);
        match("?U?L?A?D?S", "AAAAB_0 X", false);
        match("?U?L?A?D?S", "Ula3 ", true);
        match("?U?L?A?D?S", "", false);
    }

    public void testMixedWildcardsNegative() {
        match("!u!l!a!d!s", "aA# 5", true);
        match("!u!l!a!d!s", "AA# 5", false);
        match("!u!l!a!d!s", "aa# 5", false);
        match("!u!l!a!d!s", "aAa 5", false);
        match("!u!l!a!d!s", "aA#55", false);
        match("!u!l!a!d!s", "aA#5 ", false);
        match("!U!L!A!D!S", "aaaAAAA#### \t\t\n\r56789", true);
        match("!U!L!A!D!S", "Aaa1 ", false);
        match("!U!L!A!D!S", "Ula0\t", false);
        match("!U!L!A!D!S", "", false);
    }

    public void testSingleCharacterDigitPositive() {
        match("?d", "0", true);
        match("?d", "1", true);
        match("?d", "2", true);
        match("?d", "3", true);
        match("?d", "4", true);
        match("?d", "5", true);
        match("?d", "6", true);
        match("?d", "7", true);
        match("?d", "8", true);
        match("?d", "9", true);
        match("?d", "99", false);
        match("?d", "A", false);
        match("?d?d?d", "012", true);
        match("?d?d?d", "01a", false);
    }

    public void testSingleCharacterWhitespacePositive() {
        match("?s", " ", true);
        match("?s", "\t", true);
        match("?s", "\r", true);
        match("?s", "\n", true);
        match("?s", "9", false);
        match("?s", "  ", false);
        match("?s", " a", false);
        match("?s?s?s?s", " \t\n\r", true);
        match("?s?s?s?s", " \t\nr", false);
    }

    public void testSingleCharacterUpperPositive() {
        match("?u", "A", true);
        match("?u", "Z", true);
        match("?u", "a", false);
        match("?u", "z", false);
        match("?u", "AA", false);
        match("?u", " ", false);
        match(
            "?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u"
        ,   "QUICKBROWNFOXJUMPSOVERTHELAZYDOG"
        ,   true);
        match(
            "?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u"
        ,   "QUICKBROWNFOXJUMPSOVERTHELAZYdOG"
        ,   false);
    }

    public void testSingleCharacterLowerPositive() {
        match("?l", "a", true);
        match("?l", "z", true);
        match("?l", "A", false);
        match("?l", "Z", false);
        match("?l", "aa", false);
        match("?l", " ", false);
        match(
            "?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l"
        ,   "quickbrownfoxjumpsoverthelazydog"
        ,   true);
        match(
            "?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l"
        ,   "quickbrownfoxjumpsoverthelazyDog"
        ,   false);
    }

    public void testSingleCharacterLetterPositive() {
        match("?a", "a", true);
        match("?a", "z", true);
        match("?a", "A", true);
        match("?a", "Z", true);
        match("?a", "0", false);
        match("?a", " ", false);
        match("?a", "aa", false);
        match("?a", " a", false);
        match(
            "?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
        ,   "quickbrownfoxjumpsoverthelazydog"
        ,   true);
        match(
            "?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
        ,   "QuickBrownFoxJumpsOverTheLazyDog"
        ,   true);
        match(
            "?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
        ,   "QuickBrownFoxJumps@verTheLazyDog"
        ,   false);
    }

    public void testMultiCharacterDigitPositive() {
        match("?D", "0123456789", true);
        match("?D", "4", true);
        match("?D", "", false);
        match("?Dxyz", "0123456789xyz", true);
        match("?Dxyz", "1xyz", true);
        match("?Dxyz", "xyz", false);
    }

    public void testMultiCharacterWhitespacePositive() {
        match("?S", "    \t\t\t\t\r\r\r\r\n\n\n\n   \t", true);
        match("?S", " ", true);
        match("?S", "", false);
        match("?Sxyz", "     xyz", true);
        match("?Sxyz", "\nxyz", true);
        match("?Sxyz", "xyz", false);
    }

    public void testMultiCharacterLetterPositive() {
        match("?A", "QuIcKbRoWnFoXjUmPsOvErThElAzYdOg", true);
        match("?A", "G", true);
        match("?A", "", false);
        match("123?A789", "123qUICKbROWNfFOXjUMPoVERtHElAZYdOG789", true);
        match("123?A789", "123a789", true);
        match("123?A789", "123789", false);
        match("123?A", "123qUICKbROWNfFOXjUMPoVERtHElAZYdOG", true);
        match("123?A", "123z", true);
        match("123?A", "123", false);
        match("?A789", "qUICKbROWNfFOXjUMPoVERtHElAZYdOG789", true);
        match("?A789", "q789", true);
        match("?A789", "789", false);
        match("?A789", "QuIcKbRoWnFoXjUmPsOvErThElAzYdOg78", false);
        match("?A789", "89", false);
    }

    public void testMultiCharacterUpperPositive() {
        match("?U", "QUICKBROWNFOXJUMPSOVERTHELAZYDOG", true);
        match("?U", "Z", true);
        match("?U", "", false);
        match("abc?Uxyz", "abcQUICKBROWNFOXJUMPSOVERTHELAZYDOGxyz", true);
        match("abc?Uxyz", "abcQxyz", true);
        match("abc?Uxyz", "abcxyz", false);
        match("abc?U", "abcQUICKBROWNFOXJUMPSOVERTHELAZYDOG", true);
        match("abc?U", "abcD", true);
        match("abc?U", "abc", false);
        match("?Uxyz", "QUICKBROWNFOXJUMPSOVERTHELAZYDOGxyz", true);
        match("?Uxyz", "Wxyz", true);
        match("?Uxyz", "xyz", false);
        match("?Uxyz", "QUICKBROWNFOXJUMPSOVERTHELAZYDOGxy", false);
        match("Uxyz", "xy", false);
    }

    public void testMultiCharacterLowerPositive() {
        match("?L", "quickbrownfoxjumpsoverthelazydog", true);
        match("?L", "s", true);
        match("?L", "", false);
        match("ABC?LXYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", true);
        match("ABC?LXYZ", "ABCdXYZ", true);
        match("ABC?LXYZ", "ABCXYZ", false);
        match("ABC?L", "ABCquickbrownfoxjumpsoverthelazydog", true);
        match("ABC?L", "ABCd", true);
        match("ABC?L", "ABC", false);
        match("?LXYZ", "quickbrownfoxjumpsoverthelazydogXYZ", true);
        match("?LXYZ", "wXYZ", true);
        match("?LXYZ", "XYZ", false);
        match("?LXYZ", "ABCXYZ", false);
        match("LXYZ", "xy", false);
    }

    public void testSingleCharacterDigitNegative() {
        match("!d", "0", false);
        match("!d", "1", false);
        match("!d", "2", false);
        match("!d", "3", false);
        match("!d", "4", false);
        match("!d", "5", false);
        match("!d", "6", false);
        match("!d", "7", false);
        match("!d", "8", false);
        match("!d", "9", false);
        match("!d", "99", false);
        match("!d", "A", true);
        match("!d", "#", true);
        match("!d", "\t", true);
        match("!d!d!d", "A# ", true);
        match("!d!d!d", "A5 ", false);
    }

    public void testSingleCharacterWhitespaceNegative() {
        match("!s", "A", true);
        match("!s", "a", true);
        match("!s", "#", true);
        match("!s", "/", true);
        match("!s", "s", true);
        match("!s", "!", true);
        match("!s", " ", false);
        match("!s", "\t", false);
        match("!s", "\r", false);
        match("!s", "\n", false);
        match("!s!s!s!s", "a@S_", true);
        match("!s!s!s!s", "a@S ", false);
    }

    public void testSingleCharacterUpperNegative() {
        match("!u", "a", true);
        match("!u", "z", true);
        match("!u", "A", false);
        match("!u", "Z", false);
        match("!u", "AA", false);
        match(
            "!u!u!u!u!u!u!u!u!u!u"
        ,   "_0@#/asd\t&"
        ,   true);
        match(
            "!u!u!u!u!u!u!u!u!u!u"
        ,   "_0@#/asD\t&"
        ,   false);
    }

    public void testSingleCharacterLowerNegative() {
        match("!l", "A", true);
        match("!l", "Z", true);
        match("!l", "a", false);
        match("!l", "z", false);
        match("!l", "aa", false);
        match(
            "!l!l!l!l!l!l!l!l!l!l"
        ,   "_0@#/ASD\t&"
        ,   true);
        match(
            "!l!l!l!l!l!l!l!l!l!l"
        ,   "_0@#/ASd\t&"
        ,   false);
    }

    public void testSingleCharacterLetterNegative() {
        match("!a", "@", true);
        match("!a", "0", true);
        match("!a", "*", true);
        match("!a", "'", true);
        match("!a", "_", true);
        match("!a", "A", false);
        match("!a", "a", false);
        match("!a", "", false);
    }

    public void testMultiCharacterDigitNegative() {
        match("!D", "ASDFG##$%(*&(*&($#*@#$", true);
        match("!D", "_ _ _ _ _ _ _ _ _ _ _", true);
        match("!D123", "xyz123", true);
        match("!D123", "z123", true);
        match("!D123", "123", false);
        match("!D", "", false);
    }

    public void testMultiCharacterWhitespaceNegative() {
        match("!S", "ASDFG##$%(*&(*&($#*@#$", true);
        match("!S   ", "xyz   ", true);
        match("!S   ", "-   ", true);
        match("!S", "-", true);
        match("!S", "", false);
    }

    public void testMultiCharacterLetterNegative() {
        match("!A", " @#$%^&*()!0123456789", true);
        match("!A   ", "###   ", true);
        match("!A   ", "1   ", true);
        match("!A   ", "   ", false);
        match("!A", " ", true);
        match("!A", "", false);
    }

    public void testMultiCharacterUpperNegative() {
        match("!U", "quickbrownfoxjumpsoverthelazydog", true);
        match("!U", "%", true);
        match("!U", "", false);
    }

    public void testMultiCharacterLowerNegative() {
        match("!L", "QUICKBROWNFOXJUMPSOVERTHELAZYDOG", true);
        match("!L", "$", true);
        match("!L", "", false);
    }

    public void testSingleAnyCharacterPositive() {
        match("?c", " ", true);
        match("?c", "a", true);
        match("?c", "0", true);
        match("?c", "?", true);
        match("?c", "_", true);
        match("?c", "/", false);
    }

    public void testSingleAnyCharacterNegative() {
        match("!c", " ", true);
        match("!c", "a", true);
        match("!c", "0", true);
        match("!c", "?", true);
        match("!c", "_", true);
        match("!c", "/", true);
    }

    public void testMultipleAnyCharacterPositive() {
        match("?C", "", false);
        match("?C", "/", false);
        match("?C", "A", true);
        match("?C", "    ", true);
        match("?C", "aaaa", true);
        match("?C", "0000", true);
        match("?C", "????", true);
        match("?C", "____", true);
        match("?C", "a/a/", false);
    }

    public void testMultipleAnyCharacterNegative() {
        match("!C", "", false);
        match("!C", "/", true);
        match("!C", "    ", true);
        match("!C", "aaaa", true);
        match("!C", "0000", true);
        match("!C", "????", true);
        match("!C", "____", true);
        match("!C", "a/a/", true);
    }

    public void testMixedCaseMatching() {
        match("C", "c", true);
        match("The Quick Brown Fox", "the quick brown fox", true);
        match("aBC?LxYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", true);
        match("bBC?LxYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", false);

        matchCase("aBC?LxYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", false);
    }

    public void testJoin() {
        assertEquals(StringUtils.join(new String[] { "foo" }, " "), "foo");
        assertEquals(StringUtils.join(new String[] { "foo", "bar" }, " "), "foo bar");
        assertEquals(StringUtils.join(new String[] { "foo", "bar" }, null), "foobar");
        assertEquals(StringUtils.join(new String[] { "foo", "bar", "baz" }, " + "), "foo + bar + baz");
        assertEquals(StringUtils.join(Arrays.asList(new String[] { "foo", "bar", "baz" }), " "), "foo bar baz");
    }

    private void match(String pattern, String value, boolean expected) {
        assertEquals(expected, StringUtils.isMatch(pattern, value));
    }

    private void matchCase(String pattern, String value, boolean expected) {
        assertEquals(expected, StringUtils.isMatch(pattern, value, StringUtils.CASE_SENSITIVE));
    }

}
