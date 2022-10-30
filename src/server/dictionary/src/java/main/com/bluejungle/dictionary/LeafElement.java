/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/LeafElement.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.Arrays;
import java.util.Date;

/**
 * This class represents a leaf element of a dictionary.
 *
 * Leaf elements have two mappings - one for the users, and another one
 * for the database. The database mapping uses generated field
 * access methods. The data is stored in the <code>data</code> array.
 */
class LeafElement extends DictionaryElementBase implements IMElement {
    /** The type of this element. */
    private IElementType type;

    /** The data for <code>String</code> elements of this object */
    private String[] strings;

    /** The data for <code>Long</code> elements of this object */
    private Long[] nums;

    /** The data for <code>Date</code> elements of this object */
    private Date[] dates;

    /** The data for Numeric Array elements of this object */
    private long[][] numArrays;
    
    /** The data for long string elements of this object */
    private String[] longStrings;

    /**
     * This is a package-private constructor for Hibernate.
     */
    LeafElement() {
    }

    /**
     * Creates a LeafElement of the specific type.
     * @param type
     */
    public LeafElement( DictionaryPath path, IElementType type, DictionaryKey key, Enrollment enrollment ) {
        super(path, enrollment, key);
        if ( key == null ) {
            throw new NullPointerException("key");
        }
        setType( type );
    }

    /**
     * This is a package-private method for Hibernate.
     */
    void setType( IElementType type ) {
        if ( type == null ) {
            throw new NullPointerException("type");
        }
        this.type = type;
        int strCount = 0, numCount = 0, datCount = 0, numArrayCount = 0, longStrCount = 0;
        for (IElementField field : type.getAllFields()) {
            ElementFieldType fieldType = field.getType();
            if (fieldType == ElementFieldType.NUMBER) {
                numCount++;
            } else if (fieldType == ElementFieldType.DATE) {
                datCount++;
            } else if (fieldType == ElementFieldType.NUM_ARRAY) {
                numArrayCount++;
            } else if (fieldType == ElementFieldType.LONG_STRING) {
            	longStrCount++;
            } else if (fieldType == ElementFieldType.STRING
                    || fieldType == ElementFieldType.CS_STRING
                    || fieldType == ElementFieldType.STRING_ARRAY ) {
                strCount++;
            } else {
                throw new IllegalArgumentException(fieldType.getName());
            }
        }
        strings = new String[strCount];
        nums = new Long[numCount];
        dates = new Date[datCount];
        numArrays = new long[numArrayCount][];
        longStrings = new String[longStrCount];
    }

    /**
     * @see IElement#getType()
     */
    public IElementType getType() {
        return type;
    }

    /**
     * @see IElement#getValue(IElementField)
     */
    public Object getValue( IElementField field ) {
        if ( field == null ) {
            throw new NullPointerException("field");
        }
        return field.getValue( this );
    }

    /**
     * @see IMElement#setValue(IElementField, Object)
     */
    public boolean setValue( IElementField field, Object value ) {
        if ( field == null ) {
            throw new NullPointerException("field");
        }
        boolean same = false;
        if ( !( isNew() || isUpdated() ) ) {
            Object old = field.getValue( this );
            if (old == value) {
            	//they are the same object or they are both null
            	same = true;		
            } else if (old != null && value != null) {
                //they are not null
                if (old.getClass().isArray() && value.getClass().isArray()) {
                    same = Arrays.equals((Object[]) old, (Object[]) value);
                } else {
                    same = old.equals(value);
                }
            }else{
                //either one of them is null
                same = false;
            }
        }
        if ( !same ) {
            onUpdate();
            field.setValue( this, value );
        }
        return !same;
    }
    

	/**
     * Returns the value of the given field of the element
     * specified by an external name. The external name is looked up
     * using the enrollment.
     * @param externalName the external name of the field.
     * @return the value of the given field of the element.
     * @throws IllegalArgumentException if the external name
     * is not present in the enrollment.
     * 
     * @deprecated call {@link #getValue(IElementField)} instead
     */
    @Deprecated
    public Object getValue(String externalName) {
        //TODO may need to return more than one value.
        return getValue( getEnrollment().lookupField(type, externalName)[0]);
    }

    /**
     * Sets he value of the given field of the element
     * specified by an external name. The external name is looked up
     * using the enrollment.
     * @param externalName the external name of the field.
     * @param value the new value of the field.
     * @throws IllegalArgumentException if the external name
     * is not present in the enrollment.
     * 
     * @deprecated call {@link #setValue(IElementField, Object)} instead
     */
    @Deprecated
    public boolean setValue(String externalName, Object value) {
        boolean isSet = false;
        for (IElementField field : getEnrollment().lookupField(type, externalName)) {
            isSet |= setValue(field, value);
        }
        return isSet;
    }

    /**
     * @see DictionaryElementBase#deepCopy()
     */
    protected DictionaryElementBase deepCopy() {
        if ( type == null ) {
            throw new IllegalStateException("Element of unknown type");
        }
        LeafElement res = new LeafElement();
        res.type = type;
        res.dates = dates.clone();
        res.nums = nums.clone();
        res.strings = strings.clone();
        res.longStrings = longStrings.clone();
        return res;
    }

    /**
     * @see IElementBase#accept(IElementVisitor)
     */
    public void accept(IElementVisitor visitor) {
        visitor.visitLeaf(this);
    }

    @Override
    public String toString() {
        return "L:" + super.toString();
    }
    
    private <T> T getValue(T[] data, int pos) {
        return (data != null && pos < data.length) ? data[pos] : null;
    }
    
    private <T> void setValue(T[] data, int pos, T val) {
        if ( data != null && pos < data.length ) {
            data[pos] = val;
        }
    }

    /**
     * Gets the string value at the specified position.
     * @param pos the position at which to get the string value.
     * @return the String at the specified position.
     */
    private String getString( int pos ) {
        return getValue(strings, pos);
    }

    /**
     * Sets the string at the specified position.
     * @param pos the position at which to set the string.
     * @param val the value to set at the specified position.
     */
    private void setString( int pos, String val ) {
        setValue(strings, pos, val);
    }

    /**
     * Gets the number value at the specified position.
     * @param pos the position at which to get the numeric value.
     * @return the <code>Long</code> at the specified position.
     */
    private Long getNumber( int pos ) {
        return getValue(nums, pos);
    }

    /**
     * Sets the number at the specified position.
     * @param pos the position at which to set the number.
     * @param val the value to set at the specified position.
     */
    private void setNumber( int pos, Long val ) {
        setValue(nums, pos, val);
    }

    /**
     * Gets the number array value at the specified position.
     * @param pos the position at which to get the numeric array value.
     * @return the <code>long[]</code> at the specified position.
     */
    private long[] getNumberArray( int pos ) {
        return getValue(numArrays, pos);
    }

    /**
     * Sets the number array at the specified position.
     * @param pos the position at which to set the number array.
     * @param val the value to set at the specified position.
     */
    private void setNumberArray( int pos, long[] val ) {
        setValue(numArrays, pos, val);
    }

    /**
     * Gets the date value at the specified position.
     * @param pos the position at which to get the date value.
     * @return the Date at the specified position.
     */
    private Date getDate( int pos ) {
        return getValue(dates, pos);
    }

    /**
     * Sets the date at the specified position.
     * @param pos the position at which to set the date.
     * @param val the value to set at the specified position.
     */
    private void setDate( int pos, Date val ) {
        setValue(dates, pos, val);
    }

    /**
     * Gets the string value at the specified position.
     * @param pos the position at which to get the string value.
     * @return the String at the specified position.
     */
    private String getLongString( int pos ) {
        return getValue(longStrings, pos);
    }

    /**
     * Sets the string at the specified position.
     * @param pos the position at which to set the string.
     * @param val the value to set at the specified position.
     */
    private void setLongString( int pos, String val ) {
        setValue(longStrings, pos, val);
    }

