// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * The IPC Stub is the component that runs on the callee (or Server) component
 * and is responsible for receiving incoming requests, dispatching them to the
 * callee and then returning the result to the caller.
 * 
 * IPCStub should be instantiated from the ComponentManager. After
 * instantiation, the user should call init and pass in the request handler
 * class name and the size of the threadpool.
 * 
 * The request handler class is a custom class that implements the behavior of
 * the IPC Stub. Method invocations are dispatched to an instance of the request
 * handler class which is an implementation of IRequestHandler.
 * 
 * For an example of usage, see:
 * com.bluejungle.destiny.agent.ipc.tests.IPCRequestHandlerTest,
 * com.bluejungle.destiny.agent.ipc.tests.TestRequestHandler
 * 
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 * @see com.bluejungle.destiny.agent.ipc.tests.IPCRequestHandlerTest
 * @see com.bluejungle.destiny.agent.ipc.tests.TestRequestHandler
 *  
 */
public class IPCLinuxUserModeStub extends IPCUserModeStub {

    private static final String NAME = IPCLinuxUserModeStub.class.getName();
    private static final int MAX_RETRY_COUNT = 5;
    private static final ComponentInfo<IPCLinuxUserModeStub> COMP_INFO = new ComponentInfo<IPCLinuxUserModeStub>(
    		IPCLinuxUserModeStub.class, 
    		LifestyleType.TRANSIENT_TYPE);

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo getComponentInfo() {
        return COMP_INFO;
    }

    private long sharedFileMapping = 0;
    private long sharedMemoryHandle = 0;
    private long sendEventHandle = 0;
    private long receiveEventHandle = 0;
    private long handshakeMutex = 0;

    private ArrayList eventBucketList = new ArrayList();
    private ArrayList stubHelperArray = new ArrayList();
    private ArrayList helperThreadArray = new ArrayList();
    private Map eventChannelMap = Collections.synchronizedMap(new HashMap());

    /**
     * @see com.bluejungle.destiny.agent.ipc.IPCStubBase#getStubName()
     */
    protected String getStubName() {
        return NAME;
    }

    /**
     * Stop threadpool and close handles for all OS objects
     */
    public void stop() {
        super.stop();

        for (int i = 1; i < this.helperThreadArray.size(); i++) {
            Thread thread = (Thread) this.helperThreadArray.get(i);
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }

        final IOSWrapper osWrapper = getOSWrapper();
        osWrapper.unmapViewOfFile(this.sharedMemoryHandle);
        osWrapper.closeHandle(this.sharedFileMapping);
        osWrapper.closeHandle(this.sendEventHandle);
        osWrapper.closeHandle(this.receiveEventHandle);
        osWrapper.closeHandle(this.handshakeMutex);

        Iterator iterator = this.eventChannelMap.values().iterator();
        while (iterator.hasNext()) {
            IPCMessageChannel messageChannel = (IPCMessageChannel) iterator.next();
            messageChannel.close();
        }

    }

    /**
     * 
     * Calls run on the default IPCStubHelper instance.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ((IPCStubHelper) this.stubHelperArray.get(0)).run();
    }

    /**
     * Waits for events to be signaled.
     * 
     * An event being signaled specifies that a message is received. Messages
     * are dispatched based on the type of message. For handshake messages, call
     * performHandshake. For invoke request, add the corresponding
     * IPCMessageChannel instance to the requestQueue.
     * 
     * @param bucketNumber
     *            specifies the event bucket to use for the list of events to
     *            wait on.
     */
    public void run(int bucketNumber) {
        int retryCount = 0;
        while (!Thread.interrupted()) {
            long[] eventArray = getEventArray(bucketNumber);
            int numEvents = eventArray.length;
            if (bucketNumber != 0 && numEvents == 0) {
                // Thread no longer needed. (The only remaining event is the
                // notification event) A new thread will be created by
                // performHandshake() when an event is added to the associated
                // bucket
                return;
            }
            int event = getOSWrapper().waitForMultipleObjects(numEvents, eventArray, null, IPCConstants.TIMEOUT);
            if (Thread.interrupted()) {
                return;
            }
            if (event != -1) {
                retryCount = 0;
            } else {
                getLog().error("IPC Wait failure.");
                retryCount++;
                if (retryCount >= MAX_RETRY_COUNT) {
                    return;
                } else {
                    continue;
                }
            }
            if (event == IPCConstants.WAIT_TIMEOUT) {
                getLog().debug("IPC Stub Wait timeout: Continue to wait...");
            } else if (event >= IPCConstants.WAIT_STATUS_ABANDONED_0 && event < IPCConstants.WAIT_STATUS_ABANDONED_0 + numEvents) {
                // remove corresponding channel
                removeChannel(eventArray[event - IPCConstants.WAIT_STATUS_ABANDONED_0]);
            } else if (event - IPCConstants.WAIT_OBJECT_0 < numEvents && eventArray[event - IPCConstants.WAIT_OBJECT_0] == this.receiveEventHandle) {
                if (bucketNumber == 0) { // only the first bucket has the
                    // handshake event
                    performHandshake();
                }
            } else if (event == IPCConstants.WAIT_OBJECT_0 && bucketNumber != 0) {
                //this is a notification for newly added channel
            	//[jzhang 102506]
                //getLog().trace("Message Channel added to bucket" + bucketNumber);
                //continue;
                IPCMessageChannel messageChannel = (IPCMessageChannel) this.eventChannelMap.get(new Long(eventArray[event - IPCConstants.WAIT_OBJECT_0]));
                if (messageChannel != null) {
                    getThreadPool().doWork(messageChannel);
            } 
                else {
            	}
            }
        }
    }

