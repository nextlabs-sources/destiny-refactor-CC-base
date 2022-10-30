package com.nextlabs.shared.tools;

import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;


/**
 * @author hchan
 * @date Mar 27, 2007
 */
public class StringFormatter {
	private static final int INDEX_NOT_FOUND = -1;

	public static String wrap(String input, int width, String lineBreaker) {
		return StringFormatter.wrap(input, width, lineBreaker, "");
	}
	
	public static String wrapParagraph(String input, int width, String lineBreaker) {
		StringBuilder output = new StringBuilder(input.length());
		
		String[] inputLines = input.split(ConsoleDisplayHelper.NEWLINE);
		for(String inputLine : inputLines){
			output.append(wrap(inputLine,width,lineBreaker, ""));
		}
		return output.toString();
	}

	/**
	 * 
	 * @param input			unformatted string
	 * @param width			the width the text will be, it doesn't include the indent
	 * @param lineBreaker	the break line string
	 * @param indent		the string that will put before every new line
	 * @return				formated string
	 */
	public static String wrap(String input, int width, String lineBreaker, String indent) {
		StringBuffer output = new StringBuffer(input.length());
		int totalLength = input.length();
		int i = 0; //findNextPostionToStart(input, 0);
		
		//continue while the whole input string is processed.
		while (i < totalLength) {
			//append indent at the beginning of each line
			output.append(indent);
			
			int endIndex = Math.min(i + width, totalLength);

			//if start with line break, don't remove the white space in front
			if (input.startsWith(lineBreaker, i)) {
				output.append(lineBreaker);
				i += lineBreaker.length();
				endIndex = Math.min(i + width, totalLength);
			}

			int positionToBreak = findNextPostionToBreak(input, i, endIndex, lineBreaker);
			output.append(input.substring(i, positionToBreak));
			i = findNextPostionToStart(input, positionToBreak, lineBreaker);

			if (!output.toString().endsWith(lineBreaker)) {
				output.append(lineBreaker);
			}

		}
		return output.toString();
	}
	
	private static int findNextPostionToBreak(String input, int beginIndex, int endIndex,
			String lineBreaker) {
		int stopIndex = Math.min(input.length(), endIndex + lineBreaker.length() - 1);
		String temp = input.substring(beginIndex, stopIndex);

		int lineBreakIndex = temp.indexOf(lineBreaker);
		if (lineBreakIndex < 0) {
			temp = input.substring(beginIndex, endIndex);
			//no line break found
			if (endIndex == input.length()) {
				//no more new line
				return input.length();
			} else {
				char firstCharInNextLine = input.charAt(endIndex);
				if (Character.isWhitespace(firstCharInNextLine)) {
					//space char is at the beginning of the next line
					return endIndex;
				} else {
					int lastSpace = lastWhiteSpaceIndex(temp);
					if(lastSpace == INDEX_NOT_FOUND 
							|| lastIndexOfContinuousWhiteSpace(temp) == lastSpace){
						//no space find in the current line 
						//OR last white space is same as the first white space
						return endIndex;
					} else {
						return beginIndex + lastSpace;
					}
				}
			}
		} else if (lineBreakIndex == 0) {
			return beginIndex + 1;
		} else {
			return beginIndex + lineBreakIndex;
		}
	}
	
	private static int lastWhiteSpaceIndex(String str) {
		for (int i = str.length() - 1; i >= 0; i--) {
			if (Character.isWhitespace(str.charAt(i))) {
				return i;
			}
		}
		return INDEX_NOT_FOUND;
	}
	
	private static int lastIndexOfContinuousWhiteSpace(String str) {
		int whiteSpaceLength = 0;

		for (int i = 0; i < str.length(); i++) {
			if (Character.isWhitespace(str.charAt(i))) {
				whiteSpaceLength++;
			} else {
				//is the non white space in the first index?
				//if true, not found
				//else return the index
				return i == 0
						? INDEX_NOT_FOUND
						: whiteSpaceLength - 1;
			}
		}
		return INDEX_NOT_FOUND;
	}
	
	private static int findNextPostionToStart(String input, int beginIndex, String lineBreaker) {
		if (beginIndex < input.length() && input.startsWith(lineBreaker, beginIndex)) {
			return beginIndex + lineBreaker.length();
		}

		int length = input.length();
		int nextStartIndex;
		for (nextStartIndex = beginIndex; nextStartIndex < length; nextStartIndex++) {
			char currentChar = input.charAt(nextStartIndex);
			if (!Character.isWhitespace(currentChar)) {
				break;
			}
		}
		return nextStartIndex;
	}

	//return a string that the length is exactly same as <code>length</code>
	public static String fitLength(String message, int length) {
		if (message.length() > length) {
			return message.substring(0, length);
		} else {
			int pad = length - message.length();
			return message + StringFormatter.repeat(' ', pad);
		}
	}

	public static String repeat(char c, int times) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < times; i++) {
			sb.append(c);
		}
		return sb.toString();
	}
}
