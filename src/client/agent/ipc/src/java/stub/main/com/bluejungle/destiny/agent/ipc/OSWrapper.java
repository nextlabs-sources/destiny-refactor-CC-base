/*
 * Created on May 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/stub/main/com/bluejungle/destiny/agent/ipc/OSWrapper.java#1 $:
 */

package com.bluejungle.destiny.agent.ipc;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

public class OSWrapper extends ComponentImplBase implements IOSWrapper, IHasComponentInfo<OSWrapper> {
    public static final String NAME = OSWrapper.class.getName();

    private static final ComponentInfo<OSWrapper> COMP_INFO = new ComponentInfo<OSWrapper>(
    		NAME, 
    		OSWrapper.class, 
    		IOSWrapper.class, 
    		LifestyleType.SINGLETON_TYPE);

    /**
     * @return ComponentInfo to help creating an instance with Component Manager
     * 
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<OSWrapper> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @returns whether or not this implementation of the OSWrapper is a stub. This
     * one is a stub
     */
    public boolean isStub() {
        return true;
    }
    
    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#createFileMapping(java.lang.String,
     *      int)
     */
    public long createFileMapping(String name, int size) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#hashChallenge(java.lang.String)
     */
    public String hashChallenge(String challenge) { return challenge; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#openFileMapping(java.lang.String)
     */
    public long openFileMapping(String name) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#mapViewOfFile(long)
     */
    public long mapViewOfFile(long handle) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#unmapViewOfFile(long)
     */
    public boolean unmapViewOfFile(long handle) { return true; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#createEvent(java.lang.String)
     */
    public long createEvent(String name) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#openEvent(java.lang.String)
     */
    public long openEvent(String name) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setEvent(long)
     */
    public boolean setEvent(long handle) { return true; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#createMutex(java.lang.String)
     */
    public long createMutex(String name) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#openMutex(java.lang.String)
     */
    public long openMutex(String name) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#releaseMutex(int)
     */
    public boolean releaseMutex(long handle) { return true;} ;

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#waitForSingleObject(int)
     */
    public int waitForSingleObject(long handle) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#waitForMultipleObjects(int,
     *      int[], string[], int)
     */
    public int waitForMultipleObjects(int count, long[] handles, String[] names, int timeout) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#readString(int)
     */
    public String readString(long handle) { return null; };

    public String[] readIPCResponse(long requestSharedMem) { return null; };

    public String[] readIPCRequest(long requestSharedMem) { return null; };

    public String[] readPolicyIPCRequest(long requestSharedMem) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#writeString(int,
     *      java.lang.String)
     */
    public void writeString(long handle, String str) {};

    public void writeIPCRequest(long requestSharedMem, Object[] inputParams) {};

    public void writeIPCResponse(long requestSharedMem, Object[] outputParams) {};

    public void writePolicyIPCResponse(long requestSharedMem, Object[] outputParams) {};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#closeHandle(int)
     */
    public boolean closeHandle(long handle) {return true;};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getProcesstoken()
     */
    public long getProcessToken(int pid) { return 0;};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#closeProcessToken()
     */
    public boolean closeProcessToken(Object opaque) { return true;};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getProcessId()
     */
    public int getProcessId() { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getRDPAddress()
     */
    public long getRDPAddress(int processId) { return 0; };
    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#logEvent(int,
     *      java.lang.Object[])
     */
    public void logEvent(int type, int eventId, Object[] params) {};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setupKernelIPC(int,
     *      int[])
     */
    public int setupKernelIPC(int numberOfSlots, long[] eventArray) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#uninitKernelIPC(int)
     */
    public void uninitKernelIPC(long ipcSetupInfoHandle) {};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setupSocket(long)
     */
    public long setupSocket(int socketType) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#shutdownSocket(int)
     */
    public void shutdownSocket(long socketHandle) {};

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getLoggedInUsers()
     */
    public String[] getLoggedInUsers() {return new String[]{}; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileCreateTime()
     */
    public long getFileCreateTime(String fileName, Object opaque) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileModifiedTime()
     */
    public long getFileModifiedTime(String fileName, Object opaque) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileAccessTime()
     */
    public long getFileAccessTime(String fileName, Object opaque) { return 0; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileCustomAttributes()
     */
    public String [] getFileCustomAttributes (String fileName, Object opaque) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileStandardAttributes()
     */
    public String [] getFileStandardAttributes (String fileName, int agentType, Object opaque) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileBasicAttributes()
     */
    public long [] getFileBasicAttributes (String fileName, Object opaque) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileOwnerSID()
     */
    public String getOwnerSID(String fileName, int agentType, Object opaque) { return "nobody"; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFileGroupSID()
     */
    public String getGroupSID(String fileName, Object opaque) { return "nobody"; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getAppInfo()
     */
    public synchronized String getAppInfo(String appName) { return "no app info"; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getFQDN()
     */
    public String getFQDN() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            return "";
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getSharePhysicalPath(java.lang.String)
     */
    public String getSharePhysicalPath(String shareName) { return shareName; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getMyDocumentsFolder()
     */
    public String getMyDocumentsFolder() { return "/root/Documents"; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getMyDesktopFolder()
     */
    public String getMyDesktopFolder() { return "/root/Desktop"; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getNextKernelPolicyRequest(int[])
     */
    public Object[] getNextKernelPolicyRequest(long[] kernelEventArray) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#setKernelPolicyResponse(int, long,
     *      long, long)
     */
    public boolean setKernelPolicyResponse(long handle, long uniqueNumber, long allow, long allowType) { return true; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#sendPolicyResponse(long, long, long, long)
     */
    public boolean sendPolicyResponse(long handle, String uniqueNumber, long allow, String[] attrs) { return true; };
   

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#isRemovableMedia (String fileName)
     */
    public boolean isRemovableMedia(String fileName) { return false; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getUserName(java.lang.String)
     */
    public String getUserName(String userSid) { return userSid; };
    
    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#isLinux()
     */
    public boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.equals("Linux");
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#isEmptyDirectory(String, object)
     */
    public boolean isEmptyDirectory(String fileName, Object opaque) { return false; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getContentAnalysisAttributes(String, int, Object, String, int, String[])
     */
    public int[] getContentAnalysisAttributes(String filename, int pid, Object opaque, String userSid, int level, String[] caDescriptions) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getNetworkDiskResources(Object)
     */
    public String[] getNetworkDiskResources(Object opaque) { return null; };

    /**
     * @see com.bluejungle.destiny.agent.ipc.IOSWrapper#getPathFromPID(int)
     */
    public String getPathFromPID(int pid) { return null; };

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

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

}
