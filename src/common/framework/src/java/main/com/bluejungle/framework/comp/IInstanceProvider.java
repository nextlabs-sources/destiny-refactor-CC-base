package com.bluejungle.framework.comp;

import org.apache.commons.logging.Log;

// Copyright Blue Jungle, Inc.

/**
 * IInstanceProvider is an interface that represents a provider of
 * component instances.  This interface should be implemented by any
 * such provider.
 *
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/comp/IInstanceProvider.java#1 $
 */
public interface IInstanceProvider
{
	/**
	 * @requires: info, log is not null
	 * @effects: returns a component by the given name, initialized as necessary
	 *
	 * @param info    definition of the component to retrieve
	 * @param instance if the component has been instantiated during the info discovery process
	 * then this is the instance used
	 * @param log    logger that the component should be supplied with, if it requires one
	 * @return instance of a component
	 */
	<T> T getComponent(ComponentInfo<T> info, Object instance, Log log);

	/**
	 * requires: comp is not null
	 * effects: releases the component from management
	 *
	 * @param comp the component to release
	 */
	void release(Object comp);
	
	/**
	 * effects: stops running components, disposes of disposable components
	 * 
	 */	
	void shutdown();
}
