/*
 * Created on May 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;

import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/nextlabs/shared/tools/PrintStreamMonitor.java#1 $
 */

public class PrintStreamMonitor extends PrintStream implements Closeable {
	private final StringBuffer sb;
	private boolean opened;
	
	public PrintStreamMonitor(OutputStream out) {
		super(out);
		sb = new StringBuffer();
		opened = true;
	}
	
	@Override
	public void print(String s) {
		super.print(s);
		if (opened) {
			sb.append(s);
		}
	}

	@Override
	public void println() {
		super.println();
		if (opened) {
			sb.append(ConsoleDisplayHelper.NEWLINE);
		}
	}

	@Override
	public void println(String x) {
		super.println(x);
		if (opened) {
			sb.append(ConsoleDisplayHelper.NEWLINE);
		}
	}
	
	public String getString(){
		return sb.toString();
	}
	
	public void clear(){
		sb.setLength(0);
	}
	
	public void close(){
		clear();
		opened = false;
	}
	
	public boolean isOpen(){
		return opened;
	}
	
}
