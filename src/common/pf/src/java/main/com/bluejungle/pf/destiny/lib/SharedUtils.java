package com.bluejungle.pf.destiny.lib;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluejungle.destiny.server.shared.configuration.IActionConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IResourceAttributeConfigurationDO;
import com.bluejungle.destiny.services.policy.types.AttributeDescriptorList;
import com.bluejungle.destiny.services.policy.types.PolicyActionsDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.PolicyActionsDescriptorList;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;

/**
 * Contains a set of methods that can be used by the policy editor service or any
 * other client such as helpdesk.
 * 
 * @author ssen
 *
 */
public class SharedUtils {

    public static  PolicyActionsDescriptorList getPolicyActionsDescriptorList(
            IDestinyConfigurationStore confStore) {
        List<PolicyActionsDescriptorDTO> policyActionsDescriptorDTOList = 
            new ArrayList<PolicyActionsDescriptorDTO>();

        // Fixed actions
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.CHANGE_PROPERTIES.getName(), "Change Attributes", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.CHANGE_SECURITY.getName(), "Change File Permissions", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.EDIT.getName(), "Create / Edit", "Transform"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.COPY.getName(), "Copy / Embed File", "Transform"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.COPY_PASTE.getName(), "Copy Content", "Transform"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.DELETE.getName(), "Delete", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.MOVE.getName(), "Move", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.OPEN.getName(), "Open", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.PRINT.getName(), "Print", "Transform"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.RENAME.getName(), "Rename", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.EMAIL.getName(), "Email", "Communication"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.IM.getName(), "Instant Message", "Communication"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.EXPORT.getName(), "Export", "Transform"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.ATTACH.getName(), "Attach to Item", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.RUN.getName(), "Run", "Access"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.AVD.getName(), "Voice Call / Video Call", "Communication"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.MEETING.getName(), "Invite", "Web Meeting"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.SHARE.getName(), "Share in Meeting", "Web Meeting"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.RECORD.getName(), "Record", "Web Meeting"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.QUESTION.getName(), "Question", "Web Meeting"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.VOICE.getName(), "Voice Call", "Web Meeting"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.VIDEO.getName(), "Video Call", "Web Meeting"));
        policyActionsDescriptorDTOList.add(new PolicyActionsDescriptorDTO(
                DAction.JOIN.getName(), "Join Meeting", "Web Meeting"));

        // User defined actions in the configuration file
        if (confStore.retrieveActionListConfig() != null) {
            for (IActionConfigDO configAction : confStore.retrieveActionListConfig().getActions()) {
                policyActionsDescriptorDTOList.add(
                        new PolicyActionsDescriptorDTO(configAction.getName(),
                                configAction.getDisplayName(),
                                configAction.getCategory()));
            }
        }

        return  new PolicyActionsDescriptorList(policyActionsDescriptorDTOList.toArray(
                new PolicyActionsDescriptorDTO[policyActionsDescriptorDTOList.size()]));
    }

    public static Collection<PolicyActionsDescriptor> getPolicyActionDescriptors(
            IDestinyConfigurationStore confStore)  {
        return DTOUtils.makePolicyActionsDescriptorCollection(
                getPolicyActionsDescriptorList(confStore));
    }

    public static AttributeDescriptorList getSubjectAttributes(
            EntityType entityType, 
            IDPSComponentConfigurationDO dpsConfig,
            IPolicyEditorService service) throws Exception {
        if (entityType == null) {
            throw new NullPointerException("domainObjectType");
        }
        Map<String,List<AttributeDescriptor>> compatible = new HashMap<String,List<AttributeDescriptor>>();
        for (IResourceAttributeConfigurationDO customDef : dpsConfig.getCustomResourceAttributes()) {
            List<String> attrs = customDef.getAttributes();
            if (attrs != null && attrs.size() != 0) {
                String pqlName = customDef.getPqlName();
                if (pqlName != null && pqlName.length() != 0) {
                    pqlName = pqlName.toLowerCase();
                } else {
                    continue;
                }
                for (String attrDescr : attrs) {
                    String[] tokens = attrDescr.split("[.]");
                    if (tokens.length == 2 && tokens[0].equalsIgnoreCase("user")) {
                        String userAttrName = tokens[1].toLowerCase();
                        List<AttributeDescriptor> toAdd = compatible.get(userAttrName);
                        if (toAdd == null) {
                            compatible.put(userAttrName, toAdd = new ArrayList<AttributeDescriptor>());
                        }
                        toAdd.add(new AttributeDescriptor(
                                customDef.getGroupName()
                                ,   customDef.getDisplayName()
                                ,   AttributeType.forName(customDef.getTypeName())
                                ,   false
                                ,   ResourceAttribute.forNameAndType(pqlName, ResourceAttribute.FILE_SYSTEM_SUBTYPE)
                        ));
                    }
                }
            }
        }

        List<AttributeDescriptor> res = new ArrayList<AttributeDescriptor>();
        for (AttributeDescriptor ad : service.getAttributeDescriptors(entityType)) {
            List<AttributeDescriptor> compatibleCustom = compatible.get(ad.getPqlName());
            if (compatibleCustom != null && !compatibleCustom.isEmpty()) {
                res.add(new AttributeDescriptor(
                        ad.getGroupName()
                        ,   ad.getDisplayName()
                        ,   ad.getType()
                        ,   ad.isRequired()
                        ,   ad.getAttribute()
                        ,   new RelationOp[] {RelationOp.EQUALS, RelationOp.NOT_EQUALS}
                        ,   compatibleCustom.toArray(new AttributeDescriptor[compatibleCustom.size()])
                        ,   null
                ));
            } else {
                res.add(ad);
            }
        }
        return DTOUtils.makeAttributeDescriptorList(res);
    }
    
    public static AttributeDescriptorList getResourceAttributes(
            String subtypeName, IDPSComponentConfigurationDO dpsConfig)
    throws Exception {
        boolean fileSystem = 
            subtypeName.equalsIgnoreCase(ResourceAttribute.FILE_SYSTEM_SUBTYPE);
        boolean portal = 
            subtypeName.equalsIgnoreCase(ResourceAttribute.PORTAL_SUBTYPE);
        if (!(fileSystem || portal)) {
            throw new IllegalArgumentException("Illegal resource subtype: "+subtypeName);
        }
        List<AttributeDescriptor> toConvert = new ArrayList<AttributeDescriptor>();

        if (fileSystem) {
            toConvert.add(new AttributeDescriptor("Access Date", AttributeType.DATE, ResourceAttribute.ACCESS_DATE));
            toConvert.add(new AttributeDescriptor("Created Date", AttributeType.DATE, ResourceAttribute.CREATED_DATE));
            toConvert.add(new AttributeDescriptor("Directory", AttributeType.STRING, ResourceAttribute.DIRECTORY));
            toConvert.add(new AttributeDescriptor("Full Name", AttributeType.STRING, ResourceAttribute.NAME));
            toConvert.add(new AttributeDescriptor("Include Only Directories", AttributeType.BOOLEAN, ResourceAttribute.IS_DIRECTORY));
            toConvert.add(new AttributeDescriptor("Modified Date", AttributeType.DATE, ResourceAttribute.MODIFIED_DATE));
            toConvert.add(new AttributeDescriptor("Owner", AttributeType.STRING, ResourceAttribute.OWNER));
            toConvert.add(new AttributeDescriptor("Owner User Component", AttributeType.STRING, ResourceAttribute.OWNER_GROUP));
            toConvert.add(new AttributeDescriptor("Size", AttributeType.LONG, ResourceAttribute.SIZE));
            toConvert.add(new AttributeDescriptor("Type", AttributeType.STRING, ResourceAttribute.TYPE));
        }

        if (portal) {
            toConvert.add(new AttributeDescriptor("Name", AttributeType.STRING, ResourceAttribute.PORTAL_NAME));
            toConvert.add(new AttributeDescriptor("Title", AttributeType.STRING, ResourceAttribute.PORTAL_TITLE));
            toConvert.add(new AttributeDescriptor("Created", AttributeType.DATE, ResourceAttribute.PORTAL_CREATED));
            toConvert.add(new AttributeDescriptor("Modified", AttributeType.DATE, ResourceAttribute.PORTAL_MODIFIED));
            toConvert.add(new AttributeDescriptor("Created By", AttributeType.STRING, ResourceAttribute.PORTAL_CREATED_BY));
            toConvert.add(new AttributeDescriptor("Modified By", AttributeType.STRING, ResourceAttribute.PORTAL_MODIFIED_BY));
            toConvert.add(new AttributeDescriptor("Type", AttributeType.STRING, ResourceAttribute.PORTAL_TYPE));
            toConvert.add(new AttributeDescriptor("Sub-type", AttributeType.STRING, ResourceAttribute.PORTAL_SUB_TYPE));
            toConvert.add(new AttributeDescriptor("Filesize", AttributeType.LONG, ResourceAttribute.PORTAL_FILESIZE));
            toConvert.add(new AttributeDescriptor("Description", AttributeType.STRING, ResourceAttribute.PORTAL_DESC));
        }

        addCustomResourceAttributes(subtypeName, toConvert, dpsConfig);

        sortAttributeDescriptors(toConvert);

        return DTOUtils.makeAttributeDescriptorList(toConvert);
    }
    
    public static AttributeDescriptorList getCustomResourceAttributes(
            String subtypeName, IDPSComponentConfigurationDO dpsConfig) 
    throws Exception {
        List<AttributeDescriptor> customAttributes = addCustomResourceAttributes(
                subtypeName, null, dpsConfig);
        sortAttributeDescriptors(customAttributes);

        return DTOUtils.makeAttributeDescriptorList(customAttributes);
    }
    
    public static List<AttributeDescriptor> addCustomResourceAttributes(
            String subtypeName, List<AttributeDescriptor> toConvert,
            IDPSComponentConfigurationDO dpsConfig)   {
        boolean fileSystem = subtypeName.equalsIgnoreCase(ResourceAttribute.FILE_SYSTEM_SUBTYPE);
        boolean portal = subtypeName.equalsIgnoreCase(ResourceAttribute.PORTAL_SUBTYPE);

        if (toConvert == null) {
            toConvert = new ArrayList<AttributeDescriptor>();
        }
        
        IResourceAttributeConfigurationDO[] cfgAttributes = dpsConfig.getCustomResourceAttributes();

        for (int i = 0 ; i != cfgAttributes.length ; i++) {
            String pqlName = cfgAttributes[i].getPqlName();
            if (pqlName != null && pqlName.length() != 0) {
                pqlName = pqlName.toLowerCase();
            } else {
                continue;
            }
            int pos = pqlName.indexOf(':');
            if (pos != -1) {
                String attrSubtype = pqlName.substring(0, pos);
                if (portal && !attrSubtype.equalsIgnoreCase(ResourceAttribute.PORTAL_SUBTYPE)) {
                    continue;
                }
                if (fileSystem && !attrSubtype.equalsIgnoreCase(EntityType.RESOURCE.getName())) {
                    continue;
                }
                pqlName = pqlName.substring(pos+1);
            } else if (!fileSystem) {
                continue;
            }

            List<String> enumeratedValues = cfgAttributes[i].getEnumeratedValues();

            toConvert.add(
                new AttributeDescriptor(
                    cfgAttributes[i].getGroupName()    
                ,   cfgAttributes[i].getDisplayName()
                ,   AttributeType.forName(cfgAttributes[i].getTypeName())
                ,   false
                ,   ResourceAttribute.forNameAndType(pqlName,  subtypeName)
                ,   enumeratedValues.toArray(new String[enumeratedValues.size()])
                )
            );
        }

        return toConvert;
    }
    
    public static void sortAttributeDescriptors(List<AttributeDescriptor> descriptors) {
        Collections.sort(descriptors, new Comparator<AttributeDescriptor>() {
            public int compare(AttributeDescriptor lhs, AttributeDescriptor rhs) {
                return lhs.getDisplayName().compareToIgnoreCase(rhs.getDisplayName());
            }
        });
    }
}
