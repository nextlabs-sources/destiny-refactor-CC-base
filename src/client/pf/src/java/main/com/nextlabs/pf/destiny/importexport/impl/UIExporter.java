package com.nextlabs.pf.destiny.importexport.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.ExportException;
import com.nextlabs.pf.destiny.importexport.ExportFile;
import com.nextlabs.pf.destiny.importexport.IExporter;
import com.nextlabs.pf.destiny.importexport.mapping.App;
import com.nextlabs.pf.destiny.importexport.mapping.Host;
import com.nextlabs.pf.destiny.importexport.mapping.Hostgroup;
import com.nextlabs.pf.destiny.importexport.mapping.User;
import com.nextlabs.pf.destiny.importexport.mapping.Usergroup;

//import com.bluejungle.destiny.policymanager.ui.EntityInfoProvider;
//import com.bluejungle.destiny.policymanager.ui.PolicyServerProxy;

/**
 * Exporter class for UI implementation.
 * 
 * @author clee
 * 
 */
public class UIExporter implements IExporter {
	private static final Log LOG = LogFactory.getLog(UIExporter.class);
	
	private static final String MAPPING_XML_FILENAME = "/com/nextlabs/pf/destiny/importexport/mapping/mapping.xml";

	private static final int DEFAULT_VERSION = -1;
	
	private static final long[] EMPTY_IDS = new long[0];
	
	protected final IPolicyEditorClient client;
	
	public ExportFile exportFile;
	
	private int version; // default version value
	
	protected Collection<DomainObjectDescriptor> requiredComponents;
	
	public UIExporter(){
		IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
		client = compMgr.getComponent(PolicyEditorClient.COMP_INFO);
		version = DEFAULT_VERSION;
		exportFile = new ExportFile();
	}
	
	/**
	 * This method retrieves takes in a Collection of policies to be exported,
	 * finds all dependencies, and parses all necessary components that are
	 * available.
	 * 
	 * @param selected
	 * @return requiredComponents, the Collection of components plus their
	 *         dependencies
	 * @throws ExportException
	 */
	public Collection<DomainObjectDescriptor> prepareForExport(
			Collection<DomainObjectDescriptor> selected) throws ExportException {
		
		//addEntities will put leaf objects into exportFile
		requiredComponents = addEntities(selected, exportFile);
		
		return requiredComponents;
	}
	
	
	/**
	 * This method is called after prepareForExport has prepared selected files
	 * for export.  It takes the ACPL data, wraps it in XML, and writes to the
        * specified file
	 * 
	 * @param xmlFile
	 * @throws ExportException
	 */
	public void executeExport(File xmlFile) throws ExportException {
            exportFile = makeExportFile(requiredComponents, exportFile);
            exportToXML(exportFile, xmlFile);
	}
	
	/**
	 * This method uses Castor to translate the components
	 * visible for unit test
	 * @param xmlFile
	 *            the destination File for export
	 * @throws ExportException
	 */
	void exportToXML(ExportFile exportFile, File xmlFile) throws ExportException {
		try {
			if (xmlFile != null) {
				Mapping map = new Mapping();
				
				InputStream exportFileIs = getClass().getResourceAsStream(MAPPING_XML_FILENAME);
				map.loadMapping(new InputSource(exportFileIs));
				FileOutputStream xmlOutputStream = new FileOutputStream(xmlFile);
				OutputStreamWriter xmlWriter = new OutputStreamWriter(xmlOutputStream, "UTF-8");
				
				// TODO: prettyprinting. why doesn't that line work.
				LocalConfiguration.getInstance().getProperties().setProperty(
						"org.exolab.castor.indent", "true");

				Marshaller mar = new Marshaller(xmlWriter);
				mar.setMapping(map);
				mar.marshal(exportFile);
				
				xmlWriter.close();
			}else{
				LOG.warn("xmlFile is null");
			}
		} catch (MappingException e) {
			throw new ExportException(e);
		} catch (MarshalException e) {
			throw new ExportException(e);
		} catch (ValidationException e) {
			throw new ExportException(e);
		} catch (IOException e) {
			throw new ExportException(e);
		}
	}
	
	/**
	 * This method takes a collection of DomainObjectDescriptors of policies to
	 * be exported, runs a dependency check to find dependant components, and
	 * returns the previous collection plus its dependant components. It will
	 * add subcomponents to the subcomponents Collection, also.
	 * 
	 * visible for unit test
	 * 
	 * @param selected,
	 *            the Collection of policies selected for export.
	 * @return allComponents, the previous Collection, plus components that are
	 *         depended upon.
	 * @throws ExportException
	 */
	 private Collection<DomainObjectDescriptor> addEntities(Collection<DomainObjectDescriptor> selected, ExportFile exportFile) throws ExportException {
		Collection<DomainObjectDescriptor> requiredComponents = new ArrayList<DomainObjectDescriptor>(selected);

		// dependency check selected components
		try {
			Collection<DomainObjectDescriptor> dependencies = client.getDependencies(selected);
			requiredComponents.addAll(dependencies);

			if(LOG.isDebugEnabled()){
				// stores user information required in those selected entities
				for(DomainObjectDescriptor component : requiredComponents){
					try {
						client.getEntitiesForDescriptors(Collections.singleton(component));
					} catch (Exception e) {
						LOG.error("can't get entity for descriptor " + component.getName());
					}
				}
			}
			
			
			Collection<? extends IHasId> policies = client.getEntitiesForDescriptors(requiredComponents);
			if (!policies.isEmpty()) {
				storeLeafDependencies(policies, exportFile);
			}
		} catch (CircularReferenceException e) {
			throw new ExportException(e);
		} catch (PolicyEditorException e) {
			throw new ExportException(e);
		} catch (PQLException e) {
			throw new ExportException(e);
		}

		return requiredComponents;
	}
	

