package com.nextlabs.shared.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.nextlabs.shared.tools.impl.InteractiveQuestion;
import com.nextlabs.shared.tools.impl.OptionHelper;
import com.nextlabs.pf.destiny.importexport.ExportException;
import com.nextlabs.pf.destiny.importexport.IExporter;
import com.nextlabs.pf.destiny.importexport.impl.UIExporter;

/**
 * provide the main functionality of the export tools
 * 
 * @author hchan
 * @date Mar 29, 2007
 */
public class EntityExport extends EntityImportExport {
	protected final static int	STATUS_SUCCESS			= 0;
	protected final static int	STATUS_ERR_UNKNOWN_HOST	= -1;
	protected final static int	STATUS_ERR_CONNECTION	= -2;
	protected final static int	STATUS_ERR_TARGET		= -3;
	protected final static int	STATUS_ERR_FILE			= -4;

	/**
	 * @throws InvalidOptionDescriptorException
	 */
	EntityExport() throws InvalidOptionDescriptorException {
		super();
		consoleApplicationDescriptor = new EntityExportOptionDescriptorEnum();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new EntityExport().parseAndExecute(args);
		} catch (Exception e) {
			printException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.EntityImportExport#doAction(com.nextlabs.shared.tools.ICommandLine)
	 */
	protected int doAction(ICommandLine commandLine) throws ParseException, PolicyEditorException,
			IOException {
		IPolicyEditorClient client;
		try {
			client = createPolicyEditorClient(commandLine);
			client.login();
		} catch (LoginException e) {
			printException(e);
			return STATUS_ERR_CONNECTION;
		}

		Boolean isOverWrite;
		
		Collection<OptionId<Boolean>> selectedOptionIds =
				OptionHelper.findTrueOption(
						EntityExportOptionDescriptorEnum.OVERWRITES_OPTION_IDS, commandLine);
		
		if (selectedOptionIds.size() > 1) {
			throw new IllegalArgumentException(
					"you can't select more one overwrite option."
							+ CollectionUtils.asString(selectedOptionIds, ",", "-"));
		} 
			
		OptionId<Boolean> selectedOptionId;
		if (selectedOptionIds.size() == 0) {
			selectedOptionId = null;
		} else {
			selectedOptionId = selectedOptionIds.iterator().next();
		}
		
		if (selectedOptionId == EntityExportOptionDescriptorEnum.ABORT_OPTION) {
			isOverWrite = false;
		} else if (selectedOptionId == EntityExportOptionDescriptorEnum.OVERWRITE_OPTION) {
			isOverWrite = true;
		} else{
			isOverWrite = null;
		}

		Collection<DomainObjectDescriptor> domainObjCollection = getDomainObjectDescriptors(commandLine, client);

		if(domainObjCollection.size() ==0){
			System.out.println("WARNING: You have no policies to export.");
		}
		
		try {
			if (getValue(commandLine, EntityExportOptionDescriptorEnum.LISR_ALL_OPTION)) {
				printList(domainObjCollection);
			}
			//filename could be null because the user may want to see the output in console only.
			String filename = getValue(commandLine, EntityExportOptionDescriptorEnum.FILE_OPTION);
			if (filename != null) {
				File file = new File(filename);
				if (file.exists()) {
					//if the file exists, ask the user if he wants to overwrite.
					if (isOverWrite == null) {
						InteractiveQuestion q = new InteractiveQuestion("Are you sure to overwrite \"" + filename
								+ "\"? [y/n]");
						isOverWrite = StringUtils.stringToBoolean(q.prompt(), false);
					}
					
					if(!isOverWrite){
						System.err.println("file \"" + filename + "\" already exists. And you choice not to overwrite");
						return STATUS_ERR_FILE;
					}else{
						//rename the overwrite file to a new file
						//filename + //d+ + extension
						int i = 1;
						File newFile;
						do{
							final int dotIndex = filename.lastIndexOf('.');
							String newFilename = dotIndex == -1 ? 
									filename + i : //if filename doesn't have extension
									filename.substring(0, dotIndex) + i + filename.substring(dotIndex)	;
							newFile = new File(newFilename);
							i++;
						}while(newFile.exists());
						
						if(file.renameTo(newFile)){
							System.out.println("Your old file renamed to " + newFile);
						}
						file = new File(filename);
					}
				} else {
					//does the folder exist?
					File folder = file.getAbsoluteFile().getParentFile();
					final String folderName = folder.getAbsolutePath();
					
					if (!folder.exists()) {
						if (isOverWrite == null) {
							InteractiveQuestion q = new InteractiveQuestion(
									"The output folder does not exist."
											+ " Do you want to create the folder? \""
											+ folderName + "\"? [y/n]");
							if (!StringUtils.stringToBoolean(q.prompt(), true)) {
								System.err.println("The output folder \""
										+ folderName + "\" is not created. Abort.");
								return STATUS_ERR_FILE;
							}
						} else if (isOverWrite == false) {
							System.err.println("The output folder \""
									+ folderName + "\" needs to be created before export.");
							return STATUS_ERR_FILE;
						}

						if (!folder.mkdirs()) {
							//problem on creating a folder.
							System.err.println("The output folder \"" + folderName
									+ "\" can not be created. Abort.");
							return STATUS_ERR_FILE;
						}
					} else if (folder.isFile() && folder.exists()) {
						System.err.println("The output folder \"" + folderName
								+ "\" already exists with the same filename."
								+ "You need to choose the other folder. " 
								+ "Or rename that file which is the same name.");
						return STATUS_ERR_FILE;
					}
				}
				IExporter exporter = new UIExporter();
				Collection<DomainObjectDescriptor> requiredComponents = exporter.prepareForExport(domainObjCollection);
				exporter.executeExport(file);
			}
		} catch (ExportException e) {
			printException(e);
			return STATUS_ERR_TARGET;
		}

		return STATUS_SUCCESS;
	}

	private void printList(Collection<DomainObjectDescriptor> domainObjCollection) {
		for (DomainObjectDescriptor dod : domainObjCollection) {
			System.out.println(dod.getName());
		}
	}
	
	/**
	 * what is the hidden thing again?
	 */
	private static final boolean INCLUDE_HIDDEN	= false;

	/**
	 * create Collection of DomainObjectDescriptor depends on the commandLine arguments
	 * @param commandLine
	 * @param client
	 * @return
	 * @throws PolicyEditorException
	 */
	private Collection<DomainObjectDescriptor> getDomainObjectDescriptors(ICommandLine commandLine,
			IPolicyEditorClient client) throws PolicyEditorException {
		Collection<DomainObjectDescriptor> domainObjCollection = new ArrayList<DomainObjectDescriptor>();

		//if INCLUDE_ALL_OPTION is selected or none of option is selected, action = INCLUDE_ALL_OPTION
		if (commandLine.isOptionExist( EntityExportOptionDescriptorEnum.INCLUDE_ALL_OPTION)) {
			domainObjCollection.addAll(client.getDescriptorsForNameAndType("%", EntityType.POLICY, false));
		} else if (!commandLine.isOptionExist( EntityExportOptionDescriptorEnum.INCLUDE_DIR_OPTION)
				&& !commandLine.isOptionExist( EntityExportOptionDescriptorEnum.INCLUDE_DIR_R_OPTION)
				&& !commandLine.isOptionExist( EntityExportOptionDescriptorEnum.INCLUDE_PATH_NAME_OPTION)) {
			domainObjCollection.addAll(client.getDescriptorsForNameAndType("%", EntityType.POLICY, false));
		} else {
			if (commandLine.isOptionExist( EntityExportOptionDescriptorEnum.INCLUDE_DIR_OPTION)) {
				List<String> values = commandLine.getParsedValues(EntityExportOptionDescriptorEnum.INCLUDE_DIR_OPTION);
				for (String value : values) {
					String name = convertFolderNameToOurType(value);
					int level = StringUtils.count(name, '/');
					//the policy client can only get the descirptors recursively.
					//segery suggests to count the '/' to determine the descriptor is subfolder or not.
					Collection<DomainObjectDescriptor> descriptors = client.getDescriptorsForNameAndType(name,
							EntityType.POLICY, INCLUDE_HIDDEN);
					for (DomainObjectDescriptor descriptor : descriptors) {
						int currentDescriptorLevel = StringUtils.count(descriptor.getName(), '/');
						if (currentDescriptorLevel == level) {
							domainObjCollection.add(descriptor);
						}
					}
				}
			}

			if (commandLine.isOptionExist(EntityExportOptionDescriptorEnum.INCLUDE_DIR_R_OPTION)) {
				List<String> values = commandLine.getParsedValues(EntityExportOptionDescriptorEnum.INCLUDE_DIR_R_OPTION);
				for (String value : values) {
					String name = convertFolderNameToOurType(value);
					domainObjCollection.addAll(client.getDescriptorsForNameAndType(name, EntityType.POLICY,
							INCLUDE_HIDDEN));
				}
			}

			if (commandLine.isOptionExist(EntityExportOptionDescriptorEnum.INCLUDE_PATH_NAME_OPTION)) {
				List<String> values = commandLine.getParsedValues(EntityExportOptionDescriptorEnum.INCLUDE_PATH_NAME_OPTION);
				for (String name : values) {
					name = name.replace('\\', '/');
//					if(DEBUG_MODE){
//						if(name.contains("\\")){
//							System.out.println("DEBUG: you have '\\' in your value. Maybe you mean '/'");
//						}
//					}
					domainObjCollection.addAll(client.getDescriptorsForNameAndType(name, EntityType.POLICY,
							INCLUDE_HIDDEN));
				}
			}
		}
		return domainObjCollection;
	}

	/**
	 * convert the user input folder name to a folder name that domainOjbCollection can accept.
	 * @param value
	 * @return
	 */
	private String convertFolderNameToOurType(String name) {
		name = name.replace('\\', '/');
		name += name.endsWith("/") ? "%" : "/%";
		return name;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.EntityImportExport#getReturnMessage(int)
	 */
	protected String getReturnMessage(int returnCode) {
		switch (returnCode) {
		case STATUS_SUCCESS:
			return "Export successful";
		case STATUS_ERR_UNKNOWN_HOST:
			return "Unknown host";
		case STATUS_ERR_CONNECTION:
			return "Connection refused. Check the credentials and try again.";
		case STATUS_ERR_TARGET:
			return "Target not found, export aborted.";
		case STATUS_ERR_FILE:
			return "Cannot create export file. Check for disk full, or inadequate permissions. Export aborted.";
                case STATUS_ERR_UNKNOWN_EXCEPTION_THROWN:
                        return "Exception thrown when exporting";
		default:
			throw new IllegalArgumentException( "Unknown Return Code (" + returnCode + "), bad bad bad");
		}
	}
}
