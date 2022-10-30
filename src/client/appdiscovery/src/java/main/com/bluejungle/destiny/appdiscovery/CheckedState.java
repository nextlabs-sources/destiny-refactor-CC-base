package com.bluejungle.destiny.appdiscovery;

import java.io.Serializable;

/*
 * Created on Dec 9, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

public class CheckedState implements Serializable {
    private final boolean checked;
    private final boolean byDefault;
    protected CheckedState( boolean checked, boolean byDefault ) {
        this.checked = checked;
        this.byDefault = byDefault;
    }
    public boolean isChecked() {
        return checked;
    }
    public boolean isDefault() {
        return byDefault;
    }
    public CheckedState getDefault() {
        return this;
    }
    public CheckedState getNonDefault() {
        return this;
    }
    static public CheckedState CHECKED = new CheckedState( true, false ) {
        public CheckedState getDefault() {
            return CHECKED_DEF;
        }
    };
    static public CheckedState UNCHECKED = new CheckedState( false, false ) {
        public CheckedState getDefault() {
            return UNCHECKED_DEF;
        }
    };
    static public CheckedState CHECKED_DEF = new CheckedState( true, true ) {
        public CheckedState getNonDefault() {
            return CHECKED;
        }
    };
    static public CheckedState UNCHECKED_DEF = new CheckedState( false, true ) {
        public CheckedState getNonDefault() {
            return UNCHECKED;
        }
    };
    private Object readResolve() {
        if ( isChecked() ) {
            return isDefault() ? CHECKED_DEF : CHECKED;
        } else {
            return isDefault() ? UNCHECKED_DEF : UNCHECKED;
        }
    }
}
