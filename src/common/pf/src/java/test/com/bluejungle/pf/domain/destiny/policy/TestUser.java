package com.bluejungle.pf.domain.destiny.policy;

import java.util.Set;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/test/com/bluejungle/pf/domain/destiny/policy/TestUser.java#1 $:
 */

public class TestUser extends EnumBase {
    
    private static final long serialVersionUID = 1L;

    public static final TestUser HCHAN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3481", "hchan");
    public static final TestUser BMENG = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3468", "bmeng");
    public static final TestUser AYEN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3394", "ayen");
    public static final TestUser RLIN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-2628", "rlin");
    public static final TestUser AMORGAN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3414", "amorgan");
    public static final TestUser IHANEN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-1140", "ihanen");
    public static final TestUser SERGEY = new TestUser("S-1-5-21-668023798-3031861066-1043980994-1157", "sergey");
    public static final TestUser KENG = new TestUser("S-1-5-21-668023798-3031861066-1043980994-1114", "keng");
    public static final TestUser ISUNDIUS = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3322", "isundius");
    public static final TestUser AHAN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-1149", "ahan");
    public static final TestUser SGOLDSTEIN = new TestUser("S-1-5-21-668023798-3031861066-1043980994-2634", "sgoldstein");
    public static final TestUser HZHOU = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3455", "hzhou");
    public static final TestUser JGARFIELD = new TestUser("S-1-5-21-830805687-550985140-3285839444-1171", "james.garfield");
    public static final TestUser JPOLK = new TestUser("S-1-5-21-830805687-550985140-3285839444-1188", "james.polk");
    public static final TestUser JCARTER = new TestUser("S-1-5-21-830805687-550985140-3285839444-1164", "jimmy.carter");
    public static final TestUser RNIXON = new TestUser("S-1-5-21-830805687-550985140-3285839444-1186", "richard.nixon");
    public static final TestUser BCLINTON = new TestUser("S-1-5-21-830805687-550985140-3285839444-1166", "william.clinton");
    public static final TestUser JZHUANG = new TestUser("S-1-5-21-668023798-3031861066-1043980994-3510", "jzhuang");
    public static final TestUser BUILTIN_ADMIN = new TestUser("S-1-5-32-544", "builtin-administrator");

    public static final String SMALL_APP_FP = "(null):0:0:f7513948613aa6b5eb31fbb0726147833630e5d2";

    private String login;
    
    private TestUser(String sid, String login) {
        super(sid, TestUser.class);
        this.login = login;
    }
    
    public String getSID() {
        return getName();
    }
    
    public String getLogin() {
        return login;
    }
    
    public static Set<TestUser> getAllUsers() {
        return elements(TestUser.class);
    }
}
