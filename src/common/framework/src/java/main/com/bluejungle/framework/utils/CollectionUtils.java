package com.bluejungle.framework.utils;

import java.util.Collection;
import java.util.Iterator;

import com.bluejungle.framework.utils.ArrayUtils;

/**
 * A collectin of useful methods that manipulate Collection
 *
 * @author hchan
 * @date Apr 5, 2007
 */
public final class CollectionUtils {
	public static String[] toStringArray(Collection<String> strs) {
		return strs.toArray(new String[strs.size()]);
	}
	
	private static String DEFAULT_LINE_BREAKER = System.getProperty("line.separator");
	
	public static <T> String toString(Collection<T> objects) {
		return CollectionUtils.asString(objects, DEFAULT_LINE_BREAKER);
	}
	
	public static <T> String asString(Collection<T> objects, String seperator) {
		return CollectionUtils.asString(objects, seperator, "");
	}
	
	public static <T> String asString(Collection<T> objects, String seperator, String prefix) {
		return CollectionUtils.asString(objects, seperator, prefix,  new Formatter<T>(){
            public String toString(T t) {
                return t == null ? "null" : t.toString();
            }
        });
	}
	
	public static <T> String asString(Collection<T> objects, String seperator, String prefix,
            Formatter<T> formatter) {
        StringBuilder sb = new StringBuilder();
        Iterator<T> it = objects.iterator();
        while (it.hasNext()) {
            // beware a null object
            sb.append(prefix).append(formatter.toString(it.next()));
            if (it.hasNext()) {
                sb.append(seperator);
            }
        }

        return sb.toString();
    }
	
	public static long[] toLong(Collection<Long> longs) {
		return longs != null 
				? ArrayUtils.toLong(longs.toArray(new Long[longs.size()])) 
				: null;
	}
	
	public static byte[] toByte(Collection<Byte> bytes){
		return bytes != null
				? ArrayUtils.toByte(bytes.toArray(new Byte[bytes.size()]))
				: null;
	}
}
