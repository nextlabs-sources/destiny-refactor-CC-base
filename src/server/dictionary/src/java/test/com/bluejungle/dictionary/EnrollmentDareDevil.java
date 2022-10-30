/*
 * Created on Nov 13, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestSuite;

import com.bluejungle.framework.expressions.PredicateConstants;
import com.nextlabs.random.RandomString;

/**
 * A very random test. The test will perform random action and expect the enrollment still work correctly.
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/EnrollmentDareDevil.java#1 $
 */

public class EnrollmentDareDevil extends AbstractDictionaryTest{
    public static TestSuite suite() {
        return new TestSuite(EnrollmentDareDevil.class);
    }
    
    enum Action{
        CREATE,
        DELETE,
//        COPY,
//        MOVE,
//        RENAME,
        EDIT,
    }
    
    private List<IMElement> elements;
    private List<IMGroup> enumGroups;
    private List<IMGroup> structGroups;
    
    private Random r = new Random();
    private com.nextlabs.random.Dictionary englishDictionary;
    
    private Collection<IMElementBase> elementsToSave;
    private Collection<IMElementBase> elementsToDelete;
    
    private Map<IElementType, TestElementType> elementTypeToTestTypeMap;
    
    @Override
    public void testSetupDictionary() throws Exception {
        init();
    }
    
    public void test1() throws DictionaryException, IOException{
        assertNull(elements);
        elements = new ArrayList<IMElement>();
        assertNull(enumGroups);
        enumGroups = new ArrayList<IMGroup>();
        assertNull(structGroups);
        structGroups = new ArrayList<IMGroup>();
        
        assertNull(elementsToSave);
        elementsToSave = new HashSet<IMElementBase>();
        assertNull(elementsToDelete);
        elementsToDelete = new HashSet<IMElementBase>();
        
        englishDictionary = new com.nextlabs.random.Dictionary(); 
        elementTypeToTestTypeMap = new HashMap<IElementType, TestElementType>();
        elementTypeToTestTypeMap.put(userStruct.type,    userStruct);
        elementTypeToTestTypeMap.put(hostStruct.type,    hostStruct);
        elementTypeToTestTypeMap.put(appStruct.type,     appStruct);
        elementTypeToTestTypeMap.put(contactStruct.type, contactStruct);
        
        Date asOf = dictionary.getLatestConsistentTime();
        assertNotNull(asOf);
        
        IDictionaryIterator<?> iter;
        
        iter = dictionary.query(PredicateConstants.TRUE, asOf, null, new Page(0, 1));
        assertFalse(iter.hasNext());
        iter.close();
        
        iter = dictionary.getEnumeratedGroups(PredicateConstants.TRUE, null, asOf, new Page(0, 1));
        assertFalse(iter.hasNext());
        iter.close();
        
        iter = dictionary.getStructuralGroups(PredicateConstants.TRUE, null, asOf, new Page(0, 1));
        assertFalse(iter.hasNext());
        iter.close();
        
        long startTime = System.currentTimeMillis();
        
        final int averageNumber = 1000;
        
        for (int totalCount = 0; System.currentTimeMillis() - startTime < 60000; ) {
            float f =  r.nextFloat();
            
            float deleteChance = totalCount < averageNumber ? 0f : averageNumber / 10f / totalCount;
            float createChance = totalCount < averageNumber ? 1f : averageNumber / 10f / totalCount;
            // float editChance = 1;   //always 1 since it is the last one
            
            Action action;
            if (f < deleteChance) {
                action = Action.DELETE;
            } else if (f < deleteChance + createChance) {
                action = Action.CREATE;
                totalCount++;
            } else {
                action = Action.EDIT;
            }
            
            IElementType elementType;
            f =  r.nextFloat();
            if (f < 0.98f) {
                elementType = IElementType.ENUM_GROUP_TYPE;
            } else if (f < 0.995f) {
                elementType = IElementType.STRUCT_GROUP_TYPE;
            } else {
                f =  r.nextFloat();
                if (f < 0.625f) {
                    elementType = userStruct.type;
                } else if (f < 0.95f) {
                    elementType = hostStruct.type;
                } else if (f < 0.975f) {
                    elementType = appStruct.type;
                } else {
                    elementType = contactStruct.type;
                }
            }
            
            act(action, elementType);
            
            //TODO flush or commit
        }
    }
    
    private static int[] number = new int[3];

    private void act(Action action, IElementType elementType) {
        switch (action) {
        case DELETE:
            number[0]++;
            delete(elementType);
            break;
        case CREATE:
            number[1]++;
            create(elementType);
            break;
        case EDIT:
            number[2]++;
            update(elementType);
            break;
        }
        
        
        
        System.out.println(String.format("%5d %5d %5d", number[0], number[1], number[2]));
    }
    
    private void delete(IElementType elementType) {
        IMElementBase element = null;
        if (elementType == IElementType.ENUM_GROUP_TYPE) {
            if (!enumGroups.isEmpty()) {
                element = enumGroups.remove(r.nextInt(enumGroups.size()));
            }
        } else if (elementType == IElementType.STRUCT_GROUP_TYPE) {
            if (!structGroups.isEmpty()) {
                element = structGroups.remove(r.nextInt(structGroups.size()));
            }
        } else {
            if (!elements.isEmpty()) {
                element = elements.remove(r.nextInt(elements.size()));
            }
        }
        if (element != null) {
            elementsToDelete.add(element);
        }
    }
    
