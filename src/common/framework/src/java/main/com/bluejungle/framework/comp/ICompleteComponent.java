package com.bluejungle.framework.comp;

// Copyright Blue Jungle, Inc.

/**
 * ICompleteComponent is a convenience interface that is a superset of
 * all the possible interfaces a component can implement.
 *
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/comp/ICompleteComponent.java#1 $
 */
public interface ICompleteComponent
		extends ILogEnabled,
				IManagerEnabled,
				IInitializable,
				IDisposable,
				IStartable,
				IConfigurable
{

}
