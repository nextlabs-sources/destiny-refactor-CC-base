package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPUser.java#1 $:
 */

public class PDPUser extends PDPNamedAttributes implements IPDPUser
{
    public static final String WINDOWS_NOBODY_ID = "S-1-0-0";
    public static final String UNIX_NOBODY_ID = "32767";  
    private static final String DIMENSION_NAME = "user";

    /**
     * Create a user object
     *
     * @param id the unique user id.  This will typically be a sid or email address.  This is matched up
     *           against the contents of the bundle, is a vital part of policy evaluation, and is thus
     *           required.
     *
     * Note: Both Windows and Unix "nobody" ids are available as
     * PDPUSER.WINDOWS_NOBODY_ID and PDPUser.UNIX_NOBODY_ID.  If you
     * really don't know the user then you should use whichever of
     * these is appropriate.  Note that id 0 on unix is the superuser
     * and should not be used to indicate an unknown user
     */

    public PDPUser(String id) {
        super(DIMENSION_NAME);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        setAttribute("id", id);
    }

    /**
     * Create a user object
     *
     * @param id the unique user id.  This will typically be a sid or email address.  This is matched up
     *           against the contents of the bundle, is a vital part of policy evaluation, and is thus
     *           required.
     * @param name the display user name.
     *
     * Note: Both Windows and Unix "nobody" ids are available as
     * PDPUSER.WINDOWS_NOBODY_ID and PDPUser.UNIX_NOBODY_ID.  If you
     * really don't know the user then you should use whichever of
     * these is appropriate.  Note that id 0 on unix is the superuser
     * and should not be used to indicate an unknown user
     */
    public PDPUser(String id, String name) {
        super(DIMENSION_NAME);

        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }

        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        setAttribute("id", id);
        setAttribute("name", name);
    }
}
