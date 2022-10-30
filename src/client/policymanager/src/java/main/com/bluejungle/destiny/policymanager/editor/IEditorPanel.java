/*
 * Created on Mar 14, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/IEditorPanel.java#5 $:
 */

public interface IEditorPanel {

    public void relayout();

    public CompositePredicate getControlDomainObject(int controlId, IHasId domainObject);

    public void saveContents();

    public void dispose();

    public IHasId getDomainObject();

    public boolean isEditable();
}
