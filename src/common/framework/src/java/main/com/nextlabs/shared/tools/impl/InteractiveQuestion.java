package com.nextlabs.shared.tools.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Get the user input from system in. The question and answer must be String
 *
 * @author hchan
 * @date Apr 5, 2007
 */
public class InteractiveQuestion {
	private final String question;
	public InteractiveQuestion(String question) {
		this.question = question;
	}
	
	public String prompt() throws IOException{
		System.out.print("> " + question);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		return in.readLine();
	}
	
	public String prompt2() throws IOException{
		System.out.println(question);
		InputStream in = System.in;
		char[] buffer = null;
		buffer = new char[128];
		int offset = 0;

		boolean continueLoop = true;
		while (continueLoop) {
			int c = in.read();
			switch (c) {
			case -1:
			case '\n':
				continueLoop = false;
				break;
			case '\r':
				int c2 = in.read();
				if ((c2 != '\n') && (c2 != -1))
					in.read();
				// guarantees that mac & dos line end
				// sequences are completely read thru
				// but not beyond
				continueLoop = false;
				break;
			default:
				buffer = checkBuffer(buffer, offset);
				buffer[offset++] = (char) c;
				break;
			}
		}

		char[] result = new char[offset];
		System.arraycopy(buffer, 0, result, 0, offset);
		return new String(result);
	}
	
	
	public static final char[] checkBuffer(char[] buffer, int offset) throws IllegalArgumentException {
        if (buffer == null) {
            throw new NullPointerException("buffer cannot be null.");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset = " + offset + " is < 0");
        }

        char[] bufferToReturn = buffer;
        if (offset >= buffer.length) {
            bufferToReturn = new char[offset + 128];
            System.arraycopy(buffer, 0, bufferToReturn, 0, buffer.length);
        }

        return bufferToReturn;
    }
	
	public static String prompt(String question) throws IOException{
		InteractiveQuestion q = new InteractiveQuestion(question);
		return q.prompt();
	}
	
	public static <T> T multiChoice(InteractiveMCItems<T> selections) throws IOException{
		StringBuilder sb = new StringBuilder();
		for(InteractiveMCItem<T> mcItem :selections.items){
			sb.append(mcItem);
			sb.append("\n");
		}
		
		System.out.print(sb);
		InputStream in = System.in;
		System.out.print(">");		
		
		char[] buffer = new char[selections.longestIdLength];
		int offset = 0;

		InteractiveMCItem<T> item = null;
		
		boolean continueLoop = true;
		while (continueLoop) {
			int c = in.read();
			switch (c) {
			case -1:
			case '\n':
				continueLoop = false;
				break;
			case '\r':
				int c2 = in.read();
				if ((c2 != '\n') && (c2 != -1))
					in.read();
				// guarantees that mac & dos line end
				// sequences are completely read thru
				// but not beyond
				continueLoop = false;
				break;
			default:
				if(offset > buffer.length){
					continueLoop = false;
				}else{
					buffer[offset++] = (char) c;
					final String testStr = new String(buffer);
                    if ((item = selections.getById(testStr)) != null) {
                        //item found
                        //don't return yet, there may still have some extra value in the input
                    } else {
                        System.out.println("unknown answer");
                    }
				}
				break;
			}
		}
		
		return item != null ? item.object : null; 
	}
	
	
//	public static String multiChoice(String[] selections) throws IOException{
//		if(selections.length > MULTI_CHOICE_MAX){
//			throw new IllegalArgumentException("the selections max size is " + MULTI_CHOICE_MAX);
//		}
//		StringBuilder sb = new StringBuilder();
//		for(int i=0; i< selections.length; i++){
//			sb.append((char)(i + (i < 9 ? '1' : '8')));
//			sb.append(". ");
//			sb.append(selections[i]);
//			sb.append("\n");
//		}
//		
//		System.out.println(sb);
//		InputStream in = System.in;
//		
//		
//		int c=0;;
//		boolean continueLoop = true;
//		while (continueLoop) {
//			c = in.read();
//			switch (c) {
//			case -1:
//			case '\n':
//				break;
//			case '\r':
//				if ((c != '\n') && (c != -1))
//					in.read();
//				// guarantees that mac & dos line end
//				// sequences are completely read thru
//				// but not beyond
//				break;
//			default:
//				break;
//			}
//			
//			in.skip(in.available());
//		}
//		
//		return ""+c;
//	}
//	
	private static class InteractiveMCItem<T>{ 
		final String id;
		final String displayText;
		final T object;
		
		public InteractiveMCItem(String id, String displayText, T object) {
			super();
			this.id = id;
			this.displayText = displayText;
			this.object = object;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final InteractiveMCItem other = (InteractiveMCItem) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(id.length() == 1 && id.charAt(0) < 0x0021 || id.charAt(0) > 0x007E){
				sb.append("Control + TODO");
			}else{
				sb.append(new String(id));
			}
			sb.append(". ");
			sb.append(displayText);
			return sb.toString();
		}
	}
	
	public static class InteractiveMCItems<T>{
		private List<InteractiveMCItem<T>> items = new ArrayList<InteractiveMCItem<T>>();
		
		private int longestIdLength;
		
		public void add(String id, String displayText, T object){
			if(items.contains(id)){
				throw new IllegalArgumentException("Duplicated id " + id + ", "
						+ items.get(items.indexOf(id)));
			}
			items.add(new InteractiveMCItem<T>(id, displayText, object));
			longestIdLength = Math.max(id.length(), longestIdLength);
		}
		
		private InteractiveMCItem<T> getById(String id){
		    for (InteractiveMCItem<T> item : items){
		        if(item.id.equals(id)){
		               return item;
		        }
		    }
		    return null;
		}
	}
}
