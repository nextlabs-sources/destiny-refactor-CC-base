/*
 * Created on April 22, 2013. All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.pf.destiny.importexport.ConflictResolution;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.IImporter;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.IImportState.RetainStatus;
import com.nextlabs.pf.destiny.importexport.IImportState.Shallow;
import com.nextlabs.pf.destiny.importexport.impl.Importer;
import com.nextlabs.shared.tools.impl.DetailUsagePrinter;
import com.nextlabs.shared.tools.impl.OptionHelper;
import com.nextlabs.shared.tools.impl.UsagePrinterBase;

/**
 * provide the main functionality for import seed data
 * @author ichiang
 */
public class EntityImportSeedData extends EntityImportExport {
	
    protected final static int STATUS_SUCCESS   = 0;
    protected final static int STATUS_ERR_UNKNOWN_HOST = -1;
    protected final static int STATUS_ERR_CONNECTION = -2;
    protected final static int STATUS_ERR_FILE   = -3;
    protected final static int STATUS_ERR_USER_CANCEL  = -4;
    protected final static int STATUS_ERR_SERVER_CANCEL = -5;
 
    private static final SimpleDateFormat NICE_DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
	
	EntityImportSeedData() throws InvalidOptionDescriptorException {
        super();
        consoleApplicationDescriptor = new EntityImportOptionDescriptorEnum();
    }

    public static void main(String[] args) {
        try {
            new EntityImportSeedData().parseAndExecute(args);
        } catch (Exception e) {
            printException(e);
        }
    }
	
    @Override
    protected void printUsage() {
        String name = getDescriptor().getShortDescription();  
  
        String synopsis = "createSeedData {[-s <host>] [-u <user>] [-w <password>] [-p <port>] [-A] [-F <filename>]}";
  
        DetailUsagePrinter detailPrinter = new DetailUsagePrinter();
        detailPrinter.renderUsage(getDescriptor());
  
        List<String> optionUsuage = detailPrinter.getCache();
        List<String> seedDataUsuage = new ArrayList<String>();  
        seedDataUsuage.add (optionUsuage.get(0));
        seedDataUsuage.add (optionUsuage.get(1));
        seedDataUsuage.add (optionUsuage.get(2));
        seedDataUsuage.add (optionUsuage.get(3));
        seedDataUsuage.add (optionUsuage.get(4));
        seedDataUsuage.add (optionUsuage.get(10));
        seedDataUsuage.add (optionUsuage.get(12));
  
        String formattedOptions = CollectionUtils.toString(seedDataUsuage);
  
        UsagePrinterBase.print(name, synopsis, formattedOptions);
    }

