/*
 * Created on Mar 25, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.enrollment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentStatus;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentType;
import com.bluejungle.destiny.services.enrollment.types.Realm;
import com.bluejungle.destiny.tools.enrollment.EnrollmentMgr;
import com.bluejungle.destiny.tools.enrollment.EnrollmentMgrException;
import com.bluejungle.destiny.tools.enrollment.EnrollmentMgrSocketFactory;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.OptionHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/nextlabs/destiny/tools/enrollment/ClientInfoMgr.java#1 $
 */

public class ClientInfoMgr extends EnrollmentMgr{
    private static final String CLIENT_INFO_FILE_PATH_KEY = "clientinfo.filepath";

    public static void main(String[] args) {
        try {
            ClientInfoMgr mgr = new ClientInfoMgr();
            mgr.parseAndExecute(args);
        } catch (Exception e) {
            printException(e);
        }
    }

    /**
     * Constructor
     * @throws InvalidOptionDescriptorException
     */
    ClientInfoMgr() throws InvalidOptionDescriptorException {
        super(new ClientInfoMgrOptionDescriptorEnum());

    }

    @Override
    protected void exec(ICommandLine commandLine) throws IOException,
            FileFormatException, ParseException, EnrollmentMgrException, ServiceException {
    String domainName = getValue(commandLine,
                ClientInfoMgrOptionDescriptorEnum.DOMAIN_NAME_OPTIONS_ID);

        File xmlFile = getValue(commandLine,
                ClientInfoMgrOptionDescriptorEnum.XML_FILE_OPTIONS_ID);

        super.authenticate(commandLine);

        final OptionId<?> selectedAction = OptionHelper.findSelectedOption(
                ClientInfoMgrOptionDescriptorEnum.ACTIONS, commandLine);

        // Execute the command:
        if (commandLine.isOptionExist(ClientInfoMgrOptionDescriptorEnum.ENROLL_OPTIONS_ID)) {
            enroll(EnrollmentType.CLIENT_INFO, domainName, null, xmlFile, null, null);
        } else if (commandLine.isOptionExist(ClientInfoMgrOptionDescriptorEnum.DELETE_OPTIONS_ID)) {
            delete(domainName);
        } else if (commandLine.isOptionExist(ClientInfoMgrOptionDescriptorEnum.SYNC_OPTIONS_ID)) {
            sync(domainName);
        } else if (commandLine.isOptionExist(ClientInfoMgrOptionDescriptorEnum.LIST_OPTIONS_ID)) {
            list();
        } else {
            throw new ParseException("unknown action");
        }

        if (commandLine.isOptionExist(ClientInfoMgrOptionDescriptorEnum.SYNC_OPTIONS_ID)) {
            EnrollmentStatus enrollmentStatus = getEnrollmentStatus(domainName);
            String message = enrollmentStatus.getErrorMessage();
            String status = enrollmentStatus.getStatus();
            System.out.println("\nEnrollment sync action result: " + status + "\n" + message);
        } else {
            System.out.println("\nEnrollment " + selectedAction + " action done!");
        }

        AuthenticationContext.clearCurrentContext();

        if (commandLine.isOptionExist(ClientInfoMgrOptionDescriptorEnum.ENROLL_OPTIONS_ID)) {
            System.out.println("Please proceed with sync action.");
        }

    }
    
    @Override
    protected void sync(String name) throws EnrollmentMgrException {
        Realm realm = getTheOnlyRealm(name);
        if (realm.getStatus() != null && realm.getStatus().getStatus() != null
                && realm.getStatus().getStatus().equals("success")) {
            throw new EnrollmentMgrException("Realm " + name + " already sync.");
        }

        super.sync(name);

    }
    
    @Override
    protected void parseDefinitionFile(File xmlFile, List<EnrollmentProperty> properties)
            throws IOException, FileFormatException {
        properties.add(new EnrollmentProperty(CLIENT_INFO_FILE_PATH_KEY, new String[] { xmlFile
                .getAbsolutePath() }));
    }

    @Override
    protected Realm[] realmsFilter(Realm[] realms) {
        if (realms == null) {
            return null;
        }
        List<Realm> realmList = new ArrayList<Realm>(realms.length);

        for (Realm realm : realms) {
            if (realm.getType().equals(EnrollmentType.CLIENT_INFO)) {
                realmList.add(realm);
            }
        }
        return realmList.toArray(new Realm[] {});
    }
}
