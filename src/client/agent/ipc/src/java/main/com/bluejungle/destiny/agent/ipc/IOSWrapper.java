// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

/**
 * IOSWrapper exposes methods that will be implemented using JNI to call native
 * OS calls.
 * 
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/IOSWrapper.java#1 $:
 *  
 */
public interface IOSWrapper {
    /**
     * Enumeration of the Standard Attributes 
     *
     */
    
    public static final String CE_ATTR_CREATE_TIME     = "CE_ATTR_CREATE_TIME";
    public static final String CE_ATTR_LASTACCESS_TIME = "CE_ATTR_LASTACCESS_TIME";
    public static final String CE_ATTR_LASTWRITE_TIME  = "CE_ATTR_LASTWRITE_TIME";
    public static final String CE_ATTR_OWNER_NAME      = "CE_ATTR_OWNER_NAME";
    public static final String CE_ATTR_OWNER_ID        = "CE_ATTR_OWNER_ID";
    public static final String CE_ATTR_GROUP_ID        = "CE_ATTR_GROUP_ID";
    
    /**
     * @return true if this is the stub version of OSwrapper
     */
    public boolean isStub();
    
    /**
     * @param name
     *            name of memory mapped file
     * @param size
     *            size in bytes to allocate for memory mapped file
     * @return handle to newly created memory mapped file with the specified
     *         name and size
     */
    public long createFileMapping(String name, int size);

    /**
     * Returns the hashed version of the challenge
     * 
     * @param challenge
     *            challenge to hash
     * @return the hashed version of the challenge
     */
    public String hashChallenge(String challenge);

    /**
     * @param name
     *            name of memory mapped file
     * @return handle for memory mapped file with the specified name
     */
    public long openFileMapping(String name);

    /**
     * @param handle
     *            handle of file mapping previously returned from a call to
     *            createFileMapping or openFileMapping
     * @return mapped view of the file corresponding to the specified handle
     */
    public long mapViewOfFile(long handle);

    /**
     * @param handle
     *            handle for mapped view of file previously returned from a call
     *            to MapViewOfFile
     * @return true if unmapping is successful
     */
    public boolean unmapViewOfFile(long handle);

    /**
     * @param name
     *            name of Event
     * @return handle to newly created event
     */
    public long createEvent(String name);

    /**
     * @param name
     *            name of Event
     * @return handle to specified event.
     */
    public long openEvent(String name);

    /**
     * @param handle
     *            event handle
     * @return true if the event was successfully signaled
     * 
     * Signals the specified event
     */
    public boolean setEvent(long handle);

    /**
     * @param name
     *            name of mutex to create
     * @return handle to newly created mutex
     */
    public long createMutex(String name);

    /**
     * @param name
     *            name of mutex to open
     * @return handle of opened mutex
     */
    public long openMutex(String name);

    /**
     * @param handle
     *            handle of mutex
     * @return true if release is successful
     */
    public boolean releaseMutex(long handle);

    /**
     * @param handle
     * @return wait for object specified by the handle to be signaled
     */
    public int waitForSingleObject(long handle);

    /**
     * @param count
     *            number of objects
     * @param handles
     *            array of handles
     * @param names
     *            array of names of handles
     * @param timeout
     *            timeout for wait
     * @return handle of object that was signaled
     * 
     * this method waits till one of the events in the handle array is signaled.  The names are
     * used to determine if the event exists (if it doesn't, that indicates that the process has
     * disappeared).
     */
    public int waitForMultipleObjects(int count, long[] handles, String[] names, int timeout);

    /**
     * @param handle
     *            handle for shared memory location
     * @return string from shared memory location
     */
    public String readString(long handle);

    public String[] readIPCResponse(long requestSharedMem);

    public String[] readIPCRequest(long requestSharedMem);

    /**
     * Read an IPC policy request from a slot in the shared memory
     * 
     * @param requestSharedMem
     *            handle to the slot in shared memory
     * @return a string array containing each argument of the policy IPC request
     */
    public String[] readPolicyIPCRequest(long requestSharedMem);

    /**
     * @param handle
     *            handle for shared memory location
     * @param str
     *            writes string to shared memory location specified by handle
     */
    public void writeString(long handle, String str);

    public void writeIPCRequest(long requestSharedMem, Object[] inputParams);

    public void writeIPCResponse(long requestSharedMem, Object[] outputParams);

    public void writePolicyIPCResponse(long requestSharedMem, Object[] outputParams);

    /**
     * @param handle
     *            handle of object to close
     * @return true if call is successful. false otherwise
     */
    public boolean closeHandle(long handle);

    /**
     * @param pid the pid of the process for which we should get a token
     * @return a process token or 0 if we are unable to get one
     */
    public long getProcessToken(int pid);

    /**
     * @param opaque
     *            process token
     * @return true if call is successful. false otherwise
     */
    public boolean closeProcessToken(Object opaque);

    /**
     * @return process id of current JVM process
     */
    public int getProcessId();

    /**
     * @return The IP address of the remote access computer,
     * or zero if the computer is not accessed remotely.
     */
    public long getRDPAddress(int processId);
    /**
     * Logs the event in the event log.
     * 
     * @param eventId
     *            event id for message
     * @param params
     *            params for parameter replacement
     */
    public void logEvent(int type, int eventId, Object[] params);

    /**
     * 
     * Sends info to the IFS kernel driver to set up IPC
     * 
     * @param numberOfSlots
     *            number of IPC slots
     * @param eventArray
     *            an array filled with events to be used for notification
     * 
     * @return true if the kernel IPC is setup, false otherwise
     */
    public int setupKernelIPC(int numberOfSlots, long[] eventArray);

