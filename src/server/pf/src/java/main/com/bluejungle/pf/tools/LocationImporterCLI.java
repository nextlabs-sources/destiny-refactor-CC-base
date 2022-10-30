package com.bluejungle.pf.tools;

import java.io.BufferedReader;
import java.io.FileReader;

import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.SecureConsolePrompt;

import static com.bluejungle.pf.tools.LocationImporterOptionDescriptorEnum.*;

/**
 * TODO description
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public class LocationImporterCLI extends ConsoleApplicationBase {
	private static final int RETURN_STATUS_SUCCESS = 0;
	private static final int RETURN_STATUS_ERR = -1;
	private static final int RETURN_STATUS_UNKNOWN = RETURN_STATUS_ERR;
	
	private final IConsoleApplicationDescriptor consoleDescriptor;
	private int returnStatus;
	
	public LocationImporterCLI() throws InvalidOptionDescriptorException{
		consoleDescriptor = new LocationImporterOptionDescriptorEnum();
	}
	
	public static void main(String[] args) {
		try {
			LocationImporterCLI locationImporter = new LocationImporterCLI();
			locationImporter.parseAndExecute(args);
			switch(locationImporter.returnStatus){
				case RETURN_STATUS_SUCCESS:
					System.out.println("SUCCESS");
					break;
				case RETURN_STATUS_ERR:
					System.out.println("ERROR");
					break;
				default:
					System.out.println("unknown");	
			}
			System.exit(locationImporter.returnStatus);
		} catch (Exception e) {
			printException(e);
			System.exit(RETURN_STATUS_UNKNOWN);
		}
	}
	
	
	//make the function visible for test 
	@Override
	protected void parseAndExecute(String[] args) throws ParseException {
		super.parseAndExecute(args);
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#execute(com.nextlabs.shared.tools.ICommandLine)
	 */
	@Override
	protected void execute(ICommandLine commandLine) {
		try {
			boolean verbose = getValue(commandLine,	VERBOSE_OPTION_ID);

			String fileName = getValue(commandLine, LOCATIONS_OPTION_ID);

			String userName = getValue(commandLine, USER_ID_OPTION_ID);

			String password = getValue(commandLine, PASSWORD_OPTION_ID);
			if (password == null) {
				SecureConsolePrompt securePrompt = new SecureConsolePrompt("Password: ");
				password = new String(securePrompt.readConsoleSecure());
			}
			
			String database = getValue(commandLine, DATABASE_OPTION_ID);
			
			LocationImporter importer = new LocationImporter();
			if(commandLine.isOptionExist(JDBC_URL_OPTION_ID)){
			    String jdbcUrl = getValue(commandLine, JDBC_URL_OPTION_ID);
			    importer.setupDatasource(userName, password, database, jdbcUrl);
			} else {
			    String host = getValue(commandLine, HOST_OPTION_ID);
	            String instance = getValue(commandLine, INSTANCE_OPTION_ID);
	            int port = getValue(commandLine, PORT_OPTION_ID);
	            importer.setupDatasource(userName, host, password, instance, port, database);
			}
			
			importer.importLocations(new BufferedReader(new FileReader(fileName)), verbose);
			returnStatus = RETURN_STATUS_SUCCESS;
		} catch (Exception e) {
			returnStatus = RETURN_STATUS_ERR;
			printException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#getDescriptor()
	 */
	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return consoleDescriptor;
	}
}
