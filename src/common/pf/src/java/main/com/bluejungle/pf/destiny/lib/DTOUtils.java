package com.bluejungle.pf.destiny.lib;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 * 
 * @author Sasha Vladimirov
 * 
 * @version $Id:
 *          //depot/personal/sasha/main/Destiny/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/DTOUtils.java#40 $
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.axis.encoding.Base64;
import org.apache.axis.types.NonNegativeInteger;


import com.bluejungle.destiny.services.policy.types.Access;
import com.bluejungle.destiny.services.policy.types.AccessList;
import com.bluejungle.destiny.services.policy.types.AgentStatusDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.AgentStatusList;
import com.bluejungle.destiny.services.policy.types.AgentTypeEnum;
import com.bluejungle.destiny.services.policy.types.AttributeDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.AttributeDescriptorList;
import com.bluejungle.destiny.services.policy.types.AttributeTypeEnum;
import com.bluejungle.destiny.services.policy.types.Component;
import com.bluejungle.destiny.services.policy.types.ComponentList;
import com.bluejungle.destiny.services.policy.types.DeploymentActionEnum;
import com.bluejungle.destiny.services.policy.types.DeploymentHistoryList;
import com.bluejungle.destiny.services.policy.types.DeploymentHistoryDTO;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordDTO;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordList;
import com.bluejungle.destiny.services.policy.types.DeploymentTypeEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnumList;
import com.bluejungle.destiny.services.policy.types.DomainObjectStateEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectUsageDTO;
import com.bluejungle.destiny.services.policy.types.DomainObjectUsageListDTO;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorList;
import com.bluejungle.destiny.services.policy.types.EntityDigestDTO;
import com.bluejungle.destiny.services.policy.types.EntityDigestList;
import com.bluejungle.destiny.services.policy.types.LeafObjectDTO;
import com.bluejungle.destiny.services.policy.types.LeafObjectEnum;
import com.bluejungle.destiny.services.policy.types.LeafObjectList;
import com.bluejungle.destiny.services.policy.types.LeafObjectSearchSpecDTO;
import com.bluejungle.destiny.services.policy.types.ListOfIds;
import com.bluejungle.destiny.services.policy.types.ObligationArgumentDTO;
import com.bluejungle.destiny.services.policy.types.ObligationDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.ObligationDescriptorList;
import com.bluejungle.destiny.services.policy.types.ObligationValueDTO;
import com.bluejungle.destiny.services.policy.types.PolicyActionsDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.PolicyActionsDescriptorList;
import com.bluejungle.destiny.services.policy.types.StringList;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;
import com.bluejungle.destiny.services.policy.types.TimeRelationDTO;
import com.bluejungle.destiny.services.policy.types.TimeRelationList;
import com.bluejungle.destiny.services.policy.types.ValueTypeEnum;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.DeploymentActionType;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.ObligationArgument;
import com.bluejungle.pf.destiny.lifecycle.ObligationDescriptor;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.deployment.DeploymentBundleSignatureEnvelope;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;

/**
 * DTOUtils provides methods that convert between policy types and corresponding
 * Data Transfer Objects
 */

public class DTOUtils {

    private static Map<Component, EntityType> componentMap = new HashMap<Component, EntityType>();

    static {
        // Initialize the component map
        // FIXME We should be returning something else here.
        componentMap.put(Component.Policy, EntityType.POLICY);
        componentMap.put(Component.Location, EntityType.LOCATION);
        componentMap.put(Component.Desktop, EntityType.HOST);
        componentMap.put(Component.Application, EntityType.APPLICATION);
        componentMap.put(Component.Action, EntityType.ACTION);
        componentMap.put(Component.Resource, EntityType.RESOURCE);
        componentMap.put(Component.User, EntityType.USER);
        componentMap.put(Component.Portal, EntityType.PORTAL);
        componentMap.put(Component.Device, EntityType.DEVICE);
        componentMap.put(Component.SAP, EntityType.SAP);
        componentMap.put(Component.Enovia, EntityType.ENOVIA);

    }

    private static Field bitsetBits;
    private static Field bitsetSize;
    private static Method bitsetRecalcSize;

