/*
 * Created on May 20, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.editor;

import org.eclipse.swt.widgets.Composite;

import com.bluejungle.destiny.policymanager.PolicyEnum;
import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.destiny.policymanager.ui.ImageBundle;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

/**
 * @author dstarke
 * 
 */
public class ComponentDetailsFactory {

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

    public static Composite getEditorPanel(Composite parent, int style, Object domainObject) {
        if (domainObject instanceof IDPolicy) {
            IPolicy policy = (IDPolicy) domainObject;
            if (policy.hasAttribute(PolicyEnum.COMMUNICATION_POLICY.name())) {
                return new CommunicationPolicyDetailsComposite(parent, style, (IDPolicy) domainObject);
            } else {
                return new PolicyDetailsComposite(parent, style, (IDPolicy) domainObject);
            }
        } else if (domainObject instanceof IDSpec) {
            IDSpec spec = (IDSpec) domainObject;
            SpecType type = spec.getSpecType();
            if (type == SpecType.APPLICATION) {
                //return new ApplicationDetailsComposite(parent, style, spec);
                return new ComponentDetailsComposite(parent, style, spec, ImageBundle.APPLICATION_COMPONENT_IMG, ", Application Component", EntityType.APPLICATION);
            } else if (type == SpecType.HOST) {
                return new ComponentDetailsComposite(parent, style, spec, ImageBundle.DESKTOP_COMPONENT_IMG, ", Computer Component", EntityType.HOST);
            } else if (type == SpecType.RESOURCE) {
                return new ComponentDetailsComposite(parent, style, spec, ImageBundle.FILE_COMPONENT_IMG, ", Document Component", EntityType.RESOURCE);
            } else if (type == SpecType.USER) {
                return new ComponentDetailsComposite(parent, style, spec, ImageBundle.USER_COMPONENT_IMG, ", User Component", EntityType.USER);
            } else if (type == SpecType.ACTION) {
                return new ActionDetailsComposite(parent, style, spec);
            } else if (type == SpecType.PORTAL) {
                return new ComponentDetailsComposite(parent, style, spec, SharePointImageConstants.SHAREPOINT, ", Portal Component", EntityType.PORTAL);
            }
        }
        return null;
    }
}