	/**
	 * Populates export file from the Collection requiredComponents and
	 * subcomponents for exportToXML(destination)
	 */
	public ExportFile makeExportFile(Collection<DomainObjectDescriptor> requiredComponents, ExportFile exportFile) throws ExportException {
		exportFile.setVersion(Integer.toString(version));

		// storing the policies/components
		DomainObjectFormatter dof = new DomainObjectFormatter();
		for (DomainObjectDescriptor currDesc:  requiredComponents) {
			
			// using PolicyEditorClient's APIs for getting DomainObjects from
			// DomainObjectDescriptors
			Collection<DomainObjectDescriptor> descForCurrObj = new ArrayList<DomainObjectDescriptor>();
			descForCurrObj.add(currDesc);
			Collection currObj;
			try {
				currObj = client.getEntitiesForDescriptors(descForCurrObj);
			} catch (PolicyEditorException e) {
				throw new ExportException(e);
			}
			
			// getting PQL from DomainObject
			Iterator currObjIter = currObj.iterator();
			dof.formatDef(currObjIter.next());
			
			// finally storing it all
			ExportEntity currEnt = new ExportEntity(currDesc.getName(),
					currDesc.getType().getName(), dof.getPQL());
			exportFile.addExportEntities(currEnt);
			
			// formatter needs resetting
			dof.reset();
		}
		
		return exportFile;
	}
	
