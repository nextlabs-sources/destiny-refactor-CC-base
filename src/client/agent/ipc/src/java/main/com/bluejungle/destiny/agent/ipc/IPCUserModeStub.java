// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.Pair;

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



public class IPCUserModeStub extends IPCStubBase implements IHasComponentInfo<IPCUserModeStub>{

    private static final String NAME = IPCUserModeStub.class.getName();
    private static final int MAX_RETRY_COUNT = 5;
    private static final ComponentInfo<IPCUserModeStub> COMP_INFO = new ComponentInfo<IPCUserModeStub>(
    		IPCUserModeStub.class, 
    		LifestyleType.TRANSIENT_TYPE);

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<IPCUserModeStub> getComponentInfo() {
        return COMP_INFO;
    }

    private long sharedFileMapping = 0;
    private long sharedMemoryHandle = 0;
    private long sendEventHandle = 0;
    private long receiveEventHandle = 0;
    private long handshakeMutex = 0;

    private List<List<Pair<Long,String>>> eventBucketList = new ArrayList<List<Pair<Long,String>>>();
    private List<IPCStubHelper> stubHelperArray = new ArrayList<IPCStubHelper>();
    private List<Thread> helperThreadArray = new ArrayList<Thread>();
    private Map<Long, IPCMessageChannel> eventChannelMap = Collections.synchronizedMap(new HashMap<Long, IPCMessageChannel>());

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
            Thread thread = this.helperThreadArray.get(i);
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

        for (IPCMessageChannel messageChannel : this.eventChannelMap.values()) {
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
        this.stubHelperArray.get(0).run();
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
            String[] eventNameArray = getEventNameArray(bucketNumber);
            int numEvents = eventArray.length;
            if (bucketNumber != 0 && numEvents == 1) {
                // Thread no longer needed. (The only remaining event is the
                // notification event) A new thread will be created by
                // performHandshake() when an event is added to the associated
                // bucket
                return;
            }
            int event = getOSWrapper().waitForMultipleObjects(numEvents, eventArray, eventNameArray, IPCConstants.TIMEOUT);
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
                getLog().trace("Message Channel added to bucket" + bucketNumber);
                continue;
            } else {
                IPCMessageChannel messageChannel = this.eventChannelMap.get(eventArray[event - IPCConstants.WAIT_OBJECT_0]);
                if (messageChannel != null) {
                    getThreadPool().doWork(messageChannel);
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
            long fileMapping = osWrapper.createFileMapping(nameArray[1], IPCConstants.CHANNEL_SIZE);
            String sendEventName = nameArray[2];
            String receiveEventName = nameArray[3];

            long fileMappingHandle = osWrapper.mapViewOfFile(fileMapping);
            long sendEventHandle = osWrapper.openEvent(sendEventName);
            long receiveEventHandle = osWrapper.openEvent(receiveEventName);

            if (fileMappingHandle == 0 ||
                sendEventHandle == 0 ||
                receiveEventHandle == 0)
            {
                getLog().error("Can't get handles for handshake request:\n" + handshakeMessage);
            }

            IPCMessageChannel messageChannel = new IPCMessageChannel(fileMapping,
                                                                     fileMappingHandle,
                                                                     sendEventHandle,
                                                                     receiveEventHandle);
            addMessageChannel(messageChannel, receiveEventName);
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
    protected void addMessageChannel(IPCMessageChannel messageChannel, String receiveEventName) {
        final IOSWrapper osWrapper = getOSWrapper();
        int bucketIndex = -1;
        this.eventChannelMap.put(messageChannel.getReceiveEvent(), messageChannel);

        synchronized (this.eventBucketList) {
            List<Pair<Long, String>> eventList = null;
            for (int i = 0; i < this.eventBucketList.size(); i++) {
                eventList = this.eventBucketList.get(i);
                if (eventList.size() < IPCConstants.MAXIMUM_WAIT_OBJECTS) {
                    bucketIndex = i;
                    break;
                }
            }

            if (bucketIndex == -1) {
                eventList = new ArrayList<Pair<Long, String>>();
                //Add event for notifying thread when new channel is added.
                String notificationEventName = BUCKET_NOTIFICATION_EVENT_CONFIG_PARAM + Calendar.getInstance().getTimeInMillis();
                eventList.add(new Pair<Long, String>(osWrapper.createEvent(notificationEventName), notificationEventName));
                this.eventBucketList.add(eventList);
                bucketIndex = this.eventBucketList.size() - 1;
            }

            eventList.add(new Pair<Long, String>(messageChannel.getReceiveEvent(), receiveEventName));

            if (bucketIndex != 0) {
                if (eventList.size() == 2) {
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
                        helper = this.stubHelperArray.get(bucketIndex);
                    }
                    Thread thread = new Thread(helper);
                    if (bucketIndex >= this.helperThreadArray.size()) {
                        this.helperThreadArray.add(thread);
                    } else {
                        this.helperThreadArray.set(bucketIndex, thread);
                    }
                    thread.start();
                } else {
                    long notificationEvent = eventList.get(0).first();
                    osWrapper.setEvent(notificationEvent);
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

        final IPCMessageChannel messageChannel = this.eventChannelMap.get(receiveEvent);
        this.eventChannelMap.remove(receiveEvent);

        synchronized (this.eventBucketList) {
            // Find the event somewhere in the buckets (is there a faster way of doing this?)
            for (List<Pair<Long, String>> eventList : this.eventBucketList) {
                int removeIndex = -1;
                for (int i = 0; i < eventList.size(); i++) {
                    long eventListReceiveEvent = eventList.get(i).first();
                    if (receiveEvent == eventListReceiveEvent) {
                        removeIndex = i;
                        break;
                    }
                }

                if (removeIndex != -1) {
                    eventList.remove(removeIndex);
                    break;
                }
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
            List<Pair<Long,String>> eventList = this.eventBucketList.get(bucketNumber);
            int length = eventList.size();
            retval = new long[length];
            for (int i = 0; i < length; i++) {
                retval[i] = eventList.get(i).first();
            }
        }
        return (retval);
    }

    private String[] getEventNameArray(int bucketNumber) {
        String[] retval = null;
        synchronized (this.eventBucketList) {
            List<Pair<Long,String>> eventList = this.eventBucketList.get(bucketNumber);
            int length = eventList.size();
            retval = new String[length];
            for (int i = 0; i < length; i++) {
                retval[i] = eventList.get(i).second();
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
        final List<Pair<Long, String>> eventList = new ArrayList<Pair<Long, String>>();
        eventList.add(new Pair<Long, String>(this.receiveEventHandle, receiveEventName));
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
    public IPCMessageChannel getChannel(Long key) {
        return this.eventChannelMap.get(key);
    }
}
