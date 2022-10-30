package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/ClientInformationManager.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides client resolution functionality, and supports searches of
 * client-related information by client identifier.
 *
 * @author Sergey Kalinichenko
 */
public class ClientInformationManager implements IClientInformationManager {

    /**
     * The time at which the data in this client information manager has been built.
     */
    private final long buildTime;

    /**
     * The set of UIDs for which this client information manager has been built.
     */
    private Set<String> preparedForUids = new HashSet<String>();

    /**
     * A list of [ClientInformation+exact email] records sorted on the reversed email.
     */
    private List<ClientAddress> exact = new ArrayList<ClientAddress>();

    /**
     * A list of [ClientInformation+email with wildcard] records sorted on the reversed
     * email templates, with removed wildcard character.
     */
    private final List<ClientAddress> wildcards = new ArrayList<ClientAddress>();

    /**
     * A Map from a UID to a list of clients associated with the user.
     */
    private final Map<String,List<ClientInformation>> clientsByUid = new HashMap<String,List<ClientInformation>>();

    /**
     * A Map of client ID to its corresponding ClientInformation.
     */
    private final Map<String,ClientInformation> clientById = new HashMap<String,ClientInformation>();

    /**
     * Creates a client info manager for the specified initialization data
     * provided in three coordinated arrays.
     *
     * @param clientIds An array of client IDs (no null values).
     * @param shortNames An array of client short names (no null values).
     * @param longNames An array of client long names (no null values).
     * @param emails An array of emails associated with the client (null and
     * empty arrays are allowed)
     * @param uids An array of UIDs for the client (null and 
     * empty arrays are allowed)
     */
    public ClientInformationManager(
        Date buildTime
    ,   String[] preparedForUids
    ,   String[] clientIds
    ,   String[] shortNames
    ,   String[] longNames
    ,   String[][] emails
    ,   String[][] uids
    ) {
        if (buildTime == null) {
            throw new NullPointerException("buildTime");
        }
        if (preparedForUids == null) {
            throw new NullPointerException("preparedForUids");
        }
        if (clientIds == null) {
            throw new NullPointerException("clientIds");
        }
        if (shortNames == null) {
            throw new NullPointerException("shortNames");
        }
        if (longNames == null) {
            throw new NullPointerException("longNames");
        }
        if (emails == null) {
            throw new NullPointerException("emails");
        }
        if (uids == null) {
            throw new NullPointerException("uids");
        }
        int size = clientIds.length;
        if (shortNames.length != size) {
            throw new IllegalArgumentException("shortNames.length");
        }
        if (longNames.length != size) {
            throw new IllegalArgumentException("longNames.length");
        }
        if (emails.length != size) {
            throw new IllegalArgumentException("emails.length");
        }
        if (uids.length != size) {
            throw new IllegalArgumentException("uids.length");
        }
        for (int i = 0 ; i != size ; i++) {
            if (clientIds[i] == null) {
                throw new IllegalArgumentException("clientIds["+i+"]");
            }
            if (shortNames[i] == null) {
                throw new IllegalArgumentException("shortNames["+i+"]");
            }
            if (longNames[i] == null) {
                throw new IllegalArgumentException("longNames["+i+"]");
            }
            ClientInformation info = new ClientInformation(clientIds[i], shortNames[i], longNames[i]);
            clientById.put(clientIds[i], info);
            if (emails[i] != null) {
                for (String email : emails[i]) {
                    if (email != null && email.length() != 0) {
                        if(email.charAt(0) == '*' && email.length() != 1) {
                            wildcards.add(new ClientAddress(info, email.substring(1)));
                        } else {
                            exact.add(new ClientAddress(info, email));
                        }
                    }
                }
            }
            if (uids[i] != null) {
                for (String uid : uids[i]) {
                    List<ClientInformation> toAdd = clientsByUid.get(uid);
                    if (toAdd == null) {
                        clientsByUid.put(uid, toAdd = new ArrayList<ClientInformation>(5));
                    }
                    toAdd.add(info);
                }
            }
        }
        Collections.sort(wildcards, CLIENT_COMPARATOR);
        Collections.sort(exact, CLIENT_COMPARATOR);
        this.buildTime = buildTime.getTime();
        this.preparedForUids.addAll(Arrays.asList(preparedForUids));
    }

    /**
     * Returns the build time of this manager.
     *
     * @return the build time of this manager.
     */
    public Date getBuildTime() {
        return new Date(buildTime);
    }

    /**
     * @see IClientInformationManager#getClientIdsForEmail(String)
     */
    public String[] getClientIdsForEmail(String email) {
        Set<String> res = getClientIdsForEmail(exact, email, true);
        res.addAll(getClientIdsForEmail(wildcards, email, false));
        return res.toArray(new String[res.size()]);
    }