	/**
	 * This method allows the client to mark the export file with some sort of
	 * version, dependant on a versioning system.
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	private abstract class ReferenceVisitor implements IPQLVisitor {
	    final DomainObjectFormatter dof;
	    
	    public ReferenceVisitor(DomainObjectFormatter dof) {
	        this.dof = dof;
        }
	    
        public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
            dof.formatPolicyDef(descr, policy);
        }
        
        public void visitFolder(DomainObjectDescriptor descr) {
            dof.formatFolder(descr);
        }
        
        public void visitComponent(final DomainObjectDescriptor descr, IPredicate spec) {
            // parse users here
            spec.accept(new DefaultPredicateVisitor() {
                public void visit(IRelation pred) {
                    // This method does not pay attention to the operator 
                    // because it looks only for ID-based attributes 
                    // where the operator is either == or !=
                    addIds(pred.getLHS(), pred.getRHS(), pred);
                    addIds(pred.getRHS(), pred.getLHS(), pred);
                }
                
                // This method assumes that the attribute is on the left 
                // and the constant is on the right. We try calling this method both ways, 
                // so this approach works even when the attribute and the constant are swapped.
                @SuppressWarnings("unused")
                private void addIds(IExpression lhs, IExpression rhs, IRelation pred) {
                    if (rhs instanceof Constant) {
                        Object val = rhs.evaluate(null).getValue();
                        if (val instanceof Long) {
                            foundReference(lhs, (Long)val);
                        }
                    }
                }
            }, IPredicateVisitor.POSTORDER);
            dof.formatDef(descr, spec);
        }
        
        abstract void foundReference(IExpression lhs, Long val);
        
        public void visitLocation(DomainObjectDescriptor descr, Location location) {
            dof.formatLocation(descr, location);
        }

        @SuppressWarnings("unused")
        public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
            dof.formatAccessPolicy(accessPolicy);
        }
    }
	
	/**
	 * checks for (and stores) users that are depended on by the entities
	 * inputted in the Collection of DomainObjectDescriptors
	 * 
	 * @param entityList,
	 *            the list of user components
	 * @throws PQLException 
	 * @throws PolicyEditorException 
	 */
	private void storeLeafDependencies(Collection<? extends IHasId> entityList, ExportFile exportFile) throws PQLException, PolicyEditorException {
		// parse entityList, store ids BY TYPE
		final DomainObjectFormatter dof = new DomainObjectFormatter();
		final DomainObjectFormatter pqldof = new DomainObjectFormatter();
		final Set<Long> elementIds = new HashSet<Long>();
		final Set<Long> usergroupIds = new HashSet<Long>();
		final Set<Long> hostgroupIds = new HashSet<Long>();
		
		for(IHasId parseEnt : entityList ){	
			pqldof.reset();
			dof.reset();
			pqldof.formatDef(parseEnt);
			DomainObjectBuilder.processInternalPQL(pqldof.getPQL(), new ReferenceVisitor(dof) {
			    void foundReference(IExpression lhs, Long val){
		            if (lhs == SubjectAttribute.USER_ID
		                    || lhs == SubjectAttribute.HOST_ID
		                    || lhs == SubjectAttribute.APP_ID) {
		                elementIds.add(val);
		            }else if (lhs == SubjectAttribute.USER_LDAP_GROUP_ID) {
		                usergroupIds.add(val);
		            }else if (lhs == SubjectAttribute.HOST_LDAP_GROUP_ID) {
		                hostgroupIds.add(val);
		            }
		        }
            });
		}
		
		LOG.debug("elementIds.size() = " + elementIds.size());
		LOG.debug("usergroupIds.size() = " + usergroupIds.size());
		LOG.debug("hostgroupIds.size() = " + hostgroupIds.size());
		
		// get leaf objects by id
		List<LeafObject> elementLeaves = client.getLeafObjectsForIds(CollectionUtils.toLong(elementIds), EMPTY_IDS, EMPTY_IDS);
		
		ArrayList<LeafObject> userLeaves = new ArrayList<LeafObject>();
		ArrayList<LeafObject> hostLeaves = new ArrayList<LeafObject>();
		ArrayList<LeafObject> appLeaves = new ArrayList<LeafObject>();
		
		for (LeafObject nextElem : elementLeaves) {
		    elementIds.remove(nextElem.getId());
			if (LeafObjectType.USER == nextElem.getType()) {
				userLeaves.add(nextElem);
			} else if (LeafObjectType.HOST == nextElem.getType()) {
				hostLeaves.add(nextElem);
			} else if (LeafObjectType.APPLICATION == nextElem.getType()) {
				appLeaves.add(nextElem);
			}
		}
		
		LOG.debug("userLeaves.size() = " + userLeaves.size());
		LOG.debug("hostLeaves.size() = " + hostLeaves.size());
		LOG.debug("appLeaves.size() = " + appLeaves.size());
		// store leafobjects
		
		for(LeafObject curr: userLeaves){
			exportFile.addUser(new User(curr));
		}
		
		for(LeafObject curr: hostLeaves){
			exportFile.addHost(new Host(curr));
		}
		
		for(LeafObject curr: appLeaves){
			exportFile.addApp(new App(curr));
		}
		
		List<LeafObject> usergroupLeaves = client.getLeafObjectsForIds(EMPTY_IDS, 
		        CollectionUtils.toLong(usergroupIds), EMPTY_IDS);
		LOG.debug("usergroupLeaves.size() = " + usergroupLeaves.size());
		for(LeafObject curr: usergroupLeaves){
		    usergroupIds.remove(curr.getId());
			exportFile.addUsergroup(new Usergroup(curr));
		}
		
		List<LeafObject> hostgroupLeaves = client.getLeafObjectsForIds(EMPTY_IDS, EMPTY_IDS, 
		        CollectionUtils.toLong(hostgroupIds));
		LOG.debug("hostgroupLeaves.size() = " + hostgroupLeaves.size());
		for(LeafObject curr: hostgroupLeaves){
		    hostgroupIds.remove(curr.getId());
			exportFile.addHostgroup(new Hostgroup(curr));
		}
		
		if(!elementIds.isEmpty() || !usergroupIds.isEmpty() || !hostgroupIds.isEmpty()){
		    
		    for(IHasId parseEnt : entityList ){   
	            pqldof.reset();
	            dof.reset();
	            pqldof.formatDef(parseEnt);
	            DomainObjectBuilder.processInternalPQL(pqldof.getPQL(), new ReferenceVisitor(dof) {
	                
	                private Set<Long> unknownReference = new HashSet<Long>();
	                
	                @Override
                    public void visitComponent(DomainObjectDescriptor descr, IPredicate spec) {
	                    unknownReference.clear();
                        super.visitComponent(descr, spec);
                        if(!unknownReference.isEmpty()){
                            LOG.warn("The component '" + descr.getName() + "' contains unknown references. They are " 
                                    + CollectionUtils.asString(unknownReference, ", ") + ".");
                        }
                    }

                    void foundReference(IExpression lhs, Long val){
	                    if (lhs == SubjectAttribute.USER_ID
	                            || lhs == SubjectAttribute.HOST_ID
	                            || lhs == SubjectAttribute.APP_ID) {
	                        if(elementIds.contains(val)){
	                            unknownReference.add(val);
	                        }  
	                    }else if (lhs == SubjectAttribute.USER_LDAP_GROUP_ID) {
	                        if(usergroupIds.contains(val)){
	                            unknownReference.add(val);
                            }
	                    }else if (lhs == SubjectAttribute.HOST_LDAP_GROUP_ID) {
	                        if(hostgroupIds.contains(val)){
	                            unknownReference.add(val);
                            }
	                    }
	                }
	            });
	        }
		}
	}
}
