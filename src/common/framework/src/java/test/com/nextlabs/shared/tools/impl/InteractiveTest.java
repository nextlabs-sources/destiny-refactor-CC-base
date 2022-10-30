/**
 * 
 */
package com.nextlabs.shared.tools.impl;


import com.nextlabs.shared.tools.impl.InteractiveQuestion;
import com.nextlabs.shared.tools.impl.InteractiveQuestion.InteractiveMCItems;

/**
 * TODO description
 *
 * @author hchan
 * @date Apr 5, 2007
 */
public class InteractiveTest {
	public static void main(String[] args) throws Exception {
//		System.out.println("start");
//		InteractiveQuestion q = new InteractiveQuestion("what is your name? ");
//		String name = q.prompt();
//		System.out.println("name = " + name);
//		
//		System.out.println("done");


		
		InteractiveMCItems<String> selections = new InteractiveMCItems<String>();
		
		selections.add("1", "apple", "apple");
		selections.add("2", "orange", "orange");
		selections.add("3", "three", "three");
		selections.add("4", "four", "four");
		selections.add("5", "five", "five");
		selections.add("6", "six", "six");
		selections.add("7", "seven", "seven");
		selections.add("8", "eight", "eight");
		selections.add("9", "nine", "nine");
		selections.add("Q", "ten", "ten");
		selections.add("A", "banana", "banana");
		selections.add("S", "strawberry", "strawberry");
		selections.add("S", "pineapple", "pineapple");
		selections.add("H", "humna", "humna");
		String output = InteractiveQuestion.multiChoice(selections);
		System.out.println("output = " + output);
		
//		List<String> strs = new ArrayList<String>();
//		for(InteractiveMCItem mcItem : selections){
//			strs.add(mcItem.displayText);
//		}
//		InteractiveQuestion.multiChoice(strs.toArray(new String[]{}));
	}
	
	
}
