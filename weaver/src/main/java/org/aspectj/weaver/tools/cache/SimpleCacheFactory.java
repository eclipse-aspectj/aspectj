/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abraham Nevado (lucierna) initial implementation
 ********************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;

import org.aspectj.weaver.Dump;

public class SimpleCacheFactory {
	
	public static final String CACHE_ENABLED_PROPERTY = "aj.weaving.cache.enabled";
	public static final String CACHE_DIR = "aj.weaving.cache.dir";
	public static final String CACHE_IMPL = "aj.weaving.cache.impl";
	
	public static final String PATH_DEFAULT= "/tmp/"; // TODO windows default...?
	public static final boolean BYDEFAULT= false;	
		
		
	public static String path = PATH_DEFAULT;
	public static Boolean enabled = false;
	private static boolean determinedIfEnabled = false;
	private static SimpleCache lacache=null;
	
	public static synchronized SimpleCache createSimpleCache(){
		if (lacache==null){
		 	if (!determinedIfEnabled) {
		 		determineIfEnabled();
		 	}

			if (!enabled) {
				return null;
			}

			try {
				path = System.getProperty(CACHE_DIR);
				if (path == null){
					path = PATH_DEFAULT;
				}
				
			} catch (Throwable t) {
				path=PATH_DEFAULT;
				t.printStackTrace();
				Dump.dumpWithException(t);
			}
			File f = new File(path);
			if (!f.exists()){
				f.mkdir();
			}
			lacache= new SimpleCache(path, enabled);
		}
		return lacache;
		
	}

	private static void determineIfEnabled() {
		try {
			String property = System.getProperty(CACHE_ENABLED_PROPERTY);
			if (property == null ){
				enabled = BYDEFAULT;
			}
			else if (property.equalsIgnoreCase("true")){
				
					String impl = System.getProperty(CACHE_IMPL);
					if (SimpleCache.IMPL_NAME.equals(impl)){
						enabled = true;
					}
					else{
						enabled = BYDEFAULT;
					}
			}
			else{
				enabled = BYDEFAULT;
			}
			
		} catch (Throwable t) {
			enabled=BYDEFAULT;
			System.err.println("Error creating cache");
			t.printStackTrace();
			Dump.dumpWithException(t);
		}
		determinedIfEnabled = true;
	}
	
	// Should behave ok with two threads going through here, well whoever gets there first will set determinedIfEnabled but only after
	// it has set 'enabled' to the right value.
	public static boolean isEnabled() {
		if (!determinedIfEnabled) {
			determineIfEnabled();
		}
		return enabled;
	} 
	

}