    /**
     * reads the handshake message and completes the handshake so that
     * subsequent requests can be handled.
     * 
     * If a connection is being established, creates an instance of
     * IPCMessageChannel with the shared memory location and events specified
     * and add it to eventChannelMap. Add the receive event to the list of
     * events to wait on. The list of events are stored in buckets of size
     * (MAXIMUM_WAIT_OBJECTS - 1) since the OS only supports waiting on
     * MAXIMUM_WAIT_OBJECTS events.
     * 
     * If no existing bucket has any space remaining, a new bucket is created
     * and a new thread is created to wait on the events in the new bucket When
     * a bucket becomes empty, the thread exits. If an event is added to an
     * existing but empty bucket, a new thread is also created since the
     * original thread on that bucket will have exited.
     */
    private void performHandshake() {
        final IOSWrapper osWrapper = getOSWrapper();
        String handshakeMessage = osWrapper.readString(this.sharedMemoryHandle);
        String[] nameArray = handshakeMessage.split("\n");

        if (getLog().isTraceEnabled()) {
            getLog().trace("Received Handshake Request:\n" + handshakeMessage);
        }

        if (nameArray.length != 4) {
            getLog().error("Invalid handshake request:\n" + handshakeMessage);

        } else if (nameArray[0].equals(IPCConstants.CONNECT)) {
            long fileMapping = osWrapper.openFileMapping(nameArray[1]);
            IPCMessageChannel messageChannel = new IPCMessageChannel(fileMapping, osWrapper.mapViewOfFile(fileMapping), osWrapper.openEvent(nameArray[2]), osWrapper.openEvent(nameArray[3]));
            addMessageChannel(messageChannel);
        }

        osWrapper.setEvent(this.sendEventHandle);

    }

    /**
     * Add the receive event to the list of events to wait on. The list of
     * events are stored in buckets of size (MAXIMUM_WAIT_OBJECTS - 1) since the
     * OS only supports waiting on MAXIMUM_WAIT_OBJECTS events.
     * 
     * If no existing bucket has any space remaining, a new bucket is created
     * and a new thread is created to wait on the events in the new bucket When
     * a bucket becomes empty, the thread exits. If an event is added to an
     * existing but empty bucket, a new thread is also created since the
     * original thread on that bucket will have exited.
     * 
     * An event is added to each new bucket (except for the first one). The
     * event is used to notify the thread when a new channel is added.
     * 
     * @param messageChannel
     *            IPCMessageChannel to add
     */
    protected void addMessageChannel(IPCMessageChannel messageChannel) {
        final IOSWrapper osWrapper = getOSWrapper();
        int bucketIndex = -1;
        this.eventChannelMap.put(new Long(messageChannel.getReceiveEvent()), messageChannel);

        synchronized (this.eventBucketList) {
            ArrayList eventList = null;
            for (int i = 0; i < this.eventBucketList.size(); i++) {
                eventList = (ArrayList) this.eventBucketList.get(i);
                if (eventList.size() < IPCConstants.LINUX_MAXIMUM_WAIT_OBJECTS) {
                    bucketIndex = i;
                    break;
                }
            }

            if (bucketIndex == -1) {
                eventList = new ArrayList();
                //Add event for notifying thread when new channel is added.
                //eventList.add(new Integer(osWrapper.createEvent(BUCKET_NOTIFICATION_EVENT_CONFIG_PARAM + Calendar.getInstance().getTimeInMillis())));
                this.eventBucketList.add(eventList);
                bucketIndex = this.eventBucketList.size() - 1;
            }

            eventList.add(new Long(messageChannel.getReceiveEvent()));

            if (bucketIndex != 0) {
                if (eventList.size() == 1) {
                    // Need to start a new thread.
                    IPCStubHelper helper = null;
                    if (bucketIndex >= this.stubHelperArray.size()) {
                        // create new helper
                        helper = new IPCStubHelper();
                        helper.setBucketNumber(bucketIndex);
                        helper.setStub(this);
                        this.stubHelperArray.add(helper);
                    } else {
                        // use existing helper whose thread has exited
                        helper = (IPCStubHelper) this.stubHelperArray.get(bucketIndex);
                    }
                    Thread thread = new Thread(helper);
                    if (bucketIndex >= this.helperThreadArray.size()) {
                        this.helperThreadArray.add(thread);
                    } else {
                        this.helperThreadArray.set(bucketIndex, thread);
                    }
                    thread.start();
                } else {
                    //int notificationEvent = ((Integer) eventList.get(0)).intValue();
                    //osWrapper.setEvent(notificationEvent);
                }
            }
        }
    }