    static {
        Class<BitSet> bitsetClass = BitSet.class;

        // We pull out and make public various fields of the bitset for performance reasons.  I don't
        // know that we have done any timing tests on this and this is very brittle as it depends on
        // private names.  Yay!
        // There is no possible way to recover from an exception here - we need this stuff - so quit
        // as gracelessly as possible
        try {
            try {
                bitsetBits = bitsetClass.getDeclaredField("bits");
            } catch (NoSuchFieldException e) {
                // Try the 1.6 name
                bitsetBits = bitsetClass.getDeclaredField("words");
            }
            
            bitsetBits.setAccessible(true);

            try {
                bitsetSize = bitsetClass.getDeclaredField("unitsInUse");
            } catch (NoSuchFieldException e) {
                // Try the 1.6 name
                bitsetSize = bitsetClass.getDeclaredField("wordsInUse");
            }
            bitsetSize.setAccessible(true);

            try {
                bitsetRecalcSize = bitsetClass.getDeclaredMethod("recalculateUnitsInUse");
            } catch (NoSuchMethodException e) {
                // Try the 1.6 name
                bitsetRecalcSize = bitsetClass.getDeclaredMethod("recalculateWordsInUse");
            }

            bitsetRecalcSize.setAccessible(true);

        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static Calendar convertToCalendar(Date date){
        if(date == null){
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }
    
    private static Date convertTDate(Calendar calendar){
        if(calendar == null){
            return null;
        }
        return calendar.getTime();
    }

    /**
     * Serializes an array of long values into an array of bytes.
     *
     * @param data the data to serialize.
     * @return serialization result.
     */
    public static byte[] serializeArrayOfLongs(long[] data) {
        if (data == null) {
            return null;
        }
        if (data == null) {
            //TODO ???? double checked null
            return new byte[0];
        }
        byte[] res = new byte[data.length * 8];
        ByteBuffer bb = ByteBuffer.wrap(res);
        for (int i = 0; i != data.length; i++) {
            bb.putLong(data[i]);
        }
        return res;
    }
    
    /**
     * Serializes an input object into an array of bytes.
     * 
     * @param bitset
     *            the object to serialize.
     * @return the result of serialization.
     */
    public static byte[] serializeBitset(BitSet bitset) {
        if (bitset == null) {
            return null;
        }
        try {
            return serializeArrayOfLongs((long[])bitsetBits.get(bitset));
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("Unable to serialize data: " + iae.getMessage());
        }
    }

    /**
     * Deserializes an array of long values (long[])
     *
     * @param data the bytes to be deserialized.
     * @return the resulting array of longs.
     */
    public static long[] deserializeArrayOfLongs(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length % 8 != 0) {
            throw new IllegalArgumentException("data");
        }
        LongBuffer lb = ByteBuffer.wrap(data).asLongBuffer();
        long[] res = new long[data.length / 8];
        lb.get(res);
        return res;
    }

    /**
     * Deserializes the input array of bytes and produces a
     * <code>Serializable</code> object.
     * 
     * @param bitsetData the array of bytes to deserialize.
     * @return deserialized object from the input data.
     */
    public static BitSet deserializeBitset(byte[] bitsetData) {
        try {
            BitSet res = new BitSet();
            long[] bits = deserializeArrayOfLongs(bitsetData);
            if (bits == null) {
                bits = new long[0];
            }
            bitsetBits.set(res, bits);
            bitsetSize.set(res, new Integer(bits.length));
            bitsetRecalcSize.invoke(res);
            return res;
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException("Unable to deserialize data: " + iae.getMessage());
        } catch (InvocationTargetException ite) {
            throw new IllegalArgumentException("Unable to deserialize data: " + ite.getMessage());
        }
    }

    /**
     * Converts a subject to its DTO.
     * 
     * @param subject
     *            the subject to convert.
     * @return the resulting DTO.
     */
    public static SubjectDTO subjectToDTO(IDSubject subject) {
        //TODO subject.getId() is inconsistent with #subjectSpecToDTO(IDSpec)
        return new SubjectDTO(
                subject.getName()
              , (subject.getId() != null) ? BigInteger.valueOf(subject.getId().longValue()) : null
              , subject.getUid()
              , subject.getUniqueName()
              , subject.getSubjectType().getName()
        );
    }

    /**
     * Converts a subjectSpec to DTO.
     * 
     * @param subject
     *            spec the subject to convert.
     * @return the resulting DTO.
     */
    public static SubjectDTO subjectSpecToDTO(IDSpec subjectSpec) {
        //TODO subject.getId() is inconsistent with #subjectToDTO(IDSubject)
        return new SubjectDTO(
                subjectSpec.getName()
              , BigInteger.valueOf(subjectSpec.getId().longValue())
              , subjectSpec.getName()
              , subjectSpec.getName()
              , subjectSpec.getSpecType().getName()
        );
    }

    /**
     * Converts a <code>Collection</code> of <code>String</code> objects to
     * a <code>StringList</code>.
     * 
     * @param strings
     *            the <code>Collection</code> of <code>String</code> objects
     *            to be converted.
     * @returna <code>StringList</code> with elements from the given
     *          <code>Collection</code> of <code>String</code> objects.
     */
    public static StringList makeStringList(Collection<String> strings) {
        if (strings == null) {
            throw new NullPointerException("strings");
        }
        StringList res = new StringList();
        res.setElement(strings.toArray(new String[strings.size()]));
        return res;
    }

    /**
     * Converts a <code>StringList</code> to a <code>Collection</code> of
     * <code>String</code> objects.
     * 
     * @param list
     *            the <code>StringList</code> to be converted.
     * @return a <code>Collection</code> of <code>String</code> objects from
     *         the given <code>StringList</code>.
     */
    public static Collection<String> makeCollectionOfStrings(StringList list) {
        if (list == null) {
            throw new NullPointerException("list");
        }
        if (list.getElement() == null || list.getElement().length == 0) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(list.getElement());
        }
    }

    /**
     * Converts a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects to a
     * <code>EntityDescriptorList</code>.
     * 
     * @param descriptors
     *            the <code>Collection</code> of
     *            <code>DomainObjectDescriptor</code> objects to be converted.
     * @returna <code>EntityDescriptorList</code> with elements from the given
     *          <code>Collection</code> of <code>DomainObjectDescriptor</code>
     *          objects.
     */
    public static EntityDescriptorList makeEntityDescriptorList(Collection<DomainObjectDescriptor> descriptors) {
        if (descriptors == null) {
            return null;
        }
        EntityDescriptorList res = new EntityDescriptorList();
        EntityDescriptorDTO[] elements = new EntityDescriptorDTO[descriptors.size()];
        int i = 0;
        for (DomainObjectDescriptor descr : descriptors) {
            elements[i++] = makeEntityDescriptorDTO(descr);
        }
        res.setElement(elements);
        return res;
    }

    /**
     * Converts a <code>Collection</code> of <code>DODDigest</code> objects to a
     * <code>EntityDigestList</code>.
     * 
     * @param digests the <code>Collection</code> of
     *            <code>DODDigest</code> objects to be converted.
     * @returna <code>EntityDigestList</code> with elements from the given
     *          <code>Collection</code> of <code>DODDigest</code>
     *          objects.
     */
    public static EntityDigestList makeEntityDigestList(Collection<DODDigest> digests) {
        if (digests == null) {
            return null;
        }

        EntityDigestList res = new EntityDigestList();
        EntityDigestDTO[] elements = new EntityDigestDTO[digests.size()];
        int i = 0;
        for (DODDigest digest : digests) {
            elements[i++] = makeEntityDigestDTO(digest);
        }
        res.setElement(elements);
        return res;
    }


    /**
     * Converts a <code>EntityDigestList</code> to a <code>List</code>
     * of <code>DODDigest</code> objects.
     * 
     * @param list
     *            the <code>StringList</code> to be converted.
     * @return a <code>List</code> of <code>DODDigests</code>
     *         objects from the given <code>EntityDigestList</code>.
     */
    public static List<DODDigest> makeListOfDigests(EntityDigestList list) {
        if (list == null) {
            return null;
        }
        EntityDigestDTO[] dtos = list.getElement();
        if (dtos == null || dtos.length == 0) {
            return Collections.emptyList();
        } else {
            DODDigest[] res = new DODDigest[dtos.length];
            for (int i = 0; i != res.length; i++) {
                res[i] = makeDODDigest(dtos[i]);
            }
            return Arrays.asList(res);
        }
    }

    /**
     * Converts an <code>EntityDigestDTO</code> to a
     * <code>DODDigest</code>.
     * 
     * @param dto
     *            the <code>EntityDigestDTO</code> to convert.
     * @return a <code>DODDigest</code> corresponding to the given dto.
     */
    public static DODDigest makeDODDigest(EntityDigestDTO dto) {
        return new DODDigest( 
                new Long(dto.getId().longValue())
              , dto.getType()
              , dto.getName()
              , dto.isAccessible()
              , dto.isWithDependencies()
              , dto.isSubPolicy()
              , dto.getVersion()
              , dto.getDestinyVersion()
              , dto.getOwner()
              , dto.getOwnerName()
              , UnmodifiableDate.forDate(dto.getTimeLastUpdated())
              , UnmodifiableDate.forDate(dto.getTimeLastModified())
              , dto.getModifier()
              , dto.getModifierName()
              , UnmodifiableDate.forDate(dto.getTimeLastSubmitted())
              , dto.getSubmitter()
              , dto.getSubmitterName()
              , makeDevelopmentStatus(dto.getStatus())
              , makeDomainObjectUsage(dto.getUsage())
        );
    }

    /**
     * Converts a <code>EntityDescriptorList</code> to a <code>List</code>
     * of <code>DomainObjectDescriptor</code> objects.
     * 
     * @param list
     *            the <code>StringList</code> to be converted.
     * @return a <code>List</code> of <code>DomainObjectDescriptor</code>
     *         objects from the given <code>EntityDescriptorList</code>.
     */
    public static List<DomainObjectDescriptor> makeListOfDescriptors(EntityDescriptorList list) {
        if (list == null) {
            return null;
        }
        EntityDescriptorDTO[] dtos = list.getElement();
        if (dtos == null || dtos.length == 0) {
            return Collections.emptyList();
        } else {
            DomainObjectDescriptor[] res = new DomainObjectDescriptor[dtos.length];
            for (int i = 0; i != res.length; i++) {
                res[i] = makeDomainObjectDescriptor(dtos[i]);
            }
            return Arrays.asList(res);
        }
    }

    /**
     * Converts an <code>EntityDescriptorDTO</code> to a
     * <code>DomainObjectDescriptor</code>.
     * 
     * @param dto
     *            the <code>EntityDescriptorDTO</code> to convert.
     * @return a <code>DomainObjectDescriptor</code> corresponding to the
     *         given dto.
     */
    public static DomainObjectDescriptor makeDomainObjectDescriptor(EntityDescriptorDTO dto) {
        return new DomainObjectDescriptor(
                new Long(dto.getId().longValue())
              , dto.getName()
              , new Long(dto.getOwner())
              , makeAccessPolicy(dto.getAccesspolicy())
              , makeEntityType(dto.getType())
              , dto.getDescription()
              , makeDevelopmentStatus(dto.getStatus())
              , dto.getVersion()
              , UnmodifiableDate.forDate(dto.getTimeLastUpdated())
              , UnmodifiableDate.forDate(dto.getTimeCreated())
              , UnmodifiableDate.forDate(dto.getTimeLastModified())
              , dto.getModifier()
              , UnmodifiableDate.forDate(dto.getTimeLastSubmitted())
              , dto.getSubmitter()
              , dto.isHidden()
              , dto.isAccessible()
              , dto.isWithDependencies());
    }

    public static EntityDigestDTO makeEntityDigestDTO(DODDigest digest) {
        if (digest == null) {
            throw new NullPointerException("digest");
        }
        return new EntityDigestDTO(
                BigInteger.valueOf(digest.getId().longValue())
              , digest.getName()
              , digest.getType()
              , digest.getOwnerId() // may be NPE here. HOPE the ownerId always exists
              , digest.getOwnerName()
              , makeDomainObjectStateEnum(digest.getDevStatus())
              , digest.getVersion()
              , digest.getDestinyVersion()
              , convertToCalendar(digest.getLastUpdated())
              , convertToCalendar(digest.getLastModified())
              , digest.getModifier()
              , digest.getModifierName()
              , convertToCalendar(digest.getLastSubmitted())
              , digest.getSubmitter()
              , digest.getSubmitterName()
              , digest.isAccessible()    
              , digest.hasDependencies()        
              , digest.isSubPolicy()        
              , makeDomainObjectUsageDTO(digest.getUsageStatus())
         );
    }


    /**
     * Converts an <code>DomainObjectDescriptor</code> to a
     * <code>EntityDescriptorDTO</code>.
     * 
     * @param descr
     *            the <code>DomainObjectDescriptor</code> to convert.
     * @return the <code>EntityDescriptorDTO</code> built from the
     *         <code>DomainObjectDescriptor</code>.
     */
    public static EntityDescriptorDTO makeEntityDescriptorDTO(DomainObjectDescriptor descr) {
        if (descr == null) {
            throw new NullPointerException("descr");
        }
        long owner = descr.getOwner() != null ? descr.getOwner().longValue() : 0;
        return new EntityDescriptorDTO(
                BigInteger.valueOf(descr.getId().longValue())
              , descr.getName()
              , makeDomainObjectEnum(descr.getType())
              , owner
              , descr.getAccessPolicyString()
              , descr.getDescription()
              , makeDomainObjectStateEnum(descr.getStatus())
              , descr.getVersion()
              , convertToCalendar(descr.getLastUpdated())
              , convertToCalendar(descr.getWhenCreated())
              , convertToCalendar(descr.getLastModified())
              , descr.getModifier()
              , convertToCalendar(descr.getLastSubmitted())
              , descr.getSubmitter()
              , descr.isHidden()
              , descr.isAccessible()
              , descr.hasDependencies());
    }

        
    /**
     * Makes an instance of the <code>DomainObjectEnum</code> for an instance
     * of the <code>EntityType</code> enumeration.
     * 
     * @param entityType
     *            the <code>EntityType</code>.
     * @return an instance of the <code>DomainObjectEnum</code> for an
     *         instance of the <code>EntityType</code> enumeration.
     */
    public static DomainObjectEnum makeDomainObjectEnum(EntityType entityType) {
        if (entityType == null) {
            return null;
        }
        return DomainObjectEnum.fromValue(entityType.getName());
    }

    /**
     * Makes an instance of <code>EntityType</code> for an instance of
     * <code>DomainObjectEnum</code>.
     * 
     * @param objEnum
     *            the <code>EntityType</code> to be converted.
     * @return an instance of <code>EntityType</code> for an instance of
     *         <code>DomainObjectEnum</code>.
     */
    public static EntityType makeEntityType(DomainObjectEnum objEnum) {
        if (objEnum == null) {
            return null;
        }
        return EntityType.forName(objEnum.getValue());
    }

    /**
     * Makes an instance of the <code>LeafObjectEnum</code> for an instance of
     * the <code>LeafObjectType</code> enumeration.
     * 
     * @param leafType
     *            the <code>LeafObjectType</code>.
     * @return an instance of the <code>LeafObjectEnum</code> for an instance
     *         of the <code>LeafObjectType</code> enumeration.
     */
    public static LeafObjectEnum makeLeafObjectEnum(LeafObjectType leafType) {
        if (leafType == null) {
            return null;
        }
        return LeafObjectEnum.fromValue(leafType.getName());
    }

    /**
     * Makes an instance of <code>LeafObjectType</code> for an instance of
     * <code>LeafObjectEnum</code>.
     * 
     * @param objEnum
     *            the <code>LeafObjectType</code> to be converted.
     * @return an instance of <code>LeafObjectType</code> for an instance of
     *         <code>LeafObjectEnum</code>.
     */
    public static LeafObjectType makeLeafObjectType(LeafObjectEnum objEnum) {
        if (objEnum == null) {
            return null;
        }
        return LeafObjectType.forName(objEnum.getValue());
    }

    /**
     * Given a <code>ValueTypeEnum</code>, constructs the corresponding
     * <code>ValueType</code>.
     * 
     * @param typeEnum
     *            the <code>ValueTypeEnum</code> to be converted.
     * @return the <code>ValueType</code> corresponding to the given
     *         <code>ValueTypeEnum</code>.
     */
    public static ValueType makeValueType(ValueTypeEnum typeEnum) {
        if (typeEnum == null) {
            return null;
        }
        return ValueType.forName(typeEnum.getValue());
    }

    /**
     * Given a <code>ValueType</code>, constructs the corresponding
     * <code>ValueTypeEnum</code>.
     * 
     * @param type
     *            the <code>ValueType</code> to be converted.
     * @return the <code>ValueTypeEnum</code> corresponding to the given
     *         <code>ValueType</code>.
     */
    public static ValueTypeEnum makeValueTypeEnum(ValueType type) {
        if (type == null) {
            return null;
        }
        return ValueTypeEnum.fromString(type.getName());
    }

    /**
     * Makes a <code>LeafObject</code> from the corresponding DTO.
     * 
     * @param dto
     *            the DTO to be converted to a <code>LeafObject</code>.
     * @param nameMap
     *            a <code>Map</code> of known names. May be null.
     * @return a <code>LeafObject</code> corresponding to the given DTO.
     */
    public static LeafObject makeLeafObject(LeafObjectDTO dto) {
        if (dto == null) {
            return null;
        }
        LeafObject res = new LeafObject(makeLeafObjectType(dto.getType()));
        BigInteger dtoId = dto.getId();
        res.setId(dtoId != null ? new Long(dtoId.longValue()) : null);
        res.setName(dto.getName());
        res.setUid(dto.getUid());
        res.setUniqueName(dto.getUniqueName());
        res.setDomainName(dto.getRealmName());
        return res;
    }

    /**
     * Converts a <code>LeafObject</code> to a <code>LeafObjectDTO</code>.
     * 
     * @param leaf
     *            the <code>LeafObject</code> to be converted.
     * @return the <code>LeafObjectDTO</code> corresponding to the given
     *         <code>LeafObject</code>.
     */
    public static LeafObjectDTO makeLeafObjectDTO(LeafObject leaf) {
        if (leaf == null) {
            return null;
        }
        Long leafId = leaf.getId();
        BigInteger dtoId = null;
        if (leafId != null) {
            dtoId = BigInteger.valueOf(leafId.longValue());
        }
        return new LeafObjectDTO(
                makeLeafObjectEnum(leaf.getType())
              , dtoId
              , leaf.getName()
              , leaf.getUniqueName()
              , leaf.getUid()
              , leaf.getDomainName()
        );
    }

    /**
     * Converts a <code>Collection</code> of <code>LeafObject</code> objects
     * to a <code>LeafObjectList</code>.
     * 
     * @param leafVals
     *            the <code>Collection</code> of <code>LeafObject</code>
     *            objects to be converted.
     * @return a <code>LeafObjectList</code> with the elements of the original
     *         <code>Collection</code>.
     */
    public static LeafObjectList makeLeafObjectList(Collection<LeafObject> leafVals) {
        if (leafVals == null) {
            return null;
        }
        LeafObject[] vals = leafVals.toArray(new LeafObject[leafVals.size()]);
        LeafObjectDTO[] dtos = new LeafObjectDTO[vals.length];
        for (int i = 0; i != vals.length; i++) {
            dtos[i] = makeLeafObjectDTO(vals[i]);
        }
        return new LeafObjectList(dtos);
    }

    /**
     * Converts a <code>LeafObjectList</code> to a <code>List</code> of
     * <code>LeafObject</code> objects.
     * 
     * @param list
     *            a <code>LeafObjectList</code> to be converted.
     * @return a <code>List</code> of <code>LeafObject</code> objects
     *         corresponding to objects from the original list.
     */
    public static List<LeafObject> makeListOfLeafObjects(LeafObjectList list) {
        List<LeafObject> listToReturn = null;

        if (list != null) {
            LeafObjectDTO[] dtos = list.getLeafs();
            if (dtos == null) {
                listToReturn = Collections.emptyList();
            } else {
                listToReturn = new LinkedList<LeafObject>();

                for (int i = 0; i < dtos.length; i++) {
                    listToReturn.add(makeLeafObject(dtos[i]));
                }
            }
        }

        return listToReturn;
    }

    /**
     * Makes an instance of <code>AccessPolicy</code> for pql representing an
     * access policy.
     * 
     * @param apPQL
     *            pql representing the access policy.
     * @return an instance of <code>AccessPolicy</code> for the inpu pql
     *         representing an access policy.
     */
    public static IAccessPolicy makeAccessPolicy(String apPQL) {
        if (apPQL == null)
            return null;
        DomainObjectBuilder dob = new DomainObjectBuilder(apPQL);
        try {
            return dob.processAccessPolicy();
        } catch (PQLException e) {
            // Access policy is invalid - return null
            return null;
        }
    }

    /**
     * Converts a <code>Collection</code> of <code>EntityType</code> objects
     * to a <code>DomainObjectEnumList</code>.
     * 
     * @param types
     *            a <code>Collection</code> of <code>EntityType</code>
     *            objects.
     * @return a <code>DomainObjectEnumList</code> with the elements from the
     *         original collection.
     */
    public static DomainObjectEnumList makeDomainObjectEnumList(Collection<EntityType> types) {
        if (types == null) {
            return null;
        }
        DomainObjectEnum[] typeEnums = new DomainObjectEnum[types.size()];
        int i = 0;
        
        for (EntityType entityType : types) {
            typeEnums[i++] = makeDomainObjectEnum(entityType);
        }
        return new DomainObjectEnumList(typeEnums);
    }

    /**
     * Given a <code>DomainObjectEnumList</code>, returns a corresponding
     * <code>Collection</code> of <code>EntityType</code> objects.
     * 
     * @param typeEnumList
     *            the incoming <code>DomainObjectEnumList</code>.
     * @return the resulting <code>Collection</code> of
     *         <code>EntityType</code> objects.
     */
    public static Collection<EntityType> makeEntityTypeCollection(DomainObjectEnumList typeEnumList) {
        if (typeEnumList == null || typeEnumList.getEntityType() == null) {
            return null;
        }
        DomainObjectEnum[] typeEnums = typeEnumList.getEntityType();
        if (typeEnums == null || typeEnums.length == 0) {
            return Collections.emptyList();
        }
        EntityType[] types = new EntityType[typeEnums.length];
        for (int i = 0; i != types.length; i++) {
            types[i] = makeEntityType(typeEnums[i]);
        }
        return Arrays.asList(types);
    }

    /**
     * Makes an instance of the <code>DomainObjectStateEnum</code> for an
     * instance of the <code>DevelopmentStatus</code> enumeration.
     * 
     * @param status
     *            the <code>DevelopmentStatus</code>.
     * @return an instance of the <code>DomainObjectStateEnum</code> for an
     *         instance of the <code>DevelopmentStatus</code> enumeration.
     */
    public static DomainObjectStateEnum makeDomainObjectStateEnum(DevelopmentStatus status) {
        if (status == null) {
            return null;
        }
        return DomainObjectStateEnum.fromValue(status.getName());
    }

    /**
     * Makes an instance of the <code>DevelopmentStatus</code> for an instance
     * of the <code>DomainObjectStateEnum</code> enumeration.
     * 
     * @param status
     *            the <code>DomainObjectStateEnum</code>.
     * @return an instance of the <code>DevelopmentStatus</code> for an
     *         instance of the <code>DomainObjectStateEnum</code> enumeration.
     */
    public static DevelopmentStatus makeDevelopmentStatus(DomainObjectStateEnum enumVal) {
        if (enumVal == null) {
            return null;
        }
        return DevelopmentStatus.forName(enumVal.getValue());
    }

    /**
     * Makes an instance of the <code>DeploymentActionEnum</code> for an
     * instance of the <code>DeploymentActionType</code> enumeration.
     * 
     * @param actionType
     *            the <code>DeploymentActionType</code>.
     * @return an instance of the <code>DeploymentActionEnum</code> for an
     *         instance of the <code>DeploymentActionType</code> enumeration.
     */
    public static DeploymentActionEnum makeDeploymentActionEnum(DeploymentActionType actionType) {
        if (actionType == null) {
            return null;
        }
        return DeploymentActionEnum.fromValue(actionType.getName());
    }

    /**
     * Makes an instance of the <code>DeploymentActionType</code> for an
     * instance of the <code>DeploymentActionEnum</code> enumeration.
     * 
     * @param actionEnum
     *            the <code>DeploymentActionEnum</code>.
     * @return an instance of the <code>DeploymentActionType</code> for an
     *         instance of the <code>DeploymentActionEnum</code> enumeration.
     */
    public static DeploymentActionType makeDeploymentActionType(DeploymentActionEnum actionEnum) {
        if (actionEnum == null) {
            return null;
        }
        return DeploymentActionType.forName(actionEnum.getValue());
    }

    /**
     * Makes an instance of the <code>DeploymentTypeEnum</code> for an
     * instance of the <code>DeploymentTypeType</code> enumeration.
     * 
     * @param deploymentType
     *            the <code>DeploymentType</code>.
     * @return an instance of the <code>DeploymentTypeEnum</code> for an
     *         instance of the <code>DeploymentType</code> enumeration.
     */
    public static DeploymentTypeEnum makeDeploymentTypeEnum(DeploymentType deploymentType) {
        if (deploymentType == null) {
            return null;
        }
        return DeploymentTypeEnum.fromValue(deploymentType.getName());
    }

    /**
     * Makes an instance of the <code>DeploymentType</code> for an instance of
     * the <code>DeploymentTypeEnum</code> enumeration.
     * 
     * @param typeEnum
     *            the <code>DeploymentActionEnum</code>.
     * @return an instance of the <code>DeploymentActionType</code> for an
     *         instance of the <code>DeploymentActionEnum</code> enumeration.
     */
    public static DeploymentType makeDeploymentType(DeploymentTypeEnum typeEnum) {
        if (typeEnum == null) {
            return null;
        }
        return DeploymentType.forName(typeEnum.getValue());
    }

    /**
     * Produces a <code>DeploymentRecordDTO</code> for an instance of the
     * <code>DeploymentRecord</code>.
     * 
     * @param record
     *            the <code>DeploymentRecord</code>.
     * @return a <code>DeploymentRecordDTO</code> corresponding to the given
     *         <code>DeploymentRecord</code>.
     */
    public static DeploymentRecordDTO makeDeploymentRecordDTO(DeploymentRecord record) {
        if (record == null) {
            return null;
        }
        return new DeploymentRecordDTO(
                BigInteger.valueOf(record.getId().longValue())
              , convertToCalendar(record.getWhenRequested())
              , convertToCalendar(record.getAsOf())
              , convertToCalendar(record.getWhenCancelled())
              , makeDeploymentActionEnum(record.getDeploymentActionType())
              , makeDeploymentTypeEnum(record.getDeploymentType())
              , record.getNumberOfDeployedEntities()
              , record.isHidden()
              , record.getDeployer()
        );
    }

    /**
     * Produces a <code>DeploymentRecord</code> for an instance of the
     * <code>DeploymentRecordDTO</code>.
     * 
     * @param dto
     *            the <code>DeploymentRecordDTO</code>.
     * @return a <code>DeploymentRecord</code> corresponding to the given
     *         <code>DeploymentRecordDTO</code>.
     */
    public static DeploymentRecord makeDeploymentRecord(DeploymentRecordDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DeploymentRecord(
                new Long(dto.getId().longValue())
              , makeDeploymentActionType(dto.getActionType())
              , makeDeploymentType(dto.getDeploymentType())
              , dto.getEffectiveDate().getTime()
              , dto.getWhenRequested().getTime()
              , dto.getCancelledDate() != null ? dto.getCancelledDate().getTime() : null
              , dto.getNumberOfdeployedEntities()
              , dto.isHidden()
              , dto.getDeployer()
        );
    }

    /**
     * Converts a <code>List</code> of DomainObjectUsage objects to a
     * <code>DomainObjectUsageListDTO</code>.
     * 
     * @param objects
     *            A <code>List</code> of objects to be converted.
     * @return a <code>DomainObjectUsageListDTO</code> with the list from the
     *         <code>Collection</code> passed into this method.
     */
    public static DomainObjectUsageListDTO makeUsageListDTO(List<DomainObjectUsage> objects) {
        if (objects == null) {
            return null;
        }
        DomainObjectUsageDTO[] list = new DomainObjectUsageDTO[objects.size()];
        int i = 0;
        for (DomainObjectUsage dou : objects) {
            list[i++] = makeDomainObjectUsageDTO(dou);
        }
        return new DomainObjectUsageListDTO(list);
    }

    /**
     * Converts a single <code>DomainObjectUsage</code> to a
     * <code>DomainObjectUsageDTO</code>.
     * 
     * @param usage
     *            The <code>DomainObjectUsage</code> to be converted.
     * @return the resulting <code>DomainObjectUsageDTO</code>.
     */
    public static DomainObjectUsageDTO makeDomainObjectUsageDTO(DomainObjectUsage usage) {
        if (usage == null) {
            return null;
        }
        return new DomainObjectUsageDTO(
                usage.hasReferringObjects()
              , usage.hasBeenDeployed()
              , usage.hasFuturedeployments()
              , usage.getCurrentlydeployedvcersion()
              , usage.getLatestDeployedVersion()
        );
    }

    /**
     * Converts a <code>DomainObjectUsageListDTO</code> to a <code>List</code>
     * of DomainObjectUsage objects.
     * 
     * @param list
     *            The <code>DomainObjectUsageListDTO</code> to be converted.
     * @return a <code>List</code> with the list from the
     *         <code>DomainObjectUsageListDTO</code> passed into this method.
     */
    public static List<DomainObjectUsage> makeUsageList(DomainObjectUsageListDTO list) {
        if (list == null) {
            return null;
        }
        List<DomainObjectUsage> res = new ArrayList<DomainObjectUsage>();
        DomainObjectUsageDTO[] data = list.getUsage();
        if (data == null) {
            return null;
        }
        for (int i = 0; i != data.length; i++) {
            res.add(makeDomainObjectUsage(data[i]));
        }
        return res;
    }

    /**
     * Converts a single <code>DomainObjectUsageDTO</code> to a
     * <code>DomainObjectUsage</code>.
     * 
     * @param usage
     *            The <code>DomainObjectUsageDTO</code> to be converted.
     * @return the resulting <code>DomainObjectUsage</code>.
     */
    public static DomainObjectUsage makeDomainObjectUsage(DomainObjectUsageDTO dto) {
        if (dto == null) {
            return null;
        }
        return new DomainObjectUsage(
                dto.isIsReferenced()
              , dto.isHasBeenDeployed()
              , dto.isHasFutureDeployments()
              , dto.getCurrentlyDeployedVersion()
              , dto.getLatestDeployedVersion()
        );
    }

    /**
     * Given a <code>Collection</code> of <code>DeploymentRecord</code>
     * objects, returns a <code>DeploymentRecordList</code> with the records
     * from the <code>Collection</code> passed in.
     * 
     * @param records
     *            a <code>Collection</code> of <code>DeploymentRecord</code>
     *            objects to be converted.
     * @return a <code>DeploymentRecordList</code> with the records from the
     *         <code>Collection</code> passed in
     */
    public static DeploymentRecordList makeDeploymentRecordList(Collection<DeploymentRecord> records) {
        if (records == null) {
            return null;
        }
        DeploymentRecord[] vals = records.toArray(new DeploymentRecord[records.size()]);
        DeploymentRecordDTO[] dtos = new DeploymentRecordDTO[records.size()];
        for (int i = 0; i != vals.length; i++) {
            dtos[i] = makeDeploymentRecordDTO(vals[i]);
        }
        return new DeploymentRecordList(dtos);
    }

    /**
     * Given a <code>DeploymentRecordList</code>, returns a
     * <code>Collection</code> of <code>DeploymentRecord</code> objects from
     * the list.
     * 
     * @param list
     *            a <code>DeploymentRecordList</code> to be converted.
     * @return a <code>Collection</code> of <code>DeploymentRecord</code>
     *         objects from the list.
     */
    public static Collection<DeploymentRecord> makeCollectionOfDeploymentRecords(DeploymentRecordList list) {
        if (list == null) {
            return null;
        }
        DeploymentRecordDTO[] dtos = list.getRecords();
        if (dtos == null) {
            return Collections.emptyList();
        }
        DeploymentRecord[] vals = new DeploymentRecord[dtos.length];
        for (int i = 0; i != dtos.length; i++) {
            vals[i] = makeDeploymentRecord(dtos[i]);
        }
        return Arrays.asList(vals);
    }

    /**
     * Converts a <code>TimeRelation</code> to a <code>TimeRelationDTO</code>.
     * 
     * @param timeRel
     *            the <code>TimeRelation</code> to convert.
     * @return the resulting <code>TimeRelationDTO</code>.
     */
    public static TimeRelationDTO makeTimeRelationDTO(TimeRelation timeRel) {
        if (timeRel == null) {
            return null;
        }
        long from = -1;
        if (timeRel.getActiveFrom() != null) {
            from = timeRel.getActiveFrom().getTime();
        }
        long to = -1;
        if (timeRel.getActiveTo() != null) {
            to = timeRel.getActiveTo().getTime();
        }
        return new TimeRelationDTO(from, to);
    }

    /**
     * Converts a <code>TimeRelationDTO</code> to a <code>TimeRelation</code>.
     * 
     * @param dto
     *            the <code>TimeRelationDTO</code> to convert.
     * @return the resulting <code>TimeRelation</code>.
     */
    public static TimeRelation makeTimeRelation(TimeRelationDTO dto) {
        return new TimeRelation(
                (dto.getFrom() != -1) ? UnmodifiableDate.forTime(dto.getFrom()) : null
              , (dto.getTo() != -1) ? UnmodifiableDate.forTime(dto.getTo()) : null
        );
    }

    /**
     * Given a <code>Collection</code> of <code>TimeRelation</code> objects,
     * returns a corresponding <code>TimeRelationList</code>.
     * 
     * @param trs
     *            a <code>Collection</code> of <code>TimeRelation</code>
     *            objects.
     * @return a <code>TimeRelationList</code> corresponding to the passed in
     *         <code>Collection</code> of <code>TimeRelation</code> objects.
     */
    public static TimeRelationList makeTimeRelationList(Collection<TimeRelation> trs) {
        if (trs == null) {
            return null;
        }
        TimeRelation[] vals = trs.toArray(new TimeRelation[trs.size()]);
        TimeRelationDTO[] dtos = new TimeRelationDTO[trs.size()];
        for (int i = 0; i != dtos.length; i++) {
            dtos[i] = makeTimeRelationDTO(vals[i]);
        }
        return new TimeRelationList(dtos);
    }

    /**
     * Convert a <code>TimeRelationList</code> to a <code>Collection</code>
     * of <code>TimeRelation</code> objects.
     * 
     * @param list
     *            the <code>TimeRelationList</code> to be converted.
     * @return The resulting <code>Collection</code> of
     *         <code>TimeRelation</code> objects.
     */
    public static Collection<TimeRelation> makeCollectionOfTimeRelations(TimeRelationList list) {
        if (list == null) {
            return null;
        }
        TimeRelationDTO[] dtos = list.getRecords();
        if (dtos == null) {
            return Collections.emptyList();
        }
        TimeRelation[] vals = new TimeRelation[dtos.length];
        for (int i = 0; i != vals.length; i++) {
            vals[i] = makeTimeRelation(dtos[i]);
        }
        return Arrays.asList(vals);
    }
    
    public static DeploymentHistoryDTO makeDeploymentHistoryDTO(DeploymentHistory deploymentHistory) {
        if (deploymentHistory == null) {
            return null;
        }
        long from = -1;
        if (deploymentHistory.getTimeRelation().getActiveFrom() != null) {
            from = deploymentHistory.getTimeRelation().getActiveFrom().getTime();
        }
        long to = -1;
        if (deploymentHistory.getTimeRelation().getActiveTo() != null) {
            to = deploymentHistory.getTimeRelation().getActiveTo().getTime();
        }
        return new DeploymentHistoryDTO(
                new TimeRelationDTO(from, to)
              , convertToCalendar(deploymentHistory.getLastModified())  
              , deploymentHistory.getModifier()
              , convertToCalendar(deploymentHistory.getSubmittedTime())
              , deploymentHistory.getSubmitter()
              , convertToCalendar(deploymentHistory.getDeployTime())
              , deploymentHistory.getDeployer()
        );
    }
    
    public static DeploymentHistoryList makeDeploymentHistoryList(Collection<DeploymentHistory> trs) {
        if (trs == null) {
            return null;
        }
        DeploymentHistory[] vals = trs.toArray(new DeploymentHistory[trs.size()]);
        DeploymentHistoryDTO[] dtos = new DeploymentHistoryDTO[trs.size()];
        for (int i = 0; i != dtos.length; i++) {
            dtos[i] = makeDeploymentHistoryDTO(vals[i]);
        }
        return new DeploymentHistoryList(dtos);
    }
    
    public static DeploymentHistory makeDeploymentHistory(DeploymentHistoryDTO dto) {
        return new DeploymentHistory(
                makeTimeRelation(dto.getTimeRelation())
              , convertTDate(dto.getLastModified())
              , dto.getModifier()
              , convertTDate(dto.getSubmittedTime())
              , dto.getSubmitter()
              , convertTDate(dto.getDeployTime())
              , dto.getDeployer()
        );
    }
    
    public static Collection<DeploymentHistory> makeCollectionOfDeploymentHistory(DeploymentHistoryList list) {
        if (list == null) {
            return null;
        }
        DeploymentHistoryDTO[] dtos = list.getRecords();
        if (dtos == null) {
            return Collections.emptyList();
        }
        Collection<DeploymentHistory> vals = new ArrayList<DeploymentHistory>(dtos.length);
        for (int i = 0; i != dtos.length; i++) {
            vals.add(makeDeploymentHistory(dtos[i]));
        }
        return vals;
    }

    /**
     * Given a <code>Collection</code> of policies or domain objects, returns
     * a PQL string with definitions of all the objects in the
     * <code>Collection</code>.
     * 
     * @param objects
     *            a <code>Collection</code> of objects that can be converted
     *            to PQL.
     * @return a PQL string with definitions of all the objects in the
     *         <code>Collection</code>.
     */
    public static String makePql(Collection<?> objects) {
        String[] pqls = DomainObjectFormatter.format(objects);
        return mergeStrings(Arrays.asList(pqls));
    }

    /**
     * Merges a <code>Collection</code> of <code>String</code> objects into
     * one <code>String</code> with the '\n' separator between the elements of
     * the original <code>Collection</code>.
     * 
     * @param strings
     *            the <code>Collection</code> of <code>String</code> objects
     *            to be merged.
     * @return a <code>String</code> consisting of the elements of the
     *         original <code>Collection</code> with '\n' between them.
     */
    public static String mergeStrings(Collection<String> strings) {
        if (strings == null) {
            return null;
        }
        StringBuffer res = new StringBuffer();
        for (String s : strings) {
            res.append(s);
            res.append("\n");
        }
        return res.toString();
    }

    /**
     * Given a PQL< returns a <code>Collection</code> of entities it
     * represents.
     * 
     * @param pql
     *            the PQL to be parsed.
     * @return a <code>Collection</code> of entities pql represents.
     * @throws PQLException
     *             when the PQL passed in cannot be parsed.
     */
    public static Collection<IHasId> makeCollectionOfSpecs(String pql) throws PQLException {
        final List<IHasId> res = new LinkedList<IHasId>();

        DomainObjectBuilder.processInternalPQL(pql, new IPQLVisitor() {

            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                assert descr.getId().equals(policy.getId());
                assert descr.getName() != null;
                assert descr.getName().equals(policy.getName());
                res.add(policy);
                if (descr.getOwner() != null) {
                    policy.setOwner(new Subject(
                            descr.getOwner().toString()
                          , descr.getOwner().toString()
                          , descr.getOwner().toString()
                          , descr.getOwner()
                          , SubjectType.USER)
                    );
                }
                if (descr.getAccessPolicy() != null) {
                    policy.setAccessPolicy((AccessPolicy) descr.getAccessPolicy());
                }
            }

            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                // Will never encounter it here.
                assert false : "visitAccessPolicy in DTOUtils.makeCollectionOfSpecs";
            }

            public void visitFolder(DomainObjectDescriptor descr) {
                assert descr.getName() != null;
                PolicyFolder pf = new PolicyFolder(
                        descr.getId()
                      , descr.getName()
                      , descr.getDescription()
                      , descr.getStatus()
                );
                res.add(pf);
                if (descr.getOwner() != null) {
                    pf.setOwner(new Subject(
                            descr.getOwner().toString()
                          , descr.getOwner().toString()
                          , descr.getOwner().toString()
                          , descr.getOwner()
                          , SubjectType.USER)
                    );
                }
                if (descr.getAccessPolicy() != null) {
                    pf.setAccessPolicy((AccessPolicy) descr.getAccessPolicy());
                }
            }

            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                assert descr.getName() != null;
                String name = descr.getName();
                int index = name.indexOf(PQLParser.SEPARATOR);
                SpecType specType = SpecType.ILLEGAL;
                if (index != -1) {
                    String type = name.substring(0, index).toLowerCase();
                    try {
                        specType = SpecType.forName(type);
                    } catch (Exception e) {
                        // ignore the exception
                    }
                }
                SpecBase spec = new SpecBase(
                        null
                      , specType
                      , descr.getId()
                      , descr.getName()
                      , descr.getDescription()
                      , descr.getStatus()
                      , pred
                      , descr.isHidden()
                );
                res.add(spec);
                if (descr.getOwner() != null) {
                    spec.setOwner(new Subject(
                            descr.getOwner().toString()
                          , descr.getOwner().toString()
                          , descr.getOwner().toString()
                          , descr.getOwner()
                          , SubjectType.USER)
                   );
                }
                if (descr.getAccessPolicy() != null) {
                    spec.setAccessPolicy((AccessPolicy) descr.getAccessPolicy());
                }
            }

            public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                res.add(location);
            }

        });
        return res;
    }

    /**
     * Makes a <code>Collection</code> of <code>PolicyActionsDescriptor</code>
     * objects from a <code>PolicyActionsDescriptorList</code>
     *
     * @param list the <code>PolicyActionsDescriptorList</code>
     * @return a <code>Collection</code> of <code>PolicyActionsDescriptor</code> objects
     */
     
    public static Collection<PolicyActionsDescriptor> makePolicyActionsDescriptorCollection(
            PolicyActionsDescriptorList list) {
        if (list == null) {
            return null;
        }

        PolicyActionsDescriptorDTO[] dtos = list.getElements();

        if (dtos == null) {
            return Collections.emptyList();
        }

        List<PolicyActionsDescriptor> res = new ArrayList<PolicyActionsDescriptor>(dtos.length);

        for (int i = 0; i != dtos.length; i++) {
            res.add(makePolicyActionsDescriptor(dtos[i]));
        }
        return res;
    }

    /**
     * Converts an <code>PolicyActionsDescriptorDTO</code> to an
     * <code>PolicyActionsDescriptor</code>.
     * 
     * @param dto
     *            the <code>PolicyActionsDescriptorDTO</code> to be converted.
     * @return an <code>PolicyActionsDescriptor</code> corresponding to the given
     *         <code>PolicyActionsDescriptorDTO</code> object.
     */
    public static PolicyActionsDescriptor makePolicyActionsDescriptor(PolicyActionsDescriptorDTO dto) {
        if (dto == null) {
            return null;
        }

        return new PolicyActionsDescriptor(
                DAction.getAction(dto.getName())
              , dto.getDisplayName()
              , dto.getCategory()
        );
    }

    /**
     * Makes a <code>PolicyActionsDescriptorList</code> from an array of
     * <code>PolicyActionsDescriptor</code> objects
     *
     * @param list the <code>Collection</code> of <code>PolicyActionsDescriptor</code>
     * @return a <code>PolicyActionsDescriptorList</code> objects
     */
     
    public static PolicyActionsDescriptorList makePolicyActionsDescriptorList(PolicyActionsDescriptor[] descrs) {
        if (descrs == null) {
            return null;
        }

        PolicyActionsDescriptorDTO[] dtos = new PolicyActionsDescriptorDTO[descrs.length];
        int i = 0;
        for (PolicyActionsDescriptor descr : descrs) {
            dtos[i++] = makePolicyActionsDescriptorDTO(descr);
        }
        return new PolicyActionsDescriptorList(dtos);
    }

    /**
     * Converts an <code>PolicyActionsDescriptor</code> to an
     * <code>PolicyActionsDescriptorDTO</code>.
     * 
     * @param dto
     *            the <code>PolicyActionsDescriptorDTO</code> to be converted.
     * @return an <code>PolicyActionsDescriptorDTO</code> corresponding to the given
     *         <code>PolicyActionsDescriptor</code> object.
     */
    public static PolicyActionsDescriptorDTO makePolicyActionsDescriptorDTO(PolicyActionsDescriptor descr) {
        if ( descr == null) {
            return null;
        }

        return new PolicyActionsDescriptorDTO(
                descr.getAction().getName()
              , descr.getDisplayName()
              , descr.getCategory()
        );
    }

    /**
     * Makes a <code>Collection</code> of <code>ObligationDescriptor</code>
     * objects from a <code>ObligationDescriptorList</code>
     *
     * @param list the <code>ObligationDescriptorList</code> to be converted
     * @return a <code>Collection</code> of <code>ObligationDescriptor</code> objects
     */
    public static Collection<ObligationDescriptor> makeObligationDescriptorCollection(
            ObligationDescriptorList list) {
        if (list == null) {
            return null;
        }
        
        ObligationDescriptorDTO[] dtos = list.getElements();

        if (dtos == null) {
            return Collections.emptyList();
        }

        List<ObligationDescriptor> res = new ArrayList<ObligationDescriptor>(dtos.length);

        for (int i = 0; i != dtos.length; i++) {
            res.add(makeObligationDescriptor(dtos[i]));
        }

        return res;
    }

    /**
     * Converts an <code>ObligationDescriptorDTO</code> to an
     * <code>ObligationDescriptor</code>.
     * 
     * @param dto
     *            the <code>ObligationDescriptorDTO</code> to be converted.
     * @return an <code>ObligationDescriptor</code> corresponding to the given
     *         <code>ObligationDescriptorDTO</code> object.
     */
    public static ObligationDescriptor makeObligationDescriptor(ObligationDescriptorDTO dto) {
        if (dto == null) {
            return null;
        }
        return new ObligationDescriptor(
                dto.getDisplayName()
              , dto.getInternalName()
              , makeObligationArguments(dto.getArguments())
        );
    }

    /**
     * Converts an array of <code>ObligationArgumentDTO</code> into an array of
     * <code>ObligationArgument</code>
     *
     * @param args the array of <code>ObligationArgumentDTO</code> objects
     * @return an array of <code>ObligationArgument</code> objects
     */
    private static ObligationArgument[] makeObligationArguments(ObligationArgumentDTO[] args) {
        int length = 0;

        if (args != null) {
            length = args.length;
        }

        ObligationArgument[] res = new ObligationArgument[length];

        for (int i = 0; i < length; i++) {
            ObligationValueDTO[] dtoValues = args[i].getValues();
            String defaultValue = null;
            String[] values = null;
            
            if (dtoValues != null) {
                values = new String[dtoValues.length];
                
                for (int j = 0; j < dtoValues.length; j++) {
                    values[j] = dtoValues[j].getDisplayName();
                    if (dtoValues[j].get_default() && defaultValue == null) {
                        defaultValue = values[j];
                    }
                }
            }

            res[i] = new ObligationArgument(
                    args[i].getDisplayName()
                  , values
                  , defaultValue
                  , args[i].getUserEditable()
                  , args[i].getHidden());
        }
        
        return res;
    }

    /**
     * Converts a <code>Collection</code> of <code>AttributeDescriptor</code>
     * objects to an <code>AttributeDescriptorList</code>.
     * 
     * @param descrs
     *            the <code>Collection</code> of
     *            <code>AttributeDescriptor</code> objects to be converted.
     * @return a <code>AttributeDescriptorList</code> with the elements
     *         corresponding to DTO of elements of the original
     *         <code>Collection</code>.
     */
    public static AttributeDescriptorList makeAttributeDescriptorList(Collection<AttributeDescriptor> descrs) {
        if (descrs == null) {
            return null;
        }
        AttributeDescriptorDTO[] dtos = new AttributeDescriptorDTO[descrs.size()];
        int i = 0;
        for (AttributeDescriptor descr : descrs) {
            dtos[i++] = makeAttributedescriptorDTO(descr);
        }
        return new AttributeDescriptorList(dtos);
    }

    /**
     * Makes a <code>Collection</code> of <code>AttributeDescriptor</code>
     * objects from a <code>AttributeDescriptorList</code>.
     * 
     * @param list
     *            the <code>AttributeDescriptorList</code> obejct to be
     *            converted.
     * @return a <code>Collection</code> of <code>AttributeDescriptor</code>
     *         objects corresponding to DTOs from the
     *         <code>AttributeDescriptorList</code>.
     */
    public static List<AttributeDescriptor> makeAttributeDescriptorCollection(AttributeDescriptorList list) {
        if (list == null) {
            return null;
        }
        if (list.getElements() == null) {
            return Collections.emptyList();
        }
        AttributeDescriptorDTO[] dtos = list.getElements();

        if (dtos == null) {
            return Collections.emptyList();
        }

        List<AttributeDescriptor> res = new ArrayList<AttributeDescriptor>(dtos.length);

        for (int i = 0; i != dtos.length; i++) {
            res.add(makeAttributedescriptor(dtos[i]));
        }
        return res;
    }
    /**
     * Converts an <code>AttributeDescriptorDTO</code> to an
     * <code>AttributeDescriptor</code>.
     * 
     * @param dto
     *            the <code>AttributeDescriptorDTO</code> to be converted.
     * @return an <code>AttributeDescriptor</code> corresponding to the given
     *         <code>AttributeDescriptorDTO</code> object.
     */
    public static AttributeDescriptor makeAttributedescriptor(AttributeDescriptorDTO dto) {
        if (dto == null) {
            return null;
        }
        List<AttributeDescriptor> allowedAttributes = new ArrayList<AttributeDescriptor>();
        if (dto.getAllowedAttributes() != null) {
            for (AttributeDescriptorDTO ad : dto.getAllowedAttributes()) {
                allowedAttributes.add(makeAttributedescriptor(ad));
            }
        }
        List<RelationOp> operators = new ArrayList<RelationOp>();
        if (dto.getOperators() != null) {
            for (String op : dto.getOperators()) {
                operators.add(RelationOp.getElement(op));
            }
        }
        return new AttributeDescriptor(
            dto.getGroup()
        ,   dto.getDisplayName()
        ,   makeAttributeType(dto.getType())
        ,   dto.isIsRequired()
        ,   getAttributeForNameAndType(
                dto.getPqlName()
            ,   dto.getContextType()
            ,   dto.getContextSubtype()
            )
        ,   operators.toArray(new RelationOp[operators.size()])
        ,   allowedAttributes.toArray(new AttributeDescriptor[allowedAttributes.size()])
        ,   dto.getEnumeratedValues()
        );
    }

    private static final Set<String> subjContexts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    private static final Set<String> resContexts = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);

    static {
        subjContexts.add("user");
        subjContexts.add("host");
        subjContexts.add("application");
        resContexts.add("resource");
        resContexts.add("portal");
    }

    private static IAttribute getAttributeForNameAndType(String name, String type, String subtype) {
        if (subjContexts.contains(type)) {
            return SubjectAttribute.forNameAndType(name, SubjectType.forName(subtype));
        } else if (resContexts.contains(type)) {
            return ResourceAttribute.forNameAndType(name, subtype);
        } else {
            throw new IllegalArgumentException(
                String.format("Illegal attribute: [%2]::[%3].[%1]", name, type, subtype)
            );
        }
    }

    /**
     * Converts an <code>AttributeDescriptor</code> to an
     * <code>AttributeDescriptorDTO</code>.
     * 
     * @param descr
     *            the <code>AttributeDescriptor</code> to be converted.
     * @return the <code>AttributeDescriptorDTO</code> corresponding to the
     *         given <code>AttributeDescriptor</code> object.
     */
    public static AttributeDescriptorDTO makeAttributedescriptorDTO(AttributeDescriptor descr) {
        if (descr == null) {
            return null;
        }
        List<AttributeDescriptorDTO> allowedAttributes = new ArrayList<AttributeDescriptorDTO>();
        for (AttributeDescriptor ad : descr.getAllowedAttributes()) {
            allowedAttributes.add(makeAttributedescriptorDTO(ad));
        }
        List<String> operators = new ArrayList<String>();
        for (RelationOp op : descr.getOperators()) {
            operators.add(op.getName());
        }
        List<String> enumeratedValues = descr.getEnumeratedValues();

        return new AttributeDescriptorDTO(
                descr.getGroupName()
            ,   descr.getDisplayName()
            ,   descr.getPqlName()
            ,   makeAttributeTypeEnum(descr.getType())
            ,   descr.isRequired()
            ,   descr.getContextTypeName()
            ,   descr.getContextSubtypeName()    
            ,   operators.toArray(new String[operators.size()])
            ,   allowedAttributes.toArray(new AttributeDescriptorDTO[allowedAttributes.size()])
            ,   enumeratedValues.toArray(new String[enumeratedValues.size()])
        );
    }

    /**
     * Converts an <code>AttributeTypeEnum</code> to
     * <code>AttributeType</code>.
     * 
     * @param att
     *            the <code>AttributeTypeEnum</code> to be converted.
     * @return the <code>AttributeType</code> corresponding to the given
     *         <code>AttributeTypeEnum</code>.
     */
    public static AttributeType makeAttributeType(AttributeTypeEnum att) {
        if (att == null) {
            return null;
        }
        return AttributeType.forName(att.getValue());
    }

    /**
     * Converts an <code>AttributeType</code> to
     * <code>AttributeTypeEnum</code>.
     * 
     * @param att
     *            the <code>AttributeType</code> to be converted.
     * @return the <code>AttributeTypeEnum</code> corresponding to the given
     *         <code>AttributeType</code>.
     */
    public static AttributeTypeEnum makeAttributeTypeEnum(AttributeType type) {
        if (type == null) {
            return null;
        }
        return AttributeTypeEnum.fromString(type.getName());
    }

    /**
     * Converts an <code>IAction</code> to <code>Access</code>.
     * 
     * @param access
     *            the <code>IAction</code> to be converted.
     * @return the resulting <code>Access</code>.
     */
    public static Access makeAccess(IAction action) {
        if (action == null) {
            return null;
        }
        return Access.fromString(action.getName());
    }

    /**
     * Converts an <code>Access</code> to <code>IAction</code>.
     * 
     * @param access
     *            the <code>Access</code> to be converted.
     * @return the resulting <code>IAction</code>.
     */
    public static IAction makeAction(Access access) {
        if (access == null) {
            return null;
        }
        return DAction.getAction(access.getValue());
    }

    /**
     * Converts a <code>Collection</code> of <code>IAction</code> objects to
     * an <code>AccessList</code>.
     * 
     * @param list
     *            the <code>Collection</code> of <code>IAction</code>
     *            objects to be converted.
     * @return the resulting <code>AccessList</code>.
     */
    public static AccessList makeAccessList(Collection<? extends IAction> actions) {
        if (actions == null) {
            return null;
        }
        IAction[] act = actions.toArray(new IAction[actions.size()]);
        Access[] acc = new Access[act.length];
        for (int i = 0; i != act.length; i++) {
            acc[i] = makeAccess(act[i]);
        }
        return new AccessList(acc);
    }

    /**
     * Converts an <code>AccessList</code> to a <code>Collection</code> of
     * <code>IAction</code> objects.
     * 
     * @param list
     *            the <code>AccessList</code> to be converted.
     * @return the resulting <code>Collection</code> of <code>IAction</code>
     *         objects.
     */
    public static Collection<IAction> makeCollectionOfActions(AccessList list) {
        if (list == null) {
            return null;
        }
        Access[] accesses = list.getAccess();
        if (accesses == null) {
            return null;
        }
        IAction[] res = new IAction[accesses.length];
        for (int i = 0; i != accesses.length; i++) {
            res[i] = makeAction(accesses[i]);
        }
        return Arrays.asList(res);
    }

    public static Collection<AgentStatusDescriptor> makeCollectionOfAgentStatusDescriptors(AgentStatusList asl) {
        if (asl == null) {
            return null;
        }
        AgentStatusDescriptorDTO[] asd = asl.getElements();
        if (asd == null) {
            return Collections.emptyList();
        }
        List<AgentStatusDescriptor> res = new ArrayList<AgentStatusDescriptor>(asd.length);
        for (int i = 0; i != asd.length; i++) {
            res.add(makeAgentStatusDescriptor(asd[i]));
        }
        return res;
    }

    public static AgentStatusDescriptor makeAgentStatusDescriptor(AgentStatusDescriptorDTO dto) {
        if (dto == null) {
            return null;
        }
        return new AgentStatusDescriptor(
            dto.getAgentId() 
        ,   dto.getAgentHost()
        ,   AgentTypeEnumType.getAgentType(dto.getAgentType().getValue())
        ,   dto.getLastUpdated() != null ? dto.getLastUpdated().getTime() : UnmodifiableDate.START_OF_TIME
        ,   dto.getNumPolicies()
        ,   dto.getNumComponents());
    }

    public static AgentStatusList makeAgentStatusList(Collection<AgentStatusDescriptor> asd) {
        if (asd == null) {
            return null;
        }
        AgentStatusDescriptorDTO[] res = new AgentStatusDescriptorDTO[asd.size()];
        int i = 0;
        for (AgentStatusDescriptor sd : asd) {
            res[i++] = makeAgentStatusDescriptorDTO(sd);
        }
        return new AgentStatusList(res);
    }

    public static AgentStatusDescriptorDTO makeAgentStatusDescriptorDTO(AgentStatusDescriptor asd) {
        if (asd == null) {
            return null;
        }
        return new AgentStatusDescriptorDTO(
                asd.getId()
              , asd.getHostName()
              , AgentTypeEnum.fromString(asd.getAgentType().getName())
              , convertToCalendar(asd.getLastUpdated())
              , asd.getNumPolicies()
              , asd.getNumComponents()
        );
    }

    public static Collection<EntityType> makeCollectionOfEntityTypes(ComponentList cl) {
        Component[] comps = cl.getComponents();
        if (comps == null) {
            return Collections.emptyList();
        }
        List<EntityType> res = new ArrayList<EntityType>(comps.length);
        for (int i = 0; i < comps.length; i++) {
            res.add(componentMap.get(comps[i]));
        }
        return res;
    }

    /**
     * Converts a <code>Collection</code> of <code>EntityType</code> objects
     * to a <code>ComponentList</code>.
     * 
     * @param entityTypes
     *            the <code>Collection</code> of <code>EntityType</code>
     *            objects to be converted.
     * @return a <code>ComponentList</code> with the elements of the original
     *         <code>Collection</code>.
     */
    public static ComponentList makeComponentList(Collection<Component> components) {
        if (components == null) {
            return null;
        }
        return new ComponentList(components.toArray(new Component[components.size()]));
    }

    public static final String encodeDeploymentBundle(DeploymentBundleSignatureEnvelope deploymentBundleEnvelope) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            GZIPOutputStream zipStream = new GZIPOutputStream(outStream);
            ObjectOutputStream oos = new ObjectOutputStream(zipStream);
            oos.writeObject(deploymentBundleEnvelope);
            oos.close();

            byte[] bytes = outStream.toByteArray();
            return Base64.encode(bytes);
        } catch (IOException ioe) {
            // this should never happen
            throw new RuntimeException(ioe);
        }
    }

    public static final DeploymentBundleSignatureEnvelope decodeDeploymentBundle(String db) {
        try {
            byte[] bytes = Base64.decode(db);

            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes)));
            DeploymentBundleSignatureEnvelope rv = (DeploymentBundleSignatureEnvelope) ois.readObject();
            ois.close();
            return rv;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    /**
     * Converts a <code>LeafObjectSearchSpecDTO</code> to a
     * <code>LeafObjectSearchSpec</code>.
     * 
     * @param dto
     *            the <code>LeafObjectSearchSpecDTO</code> to convert.
     * @return the resulting <code>LeafObjectSearchSpec</code>.
     */
    public static final LeafObjectSearchSpec searchSpecFromDTO(LeafObjectSearchSpecDTO dto) {
        if (dto == null) {
            throw new NullPointerException("dto");
        }
        String pql = dto.getSpec();
        if (pql == null) {
            throw new NullPointerException("dto.spec");
        }
        DomainObjectBuilder dob = new DomainObjectBuilder(pql);
        final IPredicate[] res = new IPredicate[1];
        try {
            dob.processInternalPQL(new DefaultPQLVisitor() {

                /**
                 * @see DefaultPQLVisitor#visitComponent(DomainObjectDescriptor,
                 *      IPredicate)
                 */
                public void visitComponent(DomainObjectDescriptor descriptor, IPredicate spec) {
                    res[0] = spec;
                }
            });
        } catch (PQLException pqlEx) {
            throw new IllegalArgumentException("dto contains an invalid search spec.");
        }
        return new LeafObjectSearchSpec(
                makeLeafObjectType(dto.getType())
              , res[0]
              , dto.getMaxResults().intValue()
        );
    }

    /**
     * Converts a <code>LeafObjectSearchSpec</code> to a
     * <code>LeafObjectSearchSpecDTO</code>
     * 
     * @param spec
     *            the <code>LeafObjectSearchSpec</code> to convert.
     * @return the resulting <code>LeafObjectSearchSpecDTO</code>.
     */
    public static final LeafObjectSearchSpecDTO searchSpecToDTO(LeafObjectSearchSpec spec) {
        DomainObjectFormatter dof = new DomainObjectFormatter();
        dof.formatDef(new DomainObjectDescriptor(
                null
              , "IGNORED"
              , null
              , null
              , EntityType.COMPONENT
              , null
              , DevelopmentStatus.APPROVED)
            , spec.getSearchPredicate());
        return new LeafObjectSearchSpecDTO(
                dof.getPQL()
              , spec.getNamespaceId()
              , makeLeafObjectEnum(spec.getLeafObjectType())
              , new NonNegativeInteger("" + spec.getMaxResults())
        );
    }

    /**
     * Transforms an array of <code>long</code> values into a
     * <code>ListOfIds</code>.
     * 
     * @param ids
     *            an array of <code>long</code> values to transform.
     * @return a resulting <code>ListOfIds</code>.
     */
    public static ListOfIds makeListOfIds(long[] ids) {
        if (ids == null) {
            return null;
        }
        BigInteger[] res = new BigInteger[ids.length];
        for (int i = 0; i != ids.length; i++) {
            res[i] = BigInteger.valueOf(ids[i]);
        }
        return new ListOfIds(res);
    }

    /**
     * Transforms a <code>List</code> of <code>Long</code> values into a
     * <code>ListOfIds</code>.
     * 
     * @param ids
     *            an <code>List</code> of <code>Long</code> values to transform.
     * @return a resulting <code>ListOfIds</code>.
     */
    public static ListOfIds makeListOfIds(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        BigInteger[] res = new BigInteger[ids.size()];

        int i = 0;
        for (Long id : ids) {
            res[i++] = BigInteger.valueOf(id);
        }
        return new ListOfIds(res);
    }

    /**
     * Transform a <code>ListOfIds</code> into a <code>List</code> of <code>Long</code>
     * values
     *
     * @param ids the <code>ListOfIds</code>
     * @return a <code>List</code> of <code>Long</code> values representing the ids
     */
    public static List<Long> makeListFromListOfIds(ListOfIds list) {
        if (list == null) {
            return null;
        }
        BigInteger[] ids = list.getId();
        if (ids == null) {
            return new ArrayList<Long>();
        }
        
        Long[] res = new Long[ids.length];
        for (int i = 0; i != ids.length; i++) {
            res[i] = ids[i].longValue();
        }
        return Arrays.asList(res);
    }

    /**
     * Transforms a <code>ListOfIds</code> into an array of <code>long</code>
     * values.
     * 
     * @param list
     *            a <code>ListOfIds</code> to transform.
     * @return a resulting array of <code>long</code> values.
     */
    public static long[] makeArrayOfIds(ListOfIds list) {
        if (list == null) {
            return null;
        }
        BigInteger[] ids = list.getId();
        if (ids == null) {
            return new long[0];
        }
        long[] res = new long[ids.length];
        for (int i = 0; i != ids.length; i++) {
            res[i] = ids[i].longValue();
        }
        return res;
    }

}
