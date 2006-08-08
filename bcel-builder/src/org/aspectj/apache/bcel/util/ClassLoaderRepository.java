package org.aspectj.apache.bcel.util;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;

/**
 * The repository maintains information about which classes have
 * been loaded.
 *
 * It loads its data from the ClassLoader implementation
 * passed into its constructor.
 *
 * @see org.aspectj.apache.bcel.Repository
 *
 * @version $Id: ClassLoaderRepository.java,v 1.6 2006/08/08 11:26:28 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @author David Dixon-Peugh
 */
public class ClassLoaderRepository implements Repository {
  private java.lang.ClassLoader loader;
  private WeakHashMap /*<String classname,JavaClass>*/loadedClassesLocalCache = new WeakHashMap(); 
  private static Map /*<URL,JavaClass>*/loadedUrlsSharedCache = new HashMap(); 
  public static boolean useSharedCache = true;
  
  private static long timeManipulatingURLs = 0L; 
  private static long timeSpentLoading     = 0L;
  private static int  classesLoadedCount = 0;
  private static int  cacheHitsShared    = 0;
  private static int  missSharedEvicted  = 0; // Misses in shared cache access due to reference GC
  private static int  misses             = 0;
  private int  cacheHitsLocal     = 0;
  private int  missLocalEvicted   = 0; // Misses in local cache access due to reference GC

  static {
    useSharedCache = System.getProperty("org.aspectj.apache.bcel.useSharedCache","true").equalsIgnoreCase("true");
  }
  
  public ClassLoaderRepository( java.lang.ClassLoader loader ) {
    this.loader = loader;
  }

  /**
   * Store a new JavaClass into this repository as a soft reference and return the reference
   */
  private Reference storeClassAsReference( JavaClass clazz ) {
	Reference ref = new SoftReference(clazz);
    loadedClassesLocalCache.put( clazz.getClassName(), ref);		       
    clazz.setRepository( this );
    return ref;
  }
  
  /**
   * Store a reference in the shared cache
   */
  private void storeReferenceShared(URL url, Reference ref) {
	  if (useSharedCache) loadedUrlsSharedCache.put(url, ref);
  }

  /**
   * Store a new JavaClass into this Repository.
   */
  public void storeClass( JavaClass clazz ) {
	  storeClassAsReference(clazz);
  }

  /**
   * Remove class from repository
   */
  public void removeClass(JavaClass clazz) {
    loadedClassesLocalCache.remove(clazz.getClassName());
  }

  /**
   * Find an already defined JavaClass in the local cache.
   */
  public JavaClass findClass( String className ) {
    Object o = loadedClassesLocalCache.get( className );
    if (o != null) {
    	o = ((Reference)o).get();
    	if (o != null) {
    		return (JavaClass)o;
    	} else {
    		missLocalEvicted++;
    	}
    }
    return null;
  }
  
  /**
   * Find an already defined JavaClass in the shared cache.
   */
  private JavaClass findClassShared(URL url) {
	  if (!useSharedCache) return null;
	  Object o = (Reference)loadedUrlsSharedCache.get(url);
	  if (o != null) {
		o = ((Reference)o).get();
		if (o != null) {
			return (JavaClass)o; 
		} else { 
			missSharedEvicted++; 
		}
	  }
	  return null;
  }

  
  /**
   * Lookup a JavaClass object from the Class Name provided.
   */
  public JavaClass loadClass( String className ) throws ClassNotFoundException {
    String classFile = className.replace('.', '/');

    // Check the local cache
    JavaClass clazz = findClass(className);
    if (clazz != null) { cacheHitsLocal++; return clazz; }

    try {
    	// Work out the URL
    	long time = System.currentTimeMillis();
    	java.net.URL url = (useSharedCache?loader.getResource( classFile + ".class" ):null);
    	if (useSharedCache && url==null) throw new ClassNotFoundException(className + " not found.");
		InputStream is = (useSharedCache?url.openStream():loader.getResourceAsStream( classFile + ".class" ));
		timeManipulatingURLs += (System.currentTimeMillis() - time);
		
		// Check the shared cache
		clazz = findClassShared(url);
		if (clazz != null) { cacheHitsShared++; timeSpentLoading+=(System.currentTimeMillis() - time); return clazz; } 

		// Didn't find it in either cache
		misses++;
	    
        if (is == null) { // TODO move this up?
    	  throw new ClassNotFoundException(className + " not found.");
        }

        ClassParser parser = new ClassParser( is, className );
        clazz = parser.parse();
	    
        // Store it in both caches
        Reference ref = storeClassAsReference( clazz );
        storeReferenceShared(url,ref);

        timeSpentLoading += (System.currentTimeMillis() - time);
	    classesLoadedCount++;
        return clazz;
    } catch (IOException e) {
      throw new ClassNotFoundException( e.toString() );
    }
  }
  

/**
   * Produce a report on cache usage.
   */
  public String reportAllStatistics() {
	  StringBuffer sb = new StringBuffer();
	  sb.append("BCEL repository report.");
	  if (!useSharedCache) sb.append(" (Shared cache deactivated)");
	  sb.append(" Total time spent loading: "+timeSpentLoading+"ms.");
	  sb.append(" Time manipulating URLs: "+timeManipulatingURLs+"ms.");
	  sb.append(" Classes loaded: "+classesLoadedCount+".");
	  if (useSharedCache) sb.append(" URL cache (hits/missDueToEviction): ("+cacheHitsShared+"/"+missSharedEvicted+").");
	  sb.append(" Local cache (hits/missDueToEviction): ("+cacheHitsLocal+"/"+missLocalEvicted+").");
	  return sb.toString();
  }
  
  public int reportLocalCacheHits() {
	  return cacheHitsLocal;
  }

  public static int reportSharedCacheHits() {
	  return cacheHitsShared;
  }
  
  /**
   * Reset statistics and clear all caches
   */
  public void reset() {
	  timeManipulatingURLs = 0L; 
	  timeSpentLoading = 0L;
	  classesLoadedCount = 0;
	  cacheHitsLocal    = 0;
	  cacheHitsShared   = 0;
	  missSharedEvicted = 0; 
	  missLocalEvicted  = 0; 
	  misses = 0;
	  clear();
	  clearShared();
  }
  
  
  public JavaClass loadClass(Class clazz) throws ClassNotFoundException {
    return loadClass(clazz.getName());
  }

  /** Clear all entries from the local cache */
  public void clear() {
      loadedClassesLocalCache.clear();
  }

  /** Clear all entries from the shared cache */
  public static void clearShared() {
	  loadedUrlsSharedCache.clear();
  }
}

