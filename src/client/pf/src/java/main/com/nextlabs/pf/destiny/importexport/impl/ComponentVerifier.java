package com.nextlabs.pf.destiny.importexport.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.ExportFile;
import com.nextlabs.pf.destiny.importexport.IComponentVerifier;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.mapping.ComponentBase;

public class ComponentVerifier implements IComponentVerifier {
	private static final Log LOG = LogFactory.getLog(ComponentVerifier.class);

	private final IPolicyEditorClient client;

	//Constructor
	public ComponentVerifier(IPolicyEditorClient client) {
		this.client = client;
	}

	private Long getComponentId(SubjectAttribute attr, LeafObjectType leafObjectType, ComponentBase component)
			throws ImportException {
		IPredicate pred = attr.buildRelation(RelationOp.EQUALS, Constant.build(component.getSid()));
		com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec searchspec = new com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec(
				leafObjectType, pred, 1);
		try {
			List<LeafObject> results = client.runLeafObjectQuery(searchspec);
			if (!results.isEmpty()) {
				return results.get(0).getId();
			} else {
				LOG.warn("Component \"" + component.getName() + "\" with sid \"" + component.getSid()
							+ "\" is not found");
				return Importer.DICTIOANRY_ERROR_SIGN;
			}
		} catch (PolicyEditorException e) {
			throw new ImportException(e);
		}
	}

	private void verifyComponent(SubjectAttribute attr, LeafObjectType leafObjectType,
			Collection<? extends ComponentBase> components, TreeMap<Long, Long> userIdMapping) throws ImportException {
		for (ComponentBase component : components) {
			long id = getComponentId(attr, leafObjectType, component);
			userIdMapping.put(new Long(component.getId()), id);
		}
	}
	
	private void verifyComponent(SubjectAttribute attr, SubjectAttribute attrBackup, LeafObjectType leafObjectType,
			Collection<? extends ComponentBase> components, TreeMap<Long, Long> userIdMapping) throws ImportException {
		for (ComponentBase component : components) {
			long id = getComponentId(attr, leafObjectType, component);
			if (id == Importer.DICTIOANRY_ERROR_SIGN) {
				id = getComponentId(attrBackup, leafObjectType, component);
			}
			userIdMapping.put(new Long(component.getId()), id);
		}
	}

	/**
	 * This method will attempt to match imported components to existing
	 * components via the webserver, and place them in the proper Collection,
	 * depending on if it is a verified components, or a conflicted component.
	 * Renamed components will have an entry in the state's replacement mapping.
	 * Each conflicted components will have an IImportConflict object created to
	 * store the user's conflict resolution for that entity.
	 * @param components the collection of 
	 * @throws ImportException 
	 * @throws PQLException
	 */
	public IImportState verifyComponents(IImportState importState, Collection<ExportEntity> components,
			ExportFile importData, TreeMap<Long, Long> userIdMapping) throws ImportException {
		/*
		 //ArrayList userList = new ArrayList(users);
		 Collection leafObjects = new ArrayList();
		 try {
		 leafObjects = client.getLeafObjects(LeafObjectType.USER);
		 } catch (PolicyEditorException e) {
		 throw new ImportException(e);
		 }
		 */
		SubjectAttribute attr, attrBackup;

		//users
		attr = SubjectAttribute.forNameAndType("windowssid", SubjectType.USER);
		attrBackup = SubjectAttribute.forNameAndType("unixid", SubjectType.USER);
		verifyComponent(attr, attrBackup, LeafObjectType.USER, importData.getUsers(), userIdMapping);

		//hosts
		attr = SubjectAttribute.forNameAndType("windowssid", SubjectType.HOST);
		attrBackup = SubjectAttribute.forNameAndType("unixid", SubjectType.HOST);
		verifyComponent(attr, attrBackup, LeafObjectType.HOST, importData.getHosts(), userIdMapping);

		//apps
		attr = SubjectAttribute.forNameAndType("appfingerprint", SubjectType.APP);
		verifyComponent(attr, LeafObjectType.APPLICATION, importData.getApps(), userIdMapping);

		//usergroups
		attr = SubjectAttribute.USER_LDAP_GROUP;
		verifyComponent(attr, LeafObjectType.USER_GROUP, importData.getUsergroups(), userIdMapping);

		//hostgroups
		attr = SubjectAttribute.HOST_LDAP_GROUP;
		verifyComponent(attr, LeafObjectType.HOST_GROUP, importData.getHostgroups(), userIdMapping);

		for(ExportEntity current : components){
			/*
			 //get a IDPolicy from pql
			 DomainObjectBuilder builder = new DomainObjectBuilder(current.getPql());
			 try {
			 IDPolicy p = builder.processPolicy();
			 if (p == null){
			 throw new ImportException("pql exception");
			 }
			 if (p.getName().compareTo(current.getName()) != 0){
			 throw new ImportException("name was incorrectly exported, 
			 ExportEntity name != component name " + p.getName() + " , " + current.getName());
			 }
			 */

			//check if policy has dictionary discrepancies:
			//first check if policy exists
			Collection<String> nameExist = new ArrayList<String>();
			nameExist.add(current.getName());
			Collection currEntityCollec;
			try {
				EntityType type = EntityType.forName(current.getType());
				currEntityCollec = client.getEntitiesForNamesAndType(nameExist, type, true);
			} catch (PolicyEditorException e) {
				throw new ImportException(e);
			}

			IHasId currEntity = (IHasId) (currEntityCollec.iterator().next());
			if (currEntity.getId() == null) { //ID in policy is null when getEntitiesForNamesAndType creates a new policy 
				//does not already exist: no conflict
				importState.addUnconflicted(current);
			} else { //conflict, report pql from first conflict.
				DomainObjectFormatter dof = new DomainObjectFormatter();
				dof.formatDef(currEntity);
				importState.addConflict(new ImportConflict(current.getName(), current.getType(),
						current.getPql(), dof.getPQL()));
			}
		}
		return importState;
	}
}
