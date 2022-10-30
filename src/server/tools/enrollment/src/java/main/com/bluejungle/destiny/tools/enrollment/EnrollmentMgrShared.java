package com.bluejungle.destiny.tools.enrollment;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;
import org.apache.axis.types.URI;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault;
import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.services.enrollment.EnrollmentIF;
import com.bluejungle.destiny.services.enrollment.EnrollmentServiceLocator;
import com.bluejungle.destiny.services.enrollment.types.Realm;
import com.bluejungle.destiny.services.enrollment.types.RealmList;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.SecureConsolePrompt;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * Shared between EnrollmentMgr and PropertyMgr
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public abstract class EnrollmentMgrShared extends ConsoleApplicationBase{
    /*
     * Welcome message
     */
    public static final String WELCOME =
        "\n--------------------------------------------------------------\n"
        +"                    Enrollment Tool\n"
        +"--------------------------------------------------------------\n";
    
    /**
     * Constructor
     */
    protected EnrollmentMgrShared(){
        System.setProperty(SOCKET_FACTORY_PROPERTY, EnrollmentMgrSocketFactory.class
                .getName());
    }


    private static final int REQUEST_TIMEOUT = Integer.parseInt(
            System.getProperty("com.bluejungle.agent.sockettimeout", "1200000"));

    /*
     * Enrollment tool home
     */
    public static final String ENROLLMENT_TOOL_HOME = "ENROLL_TOOL_HOME";
    
    public static final String ENROLL_TOOL_KEYSTORE_PASSWORD = "ENROLL_TOOL_KEYSTORE_PASSWORD";    		

    protected static final String SOCKET_FACTORY_PROPERTY = "axis.socketSecureFactory";
    
    private static final String URL = "https://%s:%d/dem/services/EnrollmentIFPort";

    private EnrollmentServiceLocator locator;
//    protected EnrollmentIF enrollmentService;
    protected EnrollmentServiceWrapper enrollmentWS;

    protected void authenticate(ICommandLine commandLine) throws ServiceException, IOException {
        String server = getValue(commandLine,
                IConsoleApplicationDescriptor.HOST_OPTION_ID);
        Integer port = getValue(commandLine,
                EnrollmentMgrOptionDescriptorEnum.PORT_OPTION_ID);
        String authUser = getValue(commandLine,
                EnrollmentMgrOptionDescriptorEnum.USER_ID_OPTION_ID);
        String authPwd = getValue(commandLine,
                EnrollmentMgrOptionDescriptorEnum.PASSWORD_OPTION_ID);
        
        String keyStorePassword = getValue(commandLine,
        		EnrollmentMgrOptionDescriptorEnum.KEYSTORE_PASSWORD_OPTION_ID);
        System.setProperty(ENROLL_TOOL_KEYSTORE_PASSWORD, keyStorePassword);

        if (authPwd == null) {
            SecureConsolePrompt securePrompt = new SecureConsolePrompt("Password: ");
            authPwd = new String(securePrompt.readConsoleSecure());
        }

        // Set authentication information in SOAP envelope header
        SecureSessionVaultGateway.setSecureSessionVault(new ISecureSessionVault(){
            private SecureSession secureSession;

            public void storeSecureSession(SecureSession secureSession) {
                this.secureSession = secureSession;
            }

            public SecureSession getSecureSession() {
                return secureSession;
            }

            public void clearSecureSession() {
                secureSession = null;
            }
        });
        AuthenticationContext authContext = AuthenticationContext.getCurrentContext();
        authContext.setUsername(authUser);
        authContext.setPassword(authPwd);

        String url = String.format(URL, server, port);

        this.locator = new EnrollmentServiceLocator();
        this.locator.setEnrollmentIFPortEndpointAddress(url);
        EnrollmentIF enrollmentService = locator.getEnrollmentIFPort();
        ((Stub) enrollmentService).setTimeout(REQUEST_TIMEOUT);
        enrollmentWS = new EnrollmentServiceWrapper(enrollmentService);
    }
    
    
    protected Realm getTheOnlyRealm(String name) throws EnrollmentMgrException{
        RealmList existingRealms = this.enrollmentWS.getRealms(name);
        if (existingRealms == null || existingRealms.getRealms() == null
                || existingRealms.getRealms().length == 0) {
            // No realm exists with the given name
            throw new EnrollmentMgrException(new EntryNotFoundException("enrollment", name).getMessage());
        } else if (existingRealms.getRealms().length != 1) {
            // We should only retrieve one realm:
            throw new EnrollmentMgrException("More than one realm exist with same name, '" + name + "'. Please contact Administrator.");
        }

        return existingRealms.getRealms()[0];
    }
    
    protected abstract void exec(ICommandLine commandLine) throws Exception;
    
    @Override
    protected void execute(ICommandLine commandLine) {
        try {
            exec(commandLine);
        } catch (EnrollmentMgrException e) {
            printExceptionEnrollManager(e.getMessage(), e);
        } catch (FileFormatException e) {
            printExceptionEnrollManager(e.getMessage() + "  Check configuration.", e);
        } catch (FileNotFoundException e) {
            printExceptionEnrollManager(e.getMessage() + "  Check the parameter.", e);
        } catch (ServiceException e) {
            printExceptionEnrollManager("Service error", e);
        //before IO exception
        } catch (URI.MalformedURIException e) {
            printExceptionEnrollManager("MalformedURI ", e);
        //start IO Exception
        } catch (IOException e) {
            printExceptionEnrollManager(e.getMessage(), e);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    protected void handleException(Exception e){
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Enrollment error: ");
        errorMessage.append(e.getMessage() != null ? e.getMessage() : e.toString());
        if (e.getCause() != null) {
            errorMessage.append(ConsoleDisplayHelper.NEWLINE).append(e.getCause());
        }
        System.err.println(errorMessage);
    }
    
    
    protected void printExceptionEnrollManager(String message, Throwable t){
        System.err.println (message);
    }
}
