/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

import java.lang.ref.WeakReference;

/**
 * Wraps a reference to a classloader inside a WeakReference. This should be used where we do not want the existence of a
 * classloader reference to prevent garbage collection of that classloader (and possibly an associated weaver instance in the case
 * of load time weaving).
 * <p>
 * In more detail:<br>
 * When load time weaving, the class Aj maintains a WeakHashMap from the classloader instance to a weaver instance. The aim is that
 * the weaver is around as long as the classloader is and should the classloader be dereferenced then the weaver can also be garbage
 * collected. The problem is that if there are many references to the classloader from within the weaver, these are considered hard
 * references and cause the classloader to be long lived - even if the user of the classloader has dereferenced it in their code.
 * The solution is that the weaver should use instances of WeakClassLoaderReference objects - so that when the users hard reference
 * to the classloader goes, nothing in the weaver will cause it to hang around. There is a big assertion here that the
 * WeakClassLoaderReference instances will not 'lose' their ClassLoader references until the top level ClassLoader reference is
 * null'd. This means there is no need to check for the null case on get() in this WeakReference logic below, because we shouldn't
 * be using this weaver if its associated ClassLoader has been collected. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=210470
 * 
 * 
 * @author Andy Clement, Abraham Nevado
 */
public class WeakClassLoaderReference{

	protected final int hashcode;

	private final WeakReference loaderRef;

	public WeakClassLoaderReference(ClassLoader loader) {
		loaderRef = new WeakReference(loader);
		if(loader == null){
			// Bug: 363962 
			// Check that ClassLoader is not null, for instance when loaded from BootStrapClassLoader
			hashcode = System.identityHashCode(this);
		}else{
			hashcode = loader.hashCode() * 37;
		}
	}

	public ClassLoader getClassLoader() {
		ClassLoader instance = (ClassLoader) loaderRef.get();
		// Assert instance!=null
		return instance;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof WeakClassLoaderReference))
			return false;
		WeakClassLoaderReference other = (WeakClassLoaderReference) obj;
		return (other.hashcode == hashcode);
	}

	public int hashCode() {
		return hashcode;
	}

}
