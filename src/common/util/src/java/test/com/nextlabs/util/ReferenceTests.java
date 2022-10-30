package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/ReferenceTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;
import com.nextlabs.util.ref.Reference;

/**
 * Tests for the base reference class.
 *
 * @author Sergey Kalinichenko
 */
public class ReferenceTests {

    /**
     * The referenced name.
     */
    private static final Path REF_PATH = new Path(new String[] {"abc", "def"});

    /**
     * The referenced ID.
     */
    private static final long REF_ID = 1;

    @Test
    public void createByPath() {
        IReference<Long> ref = ref(REF_PATH, Long.class);
        assertEquals(REF_PATH, ref.getPath());
        assertTrue(ref.isByPath());
    }

    @Test(expected=NullPointerException.class)
    public void createByPathNull() {
        ref(null, Long.class);
    }

    @Test
    public void createById() {
        IReference<Long> ref = ref(REF_ID, Long.class);
        assertEquals(REF_ID, ref.getId());
        assertFalse(ref.isByPath());
    }

    @Test(expected=NullPointerException.class)
    public void createByIdNullClass() {
        ref(REF_ID, null);
    }

    @Test(expected=NullPointerException.class)
    public void createByPathNullClass() {
        ref(REF_PATH, null);
    }

    @Test
    public void defaultCanResolveReturnsFalse() {
        assertEquals(false, ref(REF_ID, Long.class).canResolve());
    }

    @Test(expected=IllegalStateException.class)
    public void cannotResolveByDefault() {
        ref(REF_ID, Long.class).get();
    }

    @Test
    public void storeRefClassById() {
        assertEquals(Long.class, ref(REF_ID, Long.class).getRefClass());
    }

    @Test
    public void storeRefClassByPath() {
        assertEquals(String.class, ref(REF_PATH, String.class).getRefClass());
    }

    @Test
    public void resolveOnceByDefault() {
        final int[] callCount = new int[] {0};
        IReference<Long> ref = new Reference<Long>(0, Long.class) {
            @Override
            public boolean canResolve() {
                return true;
            }
            @Override
            protected Long resolve() {
                callCount[0]++;
                return 12345L;
            }
        };
        for ( int i = 0 ; i != 10 ; i++ ) {
            assertEquals(12345L, ref.get());
        }
        assertEquals(1, callCount[0]);
    }

    @Test(expected=IllegalStateException.class)
    public void getIdInvalid() {
        IReference<Long> ref = ref(REF_PATH, Long.class);
        ref.getId();
    }

    @Test(expected=IllegalStateException.class)
    public void getNameInvalid() {
        IReference<Long> ref = ref(REF_ID, Long.class);
        ref.getPath();
    }

    @Test
    public void toStringId() {
        IReference<Long> ref = ref(REF_ID, Long.class);
        assertEquals("id "+REF_ID, ref.toString());
    }

    @Test
    public void toStringName() {
        IReference<Long> ref = ref(REF_PATH, Long.class);
        assertEquals("#"+REF_PATH, ref.toString());
    }

    @Test
    public void equalsByIdTrue() {
        IReference<Long> a = ref(REF_ID, Long.class);
        IReference<Long> b = ref(REF_ID, Long.class);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, a);
        assertEquals(b, b);
    }

    @Test
    public void equalsByIdFalse() {
        IReference<Long> a = ref(REF_ID, Long.class);
        IReference<Long> b = ref(REF_ID+1, Long.class);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void equalsByPathTrue() {
        IReference<Long> a = ref(REF_PATH, Long.class);
        IReference<Long> b = ref(REF_PATH, Long.class);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a, a);
        assertEquals(b, b);
    }

    @Test
    public void equalsByPathFalse() {
        IReference<Long> a = ref(REF_PATH, Long.class);
        IReference<Long> b = ref(new Path("xyz"), Long.class);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    public void notEqualsByPathById() {
        IReference<Long> a = ref(REF_PATH, Long.class);
        IReference<Long> b = ref(REF_ID, Long.class);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void hashCodeWorks() {
        IReference<Long> refs[] = new IReference[] {
            ref(REF_PATH, Long.class)
        ,   ref(REF_PATH, Long.class)
        ,   ref(REF_ID, Long.class)
        ,   ref(REF_ID, Long.class)
        ,   ref(REF_ID+1, Long.class)
        ,   ref(new Path("xyz"), Long.class)
        };
        int equalCount = 0;
        for (int i = 0 ; i != refs.length ; i++) {
            for (int j = i+1 ; j != refs.length ; j++) {
                if (refs[i].equals(refs[j])) {
                    equalCount++;
                    assertEquals(refs[i].hashCode(), refs[j].hashCode());
                }
            }
        }
        assertTrue(equalCount >= 2);
    }

    @Test
    public void equalsWithWrongTypeFalse() {
        assertFalse(ref(REF_PATH, Long.class).equals(""));
        assertFalse(ref(REF_ID, Long.class).equals(""));
    }

    @Test
    public void equalsWithNullFalse() {
        assertFalse(ref(REF_PATH, Long.class).equals(null));
        assertFalse(ref(REF_ID, Long.class).equals(null));
    }

    private static <T> IReference<T> ref(Path path, Class<T> type) {
        return IReferenceFactory.DEFAULT.create(path, type);
    }

    private static <T> IReference<T> ref(long id, Class<T> type) {
        return IReferenceFactory.DEFAULT.create(id, type);
    }

}
