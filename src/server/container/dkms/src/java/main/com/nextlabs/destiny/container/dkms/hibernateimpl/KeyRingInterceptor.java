package com.nextlabs.destiny.container.dkms.hibernateimpl;

import java.io.Serializable;
import java.util.Date;

import com.bluejungle.framework.datastore.hibernate.DefaultInterceptor;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.type.Type;

public class KeyRingInterceptor extends DefaultInterceptor {

    private int posLastUpdated = -1;
    private int posCreated = -1;
    
    public boolean onFlushDirty(
            Object obj
          , Serializable id
          , Object[] state
          , Object[] prevState
          , String[] names
          , Type[] types
    ) throws CallbackException {
        if (obj instanceof KeyRingDO) {
            updateLastUpdated((KeyRingDO) obj, state, names);
            return true;
        } else {
            return false;
        }
    }

    public boolean onSave(
            Object obj
          , Serializable id
          , Object[] state
          , String[] names
          , Type[] types
    ) throws CallbackException {
        if (obj instanceof KeyRingDO) {
            updateLastUpdated((KeyRingDO) obj, state, names);
            return true;
        } else {
            return false;
        }
    }

    private void updateLastUpdated(KeyRingDO keyRing, Object[] state, String[] names) {
        Date now = new Date();
        keyRing.setLastUpdated(now);
        if (keyRing.getCreated() == null) {
            keyRing.setCreated(now);
        }
        if (posLastUpdated == -1) {
            for (int i = 0; i != names.length ; i++) {
                if ("lastUpdated".equals(names[i])) {
                    posLastUpdated = i;
                }
                if ("created".equals(names[i])) {
                    posCreated = i;
                }
            }
        }
        // Fix the state to avoid an extra trip to DB from hibernate
        if (posLastUpdated != -1) {
            state[posLastUpdated] = now;
        }
        if (posCreated != -1) {
            state[posCreated] = keyRing.getCreated();
        }
    }
    
}
