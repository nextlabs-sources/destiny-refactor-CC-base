package com.nextlabs.shared.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.bluejungle.framework.utils.CollectionUtils;

import static org.junit.Assert.*;

/**
 * TODO finish it
 *
 * @author hchan
 * @date Jul 10, 2007
 */
public class DynamicConsoleTester {
	private final List<List<String>> allPossibleComboList = new ArrayList<List<String>>();
	
	public static final String SPLITTER = "\n";
	
	private List<String[]> successComboList = new ArrayList<String[]>();
	
	public DynamicConsoleTester( List<String> switches){
		init(switches);
	}
	
	public DynamicConsoleTester( List<String> switches, Class clazz){
		this(switches);
		try {
			Object object = clazz.newInstance();
			if(object instanceof ConsoleApplicationBase){
				run(clazz);
			}else{
				System.err.println("wrong class " + clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		List<String> inputs = new ArrayList<String>();
		inputs.add("-a");
		inputs.add("-b");
		inputs.add("-c");
		inputs.add("-d\ne");
		DynamicConsoleTester dct = new DynamicConsoleTester(inputs);
		for(List<String> ss: dct.allPossibleComboList){
			for(String s: ss){
				System.out.print(s+",");
			}
			System.out.print("\n");
		}
	}

	private void init(List<String> switches) {
		if(switches.size() >= 12){
			System.out.println("You list is pretty larege. It may be slow and not enough resource");
		}
		for(String s : switches){
			List<List<String>> newAddList = new ArrayList<List<String>>();
			for (List<String> possibleComboList : allPossibleComboList) {
				List<String> cloneList = Arrays.asList(possibleComboList.toArray(new String[] {}));
			
				newAddList.add(cloneList);
			}
			allPossibleComboList.addAll(newAddList);
			allPossibleComboList.add(Collections.singletonList(s));
		}
	}
	
	private void run(ConsoleApplicationBase console, String[] args){
		try {
			console.parseAndExecute(args);
			successComboList.add(args);
		} catch (ParseException e) {
			//ignore
		}
	}
	
	private void run(Class clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		if(allPossibleComboList.size() == 0){
			System.err.println( "There is no possible combo");
		}
		
		for(List<String> possibleComboList : allPossibleComboList){
			ConsoleApplicationBase console = (ConsoleApplicationBase)clazz.newInstance();
			run(console, CollectionUtils.toStringArray(possibleComboList));
		}
	}
	
	public final List<List<String>> getAllPossibleComboList() {
		return allPossibleComboList;
	}

	public final List<String[]> getSuccessComboList() {
		return successComboList;
	}

	/** v2 version start below**/
	
	public static List<String> generateListV2(int numberOfElements){
		List<String> returnList = new ArrayList<String>(factorialsInt(numberOfElements));
		
		return returnList;
	}
	
	//keep f less than 20
	private static long factorialsLong(int f){
		if (f > 20) {
			throw new RuntimeException("input can't be longer than 20");
		}
		long l =1;
		for(int i=1; i<=f; i++){
			l *= i;
		}
		return l;
	}
	
//	keep f less than 12
	private static int factorialsInt(int f){
		if (f > 12) {
			throw new RuntimeException("input can't be longer than 12");
		}
		int l =1;
		for(int i=1; i<=f; i++){
			l *= i;
		}
		return l;
	}
	
	@Test
	public void testFactorials1(){
		assertEquals(1L, factorialsLong(1));
		assertEquals(2L, factorialsLong(2));
		assertEquals(6L, factorialsLong(3));
		assertEquals(24L, factorialsLong(4));
		assertEquals(3628800L, factorialsLong(10));
//		assertEquals(2432902008176640000L, factorials(20));
	}
}