    /**
     * Sends request to IFS Filter to uninit IPC.
     * 
     * @param ipcSetupInfoHandle
     *            handle returned by setupKernelIPC
     */
    public void uninitKernelIPC(long ipcSetupInfoHandle);

    /**
     * 
     * Open a socket for Kernel/User communication
     * 
     * @param 
     * 
     * @return the socket handle for later use
     */
    public long setupSocket(int socketType);

    /**
     * Close a socket for kernel/user communication
     * 
     * @param socketHandle
     *            handle returned by setupSocket
     */
    public void shutdownSocket(long socketHandle);

    /**
     * @return array of SIDs for all users currently logged in
     */
    public String[] getLoggedInUsers();

    /**
     * @return creation time for file named fileName
     */
    public long getFileCreateTime(String fileName, Object opaque);

    /**
     * @return modification time for file named fileName
     */
    public long getFileModifiedTime(String fileName, Object opaque);

    /**
     * @return last access time for file named fileName
     */
    public long getFileAccessTime(String fileName, Object opaque);


    /**
     * @return get a list of File Custom Attributes,  the value of the key, null when
     *         there is no custom attirbutes associated with the file.
     *         Note that the attribute key/value could be empty string
     */
    public String[] getFileCustomAttributes(String fileName, Object opaque);

    /**
     * @return get a list of File Custom Attributes,  the value of the key 
     */
    public String[] getFileStandardAttributes (String fileName, int agentType, Object opaque);

    /**
     * @return get a list of basic file attributes.  Similar to the (never used) getFileStandardAttributes,
     * but should actually do what we want and nothing more
     */
    public long[] getFileBasicAttributes (String fileName, Object opaque);


    /**
     * Returns the address of the next kernel policy request to process. This
     * function assumes that the setupKernelIPC function has been called and
     * returned successfully.
     * 
     * @return an array of two objects. The first object in the list is a string
     *         containing the request unique number. It should be passed back
     *         when replying to the request. The second object is an array of
     *         string containing the request parameters
     */
    public Object[] getNextKernelPolicyRequest(long[] kernelEventArray);


    /**
     * Returns if the fileName resides in a removable media.
     * Please note that this function has performance impact because OS needs
     * to cycle up the drive (e.g. CD drive) before it can determine if the
     * file is in removable media.  So, this function should be called
     * sparsely.
     * 
     * @return true if fileName resides on the removable media, false for everything else.
     */
    public boolean isRemovableMedia (String fileName);

    /**
     * @return owner's SID for file named fileName
     */
    public String getOwnerSID(String fileName, int agentType, Object opaque);

    /**
     * @return group owner's SID for file named fileName
     */
    public String getGroupSID(String fileName, Object opaque);

    /**
     * @return
     */
    public String getAppInfo(String fileName);

    /**
     * @return fully-qualified DNS name of this host, if there's a problem
     *         retrieving the name an empty string is returned.
     */
    public String getFQDN();

    /**
     * @param shareName
     *            name of local share
     * @return physical location of local share specified by share name
     */
    public String getSharePhysicalPath(String shareName);

    /**
     * @return the location of current logged in user's "My Documents" folder
     */
    public String getMyDocumentsFolder();

    /**
     * @return the location of current logged in user's "My Desktop" folder
     */
    public String getMyDesktopFolder();

    /**
     * @param userSid
     *            SID of user
     * @return username@domain for the SID specified.
     */
    public String getUserName(String userSid);

    /**
     * Sends a policy evaluation response back to the kernel. This API is used
     * only for the kernel IPC policy requests.
     * 
     * @param uniqueNumber
     *            policy evaluation request number passed by the kernel. It must
     *            be passed back with the same value.
     * @param allow
     *            set to ALLOW(2) or DENY(1)
     * @param allowType
     *            says whether or not to keep watching the object after this
     *            evaluation. Valid values are NOT_WATCHED (1), WATCH_NEXT_OP(2)
     *            and ALLOW_UNTIL_CLOSE(3)
     * @return
     */
    public boolean setKernelPolicyResponse(long handle, long uniqueNumber, long allow, long allowType);
    
    /**
     * @param filename the directory path
     * @return true if the directory is empty, false if it is not.  undefined if path does not
     * specify a directory
     */
    public boolean isEmptyDirectory(String fileName, Object opaque);

    public boolean sendPolicyResponse(long handle, String uniqueNumber, long allow, String[] attrs);
    
    public boolean isLinux();

    /**
     * Perform content analysis on the specified file
     *
     * @param filename the file
     * @param pid the pid of the original process
     * @param opaque the process token
     * @param userSid the user sid
     * @param level the logging level
     * @param caDescriptions content analysis to perform.  This is organized as groups of four
     *        strings.  Examples given are of the PQL and the corresponding four strings:
     *
     *        REG:ssn>=5    :   "REG:ssn"  "\d{3} \d\d \d{4}"  ">="   "5"
     *        KEY:foo bar>=3:   "KEY:"     "foo bar"           ">=    "3"
     * @return results as an array of integers (this will be 1/4 the length of caDescriptions).
     *        0 = did not match, anything else is a match
     */
    public int[] getContentAnalysisAttributes(String filename, int pid, Object opaque, String userSid, int level, String[] caDescriptions);


    /**
     * Get a list of all the mounted network disk resources on this system.
     */
    public String[] getNetworkDiskResources(Object opaque);

    /**
     * Obtain full path of process
     *
     * @param pid the pid of the process
     * @return String full path to the process executable.  Empty string on error.
     */
    public String getPathFromPID(int pid);
}
