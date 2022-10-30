/*
 * Created on Jul 14, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.daredevil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.ActiveDirectoryTestHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.RetrievalFailedException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncDispatcher;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.DistinguishedName;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.shared.tools.StringFormatter;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/daredevil/ActiveDirectoryDaredevil.java#1 $
 */

public class ActiveDirectoryDaredevil {
    enum Action{
        CREATE,
        DELETE,
        COPY,
        MOVE,
        RENAME,
        EDIT,
    }
    
    enum ObjectType{
        ROOT_DC("dc"),
        OU("ou"),
        GROUP("cn"),
        USER("cn"),
        HOST("cn"),
        ;
        
        final String rdnAttribute; 
        private ObjectType(String rdnAttribute) {
            this.rdnAttribute = rdnAttribute;
        }
        
        String getPrefix(){
            return rdnAttribute + "=";
        }
    }
    
    private final MyLDAPEntry rootOu;
    private final ActiveDirectoryTestHelper adUtil;
    private final DirSyncDispatcher dispatcher;
    
    private final Map<ObjectType, BaseActor> objectToActorMap;
    
    private ActiveDirectoryDaredevil(
            String server, 
            int port, 
            String rootDn,
            String username, 
            String password) {
        adUtil = new ActiveDirectoryTestHelper(server, port, rootDn, rootDn, username, password);
        
        objectToActorMap = new HashMap<ObjectType, BaseActor>();
        objectToActorMap.put(ObjectType.OU,     new OUActor(adUtil));
        objectToActorMap.put(ObjectType.GROUP,  new GroupActor(adUtil));
        objectToActorMap.put(ObjectType.HOST,   new HostActor(adUtil));
        objectToActorMap.put(ObjectType.USER,   new UserActor(adUtil));
        
        rootOu = new MyLDAPEntry(rootDn, ObjectType.ROOT_DC, null);
        
        dispatcher = new DirSyncDispatcher(
                null, 
                server, 
                port, 
                username,
                new ReversibleEncryptor().encrypt(password), 
                new String[] { rootDn },
                new String[] {}, 
                null,
                true, 
                null,
                false,
                512 );
    }
    
    public void init() throws NoSuchElementException, RetrievalFailedException {
        dispatcher.pull();

        while (dispatcher.hasMore()) {
            LDAPEntry entry = dispatcher.next();
            String dn = entry.getDN();
            String rootDn = rootOu.getDN();
            if(dn.length() < rootDn.length() + 1){
                continue;
            }
            dn = dn.substring(0, dn.length() - rootDn.length() - 1);
            
            String[] paths = DistinguishedName.splitPath(dn);
            //reverse the order
            ArrayUtils.reverse(paths);
            LinkedList<String> strs = new LinkedList<String>(Arrays.asList(paths));
            String rdn = strs.removeLast();
            
            MyLDAPEntry parentOfCurrentEntry = rootOu;
            if (!strs.isEmpty()) {
                parentOfCurrentEntry = add(strs, rootOu);
            }
            ObjectType objectType = null;
            for(BaseActor actor : objectToActorMap.values()){
                if(actor.isMatch(entry.getAttributeSet())){
                    objectType = actor.getObjectType();
                    break;
                }
            }
            if(objectType == null){
                System.out.println("can't find matching type for " + entry);
            }
            
            if(parentOfCurrentEntry.contains(rdn)){
                MyLDAPEntry current = parentOfCurrentEntry.get(rdn);
                current.update(entry.getAttributeSet());
            }else{
                MyLDAPEntry current = new MyLDAPEntry(rdn.split("=", 2)[1], objectType, entry.getAttributeSet());
                current.add(parentOfCurrentEntry);
            }
        }
    }
    
    private MyLDAPEntry add(Queue<String> dn, MyLDAPEntry currentNode){
        String head = dn.poll();
        if(head == null){
            return currentNode;
        }

        MyLDAPEntry matchedChild = null;
        for(MyLDAPEntry child : currentNode.chilren){
            if(head.equalsIgnoreCase(child.getRdn())){
                matchedChild = child;
            }
        }
        if(matchedChild == null){
            String[] split = head.split("=", 2);
            
            matchedChild = new MyLDAPEntry(split[1], ObjectType.OU, null);
            matchedChild.add(currentNode);
        }
        
        return add(dn, matchedChild);
    }
    