    /**
     * Close handles for OS objects corresponding the specified communication
     * channel.
     * 
     * @param channel
     *            channel to be removed.
     */
    public void removeChannel(IPCMessageChannel channel) {
        removeChannel(channel.getReceiveEvent());
    }

    /**
     * Close handles for OS objects corresponding the communication channel
     * corresponding to the specified event.
     * 
     * @param receiveEvent
     *            receiveEvent corresponding to connection that is being
     *            dropped.
     */
    public void removeChannel(long receiveEvent) {
        if (getLog().isTraceEnabled()) {
            getLog().trace("Channel removed: " + receiveEvent);
        }

        Long key = new Long(receiveEvent);
        final IPCMessageChannel messageChannel = (IPCMessageChannel) this.eventChannelMap.get(key);
        this.eventChannelMap.remove(key);

        synchronized (this.eventBucketList) {
            for (int i = 0; i < this.eventBucketList.size(); i++) {
                final ArrayList eventList = (ArrayList) this.eventBucketList.get(i);
                eventList.remove(key);
            }
        }
        if (messageChannel != null) {
            messageChannel.close();
        }
    }

    /**
     * @return array representation of eventList.
     * 
     * this method may cause performance issues. Will consider changing
     * implementation so that eventList is stored as an array.
     */
    private long[] getEventArray(int bucketNumber) {
        long[] retval = null;
        synchronized (this.eventBucketList) {
            ArrayList eventList = (ArrayList) this.eventBucketList.get(bucketNumber);
            int length = eventList.size();
            retval = new long[length];
            for (int i = 0; i < length; i++) {
                retval[i] = ((Long) eventList.get(i)).longValue();
            }
        }
        return (retval);
    }

    /**
     * Initialize the IPC Stub by creating the shared memory location, events
     * and mutex that are used for the handshake and also creating threads and
     * request handler instance as specified by the parameters.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        String serverSharedMemoryFileName = getRequestHandlerClassName() + IPCConstants.SHARED_MEMORY_SUFFIX;
        String sendEventName = getRequestHandlerClassName() + IPCConstants.SEND_EVENT_SUFFIX;
        String receiveEventName = getRequestHandlerClassName() + IPCConstants.RECEIVE_EVENT_SUFFIX;
        String mutexName = getRequestHandlerClassName() + IPCConstants.MUTEX_SUFFIX;
        final IOSWrapper osWrapper = getOSWrapper();
        this.sharedFileMapping = osWrapper.createFileMapping(serverSharedMemoryFileName, IPCConstants.CHANNEL_SIZE);
        this.sharedMemoryHandle = osWrapper.mapViewOfFile(this.sharedFileMapping);
        this.sendEventHandle = osWrapper.createEvent(sendEventName);
        this.receiveEventHandle = osWrapper.createEvent(receiveEventName);

        this.handshakeMutex = osWrapper.createMutex(mutexName);

        // create first bucket and helper
        final ArrayList eventList = new ArrayList();
        eventList.add(new Long(this.receiveEventHandle));
        this.eventBucketList.add(eventList);

        final IPCStubHelper helper = new IPCStubHelper();
        helper.setBucketNumber(0);
        helper.setStub(this);
        this.stubHelperArray.add(helper);
        this.helperThreadArray.add(Thread.currentThread());
    }

    /**
     * @param key
     * @return
     */
    public IPCMessageChannel getChannel(Integer key) {
        return (IPCMessageChannel) this.eventChannelMap.get(key);
    }
}
