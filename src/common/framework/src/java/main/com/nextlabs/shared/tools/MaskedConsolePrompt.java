/*
 * Created on Nov 7, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

/**
 * source from http://java.sun.com/developer/technicalArticles/Security/pwordmask/
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/MaskedConsolePrompt.java#1 $
 */

public class MaskedConsolePrompt implements ISecureConsolePrompt {
	private String prompt;

	public MaskedConsolePrompt(String prompt) {
		this.prompt = prompt;
	}

	/**
	 * @see com.nextlabs.shared.tools.ISecureConsolePrompt#readConsoleSecure()
	 */
	public char[] readConsoleSecure() throws IOException {
		return MaskedConsolePrompt.getPassword(System.in, prompt);
	}
	
	/**
	 *@param input stream to be used (e.g. System.in)
	 *@param prompt The prompt to display to the user.
	 *@return The password as entered by the user.
	 */
	@SuppressWarnings("fallthrough")
	public static final char[] getPassword(InputStream in, String prompt) throws IOException {
		MaskingThread maskingthread = new MaskingThread(prompt);
		Thread thread = new Thread(maskingthread);
		thread.start();

		char[] lineBuffer;
		char[] buf;

		buf = lineBuffer = new char[128];

		int room = buf.length;
		int offset = 0;
		int c;

		loop: while (true) {
			switch (c = in.read()) {
			case -1:
			case '\n':
				break loop;

			case '\r':
				int c2 = in.read();
				if ((c2 != '\n') && (c2 != -1)) {
					if (!(in instanceof PushbackInputStream)) {
						in = new PushbackInputStream(in);
					}
					((PushbackInputStream) in).unread(c2);
				} else {
					break loop;
				}
			default:
				if (--room < 0) {
					buf = new char[offset + 128];
					room = buf.length - offset - 1;
					System.arraycopy(lineBuffer, 0, buf, 0, offset);
					Arrays.fill(lineBuffer, ' ');
					lineBuffer = buf;
				}
				buf[offset++] = (char) c;
				break;
			}
		}
		maskingthread.stopMasking();
		if (offset == 0) {
			return null;
		}
		char[] ret = new char[offset];
		System.arraycopy(buf, 0, ret, 0, offset);
		Arrays.fill(buf, ' ');
		return ret;
	}
}

/**
 * This class attempts to erase characters echoed to the console.
 */
class MaskingThread extends Thread {
	/**
	 * Comment for <code>MASK_CHAR</code>
	 */
	private volatile boolean stop;
	private static final char MASK_CHAR = '*';

	/**
	 *@param prompt The prompt displayed to the user
	 */
	public MaskingThread(String prompt) {
		super("ConsoleTextMask");
		System.out.print(prompt);
	}

	/**
	 * Begin masking until asked to stop.
	 */
	@SuppressWarnings("static-access")
	public void run() {

		int priority = Thread.currentThread().getPriority();
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		try {
			stop = true;
			while (stop) {
				System.out.print("\010" + MASK_CHAR);
				try {
					// attempt masking at this rate
					Thread.currentThread().sleep(1);
				} catch (InterruptedException iex) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		} finally { // restore the original priority
			Thread.currentThread().setPriority(priority);
		}
	}

	/**
	 * Instruct the thread to stop masking.
	 */
	public void stopMasking() {
		this.stop = false;
	}
}
