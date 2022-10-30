/*
 * Created on Apr 20, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/LockByName.java#1 $:
 */
package com.bluejungle.framework.utils;

import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lock based on the value of a string - not the *identity* of a String.
 * There are a number of solutions that work - if you don't care about
 * cleaning up the locks after you are done. We do.

 * One non-solution involves interning the string and then locking on that.
 * Unfortunately, interned strings might live forever (or at least for an
 * indeterminate amount of time), so that's not great.
 *
 * Some non-working solutions revolve around a Map (concurrent or not) keyed
 * off of the string, with a Lock as the value. Removing the key/value pair
 * is still a problem
 *
 * A WeakHashMap will automatically remove key/value pairs if the key
 * is no longer referenced, but our primary need for this is keying
 * off of resource names. These are the sorts of things that might
 * stick around for a long time (e.g. in activity logs), which is a
 * problem.
 *
 * A ConcurrentHashMap simplifies some things, but it's still hard to
 * remove the Lock from the map. The idea would be to create a new
 * lock each time and use putIfAbsent. If another lock already existed
 * then it would be returned and the code uses that instead. The
 * problem here is that there is a gap between getting the lock from
 * the map and using it. So you can have the case of thread A getting
 * the lock from the map, thread B deciding that no-one is waiting on
 * the lock and removing it, and thread C keying off the same value
 * and getting a completely new lock.  The way around *this* appears
 * to be synchronized blocks, but if you are going to use synchronized
 * blocks then there is a simpler approach.
 *
 * The simpler approach uses a Set of names - that's it. Threads wait until
 * their name isn't in the set and then add it to the set in a sync block.
 * They are woken by a notifyAll() in the unlock code.
 *
 * Note that this code is thread safe, but does not allow reentrant
 * synchronization.  In brief, while a thread (or multiple threads)
 * can lock and then unlock without any problems, if a thread locks A,
 * and then tries to lock B, it will *not* block at the synchronize in
 * lock(). This could be a problem (a very hard to debug problem) if a
 * second thread is acquiring a lock at the same time.
 */
public class LockByName {
    private HashSet<String> s;

    public LockByName() {
        s = new HashSet<String>();
    }

    /**
     * Acquires a lock by name.
     */
    public void lock(String name) throws InterruptedException {
        synchronized (s) {
            while (s.contains(name)) {
                s.wait();  // This moves us out of the sychronized block
            }
            s.add(name);
        }
    }

    /**
     * Releases the lock associated with this name.
     */
    public void unlock(String name) {
        synchronized (s) {
            s.remove(name);
            s.notifyAll();
        }
    }
}
