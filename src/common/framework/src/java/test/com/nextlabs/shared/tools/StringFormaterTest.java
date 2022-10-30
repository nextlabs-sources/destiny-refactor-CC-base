/**
 * 
 */
package com.nextlabs.shared.tools;

import org.junit.Test;

import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

import static org.junit.Assert.*;

/**
 * @author hchan
 * @date Mar 27, 2007
 */
public class StringFormaterTest{
	private void testWrap(final String n) {
		String output = StringFormatter.wrap("ab cdefg hij", 5, n);
		assertEquals("ab" + n + "cdefg" + n + "hij" + n, output);
		
		output = StringFormatter.wrap("abcdefghij", 5, n);
		assertEquals("abcde" + n + "fghij" + n, output);
		
		output = StringFormatter.wrap("abcdefghi", 5, n);
		assertEquals("abcde" + n + "fghi" + n, output);
		
		output = StringFormatter.wrap(" abcdefghi", 5, n);
		assertEquals(" abcd" + n + "efghi" + n, output);
		
		output = StringFormatter.wrap("  abcdefghi", 5, n);
		assertEquals("  abc" + n + "defgh" + n + "i" + n, output);
		
		output = StringFormatter.wrap("  ab  c defghi", 5, n);
		assertEquals("  ab " + n + "c" + n + "defgh" + n + "i" + n, output);
		
		output = StringFormatter.wrap("ab" + n + " cdefghi", 5, n);
		assertEquals("ab" + n + " cdef" + n + "ghi" + n, output);
		
		output = StringFormatter.wrap("ab" + n + "  cdefghi", 5, n);
		assertEquals("ab" + n + "  cde" + n + "fghi" + n, output);
		
		output = StringFormatter.wrap("ab" + n + " c defghi", 5, n);
		assertEquals("ab" + n + " c" + n + "defgh" + n + "i" + n, output);
		
		output = StringFormatter.wrap("abcdefghij", 5, n);
		assertEquals("abcde" + n + "fghij" + n, output);
		
		output = StringFormatter.wrap(n + "abcdefghij", 5, n);
		assertEquals(n + "abcde" + n + "fghij" + n, output);
	}
	
	@Test 
	public void wrapWithSingleNewlineChar(){
		testWrap("\n");
	}
	
	@Test 
	public void wrapWithSystemNewline(){
		testWrap(ConsoleDisplayHelper.NEWLINE);
	}

	@Test 
	public void warpWithHtml(){
		testWrap("<br>");
	}
	
}
