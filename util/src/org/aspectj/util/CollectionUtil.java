/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.util;


import java.util.*;

public class CollectionUtil {
	public static final String[] NO_STRINGS = new String[0];
	
	
    public static List getListInMap(Map map, Object key) {
        List list = (List)map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        return list;
    }

    public static SortedSet getSortedSetInMap(Map map, Object key) {
        SortedSet list = (SortedSet)map.get(key);
        if (list == null) {
            list = new TreeSet();
            map.put(key, list);
        }
        return list;
    }

    public static Set getSetInMap(Map map, Object key) {
        Set list = (Set)map.get(key);
        if (list == null) {
            list = new HashSet();
            map.put(key, list);
        }
        return list;
    }

    public static Map getMapInMap(Map map, Object key) {
        Map list = (Map)map.get(key);
        if (list == null) {
            list = new HashMap();
            map.put(key, list);
        }
        return list;
    }
    
}
