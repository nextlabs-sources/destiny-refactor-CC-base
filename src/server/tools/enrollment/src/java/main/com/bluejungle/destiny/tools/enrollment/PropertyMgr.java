
/*
 * Created on Mar 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.services.enrollment.types.AttributeType;
import com.bluejungle.destiny.services.enrollment.types.Column;
import com.bluejungle.destiny.services.enrollment.types.ColumnList;
import com.bluejungle.destiny.services.enrollment.types.EntityType;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.OptionHelper;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMgr.java#1 $
 */

public class PropertyMgr extends EnrollmentMgrShared {
    /*
     * Column Type List 
     */
    static final Set<String> COLUMN_TYPES;
    static {
        Set<String> list = new HashSet<String>();
        list.add("STRING");
        list.add("CS-STRING");
		list.add("MULTI-STRING");
        list.add("NUMBER");
        list.add("DATE");
        COLUMN_TYPES = list;
    }

    /*
     * Column Type List 
     */
    static final Set<String> ENTITY_TYPES;
    static {
        Set<String> list = new HashSet<String>();
        list.add("USER");
        list.add("HOST");
        list.add("APPLICATION");
        ENTITY_TYPES = list;
    }
    
    private final IConsoleApplicationDescriptor descriptor;

    PropertyMgr() throws InvalidOptionDescriptorException {
        super();
        descriptor = new PropertyMgrOptionDescriptorEnum();
    }

    public static void main(String[] args) {
        try{
            PropertyMgr mgr = new PropertyMgr();
            mgr.parseAndExecute(args);
        }catch(Exception e){
            printException(e);
        }
    }
    
    @Override
    protected void exec(ICommandLine commandLine) throws Exception {
        System.out.println(WELCOME);

        String type = getValue(commandLine,
                PropertyMgrOptionDescriptorEnum.TYPE_OPTION_ID);
        String logicalName = getValue(commandLine,
                PropertyMgrOptionDescriptorEnum.LOGICAL_NAME_OPTION_ID);
        String displayName = getValue(commandLine,
                PropertyMgrOptionDescriptorEnum.DISPLAY_NAME_OPTION_ID);
        String entityType = getValue(commandLine,
                PropertyMgrOptionDescriptorEnum.ENTITY_TYPE_OPTION_ID);
        
        String keyStorePassword = getValue(commandLine,
        		EnrollmentMgrOptionDescriptorEnum.KEYSTORE_PASSWORD_OPTION_ID);
        System.setProperty(ENROLL_TOOL_KEYSTORE_PASSWORD, keyStorePassword);


        authenticate(commandLine);

        // Execute the command:

        if (commandLine.isOptionExist( PropertyMgrOptionDescriptorEnum.ADD_OPTION_ID)) {
            addColumn(type, logicalName, displayName, entityType);
        } else if (commandLine.isOptionExist( PropertyMgrOptionDescriptorEnum.DELETE_OPTION_ID)) {
            delColumn(logicalName, entityType.toUpperCase());
        } else if (commandLine.isOptionExist( PropertyMgrOptionDescriptorEnum.LIST_OPTION_ID)) {
            listColumns();
        } else {
            throw new ParseException("unknown action");
        }
        
        AuthenticationContext.clearCurrentContext();
        
        System.out.println("\nEnrollment "
                + OptionHelper.findSelectedOption(PropertyMgrOptionDescriptorEnum.ACTIONS,
                        commandLine) + " action done!");
    }

    @Override
    protected IConsoleApplicationDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Adds a searchable column
     * 
     * @param type
     * @param logicalName
     * @param displayName
     * @throws RemoteException
     * @throws EnrollmentMgrException 
     */
    private void addColumn(String type, String logicalName, String displayName, String entityType)
            throws EnrollmentMgrException{
//        if (!COLUMN_TYPES.contains(type.toUpperCase())) {
//            throw new ParseException("property type can only be [STRING|CS-STRING|NUMBER|DATE]");
//        }
//        if (!ENTITY_TYPES.contains(entityType.toUpperCase())) {
//            throw new ParseException("property type can only be [USER|HOST|APPLICATION]");
//        }
        AttributeType attributeType = AttributeType.fromString(type.toUpperCase());
        EntityType eType = EntityType.fromValue(entityType.toUpperCase());

        if(isColumnExist(attributeType, logicalName, eType)){
            throw new EnrollmentMgrException("The Column \"" + entityType+ "." + logicalName.toUpperCase() + "\" already exists");
        }
        
        Column columnToAdd = new Column(attributeType, logicalName, displayName, eType);
        enrollmentWS.addColumn(columnToAdd);
    }

    /**
     * Delete column action
     * @param logicalName
     * @param elementType
     * @throws ServiceException
     * @throws RemoteException
     */
    private void delColumn(String logicalName, String elementType) throws EnrollmentMgrException {
        enrollmentWS.delColumn(logicalName, EntityType.fromValue(elementType));
    }

    /**
     * List column action
     * @throws ServiceException
     * @throws RemoteException
     */
    private void listColumns() throws EnrollmentMgrException {
        ColumnList columns = enrollmentWS.getColumns();
        if (columns != null) {
            Column[] columnArr = columns.getColumns();
            if (columnArr.length == 0) {
                System.out.println("No columns exist");
                return;
            }
            // assuming columns returned in a list ordered by entity type
            String entityType = columnArr[0].getParentType().getValue();
            
            for (int i = 0; i < columnArr.length; i++) {
                printColumn(columnArr[i]);
                if (i < columnArr.length - 1) {
                    String newEntityType = columnArr[i + 1].getParentType().getValue();
                    // separate the columns by entity type
                    if (!entityType.equals(newEntityType)) {
                        entityType = newEntityType;
                        System.out.println();
                    }
                }
            }
        }
    }
    
    private boolean isColumnExist(AttributeType type, String logicalName, EntityType entityType)
            throws EnrollmentMgrException {
        ColumnList columns = enrollmentWS.getColumns();
        if (columns != null) {
            for (Column column : columns.getColumns()) {
                if (column.getType().equals(type) 
                        && column.getLogicalName().equalsIgnoreCase(logicalName)
                        && column.getParentType().equals(entityType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Prints out the realm
     * 
     * @param realmToPrint
     */
    private void printColumn(Column columnToPrint) {
        StringBuffer text = new StringBuffer();
        text.append(columnToPrint.getParentType().getValue() + "."
                + columnToPrint.getLogicalName() + " - "
                + columnToPrint.getDisplayName() + " : "
                + columnToPrint.getType().getValue());
        System.out.println(text.toString());
    }
}
