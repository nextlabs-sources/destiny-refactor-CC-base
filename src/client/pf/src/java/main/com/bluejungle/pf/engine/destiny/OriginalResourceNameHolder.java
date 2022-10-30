package com.bluejungle.pf.engine.destiny;

public class OriginalResourceNameHolder{
	private static ThreadLocal originalName = new ThreadLocal();
	public static String get(){
		return ((String)originalName.get());
	}
	public static void set(String name){
		originalName.set(name);
	}
}