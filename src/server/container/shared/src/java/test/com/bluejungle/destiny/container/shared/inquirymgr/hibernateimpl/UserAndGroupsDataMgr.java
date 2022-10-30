/*
 * Created on Mar 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * This is the data manager class for user and user groups. It creates user and
 * user groups data according to the BlueJungle organization.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/UserAndGroupsDataMgr.java#1 $
 */

class UserAndGroupsDataMgr extends BaseDataMgr {

    /**
     * Usernames
     */
    private static final String BERNARD_USERNAME = "Bernard";
    private static final String SRINI_USERNAME = "Srini";
    private static final String SERGEY_USERNAME = "Sergey";
    private static final String SCOTT_USERNAME = "Scott";
    private static final String SAFDAR_USERNAME = "Safdar";
    private static final String ROBERT_USERNAME = "Robert";
    private static final String KENI_USERNAME = "Keni";
    private static final String KENG_USERNAME = "Keng";
    private static final String JESSICA_USERNAME = "Jessica";
    private static final String IANNIS_USERNAME = "Iannis";
    private static final String HUYEN_USERNAME = "Huyen";
    private static final String FUAD_USERNAME = "Fuad";
    private static final String DAVID_S_USERNAME = "david S.";
    private static final String DAVID_L_USERNAME = "David L.";
    private static final String CHANDER_USERNAME = "Chander";
    private static final String BOOGIE_USERNAME = "Boogie";
    private static final String ANDY_USERNAME = "Andy";
    private static final String SASHA_USERNAME = "Sasha";

    /**
     * User SIDs
     */
    private static final String BERNARD_SID = "S-1-5-21-668023798-3031861066-1043980994-2636";
    private static final String SRINI_SID = "S-1-5-21-668023798-3031861066-1043980994-2637";
    private static final String SERGEY_SID = "S-1-5-21-668023798-3031861066-1043980994-1157";
    private static final String SCOTT_SID = "S-1-5-21-668023798-3031861066-1043980994-2634";
    private static final String SAFDAR_SID = "S-1-5-21-668023798-3031861066-1043980994-2627";
    private static final String ROBERT_SID = "S-1-5-21-668023798-3031861066-1043980994-2628";
    private static final String KENI_SID = "S-1-5-21-668023798-3031861066-1043980994-2631";
    private static final String KENG_SID = "S-1-5-21-668023798-3031861066-1043980994-1114";
    private static final String JESSICA_SID = "S-1-5-21-668023798-3031861066-1043980994-1162";
    private static final String IANNIS_SID = "S-1-5-21-668023798-3031861066-1043980994-1140";
    private static final String HUYEN_SID = "S-1-5-21-668023798-3031861066-1043980994-1141";
    private static final String FUAD_SID = "S-1-5-21-668023798-3031861066-1043980994-2624";
    private static final String DAVIDS_SID = "S-1-5-21-668023798-3031861066-1043980994-2632";
    private static final String DAVIDL_SID = "S-1-5-21-668023798-3031861066-1043980994-1159";
    private static final String CHANDER_SID = "S-1-5-21-668023798-3031861066-1043980994-1133";
    private static final String BOOGIE_SID = "S-1-5-21-668023798-3031861066-1043980994-2112";
    private static final String ANDY_SID = "S-1-5-21-668023798-3031861066-1043980994-1149";
    private static final String SASHA_SID = "S-1-5-21-668023798-3031861066-1043980994-1119";

    /**
     * User IDs
     */
    private static final Long KENG_ID = new Long(10);
    private static final Long JESSICA_ID = new Long(9);
    private static final Long IANNIS_ID = new Long(8);
    private static final Long HUYEN_ID = new Long(7);
    private static final Long FUAD_ID = new Long(6);
    private static final Long DAVIDS_ID = new Long(5);
    private static final Long DAVIDL_ID = new Long(4);
    private static final Long CHANDER_ID = new Long(3);
    private static final Long BOOGIE_ID = new Long(2);
    private static final Long ANDY_ID = new Long(1);
    private static final Long SASHA_ID = new Long(0);
    private static final Long BERNARD_ID = new Long(17);
    private static final Long SRINI_ID = new Long(16);
    private static final Long SERGEY_ID = new Long(15);
    private static final Long SCOTT_ID = new Long(14);
    private static final Long SAFDAR_ID = new Long(13);
    private static final Long ROBERT_ID = new Long(12);
    private static final Long KENI_ID = new Long(11);
    
