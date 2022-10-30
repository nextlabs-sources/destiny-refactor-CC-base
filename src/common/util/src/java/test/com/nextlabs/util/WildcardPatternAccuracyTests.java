package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/WildcardPatternAccuracyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for the WildcardPattern class.
 *
 * @author Alan Morgan
 * @author Sergey Kalinichenko
 */
@RunWith(value=Parameterized.class)
@SuiteClasses(value={WildcardPatternAccuracyTests.class})
public class WildcardPatternAccuracyTests {
    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList( new Object[][] {
        // 0
            {"abc123", "abc123", false, true }
        ,   {"abc123", "axc123", false, false}
        ,   {"", "", false, true}
        ,   {"", "a", false, false}
        ,   {"a", "", false, false}
        ,   {"abc1?c3", "abc123", false, true}
        ,   {"abc123", "abc1?3", false, false}
        ,   {"*", "", false, true}
        ,   {"abc1*3", "abc123", false, true}
        ,   {"abc123", "axc1*3", false, false}
        // 10
        ,   {"abc1*3", "abc12222222222222223", false, true}
        ,   {"abc1*3", "abc13", false, true}
        ,   {"abc1*3", "abc122222/222222223", false, false}
        ,   {"**", "", false, true}
        ,   {"abc1**3", "abc123", false, true}
        ,   {"abc123", "axc1**3", false, false}
        ,   {"abc1**3", "abc12222222222222223", false, true}
        ,   {"abc1**3", "abc13", false, true}
        ,   {"abc1**3", "abc122222/222222223", false, true}
        ,   {"**", "|", false, true}
        // 20
        ,   {"*", "abc123|", false, true}
        ,   {"abc123*", "a|bc", false, true}
        ,   {"abcdef", "abc|", false, true}
        ,   {"ab", "abc|", false, false}
        ,   {"?", "?", false, true}
        ,   {"!", "!", false, true}
        ,   {"A?", "A?", false, true}
        ,   {"A!", "A!", false, true}
        ,   {"??abc", "?abc", false, true}
        ,   {"!!abc", "!abc", false, true}
        // 30
        ,   {"!?abc", "?abc", false, true}
        ,   {"?!abc", "!abc", false, true}
        ,   {"http://www.cnn.com/index.html??a#b!!c"
            , "http://www.cnn.com/index.html?a#b!c", false, true}
        ,   {"?u?l?a?d?s", "Abc0 ", false, true}
        ,   {"?u?l?a?d?s", "AB_0 ", false, false}
        ,   {"?u?l?a?d?s", "Ab00 ", false, false}
        ,   {"?u?l?a?d?s", "Ab_# ", false, false}
        ,   {"?u?l?a?d?s", "#b_0 ", false, false}
        ,   {"?u?l?a?d?s", "", false, false}
        ,   {"?U?L?A?D?S", "AAAbbbcxx0098098 \t\t", false, true}
        // 40
        ,   {"?U?L?A?D?S", "AAAAB_0 X", false, false}
        ,   {"?U?L?A?D?S", "Ula3 ", false, true}
        ,   {"?U?L?A?D?S", "", false, false}
        ,   {"!u!l!a!d!s", "aA# 5", false, true}
        ,   {"!u!l!a!d!s", "AA# 5", true, false}
        ,   {"!u!l!a!d!s", "aa# 5", true, false}
        ,   {"!u!l!a!d!s", "aAa 5", false, false}
        ,   {"!u!l!a!d!s", "aA#55", false, false}
        ,   {"!u!l!a!d!s", "aA#5 ", false, false}
        ,   {"!U!L!A!D!S", "aaaAAAA#### \t\t\n\r56789", false, true}
        // 50
        ,   {"!U!L!A!D!S", "Aaa1 ", false, false}
        ,   {"!U!L!A!D!S", "Ula0\t", false, false}
        ,   {"!U!L!A!D!S", "", false, false}
        ,   {"?d", "0", false, true}
        ,   {"?d", "1", false, true}
        ,   {"?d", "2", false, true}
        ,   {"?d", "3", false, true}
        ,   {"?d", "4", false, true}
        ,   {"?d", "5", false, true}
        ,   {"?d", "6", false, true}
        // 60
        ,   {"?d", "7", false, true}
        ,   {"?d", "8", false, true}
        ,   {"?d", "9", false, true}
        ,   {"?d", "99", false, false}
        ,   {"?d", "A", false, false}
        ,   {"?d?d?d", "012", false, true}
        ,   {"?d?d?d", "01a", false, false}
        ,   {"?s", " ", false, true}
        ,   {"?s", "\t", false, true}
        ,   {"?s", "\r", false, true}
        // 70
        ,   {"?s", "\n", false, true}
        ,   {"?s", "9", false, false}
        ,   {"?s", "  ", false, false}
        ,   {"?s", " a", false, false}
        ,   {"?s?s?s?s", " \t\n\r", false, true}
        ,   {"?s?s?s?s", " \t\nr", false, false}
        ,   {"?u", "A", false, true}
        ,   {"?u", "Z", false, true}
        ,   {"?u", "a", true, false}
        ,   {"?u", "z", true, false}
        // 80
        ,   {"?u", "AA", false, false}
        ,   {"?u", " ", false, false}
        ,   {"?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u"
            ,"QUICKBROWNFOXJUMPSOVERTHELAZYDOG", true, true}
        ,   {"?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u?u"
            ,"QUICKBROWNFOXJUMPSOVERTHELAZYdOG", true, false}
        ,   {"?l", "a", true, true}
        ,   {"?l", "z", true, true}
        ,   {"?l", "A", true, false}
        ,   {"?l", "Z", true, false}
        ,   {"?l", "aa", false, false}
        ,   {"?l", " ", false, false}
        // 90
        ,   {"?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l"
            ,"quickbrownfoxjumpsoverthelazydog", true, true}
        ,   {"?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l?l"
            ,"quickbrownfoxjumpsoverthelazyDog", true, false}
        ,   {"?a", "a", false, true}
        ,   {"?a", "z", false, true}
        ,   {"?a", "A", false, true}
        ,   {"?a", "Z", false, true}
        ,   {"?a", "0", false, false}
        ,   {"?a", " ", false, false}
        ,   {"?a", "aa", false, false}
        ,   {"?a", " a", false, false}
        // 100
        ,   {"?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
            ,"quickbrownfoxjumpsoverthelazydog", false, true}
        ,   {"?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
            ,"QuickBrownFoxJumpsOverTheLazyDog", false, true}
        ,   {"?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
            ,"QuickBrownFoxJumps@verTheLazyDog", false, false}
        ,   {"?D", "0123456789", false, true}
        ,   {"?D", "4", false, true}
        ,   {"?D", "", false, false}
        ,   {"?Dxyz", "0123456789xyz", false, true}
        ,   {"?Dxyz", "1xyz", false, true}
        ,   {"?Dxyz", "xyz", false, false}
        ,   {"?S", "    \t\t\t\t\r\r\r\r\n\n\n\n   \t", false, true}
        // 110
        ,   {"?S", " ", false, true}
        ,   {"?S", "", false, false}
        ,   {"?Sxyz", "     xyz", false, true}
        ,   {"?Sxyz", "\nxyz", false, true}
        ,   {"?Sxyz", "xyz", false, false}
        ,   {"?A", "QuIcKbRoWnFoXjUmPsOvErThElAzYdOg", false, true}
        ,   {"?A", "G", false, true}
        ,   {"?A", "", false, false}
        ,   {"123?A789", "123qUICKbROWNfFOXjUMPoVERtHElAZYdOG789", false, true}
        ,   {"123?A789", "123a789", false, true}
        // 120
        ,   {"123?A789", "123789", false, false}
        ,   {"123?A", "123qUICKbROWNfFOXjUMPoVERtHElAZYdOG", false, true}
        ,   {"123?A", "123z", false, true}
        ,   {"123?A", "123", false, false}
        ,   {"?A789", "qUICKbROWNfFOXjUMPoVERtHElAZYdOG789", false, true}
        ,   {"?A789", "q789", false, true}
        ,   {"?A789", "789", false, false}
        ,   {"?A789", "QuIcKbRoWnFoXjUmPsOvErThElAzYdOg78", false, false}
        ,   {"?A789", "89", false, false}
        ,   {"?U", "QUICKBROWNFOXJUMPSOVERTHELAZYDOG", false, true}
        // 130
        ,   {"?U", "Z", false, true}
        ,   {"?U", "", false, false}
        ,   {"abc?Uxyz", "abcQUICKBROWNFOXJUMPSOVERTHELAZYDOGxyz", false, true}
        ,   {"abc?Uxyz", "abcQxyz", false, true}
        ,   {"abc?Uxyz", "abcxyz", false, false}
        ,   {"abc?U", "abcQUICKBROWNFOXJUMPSOVERTHELAZYDOG", false, true}
        ,   {"abc?U", "abcD", true, true}
        ,   {"abc?U", "abc", true, false}
        ,   {"?Uxyz", "QUICKBROWNFOXJUMPSOVERTHELAZYDOGxyz", true, true}
        ,   {"?Uxyz", "Wxyz", false, true}
        // 140
        ,   {"?Uxyz", "xyz", false, false}
        ,   {"?Uxyz", "QUICKBROWNFOXJUMPSOVERTHELAZYDOGxy", false, false}
        ,   {"Uxyz", "xy", false, false}
        ,   {"?L", "quickbrownfoxjumpsoverthelazydog", false, true}
        ,   {"?L", "s", false, true}
        ,   {"?L", "", false, false}
        ,   {"ABC?LXYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", false, true}
        ,   {"ABC?LXYZ", "ABCdXYZ", false, true}
        ,   {"ABC?LXYZ", "ABCXYZ", false, false}
        ,   {"ABC?L", "ABCquickbrownfoxjumpsoverthelazydog", false, true}
        // 150
        ,   {"ABC?L", "ABCd", false, true}
        ,   {"ABC?L", "ABC", false, false}
        ,   {"?LXYZ", "quickbrownfoxjumpsoverthelazydogXYZ", false, true}
        ,   {"?LXYZ", "wXYZ", false, true}
        ,   {"?LXYZ", "XYZ", false, false}
        ,   {"?LXYZ", "ABCXYZ", true, false}
        ,   {"LXYZ", "xy", false, false}
        ,   {"!d", "0", false, false}
        ,   {"!d", "1", false, false}
        ,   {"!d", "2", false, false}
        // 160
        ,   {"!d", "3", false, false}
        ,   {"!d", "4", false, false}
        ,   {"!d", "5", false, false}
        ,   {"!d", "6", false, false}
        ,   {"!d", "7", false, false}
        ,   {"!d", "8", false, false}
        ,   {"!d", "9", false, false}
        ,   {"!d", "99", false, false}
        ,   {"!d", "A", false, true}
        ,   {"!d", "#", false, true}
        // 170
        ,   {"!d", "\t", false, true}
        ,   {"!d!d!d", "A# ", false, true}
        ,   {"!d!d!d", "A5 ", false, false}
        ,   {"!s", "A", false, true}
        ,   {"!s", "a", false, true}
        ,   {"!s", "#", false, true}
        ,   {"!s", "/", false, true}
        ,   {"!s", "s", false, true}
        ,   {"!s", "!", false, true}
        ,   {"!s", " ", false, false}
        // 180
        ,   {"!s", "\t", false, false}
        ,   {"!s", "\r", false, false}
        ,   {"!s", "\n", false, false}
        ,   {"!s!s!s!s", "a@S_", false, true}
        ,   {"!s!s!s!s", "a@S ", false, false}
        ,   {"!u", "a", true, true}
        ,   {"!u", "z", true, true}
        ,   {"!u", "A", true, false}
        ,   {"!u", "Z", true, false}
        ,   {"!u", "AA", true, false}
        // 190
        ,   {"!u!u!u!u!u!u!u!u!u!u", "_0@#/asd\t&", true, true}
        ,   {"!u!u!u!u!u!u!u!u!u!u", "_0@#/asD\t&", true, false}
        ,   {"!l", "A", true, true}
        ,   {"!l", "Z", true, true}
        ,   {"!l", "a", true, false}
        ,   {"!l", "z", true, false}
        ,   {"!l", "aa", true, false}
        ,   {"!l!l!l!l!l!l!l!l!l!l", "_0@#/ASD\t&", true, true}
        ,   {"!l!l!l!l!l!l!l!l!l!l", "_0@#/ASd\t&", true, false}
        ,   {"!a", "@", false, true}
        // 200
        ,   {"!a", "0", false, true}
        ,   {"!a", "*", false, true}
        ,   {"!a", "'", false, true}
        ,   {"!a", "_", false, true}
        ,   {"!a", "A", false, false}
        ,   {"!a", "a", false, false}
        ,   {"!a", "", false, false}
        ,   {"!D", "ASDFG##$%(*&(*&($#*@#$", false, true}
        ,   {"!D", "_ _ _ _ _ _ _ _ _ _ _", false, true}
        ,   {"!D123", "xyz123", false, true}
        // 210
        ,   {"!D123", "z123", false, true}
        ,   {"!D123", "123", false, false}
        ,   {"!D", "", false, false}
        ,   {"!S", "ASDFG##$%(*&(*&($#*@#$", false, true}
        ,   {"!S   ", "xyz   ", false, true}
        ,   {"!S   ", "-   ", false, true}
        ,   {"!S", "-", false, true}
        ,   {"!S", "", false, false}
        ,   {"!A", " @#$%^&*()!0123456789", false, true}
        ,   {"!A   ", "###   ", false, true}
        // 220
        ,   {"!A   ", "1   ", false, true}
        ,   {"!A   ", "   ", false, false}
        ,   {"!A", " ", false, true}
        ,   {"!A", "", false, false}
        ,   {"!U", "quickbrownfoxjumpsoverthelazydog", false, true}
        ,   {"!U", "%", false, true}
        ,   {"!U", "", false, false}
        ,   {"!L", "QUICKBROWNFOXJUMPSOVERTHELAZYDOG", false, true}
        ,   {"!L", "$", false, true}
        ,   {"!L", "", false, false}
        // 230
        ,   {"?c", " ", false, true}
        ,   {"?c", "a", false, true}
        ,   {"?c", "0", false, true}
        ,   {"?c", "?", false, true}
        ,   {"?c", "_", false, true}
        ,   {"?c", "/", false, false}
        ,   {"!c", " ", false, true}
        ,   {"!c", "a", false, true}
        ,   {"!c", "0", false, true}
        ,   {"!c", "?", false, true}
        // 240
        ,   {"!c", "_", false, true}
        ,   {"!c", "/", false, true}
        ,   {"?C", "", false, false}
        ,   {"?C", "/", false, false}
        ,   {"?C", "A", false, true}
        ,   {"?C", "    ", false, true}
        ,   {"?C", "aaaa", false, true}
        ,   {"?C", "0000", false, true}
        ,   {"?C", "????", false, true}
        ,   {"?C", "____", false, true}
        // 250
        ,   {"?C", "a/a/", false, false}
        ,   {"!C", "", false, false}
        ,   {"!C", "/", false, true}
        ,   {"!C", "    ", false, true}
        ,   {"!C", "aaaa", false, true}
        ,   {"!C", "0000", false, true}
        ,   {"!C", "????", false, true}
        ,   {"!C", "____", false, true}
        ,   {"!C", "a/a/", false, true}
        ,   {"C", "c", false, true}
        // 260
        ,   {"The Quick Brown Fox", "the quick brown fox", false, true}
        ,   {"aBC?LxYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", false, true}
        ,   {"bBC?LxYZ"
            , "ABCquickbrownfoxjumpsoverthelazydogXYZ", false, false}
        ,   {"aBC?LxYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", true, false}
        ,   {"!C", "", false, false}
        ,   {"!C", "/", false, true}
        ,   {"!C", "    ", false, true}
        ,   {"!C", "aaaa", false, true}
        ,   {"!C", "0000", false, true}
        ,   {"!C", "????", false, true}
        // 270
        ,   {"!C", "____", false, true}
        ,   {"!C", "a/a/", false, true}
        ,   {"ABC?LXYZ", "ABCquickbrownfoxjumpsoverthelazydogXYZ", false, true}
        ,   {"ABC?LXYZ", "ABCdXYZ", false, true}
        ,   {"ABC?LXYZ", "ABCXYZ", false, false}
        ,   {"?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
            ,"quickbrownfoxjumpsoverthelazydog", false, true}
        ,   {"?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
            ,"QuickBrownFoxJumpsOverTheLazyDog", false, true}
        ,   {"?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a?a"
            ,"QuickBrownFoxJumps@verTheLazyDog", false, false}
        ,   {"CaSeSeNsItIvE", "casesensitive", true, false}
        ,   {"CaSeSeNsItIvE", "casesensitive", false, true}
        // 280
        ,   {"\u0100", "\u0100", false, true}
        ,   {"\u0100", "\u0100", true, true}
        ,   {"!\u0100", "\u0100", false, true}
        ,   {"?\u0100", "\u0100", false, true}
        ,   {"?\u0100", "\u0100\u0101", false, false}
        ,   {"?\u0100", "\u1000", false, false}
        // Chinese characters
        ,   {"?a", "\u9faf", false, false}
        ,   {"?c", "\u4e2d", false, true}
        ,   {"?a", "\u9fa6", false, false}
        ,   {"?c", "\u4e05", false, true}
        // 290
        // Bengali digits zero through nine
        ,   {"?d", "\u09e6", false, true}
        ,   {"?d", "\u09e7", false, true}
        ,   {"?d", "\u09e8", false, true}
        ,   {"?d", "\u09e9", false, true}
        ,   {"?d", "\u09ea", false, true}
        ,   {"?d", "\u09eb", false, true}
        ,   {"?d", "\u09ec", false, true}
        ,   {"?d", "\u09ed", false, true}
        ,   {"?d", "\u09ee", false, true}
        ,   {"?d", "\u09ef", false, true}
        ,   {"\u09e6", "\u09e6", false, true}
        ,   {"\u09e6", "\u09e6", true, true}
        });
    }

    private final String pattern;
    private final String text;
    private final boolean caseSensitive;
    private final boolean expected;
    private final WildcardPattern.CaseSensitivity compareMethod;

    public WildcardPatternAccuracyTests(
        String pattern
    ,   String text
    ,   boolean caseSensitive
    ,   boolean expected) {
        this.pattern = pattern;
        this.text = text;
        this.caseSensitive = caseSensitive;
        this.expected = expected;
        if (caseSensitive) {
            compareMethod = WildcardPattern.CASE_SENSITIVE;
        } else {
            compareMethod = WildcardPattern.CASE_INSENSITIVE;
        }
    }

    @Test
    public void precompiled() {
        WildcardPattern p;
        if (caseSensitive) {
            p = WildcardPattern.compile(pattern, compareMethod);
        } else {
            p = WildcardPattern.compile(pattern);
        }
        assertEquals(expected, p.isMatch(text));
    }

    @Test
    public void direct() {
        if (caseSensitive) {
            assertEquals(
                expected
            ,   WildcardPattern.isMatch(pattern, text, compareMethod)
            );
        } else {
            assertEquals(expected, WildcardPattern.isMatch(pattern, text));
        }
    }

}
