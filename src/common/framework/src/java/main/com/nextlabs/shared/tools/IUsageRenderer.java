package com.nextlabs.shared.tools;

/**
 * The format in which usage information is printed is controlled through an implementation of this 
 * interface.  Most likely, there will only ever be one default implementation with an agreed upon 
 * format, but the interface is useful in case requirements change
 * 
 * @author hchan
 * @date Mar 27, 2007
 */
public interface IUsageRenderer {
	void renderUsage(IConsoleApplicationDescriptor descriptor);
}