    public void cleanAll() throws LDAPException{
//        adUtil.removeAllUnitTestData(rootOu.getDN());
    }
    
    
    public void execute(Action action, ObjectType type) throws BullseyeException{
        BaseActor actor = objectToActorMap.get(type);
        switch (action) {
        case CREATE:
            actor.create();
            break;
        case DELETE:
            actor.delete();
            break;
        case COPY:
            actor.copy();
            break;
        case MOVE:
            actor.move();
            break;
        case EDIT:
            actor.edit();
            break;
        case RENAME:
            actor.rename();
            break;
        default:
            throw new IllegalArgumentException(action.name());
        }
    }
    
    
    public static void main(String[] args) throws BullseyeException, LDAPException, NoSuchElementException, RetrievalFailedException {
        Random r = new Random();
        
        ActiveDirectoryDaredevil daredevil = new ActiveDirectoryDaredevil(
                "linuxad01.linuxtest.bluejungle.com",
                389,
                "OU=horkan2,DC=linuxtest,DC=bluejungle,DC=com",
                "CN=Administrator,CN=Users,DC=linuxtest,DC=bluejungle,DC=com",
                "123blue!"
        );
        
//        ActiveDirectoryDaredevil daredevil = new ActiveDirectoryDaredevil(
//                "cuba.test.bluejungle.com",
//                389,
//                "ou=horkan,dc=test,dc=bluejungle,dc=com",
//                "cn=Jimmy Carter,ou=Users,ou=Fixed,dc=test,dc=bluejungle,dc=com",
//                new ReversibleEncryptor().encrypt("jimmy.carter")
//        );
        
       
        
        daredevil.cleanAll();
        daredevil.init();
        
        
//        boolean a = true;
//        if (a) {
//            print(daredevil.rootOu, 0);
//            return;
//        }
        
        int currentSize = MyLDAPEntry.ALL_ENTRIES.get(ObjectType.OU).size();
        for (int i = 0; i < 10 - currentSize; i++) {
            daredevil.execute(Action.CREATE, ObjectType.OU);
        }
        
        final Action[] actions = new Action[]{
                Action.CREATE,
                Action.DELETE,
//                Action.COPY,
                Action.MOVE,
//                Action.RENAME,
                Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT,
                Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT,
                Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT,
                Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT,
                Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT, Action.EDIT,
                
        };
        final ObjectType[] types = new ObjectType[] { 
                ObjectType.OU, 
                
                ObjectType.GROUP, ObjectType.GROUP, ObjectType.GROUP, ObjectType.GROUP, 
                
                ObjectType.USER, ObjectType.USER, ObjectType.USER, ObjectType.USER,
                ObjectType.USER, ObjectType.USER, ObjectType.USER, ObjectType.USER,
                ObjectType.USER, ObjectType.USER, ObjectType.USER, ObjectType.USER,
                ObjectType.USER, ObjectType.USER, ObjectType.USER, ObjectType.USER,
                
                ObjectType.HOST, ObjectType.HOST, ObjectType.HOST, ObjectType.HOST,
                ObjectType.HOST, ObjectType.HOST, ObjectType.HOST, ObjectType.HOST,
                ObjectType.HOST, ObjectType.HOST, ObjectType.HOST, ObjectType.HOST,
        };
        
        while(true){
            Action action = actions[r.nextInt(actions.length)];
            ObjectType type = types[r.nextInt(types.length)];
        
//            System.out.println(action + ", " + type);
            daredevil.execute(action, type);
        }
    }
    
    static void print(MyLDAPEntry entry, int level){
        System.out.println(StringFormatter.repeat(' ', level) + entry.getRdn());
        for(MyLDAPEntry c : entry.chilren){
            print(c, level+1);
        }
    }
    
    
}
