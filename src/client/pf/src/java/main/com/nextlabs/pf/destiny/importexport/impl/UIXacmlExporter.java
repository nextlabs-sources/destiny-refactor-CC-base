package com.nextlabs.pf.destiny.importexport.impl;

/*
 * Created on Apr 05, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/nextlabs/pf/destiny/importexport/impl/UIXacmlExporter.java#1 $:
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.nextlabs.pf.destiny.formatter.XACMLDomainObjectFormatter;
import com.nextlabs.pf.destiny.importexport.ExportException;
import com.nextlabs.pf.destiny.importexport.ExportFile;
import com.nextlabs.pf.destiny.importexport.mapping.App;
import com.nextlabs.pf.destiny.importexport.mapping.Host;
import com.nextlabs.pf.destiny.importexport.mapping.User;

public class UIXacmlExporter extends UIExporter {
    private static final Log LOG = LogFactory.getLog(UIXacmlExporter.class);
    private static final String NULL_SID = "S-1-0-0";

    /**
     * This method is called after prepareForExport has prepared selected files
     * for export.  It will export the components as XACML
     */
    @Override
    public void executeExport(File xacmlFile) throws ExportException {
        try {
            if (xacmlFile != null) {
                exportFile = makeExportFile(requiredComponents, exportFile);

                // Convert all .did attributes to the corresponding sid

                FileWriter xacmlFileWriter = new FileWriter(xacmlFile);
                xacmlFileWriter.write(convertComponentsToXACML(requiredComponents, exportFile));
                xacmlFileWriter.close();
            } else {
                LOG.warn("xacmlFile is null");
            }
        } catch (IOException e) {
            throw new ExportException(e);
        }
    }

    private String convertComponentsToXACML(Collection<DomainObjectDescriptor> requiredComponents, ExportFile exportFile) throws ExportException {
        XACMLDomainObjectFormatter xdof = new XACMLDomainObjectFormatter();
           
        DIDReplacer didReplace = new DIDReplacer(exportFile);

        Collection<? extends IHasId> objects = null;
        try {
            objects = client.getEntitiesForDescriptors(requiredComponents);
        } catch (PolicyEditorException e) {
            throw new ExportException(e);
        }

        for (IHasId id : objects) {
            if (id instanceof IDSpec) {
                IDSpec spec = (IDSpec)id;

                spec.setPredicate(Predicates.transform(spec.getPredicate(), didReplace));
            } else if (id instanceof IDPolicy) {
                ITarget target = ((IDPolicy)id).getTarget();

                target.setSubjectPred(Predicates.transform(target.getSubjectPred(), didReplace));
                target.setToSubjectPred(Predicates.transform(target.getToSubjectPred(), didReplace));
            }
            
        }

        xdof.formatDef(objects);
           
        return xdof.getXACML();
    }

    /**
     * If we reference enrolled entities directly, they will be expressed in PQL as <subject>.did = <number>
     * The "did" is the Destiny ID, the ID in the database. This is useless to anyone other than us, so we
     * convert the whole expression to <subject>.uid = <corresponding sid/uid>
     */
    private class DIDReplacer extends Predicates.DefaultTransformer {
        Map<SubjectType, HashMap<Long, Constant>> didMap = new HashMap<SubjectType, HashMap<Long, Constant>>();

        DIDReplacer(ExportFile exportFile) {
            HashMap<Long, Constant> userMap = new HashMap<Long, Constant>();
            didMap.put(SubjectType.USER, userMap);

            for (User user : exportFile.getUsers()) {
                userMap.put(user.getId(), Constant.build(user.getSid()));
            }

            HashMap<Long, Constant> hostMap = new HashMap<Long, Constant>();
            didMap.put(SubjectType.HOST, hostMap);

            for(Host host : exportFile.getHosts()) {
                hostMap.put(host.getId(), Constant.build(host.getSid()));
            }

            HashMap<Long, Constant> appMap = new HashMap<Long, Constant>();
            didMap.put(SubjectType.APP, appMap);

            for (App app: exportFile.getApps()) {
                appMap.put(app.getId(), Constant.build(app.getSid()));
            }
        }

        @Override
        public IPredicate transformRelation(IRelation rel) {
            if (isDID(rel.getLHS())) {
                IExpression didExpr = rel.getLHS();
                IExpression didValue = rel.getRHS();
                IExpression convertedUID = getMatchingUID(didExpr, didValue);
                return new Relation (rel.getOp(), getUIDOfType(didExpr), convertedUID);
            } else if (isDID(rel.getRHS())) {
                IExpression didExpr = rel.getRHS();
                IExpression didValue = rel.getLHS();
                IExpression convertedUID = getMatchingUID(didExpr, didValue);
                return new Relation(rel.getOp(), convertedUID, getUIDOfType(didExpr));
            }
            return rel;
        }


        private boolean isDID(IExpression expr) {
            if (expr instanceof SubjectAttribute) {
                return ((SubjectAttribute)expr).getName().equals("did");
            }
            return false;
        }

        private IExpression getUIDOfType(IExpression expr) {
            if (expr instanceof SubjectAttribute) {
                SubjectAttribute subj = (SubjectAttribute)expr;

                if (subj.getSubjectType() == SubjectType.USER) {
                    return (SubjectAttribute)SubjectAttribute.USER_UID;
                } else if (subj.getSubjectType() == SubjectType.HOST) {
                    return (SubjectAttribute)SubjectAttribute.HOST_UID;
                } else if (subj.getSubjectType() == SubjectType.APP) {
                    return (SubjectAttribute)SubjectAttribute.APP_UID;
                }
            }

            return expr;
        }

        private IExpression getMatchingUID(IExpression did, IExpression val) {
            if (did instanceof SubjectAttribute) {
                SubjectAttribute attr = (SubjectAttribute)did;
                
                if (val instanceof Constant && ((Constant)val).getValue().getType() == ValueType.LONG) {
                    Long longVal = (Long)((Constant)val).getValue().getValue();

                    IExpression matchingUID = didMap.get(attr.getSubjectType()).get(longVal);

                    if (matchingUID == null) {
                        matchingUID = Constant.build(NULL_SID);
                    }

                    return matchingUID;
                }
            }

            return val;
        }
    }
}
