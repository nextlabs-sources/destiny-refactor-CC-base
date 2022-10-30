package com.bluejungle.pf.domain.epicenter.resource;

/*
 * Created on Feb 02, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.io.Serializable;
import java.util.Map;

import com.bluejungle.framework.expressions.IEvalValue;

public interface IResourceAttributeLoader extends Serializable {
    public static final IResourceAttributeLoader EMPTY = new IResourceAttributeLoader() {
            /**
             * The default serialization ID.
             */
            private static final long serialVersionUID = 1L;

            public void getAttrs(Map<String, IEvalValue> attrs) { }

            public void setProcessToken(Long processToken) { }
        };

    public void getAttrs(Map<String, IEvalValue> attrs);

    public void setProcessToken(Long processToken);
}