    private void create(IElementType elementType) {
        IMElementBase elementBase = null;
        if (elementType == IElementType.ENUM_GROUP_TYPE) {
            DictionaryPath path = null;
            IMGroup group = enrollments[0].makeNewEnumeratedGroup(path, 
                    new DictionaryKey(path.toString().getBytes()));
            //TODO add children
            elementBase = group;
        } else if (elementType == IElementType.STRUCT_GROUP_TYPE) {
            DictionaryPath path = null;
            IMGroup group = enrollments[0].makeNewStructuralGroup(path, 
                    new DictionaryKey(path.toString().getBytes()));
            //TODO add children
            elementBase = group;
        } else {
            elementBase = createElement(elementTypeToTestTypeMap.get(elementType));
        }
        
        elementBase.setDisplayName(randomString());
        elementBase.setUniqueName(randomString());
        elementsToSave.add(elementBase);
    }
    
    private void update(IElementType elementType) {
        IMElementBase elementBase = null;
        
        float f = r.nextFloat();
        boolean updateBaseFields;
        boolean updateValues;
        if( f < 0.01){
            updateBaseFields = true;
            updateValues = true;
        } else if ( f< 0.06){
            updateBaseFields = true;
            updateValues = false;
        } else {
            updateBaseFields = false;
            updateValues = true;
        }
        
        assert updateBaseFields || updateValues;
        
        if (elementType == IElementType.ENUM_GROUP_TYPE) {
            //TODO
        } else if (elementType == IElementType.STRUCT_GROUP_TYPE) {
            //TODO
        } else {
            if (!elements.isEmpty()) {
                IMElement element = elements.get(r.nextInt(elements.size()));
                
                if (updateValues) {
                    elementType = element.getType();
                    TestElementType testElementType = elementTypeToTestTypeMap.get(elementType);
                    
                    List<IElementField> allFields = new ArrayList<IElementField>(testElementType.getAllFields());
                    int updateFieldSize = r.nextInt(allFields.size()) + 1;
                    IElementField[] updateFields = new IElementField[updateFieldSize];
                    for (int i = 0; i < updateFieldSize; i++) {
                        updateFields[i] = allFields.remove(r.nextInt(allFields.size()));
                    }
                    
                    for (IElementField field : updateFields) {
                        element.setValue(field, createValue(field.getType()));
                    }
                }
                elementBase = element;
            }
        }
        
        if (updateBaseFields) {
            int updateSize = r.nextInt(4) + 1;
            List<Character> fieldsEnum = new ArrayList<Character>();
            fieldsEnum.add('D');
            fieldsEnum.add('E');
            fieldsEnum.add('U');
            fieldsEnum.add('P');

            char[] updateFields = new char[updateSize];
            for (int i = 0; i < updateSize; i++) {
                updateFields[i] = fieldsEnum.remove(r.nextInt(fieldsEnum.size()));
            }

            for (char updateField : updateFields) {
                switch (updateField) {
                case 'D':
                    elementBase.setDisplayName(randomString());
                    break;
                case 'E':
                    //TODO
                    // elementBase.setExternalKey(key);
                    break;
                case 'U':
                    elementBase.setUniqueName(randomString());
                    break;
                case 'P':
                    //TODO
                    // elementBase.setPath(path);
                    break;
                }
            }
        }
        
        elementsToSave.add(elementBase);
    }
    
    private IMElement createElement(TestElementType testElementType) {
        testElementType.getAllFields();
        DictionaryPath path = null;
        IMElement element = enrollments[0].makeNewElement(path, testElementType.type, 
                new DictionaryKey(path.toString().getBytes()));
        
        for(IElementField field : testElementType.getAllFields()){
            if(r.nextFloat() < 0.05){
                //there is a chance the field is missed.
                continue;
            }
            element.setValue(field, createValue(field.getType()));
        }
        return element;
    }

    private Object createValue(ElementFieldType fieldType) {
        Object value;
        if (fieldType == ElementFieldType.STRING_ARRAY) {
            int size = r.nextInt(10);
            String[] temp = new String[size];
            
            for(int i =0; i< size; i++){
                temp[i] = randomString().toLowerCase();
            }
            value = temp;
        } else if (fieldType == ElementFieldType.STRING) {
            value = randomString().toLowerCase();
        } else if (fieldType == ElementFieldType.CS_STRING) {
            value = randomString();
        } else if (fieldType == ElementFieldType.NUMBER) {
            value = r.nextLong();
        } else if (fieldType == ElementFieldType.DATE) {
            value = new Date(Math.abs(r.nextLong()));
        } else if (fieldType == ElementFieldType.LONG_STRING) {
            int size = r.nextInt(10);
            Long[] temp = new Long[size];
            for(int i =0; i< size; i++){
                temp[i] = r.nextLong();
            }
            value = temp;
        } else {
            throw new RuntimeException(fieldType.toString());
        }
        return value;
    }
    
    private String randomString(){
        return r.nextBoolean() 
                ? englishDictionary.gernerateRandomString(1, 1 + r.nextInt(5), " ", false)
                : RandomString.getRandomString(0, r.nextInt(30), RandomString.PRINT);
    }
}
