/*
 * Created on May 17, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.bluejungle.destiny.policymanager.SharePointImageConstants;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * @author dstarke
 * 
 */
public class ObjectLabelImageProvider {

    public static Image getImage(Object obj) {
        if (obj instanceof IDSpec || obj instanceof IDSpecRef) {
            SpecType type = null;
            if (obj instanceof IDSpec) {
                IDSpec spec = (IDSpec) obj;
                type = spec.getSpecType();
            }
            if (obj instanceof IDSpecRef) {
                type = PredicateHelpers.getPredicateType((IDSpecRef) obj);
            }
            if (type == SpecType.APPLICATION) {
                return ImageBundle.APPLICATION_COMPONENT_IMG;
            } else if (type == SpecType.HOST) {
                return ImageBundle.DESKTOP_COMPONENT_IMG;
            } else if (type == SpecType.RESOURCE) {
                return ImageBundle.FILE_COMPONENT_IMG;
            } else if (type == SpecType.USER) {
                return ImageBundle.USER_COMPONENT_IMG;
            } else if (type == SpecType.ACTION) {
                return ImageBundle.ACTION_COMPONENT_IMG;
            } else if (type == SpecType.PORTAL) {
                return SharePointImageConstants.SHAREPOINT;
            }
        }
        if (obj instanceof IDPolicy) {
            return ImageBundle.POLICY_IMG;
        }
        if (obj instanceof DomainObjectDescriptor) {
            DomainObjectDescriptor desc = (DomainObjectDescriptor) obj;
            EntityType type = desc.getType();
            String name = desc.getName();
            int pos = name.indexOf(PQLParser.SEPARATOR);
            if (type == EntityType.COMPONENT && pos != -1) {
                type = EntityType.forName(name.substring(0, pos).toUpperCase());
            }
            if (type == EntityType.APPLICATION) {
                return ImageBundle.APPLICATION_COMPONENT_IMG;
            } else if (type == EntityType.HOST) {
                return ImageBundle.DESKTOP_COMPONENT_IMG;
            } else if (type == EntityType.LOCATION) {
                return getTemporaryFillerImage();
            } else if (type == EntityType.POLICY) {
                return ImageBundle.POLICY_IMG;
            } else if (type == EntityType.FOLDER) {
                return ImageBundle.FOLDER_IMG;
            } else if (type == EntityType.RESOURCE) {
                return ImageBundle.FILE_COMPONENT_IMG;
            } else if (type == EntityType.USER) {
                return ImageBundle.USER_COMPONENT_IMG;
            } else if (type == EntityType.ACTION) {
                return ImageBundle.ACTION_COMPONENT_IMG;
            } else if (type == EntityType.PORTAL) {
                return SharePointImageConstants.SHAREPOINT;
            }
        }
        if (obj instanceof Relation) {
            Relation rel = (Relation) obj;
            IExpression exp = rel.getLHS();
            if (exp instanceof ResourceAttribute) {
                ResourceAttribute attribute = (ResourceAttribute) exp;
                String subtype = attribute.getObjectSubTypeName();
                if (ResourceAttribute.PORTAL_SUBTYPE.equals(subtype)) {
                    return SharePointImageConstants.SHAREPOINT;
                } else {
                    return ImageBundle.FILE_IMG;
                }
            } else if (exp instanceof SubjectAttribute) {
                if (exp == SubjectAttribute.USER_UID || exp == SubjectAttribute.USER_NAME || exp == SubjectAttribute.USER_ID) {
                    return ImageBundle.USER_IMG;
                } else if (exp == SubjectAttribute.CONTACT_ID) {
                    return ImageBundle.CONTACT_IMG;
                } else if (exp == SubjectAttribute.HOST_UID || exp == SubjectAttribute.HOST_NAME || exp == SubjectAttribute.HOST_ID) {
                    return ImageBundle.DESKTOP_IMG;
                } else if (exp == SubjectAttribute.APP_UID || exp == SubjectAttribute.APP_NAME || exp == SubjectAttribute.APP_ID) {
                    return ImageBundle.APPLICATION_IMG;
                } else if (exp == SubjectAttribute.USER_LDAP_GROUP || exp == SubjectAttribute.USER_LDAP_GROUP_ID) {
                    return ImageBundle.IMPORTED_USER_GROUP_IMG;
                } else if (exp == SubjectAttribute.HOST_LDAP_GROUP || exp == SubjectAttribute.HOST_LDAP_GROUP_ID) {
                    return ImageBundle.IMPORTED_HOST_GROUP_IMG;
                }
            }
        }
        if (obj instanceof LeafObject) {
            LeafObjectType type = ((LeafObject) obj).getType();
            if (type == LeafObjectType.APPLICATION) {
                return ImageBundle.APPLICATION_IMG;
            } else if (type == LeafObjectType.HOST || type == LeafObjectType.DESKTOP_AGENT) {
                return ImageBundle.DESKTOP_IMG;
            } else if (type == LeafObjectType.HOST_GROUP || type == LeafObjectType.FILE_SERVER_AGENT) {
                return ImageBundle.IMPORTED_HOST_GROUP_IMG;
            } else if (type == LeafObjectType.PORTAL_AGENT) {
                return SharePointImageConstants.SHAREPOINT;
            } else if (type == LeafObjectType.RESOURCE) {
                return ImageBundle.FILE_IMG;
            } else if (type == LeafObjectType.USER) {
                return ImageBundle.USER_IMG;
            } else if (type == LeafObjectType.CONTACT) {
                return ImageBundle.CONTACT_IMG;
            } else if (type == LeafObjectType.USER_GROUP) {
                return ImageBundle.IMPORTED_USER_GROUP_IMG;
            } else if (type == LeafObjectType.ACTION) {
                return ImageBundle.ACTION_COMPONENT_IMG;
            } else if (type == LeafObjectType.APPUSER) {
                return ImageBundle.APP_USER_IMG;
            } else if (type == LeafObjectType.ACCESSGROUP) {
                return ImageBundle.APP_USER_GROUP_IMG;
            }
        }
        return getTemporaryFillerImage();
    }

    private static Image getTemporaryFillerImage() {
        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    }

}