    private Set<String> getClientIdsForEmail(List<ClientAddress> clients, String email, boolean exact) {
        if (email == null) {
            throw new NullPointerException("email");
        }
        int i = Collections.binarySearch(
            clients
        ,   new ClientAddress(null, email)
        ,   CLIENT_COMPARATOR
        );
        if (i < 0) {
            // No exact match - we need to look one element to the left
            // of the insertion position, which is calculated as -(i-1).
            i = -(i+2);
        }
        if (i >= -1) {
            // Find the lowest matching position
            Set<String> res = new HashSet<String>();
            int j = i;
            while (j >= 0 && endsWithIgnoreCase(email, clients.get(j).getEmail(), exact)) {
                res.add(clients.get(j--).getInfo().getIdentifier());
            }
            // Find the highest matching position
            while (++i < clients.size() && endsWithIgnoreCase(email, clients.get(i).getEmail(), exact)) {
                res.add(clients.get(i).getInfo().getIdentifier());
            }
            return res;
        } else {
            return new HashSet<String>();
        }
    }

    private static boolean endsWithIgnoreCase(String value, String ending, boolean exact) {
        int patternPos = ending.length()-1;
        int valuePos = value.length()-1;
        while (patternPos >= 0 && valuePos >= 0) {
            if (Character.toLowerCase(ending.charAt(patternPos--)) !=
                Character.toLowerCase(value.charAt(valuePos--))) {
                return false;
            }
        }
        return (!exact)
            || (patternPos == valuePos); // This happens only when both positions are equal to -1
    }

    /**
     * Given a client identifier, finds the corresponding client.
     *
     * @param clientId the client ID to find.
     * @return a ClientInformation with the corresponding ID, or null
     * if the ID does not correspond to a known client.
     */
    public ClientInformation getClient(String clientId) {
        if (clientId == null) {
            throw new NullPointerException("clientId");
        }
        return clientById.get(clientId);
    }

    /**
     * Given a UID, finds all clients associated with the corresponding user.
     *
     * @param uid the UID of the user to search.
     * @return ClientInformation[] for clients associated with the UID, or
     * an empty array if the UID does not have any associated clients.
     */
    public ClientInformation[] getClientsForUser(String uid) {
        if (uid == null) {
            throw new NullPointerException("uid");
        }
        List<ClientInformation> res = clientsByUid.get(uid);
        if (res != null) {
            return res.toArray(new ClientInformation[res.size()]);
        } else {
            return new ClientInformation[0];
        }
    }

    /**
     * Determines if this manager has been prepared for all UIDs
     * from a given list.
     *
     * @param uids the UIDs to check.
     * @return true if this manager has been prepared for all UIDs
     * from the given list; false otherwise.
     */
    public boolean includesAllUids(String[] uids) {
        if (uids == null || uids.length == 0) {
            return true;
        }
        for (String uid : uids) {
            if (!preparedForUids.contains(uid)) {
                return false;
            }
        }
        return true;
    }

    /**
     * This class represents a pair of ClientInformation and
     * its email for combined sorting together.
     */
    private static class ClientAddress {

        /**
         * The client information stored in the record.
         */
        private ClientInformation info;

        /**
         * The email stored in the record.
         */
        private String email;

        /**
         * Create a ClientAddress record with the specified client information
         * and its associated email.
         *
         * @param info the ClientInformation to put in the record.
         * @param email the email to put in the record.
         */
        public ClientAddress(ClientInformation info, String email) {
            if (email == null) {
                throw new NullPointerException("email");
            }
            this.info = info;
            this.email = email;
        }

        /**
         * Gets the email.
         *
         * @return the email.
         */
        public String getEmail() {
            return email;
        }

        /**
         * Get the client info.
         *
         * @return the client info.
         */
        public ClientInformation getInfo() {
            return info;
        }

    }

    /**
     * This comparator orders ClientAddress objects using reversed email strings.
     */
    private static final Comparator<ClientAddress> CLIENT_COMPARATOR = new Comparator<ClientAddress>() {
        /**
         * Compare two ClientAddress using reversed emails.
         *
         * @param d1 the first ClientAddress.
         * @param d2 the second ClientAddress.
         * @return 0 if d1's and d2's emails are identical, a negative
         * number if the reversed emails of d1 comes lexicographically
         * earlier than that of d2, and a positive number otherwise.
         */
        public int compare(ClientAddress d1, ClientAddress d2) {
            String lhs = d1.getEmail();
            String rhs = d2.getEmail();
            int lp = lhs.length()-1;
            int rp = rhs.length()-1;
            while (lp >= 0 && rp >= 0) {
                int diff = Character.toLowerCase(lhs.charAt(lp--))
                         - Character.toLowerCase(rhs.charAt(rp--));
                if (diff != 0) {
                    return diff;
                }
            }
            return rp-lp;
        }

    };

}
