// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

import java.io.File;

import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * 
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/OSWrapper.java#1 $:
 */
/**
 * @author fuad
 */
public class OSWrapper extends ComponentImplBase implements IOSWrapper, IHasComponentInfo<OSWrapper> {

    public static final String NAME = OSWrapper.class.getName();

    private static final ComponentInfo<OSWrapper> COMP_INFO = new ComponentInfo<OSWrapper>(
    		NAME, 
    		OSWrapper.class, 
    		IOSWrapper.class, 
    		LifestyleType.SINGLETON_TYPE);

    private static final String PDPJNI_LIBRARY_NAME = "pdpjni";
    private static final String IPCJNI_LIBRARY_NAME = "ipcjni";

    /**
     * @returns whether or not this implementation of the OSWrapper is a stub. This
     * one is real
     */
    public boolean isStub() {
        return false;
    }
    
    /**
     * @return ComponentInfo to help creating an instance with Component Manager
     * 
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<OSWrapper> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#createFileMapping(java.lang.String,
     *      int)
     */
    public native long createFileMapping(String name, int size);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#hashChallenge(java.lang.String)
     */
    public native String hashChallenge(String challenge);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#openFileMapping(java.lang.String)
     */
    public native long openFileMapping(String name);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#mapViewOfFile(long)
     */
    public native long mapViewOfFile(long handle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#unmapViewOfFile(long)
     */
    public native boolean unmapViewOfFile(long handle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#createEvent(java.lang.String)
     */
    public native long createEvent(String name);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#openEvent(java.lang.String)
     */
    public native long openEvent(String name);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setEvent(int)
     */
    public native boolean setEvent(long handle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#createMutex(java.lang.String)
     */
    public native long createMutex(String name);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#openMutex(java.lang.String)
     */
    public native long openMutex(String name);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#releaseMutex(int)
     */
    public native boolean releaseMutex(long handle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#waitForSingleObject(int)
     */
    public native int waitForSingleObject(long handle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#waitForMultipleObjects(int,
     *      long[], string[], int)
     */
    public native int waitForMultipleObjects(int count, long[] handles, String[] names, int timeout);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#readString(long)
     */
    public native String readString(long handle);

    public native String[] readIPCResponse(long requestSharedMem);

    public native String[] readIPCRequest(long requestSharedMem);

    public native String[] readPolicyIPCRequest(long requestSharedMem);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#writeString(long,
     *      java.lang.String)
     */
    public native void writeString(long handle, String str);

    public native void writeIPCRequest(long requestSharedMem, Object[] inputParams);

    public native void writeIPCResponse(long requestSharedMem, Object[] outputParams);

    public native void writePolicyIPCResponse(long requestSharedMem, Object[] outputParams);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#closeHandle(long)
     */
    public native boolean closeHandle(long handle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getProcessToken()
     */
    public native long getProcessToken(int pid);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#closeProcessToken()
     */
    public native boolean closeProcessToken(Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getProcessId()
     */
    public native int getProcessId();

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getRDPAddress()
     */
    public native long getRDPAddress(int processId);
    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#logEvent(int, java.lang.Object[])
     */
    public native void logEvent(int type, int eventId, Object[] params);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setupKernelIPC(int, long[])
     */
    public native int setupKernelIPC(int numberOfSlots, long[] eventArray);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#uninitKernelIPC(long)
     */
    public native void uninitKernelIPC(long ipcSetupInfoHandle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setupSocket(long)
     */
    public native long setupSocket(int socketType);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#shutdownSocket(long)
     */
    public native void shutdownSocket(long socketHandle);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getLoggedInUsers()
     */
    public native String[] getLoggedInUsers();

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileCreateTime()
     */
    public native long getFileCreateTime(String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileModifiedTime()
     */
    public native long getFileModifiedTime(String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileAccessTime()
     */
    public native long getFileAccessTime(String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileCustomAttributes()
     */
    public native String [] getFileCustomAttributes (String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileStandardAttributes()
     */
    public native String [] getFileStandardAttributes (String fileName, int agentType, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileBasicAttributes()
     */
    public native long [] getFileBasicAttributes (String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileOwnerSID()
     */
    public native String getOwnerSID(String fileName, int agentType, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileGroupSID()
     */
    public native String getGroupSID(String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getAppInfo()
     */
    public synchronized native String getAppInfo(String appName);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFQDN()
     */
    public native String getFQDN();

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getSharePhysicalPath(java.lang.String)
     */
    public native String getSharePhysicalPath(String shareName);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getMyDocumentsFolder()
     */
    public native String getMyDocumentsFolder();

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getMyDesktopFolder()
     */
    public native String getMyDesktopFolder();

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getNextKernelPolicyRequest(long[])
     */
    public native Object[] getNextKernelPolicyRequest(long[] kernelEventArray);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#isRemovableMedia (String fileName)
     */
    public native boolean isRemovableMedia(String fileName);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getUserName(java.lang.String)
     */
    public native String getUserName(String userSid);
    

    private static boolean checkAndLoad(String libName) {
        if (new File(libName).isFile()) {
            try {
                System.load(libName);
                return true;
            } catch (UnsatisfiedLinkError ule) {
                
            }
        }

        return false;
    }

    private static boolean basicLoadLibrary(String libName) {
        try {
            System.loadLibrary(libName);
            return true;
        } catch (UnsatisfiedLinkError ule) {
        }

        return false;
    }

    private static boolean loadLibrary64or32(String path, String libName) {
        if (path.equals("")) {
            if (basicLoadLibrary(libName)) {
                return true;
            }
        } else {
            if (checkAndLoad(path+System.mapLibraryName(libName))) {
                return true;
            }
        }

        libName = libName + "32";

        if (path.equals("")) {
            return basicLoadLibrary(libName);
        } else {
            return checkAndLoad(path+System.mapLibraryName(libName));
        }
    }

    private static boolean loadLibrary64or32(String libName) {
        return loadLibrary64or32("", libName);
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        boolean loaded = false;
        String currentDir = System.getProperty("user.dir");
        if ((currentDir != null) && new File(currentDir).isDirectory()) {
            String workingDirectory = currentDir + File.separatorChar + "bin" + File.separatorChar;

            // Let's use the full path to the library
            loaded = loadLibrary64or32(workingDirectory, PDPJNI_LIBRARY_NAME);
            loadLibrary64or32(workingDirectory, IPCJNI_LIBRARY_NAME);
        }

        if (!loaded) {
            // As a last resort let's use loadLibrary
            loadLibrary64or32(PDPJNI_LIBRARY_NAME);
            loadLibrary64or32(IPCJNI_LIBRARY_NAME);
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    
    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setKernelPolicyResponse(int, long,
     *      long, long)
     */
    public native boolean setKernelPolicyResponse(long handle, long uniqueNumber, long allow, long allowType);

    

    //function for SDK
    public native boolean sendPolicyResponse(long handle, String uniqueNumber, long allow, String[] attrs);
   
    
    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {

    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {

    }

    public boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.equals("Linux");
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#isEmptyDirectory(String, object)
     */
    public native boolean isEmptyDirectory(String fileName, Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getContentAnalysisAttributes(String, int, Object, String, int, String[])
     */
    public native int[] getContentAnalysisAttributes(String filename, int pid, Object opaque, String userSid, int level, String[] caDescriptions);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getNetworkDiskResources(Object)
     */
    public native String[] getNetworkDiskResources(Object opaque);

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getPathFromPID(int)
     */
    public native String getPathFromPID(int pid);
}
