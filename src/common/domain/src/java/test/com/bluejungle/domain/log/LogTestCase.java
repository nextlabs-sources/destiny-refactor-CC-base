package com.bluejungle.domain.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/log/LogTestCase.java#1 $
 *
 */
public abstract class LogTestCase extends TestCase {
    
    public LogTestCase() {
        super();
    }

    public LogTestCase(String arg0) {
        super(arg0);
    }
    
    ObjectInputStream externalizeData(Externalizable[] data) throws IOException {

        ByteArrayOutputStream baOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baOut);
        for (int i = 0; i < data.length; i++) {
            data[i].writeExternal(out);
        }
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baOut.toByteArray()));
        return in;
    }    

}
