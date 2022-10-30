/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.PolicyEnum;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author dstarke
 * 
 */
public class EditorPanelFactory {

    public static boolean hasEditorPanelFor(Object domainObject) {
        if (domainObject instanceof IDPolicy) {
            return true;
        } else if (domainObject instanceof IDSpec) {
            IDSpec spec = (IDSpec) domainObject;
            SpecType type = spec.getSpecType();

            if (type == SpecType.APPLICATION) {
                return true;
            } else if (type == SpecType.HOST) {
                return true;
            } else if (type == SpecType.RESOURCE) {
                return true;
            } else if (type == SpecType.USER) {
                return true;
            } else if (type == SpecType.ACTION) {
                return true;
            } else if (type == SpecType.PORTAL) {
                return true;
            }
        }
        return false;
    }

    public static EditorPanel getEditorPanel(Composite parent, int style, Object domainObject) {
        if (parent.isDisposed()) {
            return null;
        }
        if (domainObject instanceof IDPolicy) {
            IPolicy policy = (IDPolicy) domainObject;
            if (policy.hasAttribute(PolicyEnum.COMMUNICATION_POLICY.name())) {
                return new CommunicationPolicyEditor(parent, style, (IDPolicy) domainObject);
            } else {
                return new PolicyEditor(parent, style, (IDPolicy) domainObject);
            }
        } else if (domainObject instanceof IDSpec) {
            IDSpec spec = (IDSpec) domainObject;
            SpecType type = spec.getSpecType();
            if (type == SpecType.APPLICATION) {
                return new ApplicationComponentEditor(parent, style, spec);
            } else if (type == SpecType.HOST) {
                return new DesktopComponentEditor(parent, style, spec);
            } else if (type == SpecType.RESOURCE) {
                return new ResourceComponentEditor(parent, style, spec);
            } else if (type == SpecType.USER) {
                return new UserComponentEditor(parent, style, spec);
            } else if (type == SpecType.ACTION) {
                return new ActionComponentEditor(parent, style, spec);
            } else if (type == SpecType.PORTAL) {
                return new PortalComponentEditor(parent, style, spec);
            }
        }
        return null;
    }
}
