package com.nextlabs.shared.tools;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.nextlabs.pf.destiny.importexport.ConflictResolution;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.IImporter;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.IImportState.Shallow;
import com.nextlabs.pf.destiny.importexport.IImportState.RetainStatus;
import com.nextlabs.pf.destiny.importexport.impl.Importer;
import com.nextlabs.shared.tools.impl.DetailUsagePrinter;
import com.nextlabs.shared.tools.impl.OptionHelper;
import com.nextlabs.shared.tools.impl.UsagePrinterBase;


/**
 * provide the main functionality of the import tools
 *
 * @author hchan
 * @date Apr 10, 2007
 */
public class EntityImport extends EntityImportExport {
    protected final static int STATUS_SUCCESS= 0;
    protected final static int STATUS_ERR_UNKNOWN_HOST= -1;
    protected final static int STATUS_ERR_CONNECTION= -2;
    protected final static int STATUS_ERR_FILE= -3;
    protected final static int STATUS_ERR_USER_CANCEL = -4;
    protected final static int STATUS_ERR_SERVER_CANCEL = -5;

    private static final SimpleDateFormat NICE_DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");

    EntityImport() throws InvalidOptionDescriptorException {
        super();
        consoleApplicationDescriptor = new EntityImportOptionDescriptorEnum();
    }

    public static void main(String[] args) {
        try {
            new EntityImport().parseAndExecute(args);
        } catch (Exception e) {
            printException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.EntityImportExport#doAction(com.nextlabs.shared.tools.ICommandLine)
     */
    @Override
    protected int doAction(ICommandLine commandLine) throws ParseException, IOException {
        // finding the conflict solution
        final ConflictResolution conflictResolution;

        if (commandLine.isOptionExist( EntityImportOptionDescriptorEnum.CONFLICT_CANCEL_OPTION)) {
            conflictResolution = ConflictResolution.UNKNOWN;
        } else {
            Collection<OptionId<Boolean>> selectedOptionIds =
                OptionHelper.findTrueOption(
                    EntityImportOptionDescriptorEnum.CONFLICT_SOLUTION_OPTION_IDS, commandLine);

            OptionId<Boolean> selectedOptionId;
            if (selectedOptionIds.isEmpty()) {
                selectedOptionId = EntityImportOptionDescriptorEnum.CONFLICT_RENAME_NEW_OPTION;
            } else if (selectedOptionIds.size() > 1) {
                throw new IllegalArgumentException(
                    "you can't select more than than one conflict solution group."
                + CollectionUtils.asString(selectedOptionIds, ",", "-"));
            }else{
                selectedOptionId = selectedOptionIds.iterator().next();
            }

            if (selectedOptionId == EntityImportOptionDescriptorEnum.CONFLICT_RENAME_NEW_OPTION) {
                conflictResolution = ConflictResolution.RENAME_NEW;
            } else if (selectedOptionId == EntityImportOptionDescriptorEnum.CONFLICT_OVERWRITE_OPTION) {
                conflictResolution = ConflictResolution.KEEP_NEW;
            } else if (selectedOptionId == EntityImportOptionDescriptorEnum.CONFLICT_KEEP_OPTION) {
                conflictResolution = ConflictResolution.KEEP_OLD;
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
					
					System.out.println("Policy Studio must be closed before you import data. If necessary, close Policy Studio before continuing. Continue (Y/N)?");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					String answer = "";
					try {
						answer = br.readLine();
				      } catch (IOException ioe) {
				         System.out.println("I/O error while reading the imput answer");
				         return STATUS_ERR_CONNECTION;
				      }
					if(answer.equals("") || !answer.equalsIgnoreCase("y")){
						return STATUS_ERR_USER_CANCEL;
					}
					                    
                    IImportState importState = importer.doImport(entitesFromFile);
                    if (importState.hasConflicts() && conflictResolution == ConflictResolution.UNKNOWN) {
                        System.out
                            .println("Abort! import state has conflicts and you chose \"cancel on conflict\" "
                                     + EntityImportOptionDescriptorEnum.CONFLICT_CANCEL_OPTION);
                        return STATUS_ERR_USER_CANCEL;
                    }

                    final String renameSuffix = " " + NICE_DATE_TIME_FORMAT.format(new Date());

                    buildResolutions(importState.getConflicts(), conflictResolution, renameSuffix);

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

    /**
     * Build resolutions (i.e. name conflict solutions) for the entities. A conflict solution might be "overwrite" or "drop the entity"
     * or "give it a new name".
     *
     * This last one, for most entities, involves putting a renameSuffix at the end of the entity name. Entity becomes Entity1. For policy
     * exceptions, however, this is more complicated, because their parent policy might also be renamed. So Parent/Exception might become
     * Parent1/Exception1. Assuming the Parent was renamed to Parent1 and not something else.
     *
     * To fix this, we resolve the conflicts in increasing order of path depth. When resolving a path, we'll add the suffix as usual, but
     * we'll also check to see if the prefix has been resolved previously and, if so, rename it.
     */
    private void buildResolutions(Collection<IImportConflict> conflicts, ConflictResolution conflictResolution, String renameSuffix) throws ImportException {
        List<IImportConflict> sortedConflicts = new ArrayList<IImportConflict>(conflicts);
        Collections.sort(sortedConflicts,
                         new ConflictsComparator());

        for (IImportConflict conflict : sortedConflicts) {
            if(conflictResolution == ConflictResolution.RENAME_NEW) {
                ConflictResolution renameSolution =
                    new ConflictResolution(ConflictResolution.ConflictType.RENAME_NEW,
                                           renameConflictingEntity(conflict, sortedConflicts, renameSuffix));
                conflict.setResolution(renameSolution);
            }else{
                conflict.setResolution(conflictResolution);
            }
        }
    }

    private String renameConflictingEntity(IImportConflict conflict, List<IImportConflict> conflicts, String renameSuffix) {
        String newName = conflict.getName();

        // Look for the longest handled conflict whose name matches the first part of our name
        IImportConflict longestMatch = null;

        for (IImportConflict candidate : conflicts) {
            if (candidate.isResolved() && newName.startsWith(candidate.getName() + "/")) {
                longestMatch = candidate;
            }
        }

        if (longestMatch != null) {
            String replacementString = longestMatch.getResolution().getNewName();

            // Replace the prefix with the new name...
            newName = longestMatch.getResolution().getNewName() + newName.substring(longestMatch.getName().length());
        }

        // And add our own suffix
        return newName + renameSuffix;
    }

    private static class ConflictsComparator<T extends IImportConflict> implements Comparator<T> {
        public int compare(T c1, T c2) {
            int d1 = depth(c1);
            int d2 = depth(c2);

            if (d1 == d2) {
                return c1.getName().compareTo(c2.getName());
            }

            return d1-d2;
        }

        private int depth(T c) {
            return countOccurences(c.getName(), '/');
        }
        
        private static int countOccurences(String haystack, char needle) {
            int count = 0;

            if (haystack != null) {
                for (int i = 0; i < haystack.length(); i++) {
                    if (haystack.charAt(i) == needle) {
                        count++;
                    }
                }
            }

            return count;
        }

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