    /***************************************************/
    /**       GENERATED METHODS - DO NOT TOUCH        **/
    /***************************************************/
    String getString00() { return getString( 0 ); }
    void setString00( String val ) { setString( 0, val ); }
    String getString01() { return getString( 1 ); }
    void setString01( String val ) { setString( 1, val ); }
    String getString02() { return getString( 2 ); }
    void setString02( String val ) { setString( 2, val ); }
    String getString03() { return getString( 3 ); }
    void setString03( String val ) { setString( 3, val ); }
    String getString04() { return getString( 4 ); }
    void setString04( String val ) { setString( 4, val ); }
    String getString05() { return getString( 5 ); }
    void setString05( String val ) { setString( 5, val ); }
    String getString06() { return getString( 6 ); }
    void setString06( String val ) { setString( 6, val ); }
    String getString07() { return getString( 7 ); }
    void setString07( String val ) { setString( 7, val ); }
    String getString08() { return getString( 8 ); }
    void setString08( String val ) { setString( 8, val ); }
    String getString09() { return getString( 9 ); }
    void setString09( String val ) { setString( 9, val ); }
    String getString10() { return getString( 10 ); }
    void setString10( String val ) { setString( 10, val ); }
    String getString11() { return getString( 11 ); }
    void setString11( String val ) { setString( 11, val ); }
    String getString12() { return getString( 12 ); }
    void setString12( String val ) { setString( 12, val ); }
    String getString13() { return getString( 13 ); }
    void setString13( String val ) { setString( 13, val ); }
    String getString14() { return getString( 14 ); }
    void setString14( String val ) { setString( 14, val ); }
    String getString15() { return getString( 15 ); }
    void setString15( String val ) { setString( 15, val ); }
    String getString16() { return getString( 16 ); }
    void setString16( String val ) { setString( 16, val ); }
    String getString17() { return getString( 17 ); }
    void setString17( String val ) { setString( 17, val ); }
    String getString18() { return getString( 18 ); }
    void setString18( String val ) { setString( 18, val ); }
    String getString19() { return getString( 19 ); }
    void setString19( String val ) { setString( 19, val ); }
    String getString20() { return getString( 20 ); }
    void setString20( String val ) { setString( 20, val ); }
    String getString21() { return getString( 21 ); }
    void setString21( String val ) { setString( 21, val ); }
    String getString22() { return getString( 22 ); }
    void setString22( String val ) { setString( 22, val ); }
    String getString23() { return getString( 23 ); }
    void setString23( String val ) { setString( 23, val ); }
    String getString24() { return getString( 24 ); }
    void setString24( String val ) { setString( 24, val ); }
    String getString25() { return getString( 25 ); }
    void setString25( String val ) { setString( 25, val ); }
    String getString26() { return getString( 26 ); }
    void setString26( String val ) { setString( 26, val ); }
    String getString27() { return getString( 27 ); }
    void setString27( String val ) { setString( 27, val ); }
    String getString28() { return getString( 28 ); }
    void setString28( String val ) { setString( 28, val ); }
    String getString29() { return getString( 29 ); }
    void setString29( String val ) { setString( 29, val ); }
    String getString30() { return getString( 30 ); }
    void setString30( String val ) { setString( 30, val ); }
    String getString31() { return getString( 31 ); }
    void setString31( String val ) { setString( 31, val ); }
    String getString32() { return getString( 32 ); }
    void setString32( String val ) { setString( 32, val ); }
    String getString33() { return getString( 33 ); }
    void setString33( String val ) { setString( 33, val ); }
    String getString34() { return getString( 34 ); }
    void setString34( String val ) { setString( 34, val ); }
    String getString35() { return getString( 35 ); }
    void setString35( String val ) { setString( 35, val ); }
    String getString36() { return getString( 36 ); }
    void setString36( String val ) { setString( 36, val ); }
    String getString37() { return getString( 37 ); }
    void setString37( String val ) { setString( 37, val ); }
    String getString38() { return getString( 38 ); }
    void setString38( String val ) { setString( 38, val ); }
    String getString39() { return getString( 39 ); }
    void setString39( String val ) { setString( 39, val ); }
    String getString40() { return getString( 40 ); }
    void setString40( String val ) { setString( 40, val ); }
    String getString41() { return getString( 41 ); }
    void setString41( String val ) { setString( 41, val ); }
    String getString42() { return getString( 42 ); }
    void setString42( String val ) { setString( 42, val ); }
    String getString43() { return getString( 43 ); }
    void setString43( String val ) { setString( 43, val ); }
    String getString44() { return getString( 44 ); }
    void setString44( String val ) { setString( 44, val ); }
    String getString45() { return getString( 45 ); }
    void setString45( String val ) { setString( 45, val ); }
    String getString46() { return getString( 46 ); }
    void setString46( String val ) { setString( 46, val ); }
    String getString47() { return getString( 47 ); }
    void setString47( String val ) { setString( 47, val ); }
    String getString48() { return getString( 48 ); }
    void setString48( String val ) { setString( 48, val ); }
    String getString49() { return getString( 49 ); }
    void setString49( String val ) { setString( 49, val ); }
    String getString50() { return getString( 50 ); }
    void setString50( String val ) { setString( 50, val ); }
    String getString51() { return getString( 51 ); }
    void setString51( String val ) { setString( 51, val ); }
    String getString52() { return getString( 52 ); }
    void setString52( String val ) { setString( 52, val ); }
    String getString53() { return getString( 53 ); }
    void setString53( String val ) { setString( 53, val ); }
    String getString54() { return getString( 54 ); }
    void setString54( String val ) { setString( 54, val ); }
    String getString55() { return getString( 55 ); }
    void setString55( String val ) { setString( 55, val ); }
    String getString56() { return getString( 56 ); }
    void setString56( String val ) { setString( 56, val ); }
    String getString57() { return getString( 57 ); }
    void setString57( String val ) { setString( 57, val ); }
    String getString58() { return getString( 58 ); }
    void setString58( String val ) { setString( 58, val ); }
    String getString59() { return getString( 59 ); }
    void setString59( String val ) { setString( 59, val ); }
    String getString60() { return getString( 60 ); }
    void setString60( String val ) { setString( 60, val ); }
    String getString61() { return getString( 61 ); }
    void setString61( String val ) { setString( 61, val ); }
    String getString62() { return getString( 62 ); }
    void setString62( String val ) { setString( 62, val ); }
    String getString63() { return getString( 63 ); }
    void setString63( String val ) { setString( 63, val ); }
    String getString64() { return getString( 64 ); }
    void setString64( String val ) { setString( 64, val ); }
    String getString65() { return getString( 65 ); }
    void setString65( String val ) { setString( 65, val ); }
    String getString66() { return getString( 66 ); }
    void setString66( String val ) { setString( 66, val ); }
    String getString67() { return getString( 67 ); }
    void setString67( String val ) { setString( 67, val ); }
    String getString68() { return getString( 68 ); }
    void setString68( String val ) { setString( 68, val ); }
    String getString69() { return getString( 69 ); }
    void setString69( String val ) { setString( 69, val ); }
    String getString70() { return getString( 70 ); }
    void setString70( String val ) { setString( 70, val ); }
    String getString71() { return getString( 71 ); }
    void setString71( String val ) { setString( 71, val ); }
    String getString72() { return getString( 72 ); }
    void setString72( String val ) { setString( 72, val ); }
    String getString73() { return getString( 73 ); }
    void setString73( String val ) { setString( 73, val ); }
    String getString74() { return getString( 74 ); }
    void setString74( String val ) { setString( 74, val ); }
    String getString75() { return getString( 75 ); }
    void setString75( String val ) { setString( 75, val ); }
    String getString76() { return getString( 76 ); }
    void setString76( String val ) { setString( 76, val ); }
    String getString77() { return getString( 77 ); }
    void setString77( String val ) { setString( 77, val ); }
    String getString78() { return getString( 78 ); }
    void setString78( String val ) { setString( 78, val ); }
    String getString79() { return getString( 79 ); }
    void setString79( String val ) { setString( 79, val ); }
    String getString80() { return getString( 80 ); }
    void setString80( String val ) { setString( 80, val ); }
    String getString81() { return getString( 81 ); }
    void setString81( String val ) { setString( 81, val ); }
    String getString82() { return getString( 82 ); }
    void setString82( String val ) { setString( 82, val ); }
    String getString83() { return getString( 83 ); }
    void setString83( String val ) { setString( 83, val ); }
    String getString84() { return getString( 84 ); }
    void setString84( String val ) { setString( 84, val ); }
    String getString85() { return getString( 85 ); }
    void setString85( String val ) { setString( 85, val ); }
    String getString86() { return getString( 86 ); }
    void setString86( String val ) { setString( 86, val ); }
    String getString87() { return getString( 87 ); }
    void setString87( String val ) { setString( 87, val ); }
    String getString88() { return getString( 88 ); }
    void setString88( String val ) { setString( 88, val ); }
    String getString89() { return getString( 89 ); }
    void setString89( String val ) { setString( 89, val ); }
    String getString90() { return getString( 90 ); }
    void setString90( String val ) { setString( 90, val ); }
    String getString91() { return getString( 91 ); }
    void setString91( String val ) { setString( 91, val ); }
    String getString92() { return getString( 92 ); }
    void setString92( String val ) { setString( 92, val ); }
    String getString93() { return getString( 93 ); }
    void setString93( String val ) { setString( 93, val ); }
    String getString94() { return getString( 94 ); }
    void setString94( String val ) { setString( 94, val ); }
    String getString95() { return getString( 95 ); }
    void setString95( String val ) { setString( 95, val ); }
    String getString96() { return getString( 96 ); }
    void setString96( String val ) { setString( 96, val ); }
    String getString97() { return getString( 97 ); }
    void setString97( String val ) { setString( 97, val ); }
    String getString98() { return getString( 98 ); }
    void setString98( String val ) { setString( 98, val ); }
    String getString99() { return getString( 99 ); }
    void setString99( String val ) { setString( 99, val ); }
    Long getNumber00() { return getNumber( 0 ); }
    void setNumber00( Long val ) { setNumber( 0, val ); }
    Long getNumber01() { return getNumber( 1 ); }
    void setNumber01( Long val ) { setNumber( 1, val ); }
    Long getNumber02() { return getNumber( 2 ); }
    void setNumber02( Long val ) { setNumber( 2, val ); }
    Long getNumber03() { return getNumber( 3 ); }
    void setNumber03( Long val ) { setNumber( 3, val ); }
    Long getNumber04() { return getNumber( 4 ); }
    void setNumber04( Long val ) { setNumber( 4, val ); }
    Long getNumber05() { return getNumber( 5 ); }
    void setNumber05( Long val ) { setNumber( 5, val ); }
    Long getNumber06() { return getNumber( 6 ); }
    void setNumber06( Long val ) { setNumber( 6, val ); }
    Long getNumber07() { return getNumber( 7 ); }
    void setNumber07( Long val ) { setNumber( 7, val ); }
    Long getNumber08() { return getNumber( 8 ); }
    void setNumber08( Long val ) { setNumber( 8, val ); }
    Long getNumber09() { return getNumber( 9 ); }
    void setNumber09( Long val ) { setNumber( 9, val ); }
    Long getNumber10() { return getNumber( 10 ); }
    void setNumber10( Long val ) { setNumber( 10, val ); }
    Long getNumber11() { return getNumber( 11 ); }
    void setNumber11( Long val ) { setNumber( 11, val ); }
    Long getNumber12() { return getNumber( 12 ); }
    void setNumber12( Long val ) { setNumber( 12, val ); }
    Long getNumber13() { return getNumber( 13 ); }
    void setNumber13( Long val ) { setNumber( 13, val ); }
    Long getNumber14() { return getNumber( 14 ); }
    void setNumber14( Long val ) { setNumber( 14, val ); }
    Long getNumber15() { return getNumber( 15 ); }
    void setNumber15( Long val ) { setNumber( 15, val ); }
    Long getNumber16() { return getNumber( 16 ); }
    void setNumber16( Long val ) { setNumber( 16, val ); }
    Long getNumber17() { return getNumber( 17 ); }
    void setNumber17( Long val ) { setNumber( 17, val ); }
    Long getNumber18() { return getNumber( 18 ); }
    void setNumber18( Long val ) { setNumber( 18, val ); }
    Long getNumber19() { return getNumber( 19 ); }
    void setNumber19( Long val ) { setNumber( 19, val ); }
    Long getNumber20() { return getNumber( 20 ); }
    void setNumber20( Long val ) { setNumber( 20, val ); }
    Long getNumber21() { return getNumber( 21 ); }
    void setNumber21( Long val ) { setNumber( 21, val ); }
    Long getNumber22() { return getNumber( 22 ); }
    void setNumber22( Long val ) { setNumber( 22, val ); }
    Long getNumber23() { return getNumber( 23 ); }
    void setNumber23( Long val ) { setNumber( 23, val ); }
    Long getNumber24() { return getNumber( 24 ); }
    void setNumber24( Long val ) { setNumber( 24, val ); }
    Long getNumber25() { return getNumber( 25 ); }
    void setNumber25( Long val ) { setNumber( 25, val ); }
    Long getNumber26() { return getNumber( 26 ); }
    void setNumber26( Long val ) { setNumber( 26, val ); }
    Long getNumber27() { return getNumber( 27 ); }
    void setNumber27( Long val ) { setNumber( 27, val ); }
    Long getNumber28() { return getNumber( 28 ); }
    void setNumber28( Long val ) { setNumber( 28, val ); }
    Long getNumber29() { return getNumber( 29 ); }
    void setNumber29( Long val ) { setNumber( 29, val ); }
    Long getNumber30() { return getNumber( 30 ); }
    void setNumber30( Long val ) { setNumber( 30, val ); }
    Long getNumber31() { return getNumber( 31 ); }
    void setNumber31( Long val ) { setNumber( 31, val ); }
    Long getNumber32() { return getNumber( 32 ); }
    void setNumber32( Long val ) { setNumber( 32, val ); }
    Long getNumber33() { return getNumber( 33 ); }
    void setNumber33( Long val ) { setNumber( 33, val ); }
    Long getNumber34() { return getNumber( 34 ); }
    void setNumber34( Long val ) { setNumber( 34, val ); }
    Long getNumber35() { return getNumber( 35 ); }
    void setNumber35( Long val ) { setNumber( 35, val ); }
    Long getNumber36() { return getNumber( 36 ); }
    void setNumber36( Long val ) { setNumber( 36, val ); }
    Long getNumber37() { return getNumber( 37 ); }
    void setNumber37( Long val ) { setNumber( 37, val ); }
    Long getNumber38() { return getNumber( 38 ); }
    void setNumber38( Long val ) { setNumber( 38, val ); }
    Long getNumber39() { return getNumber( 39 ); }
    void setNumber39( Long val ) { setNumber( 39, val ); }
    Long getNumber40() { return getNumber( 40 ); }
    void setNumber40( Long val ) { setNumber( 40, val ); }
    Long getNumber41() { return getNumber( 41 ); }
    void setNumber41( Long val ) { setNumber( 41, val ); }
    Long getNumber42() { return getNumber( 42 ); }
    void setNumber42( Long val ) { setNumber( 42, val ); }
    Long getNumber43() { return getNumber( 43 ); }
    void setNumber43( Long val ) { setNumber( 43, val ); }
    Long getNumber44() { return getNumber( 44 ); }
    void setNumber44( Long val ) { setNumber( 44, val ); }
    Long getNumber45() { return getNumber( 45 ); }
    void setNumber45( Long val ) { setNumber( 45, val ); }
    Long getNumber46() { return getNumber( 46 ); }
    void setNumber46( Long val ) { setNumber( 46, val ); }
    Long getNumber47() { return getNumber( 47 ); }
    void setNumber47( Long val ) { setNumber( 47, val ); }
    Long getNumber48() { return getNumber( 48 ); }
    void setNumber48( Long val ) { setNumber( 48, val ); }
    Long getNumber49() { return getNumber( 49 ); }
    void setNumber49( Long val ) { setNumber( 49, val ); }
    Long getNumber50() { return getNumber( 50 ); }
    void setNumber50( Long val ) { setNumber( 50, val ); }
    Long getNumber51() { return getNumber( 51 ); }
    void setNumber51( Long val ) { setNumber( 51, val ); }
    Long getNumber52() { return getNumber( 52 ); }
    void setNumber52( Long val ) { setNumber( 52, val ); }
    Long getNumber53() { return getNumber( 53 ); }
    void setNumber53( Long val ) { setNumber( 53, val ); }
    Long getNumber54() { return getNumber( 54 ); }
    void setNumber54( Long val ) { setNumber( 54, val ); }
    Long getNumber55() { return getNumber( 55 ); }
    void setNumber55( Long val ) { setNumber( 55, val ); }
    Long getNumber56() { return getNumber( 56 ); }
    void setNumber56( Long val ) { setNumber( 56, val ); }
    Long getNumber57() { return getNumber( 57 ); }
    void setNumber57( Long val ) { setNumber( 57, val ); }
    Long getNumber58() { return getNumber( 58 ); }
    void setNumber58( Long val ) { setNumber( 58, val ); }
    Long getNumber59() { return getNumber( 59 ); }
    void setNumber59( Long val ) { setNumber( 59, val ); }
    Long getNumber60() { return getNumber( 60 ); }
    void setNumber60( Long val ) { setNumber( 60, val ); }
    Long getNumber61() { return getNumber( 61 ); }
    void setNumber61( Long val ) { setNumber( 61, val ); }
    Long getNumber62() { return getNumber( 62 ); }
    void setNumber62( Long val ) { setNumber( 62, val ); }
    Long getNumber63() { return getNumber( 63 ); }
    void setNumber63( Long val ) { setNumber( 63, val ); }
    Long getNumber64() { return getNumber( 64 ); }
    void setNumber64( Long val ) { setNumber( 64, val ); }
    Long getNumber65() { return getNumber( 65 ); }
    void setNumber65( Long val ) { setNumber( 65, val ); }
    Long getNumber66() { return getNumber( 66 ); }
    void setNumber66( Long val ) { setNumber( 66, val ); }
    Long getNumber67() { return getNumber( 67 ); }
    void setNumber67( Long val ) { setNumber( 67, val ); }
    Long getNumber68() { return getNumber( 68 ); }
    void setNumber68( Long val ) { setNumber( 68, val ); }
    Long getNumber69() { return getNumber( 69 ); }
    void setNumber69( Long val ) { setNumber( 69, val ); }
    Long getNumber70() { return getNumber( 70 ); }
    void setNumber70( Long val ) { setNumber( 70, val ); }
    Long getNumber71() { return getNumber( 71 ); }
    void setNumber71( Long val ) { setNumber( 71, val ); }
    Long getNumber72() { return getNumber( 72 ); }
    void setNumber72( Long val ) { setNumber( 72, val ); }
    Long getNumber73() { return getNumber( 73 ); }
    void setNumber73( Long val ) { setNumber( 73, val ); }
    Long getNumber74() { return getNumber( 74 ); }
    void setNumber74( Long val ) { setNumber( 74, val ); }
    Long getNumber75() { return getNumber( 75 ); }
    void setNumber75( Long val ) { setNumber( 75, val ); }
    Long getNumber76() { return getNumber( 76 ); }
    void setNumber76( Long val ) { setNumber( 76, val ); }
    Long getNumber77() { return getNumber( 77 ); }
    void setNumber77( Long val ) { setNumber( 77, val ); }
    Long getNumber78() { return getNumber( 78 ); }
    void setNumber78( Long val ) { setNumber( 78, val ); }
    Long getNumber79() { return getNumber( 79 ); }
    void setNumber79( Long val ) { setNumber( 79, val ); }
    Long getNumber80() { return getNumber( 80 ); }
    void setNumber80( Long val ) { setNumber( 80, val ); }
    Long getNumber81() { return getNumber( 81 ); }
    void setNumber81( Long val ) { setNumber( 81, val ); }
    Long getNumber82() { return getNumber( 82 ); }
    void setNumber82( Long val ) { setNumber( 82, val ); }
    Long getNumber83() { return getNumber( 83 ); }
    void setNumber83( Long val ) { setNumber( 83, val ); }
    Long getNumber84() { return getNumber( 84 ); }
    void setNumber84( Long val ) { setNumber( 84, val ); }
    Long getNumber85() { return getNumber( 85 ); }
    void setNumber85( Long val ) { setNumber( 85, val ); }
    Long getNumber86() { return getNumber( 86 ); }
    void setNumber86( Long val ) { setNumber( 86, val ); }
    Long getNumber87() { return getNumber( 87 ); }
    void setNumber87( Long val ) { setNumber( 87, val ); }
    Long getNumber88() { return getNumber( 88 ); }
    void setNumber88( Long val ) { setNumber( 88, val ); }
    Long getNumber89() { return getNumber( 89 ); }
    void setNumber89( Long val ) { setNumber( 89, val ); }
    Long getNumber90() { return getNumber( 90 ); }
    void setNumber90( Long val ) { setNumber( 90, val ); }
    Long getNumber91() { return getNumber( 91 ); }
    void setNumber91( Long val ) { setNumber( 91, val ); }
    Long getNumber92() { return getNumber( 92 ); }
    void setNumber92( Long val ) { setNumber( 92, val ); }
    Long getNumber93() { return getNumber( 93 ); }
    void setNumber93( Long val ) { setNumber( 93, val ); }
    Long getNumber94() { return getNumber( 94 ); }
    void setNumber94( Long val ) { setNumber( 94, val ); }
    Long getNumber95() { return getNumber( 95 ); }
    void setNumber95( Long val ) { setNumber( 95, val ); }
    Long getNumber96() { return getNumber( 96 ); }
    void setNumber96( Long val ) { setNumber( 96, val ); }
    Long getNumber97() { return getNumber( 97 ); }
    void setNumber97( Long val ) { setNumber( 97, val ); }
    Long getNumber98() { return getNumber( 98 ); }
    void setNumber98( Long val ) { setNumber( 98, val ); }
    Long getNumber99() { return getNumber( 99 ); }
    void setNumber99( Long val ) { setNumber( 99, val ); }
    Date getDate00() { return getDate( 0 ); }
    void setDate00( Date val ) { setDate( 0, val ); }
    Date getDate01() { return getDate( 1 ); }
    void setDate01( Date val ) { setDate( 1, val ); }
    Date getDate02() { return getDate( 2 ); }
    void setDate02( Date val ) { setDate( 2, val ); }
    Date getDate03() { return getDate( 3 ); }
    void setDate03( Date val ) { setDate( 3, val ); }
    Date getDate04() { return getDate( 4 ); }
    void setDate04( Date val ) { setDate( 4, val ); }
    Date getDate05() { return getDate( 5 ); }
    void setDate05( Date val ) { setDate( 5, val ); }
    Date getDate06() { return getDate( 6 ); }
    void setDate06( Date val ) { setDate( 6, val ); }
    Date getDate07() { return getDate( 7 ); }
    void setDate07( Date val ) { setDate( 7, val ); }
    Date getDate08() { return getDate( 8 ); }
    void setDate08( Date val ) { setDate( 8, val ); }
    Date getDate09() { return getDate( 9 ); }
    void setDate09( Date val ) { setDate( 9, val ); }
    Date getDate10() { return getDate( 10 ); }
    void setDate10( Date val ) { setDate( 10, val ); }
    Date getDate11() { return getDate( 11 ); }
    void setDate11( Date val ) { setDate( 11, val ); }
    Date getDate12() { return getDate( 12 ); }
    void setDate12( Date val ) { setDate( 12, val ); }
    Date getDate13() { return getDate( 13 ); }
    void setDate13( Date val ) { setDate( 13, val ); }
    Date getDate14() { return getDate( 14 ); }
    void setDate14( Date val ) { setDate( 14, val ); }
    Date getDate15() { return getDate( 15 ); }
    void setDate15( Date val ) { setDate( 15, val ); }
    Date getDate16() { return getDate( 16 ); }
    void setDate16( Date val ) { setDate( 16, val ); }
    Date getDate17() { return getDate( 17 ); }
    void setDate17( Date val ) { setDate( 17, val ); }
    Date getDate18() { return getDate( 18 ); }
    void setDate18( Date val ) { setDate( 18, val ); }
    Date getDate19() { return getDate( 19 ); }
    void setDate19( Date val ) { setDate( 19, val ); }
    Date getDate20() { return getDate( 20 ); }
    void setDate20( Date val ) { setDate( 20, val ); }
    Date getDate21() { return getDate( 21 ); }
    void setDate21( Date val ) { setDate( 21, val ); }
    Date getDate22() { return getDate( 22 ); }
    void setDate22( Date val ) { setDate( 22, val ); }
    Date getDate23() { return getDate( 23 ); }
    void setDate23( Date val ) { setDate( 23, val ); }
    Date getDate24() { return getDate( 24 ); }
    void setDate24( Date val ) { setDate( 24, val ); }
    Date getDate25() { return getDate( 25 ); }
    void setDate25( Date val ) { setDate( 25, val ); }
    Date getDate26() { return getDate( 26 ); }
    void setDate26( Date val ) { setDate( 26, val ); }
    Date getDate27() { return getDate( 27 ); }
    void setDate27( Date val ) { setDate( 27, val ); }
    Date getDate28() { return getDate( 28 ); }
    void setDate28( Date val ) { setDate( 28, val ); }
    Date getDate29() { return getDate( 29 ); }
    void setDate29( Date val ) { setDate( 29, val ); }
    Date getDate30() { return getDate( 30 ); }
    void setDate30( Date val ) { setDate( 30, val ); }
    Date getDate31() { return getDate( 31 ); }
    void setDate31( Date val ) { setDate( 31, val ); }
    Date getDate32() { return getDate( 32 ); }
    void setDate32( Date val ) { setDate( 32, val ); }
    Date getDate33() { return getDate( 33 ); }
    void setDate33( Date val ) { setDate( 33, val ); }
    Date getDate34() { return getDate( 34 ); }
    void setDate34( Date val ) { setDate( 34, val ); }
    Date getDate35() { return getDate( 35 ); }
    void setDate35( Date val ) { setDate( 35, val ); }
    Date getDate36() { return getDate( 36 ); }
    void setDate36( Date val ) { setDate( 36, val ); }
    Date getDate37() { return getDate( 37 ); }
    void setDate37( Date val ) { setDate( 37, val ); }
    Date getDate38() { return getDate( 38 ); }
    void setDate38( Date val ) { setDate( 38, val ); }
    Date getDate39() { return getDate( 39 ); }
    void setDate39( Date val ) { setDate( 39, val ); }
    Date getDate40() { return getDate( 40 ); }
    void setDate40( Date val ) { setDate( 40, val ); }
    Date getDate41() { return getDate( 41 ); }
    void setDate41( Date val ) { setDate( 41, val ); }
    Date getDate42() { return getDate( 42 ); }
    void setDate42( Date val ) { setDate( 42, val ); }
    Date getDate43() { return getDate( 43 ); }
    void setDate43( Date val ) { setDate( 43, val ); }
    Date getDate44() { return getDate( 44 ); }
    void setDate44( Date val ) { setDate( 44, val ); }
    Date getDate45() { return getDate( 45 ); }
    void setDate45( Date val ) { setDate( 45, val ); }
    Date getDate46() { return getDate( 46 ); }
    void setDate46( Date val ) { setDate( 46, val ); }
    Date getDate47() { return getDate( 47 ); }
    void setDate47( Date val ) { setDate( 47, val ); }
    Date getDate48() { return getDate( 48 ); }
    void setDate48( Date val ) { setDate( 48, val ); }
    Date getDate49() { return getDate( 49 ); }
    void setDate49( Date val ) { setDate( 49, val ); }
    Date getDate50() { return getDate( 50 ); }
    void setDate50( Date val ) { setDate( 50, val ); }
    Date getDate51() { return getDate( 51 ); }
    void setDate51( Date val ) { setDate( 51, val ); }
    Date getDate52() { return getDate( 52 ); }
    void setDate52( Date val ) { setDate( 52, val ); }
    Date getDate53() { return getDate( 53 ); }
    void setDate53( Date val ) { setDate( 53, val ); }
    Date getDate54() { return getDate( 54 ); }
    void setDate54( Date val ) { setDate( 54, val ); }
    Date getDate55() { return getDate( 55 ); }
    void setDate55( Date val ) { setDate( 55, val ); }
    Date getDate56() { return getDate( 56 ); }
    void setDate56( Date val ) { setDate( 56, val ); }
    Date getDate57() { return getDate( 57 ); }
    void setDate57( Date val ) { setDate( 57, val ); }
    Date getDate58() { return getDate( 58 ); }
    void setDate58( Date val ) { setDate( 58, val ); }
    Date getDate59() { return getDate( 59 ); }
    void setDate59( Date val ) { setDate( 59, val ); }
    Date getDate60() { return getDate( 60 ); }
    void setDate60( Date val ) { setDate( 60, val ); }
    Date getDate61() { return getDate( 61 ); }
    void setDate61( Date val ) { setDate( 61, val ); }
    Date getDate62() { return getDate( 62 ); }
    void setDate62( Date val ) { setDate( 62, val ); }
    Date getDate63() { return getDate( 63 ); }
    void setDate63( Date val ) { setDate( 63, val ); }
    Date getDate64() { return getDate( 64 ); }
    void setDate64( Date val ) { setDate( 64, val ); }
    Date getDate65() { return getDate( 65 ); }
    void setDate65( Date val ) { setDate( 65, val ); }
    Date getDate66() { return getDate( 66 ); }
    void setDate66( Date val ) { setDate( 66, val ); }
    Date getDate67() { return getDate( 67 ); }
    void setDate67( Date val ) { setDate( 67, val ); }
    Date getDate68() { return getDate( 68 ); }
    void setDate68( Date val ) { setDate( 68, val ); }
    Date getDate69() { return getDate( 69 ); }
    void setDate69( Date val ) { setDate( 69, val ); }
    Date getDate70() { return getDate( 70 ); }
    void setDate70( Date val ) { setDate( 70, val ); }
    Date getDate71() { return getDate( 71 ); }
    void setDate71( Date val ) { setDate( 71, val ); }
    Date getDate72() { return getDate( 72 ); }
    void setDate72( Date val ) { setDate( 72, val ); }
    Date getDate73() { return getDate( 73 ); }
    void setDate73( Date val ) { setDate( 73, val ); }
    Date getDate74() { return getDate( 74 ); }
    void setDate74( Date val ) { setDate( 74, val ); }
    Date getDate75() { return getDate( 75 ); }
    void setDate75( Date val ) { setDate( 75, val ); }
    Date getDate76() { return getDate( 76 ); }
    void setDate76( Date val ) { setDate( 76, val ); }
    Date getDate77() { return getDate( 77 ); }
    void setDate77( Date val ) { setDate( 77, val ); }
    Date getDate78() { return getDate( 78 ); }
    void setDate78( Date val ) { setDate( 78, val ); }
    Date getDate79() { return getDate( 79 ); }
    void setDate79( Date val ) { setDate( 79, val ); }
    Date getDate80() { return getDate( 80 ); }
    void setDate80( Date val ) { setDate( 80, val ); }
    Date getDate81() { return getDate( 81 ); }
    void setDate81( Date val ) { setDate( 81, val ); }
    Date getDate82() { return getDate( 82 ); }
    void setDate82( Date val ) { setDate( 82, val ); }
    Date getDate83() { return getDate( 83 ); }
    void setDate83( Date val ) { setDate( 83, val ); }
    Date getDate84() { return getDate( 84 ); }
    void setDate84( Date val ) { setDate( 84, val ); }
    Date getDate85() { return getDate( 85 ); }
    void setDate85( Date val ) { setDate( 85, val ); }
    Date getDate86() { return getDate( 86 ); }
    void setDate86( Date val ) { setDate( 86, val ); }
    Date getDate87() { return getDate( 87 ); }
    void setDate87( Date val ) { setDate( 87, val ); }
    Date getDate88() { return getDate( 88 ); }
    void setDate88( Date val ) { setDate( 88, val ); }
    Date getDate89() { return getDate( 89 ); }
    void setDate89( Date val ) { setDate( 89, val ); }
    Date getDate90() { return getDate( 90 ); }
    void setDate90( Date val ) { setDate( 90, val ); }
    Date getDate91() { return getDate( 91 ); }
    void setDate91( Date val ) { setDate( 91, val ); }
    Date getDate92() { return getDate( 92 ); }
    void setDate92( Date val ) { setDate( 92, val ); }
    Date getDate93() { return getDate( 93 ); }
    void setDate93( Date val ) { setDate( 93, val ); }
    Date getDate94() { return getDate( 94 ); }
    void setDate94( Date val ) { setDate( 94, val ); }
    Date getDate95() { return getDate( 95 ); }
    void setDate95( Date val ) { setDate( 95, val ); }
    Date getDate96() { return getDate( 96 ); }
    void setDate96( Date val ) { setDate( 96, val ); }
    Date getDate97() { return getDate( 97 ); }
    void setDate97( Date val ) { setDate( 97, val ); }
    Date getDate98() { return getDate( 98 ); }
    void setDate98( Date val ) { setDate( 98, val ); }
    Date getDate99() { return getDate( 99 ); }
    void setDate99( Date val ) { setDate( 99, val ); }
    long[] getNumArray00() { return getNumberArray( 0 ); }
    void setNumArray00( long[] val ) { setNumberArray( 0, val ); }
    long[] getNumArray01() { return getNumberArray( 1 ); }
    void setNumArray01( long[] val ) { setNumberArray( 1, val ); }
    long[] getNumArray02() { return getNumberArray( 2 ); }
    void setNumArray02( long[] val ) { setNumberArray( 2, val ); }
    long[] getNumArray03() { return getNumberArray( 3 ); }
    void setNumArray03( long[] val ) { setNumberArray( 3, val ); }
    long[] getNumArray04() { return getNumberArray( 4 ); }
    void setNumArray04( long[] val ) { setNumberArray( 4, val ); }
    long[] getNumArray05() { return getNumberArray( 5 ); }
    void setNumArray05( long[] val ) { setNumberArray( 5, val ); }
    long[] getNumArray06() { return getNumberArray( 6 ); }
    void setNumArray06( long[] val ) { setNumberArray( 6, val ); }
    long[] getNumArray07() { return getNumberArray( 7 ); }
    void setNumArray07( long[] val ) { setNumberArray( 7, val ); }
    long[] getNumArray08() { return getNumberArray( 8 ); }
    void setNumArray08( long[] val ) { setNumberArray( 8, val ); }
    long[] getNumArray09() { return getNumberArray( 9 ); }
    void setNumArray09( long[] val ) { setNumberArray( 9, val ); }
    long[] getNumArray10() { return getNumberArray( 10 ); }
    void setNumArray10( long[] val ) { setNumberArray( 10, val ); }
    long[] getNumArray11() { return getNumberArray( 11 ); }
    void setNumArray11( long[] val ) { setNumberArray( 11, val ); }
    long[] getNumArray12() { return getNumberArray( 12 ); }
    void setNumArray12( long[] val ) { setNumberArray( 12, val ); }
    long[] getNumArray13() { return getNumberArray( 13 ); }
    void setNumArray13( long[] val ) { setNumberArray( 13, val ); }
    long[] getNumArray14() { return getNumberArray( 14 ); }
    void setNumArray14( long[] val ) { setNumberArray( 14, val ); }
    long[] getNumArray15() { return getNumberArray( 15 ); }
    void setNumArray15( long[] val ) { setNumberArray( 15, val ); }
    long[] getNumArray16() { return getNumberArray( 16 ); }
    void setNumArray16( long[] val ) { setNumberArray( 16, val ); }
    long[] getNumArray17() { return getNumberArray( 17 ); }
    void setNumArray17( long[] val ) { setNumberArray( 17, val ); }
    long[] getNumArray18() { return getNumberArray( 18 ); }
    void setNumArray18( long[] val ) { setNumberArray( 18, val ); }
    long[] getNumArray19() { return getNumberArray( 19 ); }
    void setNumArray19( long[] val ) { setNumberArray( 19, val ); }
    long[] getNumArray20() { return getNumberArray( 20 ); }
    void setNumArray20( long[] val ) { setNumberArray( 20, val ); }
    long[] getNumArray21() { return getNumberArray( 21 ); }
    void setNumArray21( long[] val ) { setNumberArray( 21, val ); }
    long[] getNumArray22() { return getNumberArray( 22 ); }
    void setNumArray22( long[] val ) { setNumberArray( 22, val ); }
    long[] getNumArray23() { return getNumberArray( 23 ); }
    void setNumArray23( long[] val ) { setNumberArray( 23, val ); }
    long[] getNumArray24() { return getNumberArray( 24 ); }
    void setNumArray24( long[] val ) { setNumberArray( 24, val ); }
    long[] getNumArray25() { return getNumberArray( 25 ); }
    void setNumArray25( long[] val ) { setNumberArray( 25, val ); }
    long[] getNumArray26() { return getNumberArray( 26 ); }
    void setNumArray26( long[] val ) { setNumberArray( 26, val ); }
    long[] getNumArray27() { return getNumberArray( 27 ); }
    void setNumArray27( long[] val ) { setNumberArray( 27, val ); }
    long[] getNumArray28() { return getNumberArray( 28 ); }
    void setNumArray28( long[] val ) { setNumberArray( 28, val ); }
    long[] getNumArray29() { return getNumberArray( 29 ); }
    void setNumArray29( long[] val ) { setNumberArray( 29, val ); }
    long[] getNumArray30() { return getNumberArray( 30 ); }
    void setNumArray30( long[] val ) { setNumberArray( 30, val ); }
    long[] getNumArray31() { return getNumberArray( 31 ); }
    void setNumArray31( long[] val ) { setNumberArray( 31, val ); }
    long[] getNumArray32() { return getNumberArray( 32 ); }
    void setNumArray32( long[] val ) { setNumberArray( 32, val ); }
    long[] getNumArray33() { return getNumberArray( 33 ); }
    void setNumArray33( long[] val ) { setNumberArray( 33, val ); }
    long[] getNumArray34() { return getNumberArray( 34 ); }
    void setNumArray34( long[] val ) { setNumberArray( 34, val ); }
    long[] getNumArray35() { return getNumberArray( 35 ); }
    void setNumArray35( long[] val ) { setNumberArray( 35, val ); }
    long[] getNumArray36() { return getNumberArray( 36 ); }
    void setNumArray36( long[] val ) { setNumberArray( 36, val ); }
    long[] getNumArray37() { return getNumberArray( 37 ); }
    void setNumArray37( long[] val ) { setNumberArray( 37, val ); }
    long[] getNumArray38() { return getNumberArray( 38 ); }
    void setNumArray38( long[] val ) { setNumberArray( 38, val ); }
    long[] getNumArray39() { return getNumberArray( 39 ); }
    void setNumArray39( long[] val ) { setNumberArray( 39, val ); }
    long[] getNumArray40() { return getNumberArray( 40 ); }
    void setNumArray40( long[] val ) { setNumberArray( 40, val ); }
    long[] getNumArray41() { return getNumberArray( 41 ); }
    void setNumArray41( long[] val ) { setNumberArray( 41, val ); }
    long[] getNumArray42() { return getNumberArray( 42 ); }
    void setNumArray42( long[] val ) { setNumberArray( 42, val ); }
    long[] getNumArray43() { return getNumberArray( 43 ); }
    void setNumArray43( long[] val ) { setNumberArray( 43, val ); }
    long[] getNumArray44() { return getNumberArray( 44 ); }
    void setNumArray44( long[] val ) { setNumberArray( 44, val ); }
    long[] getNumArray45() { return getNumberArray( 45 ); }
    void setNumArray45( long[] val ) { setNumberArray( 45, val ); }
    long[] getNumArray46() { return getNumberArray( 46 ); }
    void setNumArray46( long[] val ) { setNumberArray( 46, val ); }
    long[] getNumArray47() { return getNumberArray( 47 ); }
    void setNumArray47( long[] val ) { setNumberArray( 47, val ); }
    long[] getNumArray48() { return getNumberArray( 48 ); }
    void setNumArray48( long[] val ) { setNumberArray( 48, val ); }
    long[] getNumArray49() { return getNumberArray( 49 ); }
    void setNumArray49( long[] val ) { setNumberArray( 49, val ); }
    long[] getNumArray50() { return getNumberArray( 50 ); }
    void setNumArray50( long[] val ) { setNumberArray( 50, val ); }
    long[] getNumArray51() { return getNumberArray( 51 ); }
    void setNumArray51( long[] val ) { setNumberArray( 51, val ); }
    long[] getNumArray52() { return getNumberArray( 52 ); }
    void setNumArray52( long[] val ) { setNumberArray( 52, val ); }
    long[] getNumArray53() { return getNumberArray( 53 ); }
    void setNumArray53( long[] val ) { setNumberArray( 53, val ); }
    long[] getNumArray54() { return getNumberArray( 54 ); }
    void setNumArray54( long[] val ) { setNumberArray( 54, val ); }
    long[] getNumArray55() { return getNumberArray( 55 ); }
    void setNumArray55( long[] val ) { setNumberArray( 55, val ); }
    long[] getNumArray56() { return getNumberArray( 56 ); }
    void setNumArray56( long[] val ) { setNumberArray( 56, val ); }
    long[] getNumArray57() { return getNumberArray( 57 ); }
    void setNumArray57( long[] val ) { setNumberArray( 57, val ); }
    long[] getNumArray58() { return getNumberArray( 58 ); }
    void setNumArray58( long[] val ) { setNumberArray( 58, val ); }
    long[] getNumArray59() { return getNumberArray( 59 ); }
    void setNumArray59( long[] val ) { setNumberArray( 59, val ); }
    long[] getNumArray60() { return getNumberArray( 60 ); }
    void setNumArray60( long[] val ) { setNumberArray( 60, val ); }
    long[] getNumArray61() { return getNumberArray( 61 ); }
    void setNumArray61( long[] val ) { setNumberArray( 61, val ); }
    long[] getNumArray62() { return getNumberArray( 62 ); }
    void setNumArray62( long[] val ) { setNumberArray( 62, val ); }
    long[] getNumArray63() { return getNumberArray( 63 ); }
    void setNumArray63( long[] val ) { setNumberArray( 63, val ); }
    long[] getNumArray64() { return getNumberArray( 64 ); }
    void setNumArray64( long[] val ) { setNumberArray( 64, val ); }
    long[] getNumArray65() { return getNumberArray( 65 ); }
    void setNumArray65( long[] val ) { setNumberArray( 65, val ); }
    long[] getNumArray66() { return getNumberArray( 66 ); }
    void setNumArray66( long[] val ) { setNumberArray( 66, val ); }
    long[] getNumArray67() { return getNumberArray( 67 ); }
    void setNumArray67( long[] val ) { setNumberArray( 67, val ); }
    long[] getNumArray68() { return getNumberArray( 68 ); }
    void setNumArray68( long[] val ) { setNumberArray( 68, val ); }
    long[] getNumArray69() { return getNumberArray( 69 ); }
    void setNumArray69( long[] val ) { setNumberArray( 69, val ); }
    long[] getNumArray70() { return getNumberArray( 70 ); }
    void setNumArray70( long[] val ) { setNumberArray( 70, val ); }
    long[] getNumArray71() { return getNumberArray( 71 ); }
    void setNumArray71( long[] val ) { setNumberArray( 71, val ); }
    long[] getNumArray72() { return getNumberArray( 72 ); }
    void setNumArray72( long[] val ) { setNumberArray( 72, val ); }
    long[] getNumArray73() { return getNumberArray( 73 ); }
    void setNumArray73( long[] val ) { setNumberArray( 73, val ); }
    long[] getNumArray74() { return getNumberArray( 74 ); }
    void setNumArray74( long[] val ) { setNumberArray( 74, val ); }
    long[] getNumArray75() { return getNumberArray( 75 ); }
    void setNumArray75( long[] val ) { setNumberArray( 75, val ); }
    long[] getNumArray76() { return getNumberArray( 76 ); }
    void setNumArray76( long[] val ) { setNumberArray( 76, val ); }
    long[] getNumArray77() { return getNumberArray( 77 ); }
    void setNumArray77( long[] val ) { setNumberArray( 77, val ); }
    long[] getNumArray78() { return getNumberArray( 78 ); }
    void setNumArray78( long[] val ) { setNumberArray( 78, val ); }
    long[] getNumArray79() { return getNumberArray( 79 ); }
    void setNumArray79( long[] val ) { setNumberArray( 79, val ); }
    long[] getNumArray80() { return getNumberArray( 80 ); }
    void setNumArray80( long[] val ) { setNumberArray( 80, val ); }
    long[] getNumArray81() { return getNumberArray( 81 ); }
    void setNumArray81( long[] val ) { setNumberArray( 81, val ); }
    long[] getNumArray82() { return getNumberArray( 82 ); }
    void setNumArray82( long[] val ) { setNumberArray( 82, val ); }
    long[] getNumArray83() { return getNumberArray( 83 ); }
    void setNumArray83( long[] val ) { setNumberArray( 83, val ); }
    long[] getNumArray84() { return getNumberArray( 84 ); }
    void setNumArray84( long[] val ) { setNumberArray( 84, val ); }
    long[] getNumArray85() { return getNumberArray( 85 ); }
    void setNumArray85( long[] val ) { setNumberArray( 85, val ); }
    long[] getNumArray86() { return getNumberArray( 86 ); }
    void setNumArray86( long[] val ) { setNumberArray( 86, val ); }
    long[] getNumArray87() { return getNumberArray( 87 ); }
    void setNumArray87( long[] val ) { setNumberArray( 87, val ); }
    long[] getNumArray88() { return getNumberArray( 88 ); }
    void setNumArray88( long[] val ) { setNumberArray( 88, val ); }
    long[] getNumArray89() { return getNumberArray( 89 ); }
    void setNumArray89( long[] val ) { setNumberArray( 89, val ); }
    long[] getNumArray90() { return getNumberArray( 90 ); }
    void setNumArray90( long[] val ) { setNumberArray( 90, val ); }
    long[] getNumArray91() { return getNumberArray( 91 ); }
    void setNumArray91( long[] val ) { setNumberArray( 91, val ); }
    long[] getNumArray92() { return getNumberArray( 92 ); }
    void setNumArray92( long[] val ) { setNumberArray( 92, val ); }
    long[] getNumArray93() { return getNumberArray( 93 ); }
    void setNumArray93( long[] val ) { setNumberArray( 93, val ); }
    long[] getNumArray94() { return getNumberArray( 94 ); }
    void setNumArray94( long[] val ) { setNumberArray( 94, val ); }
    long[] getNumArray95() { return getNumberArray( 95 ); }
    void setNumArray95( long[] val ) { setNumberArray( 95, val ); }
    long[] getNumArray96() { return getNumberArray( 96 ); }
    void setNumArray96( long[] val ) { setNumberArray( 96, val ); }
    long[] getNumArray97() { return getNumberArray( 97 ); }
    void setNumArray97( long[] val ) { setNumberArray( 97, val ); }
    long[] getNumArray98() { return getNumberArray( 98 ); }
    void setNumArray98( long[] val ) { setNumberArray( 98, val ); }
    long[] getNumArray99() { return getNumberArray( 99 ); }
    void setNumArray99( long[] val ) { setNumberArray( 99, val ); }
    String getLongString00() { return getLongString( 0 ); }
    void setLongString00( String val ) { setLongString( 0, val ); }
    String getLongString01() { return getLongString( 1 ); }
    void setLongString01( String val ) { setLongString( 1, val ); }
    String getLongString02() { return getLongString( 2 ); }
    void setLongString02( String val ) { setLongString( 2, val ); }
    String getLongString03() { return getLongString( 3 ); }
    void setLongString03( String val ) { setLongString( 3, val ); }
    String getLongString04() { return getLongString( 4 ); }
    void setLongString04( String val ) { setLongString( 4, val ); }
    String getLongString05() { return getLongString( 5 ); }
    void setLongString05( String val ) { setLongString( 5, val ); }
    String getLongString06() { return getLongString( 6 ); }
    void setLongString06( String val ) { setLongString( 6, val ); }
    String getLongString07() { return getLongString( 7 ); }
    void setLongString07( String val ) { setLongString( 7, val ); }
    String getLongString08() { return getLongString( 8 ); }
    void setLongString08( String val ) { setLongString( 8, val ); }
    String getLongString09() { return getLongString( 9 ); }
    void setLongString09( String val ) { setLongString( 9, val ); }
    String getLongString10() { return getLongString( 10 ); }
    void setLongString10( String val ) { setLongString( 10, val ); }
    String getLongString11() { return getLongString( 11 ); }
    void setLongString11( String val ) { setLongString( 11, val ); }
    String getLongString12() { return getLongString( 12 ); }
    void setLongString12( String val ) { setLongString( 12, val ); }
    String getLongString13() { return getLongString( 13 ); }
    void setLongString13( String val ) { setLongString( 13, val ); }
    String getLongString14() { return getLongString( 14 ); }
    void setLongString14( String val ) { setLongString( 14, val ); }
    String getLongString15() { return getLongString( 15 ); }
    void setLongString15( String val ) { setLongString( 15, val ); }
    String getLongString16() { return getLongString( 16 ); }
    void setLongString16( String val ) { setLongString( 16, val ); }
    String getLongString17() { return getLongString( 17 ); }
    void setLongString17( String val ) { setLongString( 17, val ); }
    String getLongString18() { return getLongString( 18 ); }
    void setLongString18( String val ) { setLongString( 18, val ); }
    String getLongString19() { return getLongString( 19 ); }
    void setLongString19( String val ) { setLongString( 19, val ); }
    String getLongString20() { return getLongString( 20 ); }
    void setLongString20( String val ) { setLongString( 20, val ); }
    String getLongString21() { return getLongString( 21 ); }
    void setLongString21( String val ) { setLongString( 21, val ); }
    String getLongString22() { return getLongString( 22 ); }
    void setLongString22( String val ) { setLongString( 22, val ); }
    String getLongString23() { return getLongString( 23 ); }
    void setLongString23( String val ) { setLongString( 23, val ); }
    String getLongString24() { return getLongString( 24 ); }
    void setLongString24( String val ) { setLongString( 24, val ); }
    String getLongString25() { return getLongString( 25 ); }
    void setLongString25( String val ) { setLongString( 25, val ); }
    String getLongString26() { return getLongString( 26 ); }
    void setLongString26( String val ) { setLongString( 26, val ); }
    String getLongString27() { return getLongString( 27 ); }
    void setLongString27( String val ) { setLongString( 27, val ); }
    String getLongString28() { return getLongString( 28 ); }
    void setLongString28( String val ) { setLongString( 28, val ); }
    String getLongString29() { return getLongString( 29 ); }
    void setLongString29( String val ) { setLongString( 29, val ); }
    String getLongString30() { return getLongString( 30 ); }
    void setLongString30( String val ) { setLongString( 30, val ); }
    String getLongString31() { return getLongString( 31 ); }
    void setLongString31( String val ) { setLongString( 31, val ); }
    String getLongString32() { return getLongString( 32 ); }
    void setLongString32( String val ) { setLongString( 32, val ); }
    String getLongString33() { return getLongString( 33 ); }
    void setLongString33( String val ) { setLongString( 33, val ); }
    String getLongString34() { return getLongString( 34 ); }
    void setLongString34( String val ) { setLongString( 34, val ); }
    String getLongString35() { return getLongString( 35 ); }
    void setLongString35( String val ) { setLongString( 35, val ); }
    String getLongString36() { return getLongString( 36 ); }
    void setLongString36( String val ) { setLongString( 36, val ); }
    String getLongString37() { return getLongString( 37 ); }
    void setLongString37( String val ) { setLongString( 37, val ); }
    String getLongString38() { return getLongString( 38 ); }
    void setLongString38( String val ) { setLongString( 38, val ); }
    String getLongString39() { return getLongString( 39 ); }
    void setLongString39( String val ) { setLongString( 39, val ); }
    String getLongString40() { return getLongString( 40 ); }
    void setLongString40( String val ) { setLongString( 40, val ); }
    String getLongString41() { return getLongString( 41 ); }
    void setLongString41( String val ) { setLongString( 41, val ); }
    String getLongString42() { return getLongString( 42 ); }
    void setLongString42( String val ) { setLongString( 42, val ); }
    String getLongString43() { return getLongString( 43 ); }
    void setLongString43( String val ) { setLongString( 43, val ); }
    String getLongString44() { return getLongString( 44 ); }
    void setLongString44( String val ) { setLongString( 44, val ); }
    String getLongString45() { return getLongString( 45 ); }
    void setLongString45( String val ) { setLongString( 45, val ); }
    String getLongString46() { return getLongString( 46 ); }
    void setLongString46( String val ) { setLongString( 46, val ); }
    String getLongString47() { return getLongString( 47 ); }
    void setLongString47( String val ) { setLongString( 47, val ); }
    String getLongString48() { return getLongString( 48 ); }
    void setLongString48( String val ) { setLongString( 48, val ); }
    String getLongString49() { return getLongString( 49 ); }
    void setLongString49( String val ) { setLongString( 49, val ); }
    String getLongString50() { return getLongString( 50 ); }
    void setLongString50( String val ) { setLongString( 50, val ); }
    String getLongString51() { return getLongString( 51 ); }
    void setLongString51( String val ) { setLongString( 51, val ); }
    String getLongString52() { return getLongString( 52 ); }
    void setLongString52( String val ) { setLongString( 52, val ); }
    String getLongString53() { return getLongString( 53 ); }
    void setLongString53( String val ) { setLongString( 53, val ); }
    String getLongString54() { return getLongString( 54 ); }
    void setLongString54( String val ) { setLongString( 54, val ); }
    String getLongString55() { return getLongString( 55 ); }
    void setLongString55( String val ) { setLongString( 55, val ); }
    String getLongString56() { return getLongString( 56 ); }
    void setLongString56( String val ) { setLongString( 56, val ); }
    String getLongString57() { return getLongString( 57 ); }
    void setLongString57( String val ) { setLongString( 57, val ); }
    String getLongString58() { return getLongString( 58 ); }
    void setLongString58( String val ) { setLongString( 58, val ); }
    String getLongString59() { return getLongString( 59 ); }
    void setLongString59( String val ) { setLongString( 59, val ); }
    String getLongString60() { return getLongString( 60 ); }
    void setLongString60( String val ) { setLongString( 60, val ); }
    String getLongString61() { return getLongString( 61 ); }
    void setLongString61( String val ) { setLongString( 61, val ); }
    String getLongString62() { return getLongString( 62 ); }
    void setLongString62( String val ) { setLongString( 62, val ); }
    String getLongString63() { return getLongString( 63 ); }
    void setLongString63( String val ) { setLongString( 63, val ); }
    String getLongString64() { return getLongString( 64 ); }
    void setLongString64( String val ) { setLongString( 64, val ); }
    String getLongString65() { return getLongString( 65 ); }
    void setLongString65( String val ) { setLongString( 65, val ); }
    String getLongString66() { return getLongString( 66 ); }
    void setLongString66( String val ) { setLongString( 66, val ); }
    String getLongString67() { return getLongString( 67 ); }
    void setLongString67( String val ) { setLongString( 67, val ); }
    String getLongString68() { return getLongString( 68 ); }
    void setLongString68( String val ) { setLongString( 68, val ); }
    String getLongString69() { return getLongString( 69 ); }
    void setLongString69( String val ) { setLongString( 69, val ); }
    String getLongString70() { return getLongString( 70 ); }
    void setLongString70( String val ) { setLongString( 70, val ); }
    String getLongString71() { return getLongString( 71 ); }
    void setLongString71( String val ) { setLongString( 71, val ); }
    String getLongString72() { return getLongString( 72 ); }
    void setLongString72( String val ) { setLongString( 72, val ); }
    String getLongString73() { return getLongString( 73 ); }
    void setLongString73( String val ) { setLongString( 73, val ); }
    String getLongString74() { return getLongString( 74 ); }
    void setLongString74( String val ) { setLongString( 74, val ); }
    String getLongString75() { return getLongString( 75 ); }
    void setLongString75( String val ) { setLongString( 75, val ); }
    String getLongString76() { return getLongString( 76 ); }
    void setLongString76( String val ) { setLongString( 76, val ); }
    String getLongString77() { return getLongString( 77 ); }
    void setLongString77( String val ) { setLongString( 77, val ); }
    String getLongString78() { return getLongString( 78 ); }
    void setLongString78( String val ) { setLongString( 78, val ); }
    String getLongString79() { return getLongString( 79 ); }
    void setLongString79( String val ) { setLongString( 79, val ); }
    String getLongString80() { return getLongString( 80 ); }
    void setLongString80( String val ) { setLongString( 80, val ); }
    String getLongString81() { return getLongString( 81 ); }
    void setLongString81( String val ) { setLongString( 81, val ); }
    String getLongString82() { return getLongString( 82 ); }
    void setLongString82( String val ) { setLongString( 82, val ); }
    String getLongString83() { return getLongString( 83 ); }
    void setLongString83( String val ) { setLongString( 83, val ); }
    String getLongString84() { return getLongString( 84 ); }
    void setLongString84( String val ) { setLongString( 84, val ); }
    String getLongString85() { return getLongString( 85 ); }
    void setLongString85( String val ) { setLongString( 85, val ); }
    String getLongString86() { return getLongString( 86 ); }
    void setLongString86( String val ) { setLongString( 86, val ); }
    String getLongString87() { return getLongString( 87 ); }
    void setLongString87( String val ) { setLongString( 87, val ); }
    String getLongString88() { return getLongString( 88 ); }
    void setLongString88( String val ) { setLongString( 88, val ); }
    String getLongString89() { return getLongString( 89 ); }
    void setLongString89( String val ) { setLongString( 89, val ); }
    String getLongString90() { return getLongString( 90 ); }
    void setLongString90( String val ) { setLongString( 90, val ); }
    String getLongString91() { return getLongString( 91 ); }
    void setLongString91( String val ) { setLongString( 91, val ); }
    String getLongString92() { return getLongString( 92 ); }
    void setLongString92( String val ) { setLongString( 92, val ); }
    String getLongString93() { return getLongString( 93 ); }
    void setLongString93( String val ) { setLongString( 93, val ); }
    String getLongString94() { return getLongString( 94 ); }
    void setLongString94( String val ) { setLongString( 94, val ); }
    String getLongString95() { return getLongString( 95 ); }
    void setLongString95( String val ) { setLongString( 95, val ); }
    String getLongString96() { return getLongString( 96 ); }
    void setLongString96( String val ) { setLongString( 96, val ); }
    String getLongString97() { return getLongString( 97 ); }
    void setLongString97( String val ) { setLongString( 97, val ); }
    String getLongString98() { return getLongString( 98 ); }
    void setLongString98( String val ) { setLongString( 98, val ); }
    String getLongString99() { return getLongString( 99 ); }
    void setLongString99( String val ) { setLongString( 99, val ); }
}