    /**
     * Creates a group data object
     * 
     * @param id
     *            id of the group
     * @param name
     *            group name
     * @param displayName
     *            display name
     * @return a group data object populated with the parameters
     */
    protected static IUserGroup createGroup(Long id, String name, String displayName, String enrollmentType) {
        return createGroup(id, name, displayName, new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME), enrollmentType);
    }

    /**
     * Creates a usergroup with a time relation
     * 
     * @param id
     *            group id
     * @param name
     *            group name
     * @param displayName
     *            group display name
     * @param timeRelation
     *            time relation
     * @return a user group populated with the group parameters
     */
    protected static IUserGroup createGroup(Long id, String name, String displayName, TimeRelation timeRelation, String enrollmentType) {
        final UserGroupDO userGroup = new UserGroupDO();
        userGroup.setOriginalId(id);
        userGroup.setName(name);
        userGroup.setDisplayName(displayName);
        userGroup.setTimeRelation(timeRelation);
        userGroup.setEnrollmentType(enrollmentType);
        return userGroup;
    }

    /**
     * Creates a user data object
     * 
     * @param id
     *            id of the user
     * @param name
     *            name of the user
     * @param sid
     *            sid of the user
     * @return a user data object populated with the parameters
     */
    protected static IUser createUser(Long id, String name, String sid) {
        return createUser(id, name, sid, new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME));
    }

    /**
     * Creates a user data object with a time relation
     * 
     * @param id
     *            id of the user
     * @param name
     *            name of the user
     * @param sid
     *            sid of the user
     * @param timeRelation
     *            time relation for the object
     * @return a user data object populated with the parameters
     */
    protected static IUser createUser(Long id, String name, String sid, TimeRelation timeRelation) {
        final UserDO user = new UserDO();
        user.setOriginalId(id);
        user.setDisplayName(name);
        user.setFirstName("Doesn't matter");
        user.setLastName("Doesn't matter");
        user.setSID(sid);
        user.setTimeRelation(timeRelation);
        return user;
    }

    /**
     * Creates a user - user group membership
     * 
     * @param user
     *            user object
     * @param group
     *            user group object
     * @return a valid user group member object
     */
    protected static IUserGroupMember createUserGroupMember(IUser user, IUserGroup group) {
        return createUserGroupMember(user, group, new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME));
    }

    /**
     * Creates a user - user group membership for a given time relation
     * 
     * @param user
     *            user object
     * @param group
     *            user group object
     * @param timeRelation
     *            time relation
     * @return a valid user group member object
     */
    protected static IUserGroupMember createUserGroupMember(IUser user, IUserGroup group, TimeRelation timeRelation) {
        final UserGroupMemberDO userGroupMember = new UserGroupMemberDO();
        userGroupMember.setGroupId(group.getOriginalId());
        userGroupMember.setUserId(user.getOriginalId());
        userGroupMember.setTimeRelation(timeRelation);
        return userGroupMember;
    }

    /**
     * Creates sample data for users and groups. Users and groups have
     * historical data (based pretty much on the blue jungle history).
     * 
     * @param s
     *            Hibernate session to use.
     * @throws HibernateException
     *             if database operation fails.
     */
    public static void createHistoricalUsersAndGroups(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();

            //Chander dates
            final TimeRelation chanderDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JULY, 14), SampleDataMgr.createDate(2006, Calendar.MARCH, 10));
            final TimeRelation chanderUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JULY, 14), SampleDataMgr.createDate(2006, Calendar.MARCH, 10));

            //Sasha worked on PF and PSR
            final TimeRelation sashaUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);
            final TimeRelation sashaDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), SampleDataMgr.createDate(2006, Calendar.FEBRUARY, 10));
            final TimeRelation sashaPFDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JULY, 1), SampleDataMgr.createDate(2006, Calendar.FEBRUARY, 10));
            final TimeRelation sashaPSRDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 3), SampleDataMgr.createDate(2005, Calendar.DECEMBER, 28));

            //Fuad worked on agent
            final TimeRelation fuadDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), SampleDataMgr.createDate(2006, Calendar.MARCH, 17));
            final TimeRelation fuadUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);

            //Huyen worked on the agent
            final TimeRelation huyenDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.AUGUST, 15), SampleDataMgr.createDate(2005, Calendar.SEPTEMBER, 19));
            final TimeRelation huyenUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.AUGUST, 15), UnmodifiableDate.END_OF_TIME);

            //Iannis worked on DCC and agent
            final TimeRelation iannisDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.AUGUST, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation iannisUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.AUGUST, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation iannisAgentDates = new TimeRelation(SampleDataMgr.createDate(2006, Calendar.JANUARY, 15), SampleDataMgr.createDate(2006, Calendar.MARCH, 15));

            //Keni worked on both agent and PF
            final TimeRelation keniDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.DECEMBER, 15), SampleDataMgr.createDate(2006, Calendar.FEBRUARY, 13));
            final TimeRelation keniUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.DECEMBER, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation keniAgentDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.AUGUST, 15), SampleDataMgr.createDate(2005, Calendar.DECEMBER, 28));

            //Safdar is on DCC
            final TimeRelation safdarDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.NOVEMBER, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation safdarUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.NOVEMBER, 15), UnmodifiableDate.END_OF_TIME);

            //Scott is at the same time on DCC and tools
            final TimeRelation scottDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.DECEMBER, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation scottUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.DECEMBER, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation scottDCCDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.DECEMBER, 15), SampleDataMgr.createDate(2006, Calendar.FEBRUARY, 15));
            final TimeRelation scottToolsDates = new TimeRelation(SampleDataMgr.createDate(2006, Calendar.JANUARY, 15), UnmodifiableDate.END_OF_TIME);

            //Sergey works on PF
            final TimeRelation sergeyUserDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.JANUARY, 15), UnmodifiableDate.END_OF_TIME);
            final TimeRelation sergeyDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.JANUARY, 15), UnmodifiableDate.END_OF_TIME);

            //Andy dates
            final TimeRelation andyUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.DECEMBER, 20), UnmodifiableDate.END_OF_TIME);

            //Boogie dates
            final TimeRelation boogieUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);

            //David L dates
            final TimeRelation davidLUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);

            //David S dates
            final TimeRelation davidSUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.NOVEMBER, 1), UnmodifiableDate.END_OF_TIME);

            //Jessica dates
            final TimeRelation jessicaUserDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.JUNE, 1), UnmodifiableDate.END_OF_TIME);

            //Keng user
            final TimeRelation kengUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);

            //Robert user
            final TimeRelation robertUserDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);

            //Srini user
            final TimeRelation sriniUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.SEPTEMBER, 20), UnmodifiableDate.END_OF_TIME);

            //Bernard user
            final TimeRelation bernardUserDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.FEBRUARY, 15), UnmodifiableDate.END_OF_TIME);

            //Create users
            IUser sasha = createUser(SASHA_ID, SASHA_USERNAME, SASHA_SID, sashaUserDates);
            IUser andy = createUser(ANDY_ID, ANDY_USERNAME, ANDY_SID, andyUserDates);
            IUser boogie = createUser(BOOGIE_ID, BOOGIE_USERNAME, BOOGIE_SID, boogieUserDates);
            IUser chander = createUser(CHANDER_ID, CHANDER_USERNAME, CHANDER_SID, chanderUserDates);
            IUser davidL = createUser(DAVIDL_ID, DAVID_L_USERNAME, DAVIDL_SID, davidLUserDates);
            IUser davidS = createUser(DAVIDS_ID, DAVID_S_USERNAME, DAVIDS_SID, davidSUserDates);
            IUser fuad = createUser(FUAD_ID, FUAD_USERNAME, FUAD_SID, fuadUserDates);
            IUser huyen = createUser(HUYEN_ID, HUYEN_USERNAME, HUYEN_SID, huyenUserDates);
            IUser iannis = createUser(IANNIS_ID, IANNIS_USERNAME, IANNIS_SID, iannisUserDates);
            IUser jessica = createUser(JESSICA_ID, JESSICA_USERNAME, JESSICA_SID, jessicaUserDates);
            IUser keng = createUser(KENG_ID, KENG_USERNAME, KENG_SID, kengUserDates);
            IUser keni = createUser(KENI_ID, KENI_USERNAME, KENI_SID, keniUserDates);
            IUser robert = createUser(ROBERT_ID, ROBERT_USERNAME, ROBERT_SID, robertUserDates);
            IUser safdar = createUser(SAFDAR_ID, SAFDAR_USERNAME, SAFDAR_SID, safdarUserDates);
            IUser scott = createUser(SCOTT_ID, SCOTT_USERNAME, SCOTT_SID, scottUserDates);
            IUser sergey = createUser(SERGEY_ID, SERGEY_USERNAME, SERGEY_SID, sergeyUserDates);
            IUser srini = createUser(SRINI_ID, SRINI_USERNAME, SRINI_SID, sriniUserDates);
            IUser bernard = createUser(BERNARD_ID, BERNARD_USERNAME, BERNARD_SID, bernardUserDates);

            //Save users
            final Set users = new HashSet();
            users.add(sasha);
            users.add(andy);
            users.add(boogie);
            users.add(chander);
            users.add(davidL);
            users.add(davidS);
            users.add(fuad);
            users.add(huyen);
            users.add(iannis);
            users.add(jessica);
            users.add(keng);
            users.add(keni);
            users.add(robert);
            users.add(safdar);
            users.add(scott);
            users.add(sergey);
            users.add(srini);
            users.add(bernard);
            saveOjects(s, users);

            //Create Groups - These groups have been created over time
            String activeDirectoryEnroller = "ActiveDirectory";
            String sharepointEnroller = "SharePoint";
            final TimeRelation engrGrpDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.JANUARY, 1), UnmodifiableDate.END_OF_TIME);
            final IUserGroup engineering = createGroup(new Long(1), "Engineering", "Engineering-Display", engrGrpDates, activeDirectoryEnroller);
            final TimeRelation dccGrpDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.OCTOBER, 1), UnmodifiableDate.END_OF_TIME);
            final IUserGroup dcc = createGroup(new Long(2), "DCC", "DCC-Display", dccGrpDates, sharepointEnroller);
            final TimeRelation agentGrpDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.OCTOBER, 1), UnmodifiableDate.END_OF_TIME);
            final IUserGroup agent = createGroup(new Long(3), "Agent", "Agent-Display", agentGrpDates, activeDirectoryEnroller);
            final TimeRelation pfGrpDates = new TimeRelation(SampleDataMgr.createDate(2004, Calendar.OCTOBER, 1), UnmodifiableDate.END_OF_TIME);
            final IUserGroup pf = createGroup(new Long(4), "PF", "PF-Display", pfGrpDates, sharepointEnroller);
            final TimeRelation toolsGrpDates = new TimeRelation(SampleDataMgr.createDate(2006, Calendar.FEBRUARY, 1), UnmodifiableDate.END_OF_TIME);
            final IUserGroup tools = createGroup(new Long(5), "Tools", "Tools-Display", activeDirectoryEnroller);
            final TimeRelation psrGrpDates = new TimeRelation(SampleDataMgr.createDate(2005, Calendar.OCTOBER, 1), UnmodifiableDate.END_OF_TIME);
            final IUserGroup psr = createGroup(new Long(6), "PSR", "PSR-Display", psrGrpDates, sharepointEnroller);

            final HashSet objToSave = new HashSet();
            objToSave.add(engineering);
            objToSave.add(dcc);
            objToSave.add(agent);
            objToSave.add(pf);
            objToSave.add(psr);
            objToSave.add(tools);

            objToSave.add(createUserGroupMember(chander, engineering, chanderDates));
            objToSave.add(createUserGroupMember(fuad, engineering, fuadDates));
            objToSave.add(createUserGroupMember(fuad, agent, fuadDates));
            objToSave.add(createUserGroupMember(huyen, engineering, huyenDates));
            objToSave.add(createUserGroupMember(huyen, agent, huyenDates));
            objToSave.add(createUserGroupMember(iannis, engineering, iannisDates));
            objToSave.add(createUserGroupMember(iannis, agent, iannisAgentDates));
            objToSave.add(createUserGroupMember(iannis, dcc, iannisDates));
            objToSave.add(createUserGroupMember(keni, engineering, keniDates));
            objToSave.add(createUserGroupMember(keni, pf, keniDates));
            objToSave.add(createUserGroupMember(keni, agent, keniAgentDates));
            objToSave.add(createUserGroupMember(safdar, engineering, safdarDates));
            objToSave.add(createUserGroupMember(safdar, dcc, safdarDates));
            objToSave.add(createUserGroupMember(sasha, engineering, sashaDates));
            objToSave.add(createUserGroupMember(sasha, pf, sashaPFDates));
            objToSave.add(createUserGroupMember(sasha, psr, sashaPSRDates));
            objToSave.add(createUserGroupMember(scott, engineering, scottDates));
            objToSave.add(createUserGroupMember(scott, dcc, scottDCCDates));
            objToSave.add(createUserGroupMember(scott, tools, scottToolsDates));
            objToSave.add(createUserGroupMember(sergey, engineering, sergeyDates));
            objToSave.add(createUserGroupMember(sergey, pf, sergeyDates));

            //Save groups
            saveOjects(s, objToSave);
            //Commit data
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }

    /**
     * Creates a sample data set for users and groups. The sample data is based
     * on the BlueJungle organization (so that tests can be written easily
     * without too much confusion).
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if the historical data creation fails
     */
    public static void createUsersAndGroups(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();

            IUser sasha = createUser(SASHA_ID, SASHA_USERNAME, SASHA_SID);
            IUser andy = createUser(ANDY_ID, ANDY_USERNAME, ANDY_SID);
            IUser boogie = createUser(BOOGIE_ID, BOOGIE_USERNAME, BOOGIE_SID);
            IUser chander = createUser(CHANDER_ID, CHANDER_USERNAME, CHANDER_SID);
            IUser davidL = createUser(DAVIDL_ID, DAVID_L_USERNAME, DAVIDL_SID);
            IUser davidS = createUser(DAVIDS_ID, DAVID_S_USERNAME, DAVIDS_SID);
            IUser fuad = createUser(FUAD_ID, FUAD_USERNAME, FUAD_SID);
            IUser huyen = createUser(HUYEN_ID, HUYEN_USERNAME, HUYEN_SID);
            IUser iannis = createUser(IANNIS_ID, IANNIS_USERNAME, IANNIS_SID);
            IUser jessica = createUser(JESSICA_ID, "Jessica Hart", JESSICA_SID);
            IUser keng = createUser(KENG_ID, KENG_USERNAME, KENG_SID);
            IUser keni = createUser(KENI_ID, KENI_USERNAME, KENI_SID);
            IUser robert = createUser(ROBERT_ID, ROBERT_USERNAME, ROBERT_SID);
            IUser safdar = createUser(SAFDAR_ID, SAFDAR_USERNAME, SAFDAR_SID);
            IUser scott = createUser(SCOTT_ID, SCOTT_USERNAME, SCOTT_SID);
            IUser sergey = createUser(SERGEY_ID, SERGEY_USERNAME, SERGEY_SID);
            IUser srini = createUser(SRINI_ID, SRINI_USERNAME, SRINI_SID);
            IUser bernard = createUser(BERNARD_ID, BERNARD_USERNAME, BERNARD_SID);

            //Save users
            final Set users = new HashSet();
            users.add(sasha);
            users.add(andy);
            users.add(boogie);
            users.add(chander);
            users.add(davidL);
            users.add(davidS);
            users.add(fuad);
            users.add(huyen);
            users.add(iannis);
            users.add(jessica);
            users.add(keng);
            users.add(keni);
            users.add(robert);
            users.add(safdar);
            users.add(scott);
            users.add(sergey);
            users.add(srini);
            users.add(bernard);
            saveOjects(s, users);

            //Create Groups
            String activeDirectoryEnroller = "ActiveDirectory";
            String sharepointEnroller = "SharePoint";
            IUserGroup engineering = createGroup(new Long(0), "Engineering", "Engineering-Display", activeDirectoryEnroller);
            IUserGroup marketing = createGroup(new Long(1), "Marketing", "Marketing-Display", sharepointEnroller);
            IUserGroup execs = createGroup(new Long(2), "Executives", "Executives-Display", activeDirectoryEnroller);
            IUserGroup it = createGroup(new Long(3), "IT", "IT-display", sharepointEnroller);
            IUserGroup interns = createGroup(new Long(4), "Interns", "Interns-Display", activeDirectoryEnroller);
            IUserGroup contractors = createGroup(new Long(5), "Contractors", "Contractors-Display", sharepointEnroller);
            IUserGroup qa = createGroup(new Long(6), "QA", "QA-Display", activeDirectoryEnroller);
            IUserGroup techpubs = createGroup(new Long(7), "Tech Pub", "Tech Pub-Display", sharepointEnroller);
            IUserGroup dcc = createGroup(new Long(8), "DCC", "DCC-Display", activeDirectoryEnroller);
            IUserGroup agent = createGroup(new Long(9), "Agent", "Agent-Display", sharepointEnroller);
            IUserGroup pf = createGroup(new Long(10), "Tech Pub", "Tech Pub-Display", activeDirectoryEnroller);

            final HashSet objToSave = new HashSet();
            objToSave.add(engineering);
            objToSave.add(marketing);
            objToSave.add(execs);
            objToSave.add(it);
            objToSave.add(interns);
            objToSave.add(contractors);
            objToSave.add(qa);
            objToSave.add(techpubs);
            objToSave.add(dcc);
            objToSave.add(agent);
            objToSave.add(pf);

            objToSave.add(createUserGroupMember(sasha, engineering));
            objToSave.add(createUserGroupMember(chander, engineering));
            objToSave.add(createUserGroupMember(fuad, engineering));
            objToSave.add(createUserGroupMember(huyen, engineering));
            objToSave.add(createUserGroupMember(iannis, engineering));
            objToSave.add(createUserGroupMember(keni, engineering));
            objToSave.add(createUserGroupMember(scott, engineering));
            objToSave.add(createUserGroupMember(sergey, engineering));
            objToSave.add(createUserGroupMember(safdar, engineering));
            objToSave.add(createUserGroupMember(fuad, agent));
            objToSave.add(createUserGroupMember(huyen, agent));
            objToSave.add(createUserGroupMember(iannis, dcc));
            objToSave.add(createUserGroupMember(scott, dcc));
            objToSave.add(createUserGroupMember(safdar, dcc));
            objToSave.add(createUserGroupMember(sasha, pf));
            objToSave.add(createUserGroupMember(keni, dcc));
            objToSave.add(createUserGroupMember(sergey, dcc));
            objToSave.add(createUserGroupMember(davidS, marketing));
            objToSave.add(createUserGroupMember(andy, marketing));
            objToSave.add(createUserGroupMember(keng, execs));
            objToSave.add(createUserGroupMember(bernard, execs));
            objToSave.add(createUserGroupMember(chander, execs));
            objToSave.add(createUserGroupMember(andy, execs));
            objToSave.add(createUserGroupMember(davidL, it));
            objToSave.add(createUserGroupMember(boogie, it));
            objToSave.add(createUserGroupMember(robert, interns));
            objToSave.add(createUserGroupMember(jessica, contractors));
            objToSave.add(createUserGroupMember(srini, qa));
            objToSave.add(createUserGroupMember(jessica, techpubs));

            //Save groups
            saveOjects(s, objToSave);
            //Commit data
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }

    }

    /**
     * Deletes all users and groups records
     * 
     * @param s
     *            Hibernate session to use
     * @throws HibernateException
     *             if database operation fails
     */
    public static void deleteUsersAndGroups(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            s.delete("from UserDO");
            s.delete("from UserGroupDO");
            s.delete("from UserGroupMemberDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        }
    }
}