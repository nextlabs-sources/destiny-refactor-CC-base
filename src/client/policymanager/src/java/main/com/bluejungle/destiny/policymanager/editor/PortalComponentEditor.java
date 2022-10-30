/*
 * Created on Feb 15, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import java.util.List;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.ComponentEnum;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author bmeng
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/policymanager/src/java/main/com/bluejungle/destiny/policymanager/editor/PortalComponentEditor.java#4 $
 */

public class PortalComponentEditor extends ComponentEditor {

    public PortalComponentEditor(Composite parent, int style, IDSpec domainObject) {
        super(parent, style, domainObject, true);
    }

    @Override
    protected List<String> getPropertyOperatorList() {
        return null;
    }

    @Override
    protected List<String> getPropertyList() {
        return null;
    }

    @Override
    protected SpecType getSpecType() {
        return SpecType.PORTAL;
    }

    @Override
    protected String getMemberLabel() {
        return EditorMessages.PORTALCOMPONENTEDITOR_PORTALS;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.PORTAL;
    }

    @Override
    public String getObjectTypeLabelText() {
        return EditorMessages.PORTALCOMPONENTEDITOR_PORTAL_COMPONENTS;
    }

    @Override
    protected ComponentEnum getComponentType() {
        return ComponentEnum.PORTAL;
    }

    @Override
    protected boolean hasCustomProperties() {
        return true;
    }

    protected String getLookupLabel() {
        return EditorMessages.PORTALCOMPONENTEDITOR_LOOKUP;
    }
}
