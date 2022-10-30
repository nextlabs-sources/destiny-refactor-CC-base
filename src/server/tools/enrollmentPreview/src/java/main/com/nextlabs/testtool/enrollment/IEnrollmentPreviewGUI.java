/*
 * Created on Mar 3, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import com.bluejungle.framework.comp.IInitializable;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/IEnrollmentPreviewGUI.java#1 $
 */

public interface IEnrollmentPreviewGUI extends IInitializable {
	enum TabbedPane{
		PARSED,
		UNKNOWN,
		WARNING,
	}
	
	void init();
	
	void addProperty(String key, String value);

	MyTreeNode addNode(String[] paths, TabbedPane pane);
	
	MyTreeNode[] convert(String[] strs);
	
	void setVisible(boolean visible);
	
	void setStatusMessage(String str);
}