    @Override
    protected int doAction(ICommandLine commandLine) throws ParseException, IOException {
        // finding the conflict solution
        final ConflictResolution conflictSoltuion;
  
        if (commandLine.isOptionExist( EntityImportOptionDescriptorEnum.CONFLICT_CANCEL_OPTION)) {
            conflictSoltuion = ConflictResolution.UNKNOWN;
        } else {
            Collection<OptionId<Boolean>> selectedOptionIds =
                OptionHelper.findTrueOption(
                    EntityImportOptionDescriptorEnum.CONFLICT_SOLUTION_OPTION_IDS, commandLine);
   
            OptionId<Boolean> selectedOptionId;
            if (selectedOptionIds.isEmpty()) {
                selectedOptionId = EntityImportOptionDescriptorEnum.CONFLICT_OVERWRITE_OPTION;
            } else if (selectedOptionIds.size() > 1) {
                throw new IllegalArgumentException(
                    "you can't select more than than one conflict solution group."
                + CollectionUtils.asString(selectedOptionIds, ",", "-"));
            }else{
                selectedOptionId = selectedOptionIds.iterator().next();
            }
   
            if (selectedOptionId == EntityImportOptionDescriptorEnum.CONFLICT_RENAME_NEW_OPTION) {
                conflictSoltuion = null;
            } else if (selectedOptionId == EntityImportOptionDescriptorEnum.CONFLICT_OVERWRITE_OPTION) {
                conflictSoltuion = ConflictResolution.KEEP_NEW;
            } else if (selectedOptionId == EntityImportOptionDescriptorEnum.CONFLICT_KEEP_OPTION) {
                conflictSoltuion = ConflictResolution.KEEP_OLD;
            } else {
                throw new ParseException("what is the selected option in conflict solution group? "
                                         + selectedOptionId);
            }
        }
  
        if (commandLine.isOptionExist( EntityImportOptionDescriptorEnum.FILE_OPTION)) {
            String filename = getValue(commandLine, EntityImportOptionDescriptorEnum.FILE_OPTION).toString();
            File file = new File(filename);
            if (!file.exists()) {
                System.err.println("file \"" + filename + "\" does not exist");
                return STATUS_ERR_FILE;
            }

            try {
                //what is shallow import
                Shallow failRecovery = commandLine.isOptionExist(
                    EntityImportOptionDescriptorEnum.SHALLOW_IMPORT_OPTION)
                                       ? IImportState.Shallow.SHALLOW
                                       : IImportState.Shallow.FULL;

                RetainStatus retainStatus = commandLine.isOptionExist(
                    EntityImportOptionDescriptorEnum.STATUS_OPTION)
                                            ? IImportState.RetainStatus.RETAIN
                                            : IImportState.RetainStatus.SET_TO_DRAFT;
                                            
                IImporter importer = new Importer(file, failRecovery, retainStatus);
                Collection<ExportEntity> entitesFromFile = importer.getEntities();
                if (commandLine.isOptionExist(EntityImportOptionDescriptorEnum.LIST_ALL_OPTION)) {
                    for (ExportEntity e : entitesFromFile) {
                        if (e.getType().equalsIgnoreCase("POLICY")) {
                            System.out.println(e.getName());
                        }
                    }
                } else {
                    createPolicyEditorClient(commandLine).login();
                    IImportState importState = importer.doImport(entitesFromFile);
                    if (importState.hasConflicts() 
                        && conflictSoltuion != null
                        && conflictSoltuion == ConflictResolution.UNKNOWN) {
                        System.out
                            .println("Abort! import state has conflicts and you chose \"cancel on conflict\" "
                                     + EntityImportOptionDescriptorEnum.CONFLICT_CANCEL_OPTION);
                        return STATUS_ERR_USER_CANCEL;
                    }

     
                    final String renameSuffix = " " + NICE_DATE_TIME_FORMAT.format(new Date());
                    for (IImportConflict conflict : importState.getConflicts()) {
                        if(conflictSoltuion == null){
                            ConflictResolution renameSolution = new ConflictResolution(
                                ConflictResolution.ConflictType.RENAME_NEW, conflict.getName()
                            + renameSuffix);
                            conflict.setResolution(renameSolution);
                        }else{
                            conflict.setResolution(conflictSoltuion);
                        }
                    }

                    importer.commitImport();
                }
            } catch (ImportException e) {
                printException(e);
                return STATUS_ERR_UNKNOWN_EXCEPTION_THROWN;
            } catch (LoginException e) {
                printException(e);
                return STATUS_ERR_CONNECTION;
            }
        }
  
        return STATUS_SUCCESS;
    }
 
 
    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.EntityImportExport#getReturnMessage(int)
     */
    protected String getReturnMessage(int returnCode){
        switch (returnCode) {
            case STATUS_SUCCESS:
                return "Import successful";
            case STATUS_ERR_UNKNOWN_HOST:
                return "Unknown host";
            case STATUS_ERR_CONNECTION:
                return "Connection refused. Check the credentials and try again.";
            case STATUS_ERR_FILE:
                return "Cannot open export file. File does not exist, or user has inadequate permissions. "
                    + "Import aborted.";
            case STATUS_ERR_USER_CANCEL:
                return "Import canceled by user.";
            case STATUS_ERR_SERVER_CANCEL:
                return "Import canceled by server.";
            case STATUS_ERR_UNKNOWN_EXCEPTION_THROWN:
                return "Exception thrown when importing.";
            default:
                return "Unknown Return Code (" + returnCode + ")";
        }
    }
}
