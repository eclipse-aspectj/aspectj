/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.aspectj.weaver.tools.cache.AbstractIndexedFileCacheBacking.IndexEntry;

import junit.framework.TestCase;

/**
 */
public abstract class AbstractCacheBackingTestSupport extends TestCase {
	public static final String	JAR_FILE_SUFFIX=".jar";
    /**
     * Prefix used in URL(s) that reference a resource inside a JAR
     */
    public static final String	JAR_URL_PREFIX="jar:";
    /**
     * Separator used in URL(s) that reference a resource inside a JAR
     * to denote the sub-path inside the JAR
     */
    public static final char	RESOURCE_SUBPATH_SEPARATOR='!';

	private File	targetFolder;
	private File	testTempFolder;
	protected File root;

	public static final String TEMP_SUBFOLDER_NAME="temp";

	protected AbstractCacheBackingTestSupport() {
		super();
	}

	protected AbstractCacheBackingTestSupport(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (root == null) {
			root = createTempFile("aspectj", "testdir");
			FileUtil.deleteContents(root);
		}
	}

	@Override
	public void tearDown() throws Exception {
		if (root != null) {
			FileUtil.deleteContents(root);
			root = null;
		}

		if (targetFolder != null) {
			FileUtil.deleteContents(targetFolder);
		}

		super.tearDown();
	}

    protected File ensureTempFolderExists () throws IllegalStateException {
    	synchronized(TEMP_SUBFOLDER_NAME) {
    		if (testTempFolder == null) {
    			File	parent=detectTargetFolder();
    			testTempFolder = new File(parent, TEMP_SUBFOLDER_NAME);
    		}
    	}

    	return ensureFolderExists(testTempFolder);
    }

    protected File detectTargetFolder () throws IllegalStateException {
    	synchronized(TEMP_SUBFOLDER_NAME) {
    		if (targetFolder == null) {
			try {
				File targetFolder = File.createTempFile("ajc", "TmpCacheDir");
				targetFolder.delete();
				targetFolder.mkdirs();
			} catch (IOException e) {
				throw new IllegalStateException("Unable to create cache dir",e);
			}

			
//    			if ((targetFolder=detectTargetFolder(getClass())) == null) {
//    				
////    				throw new IllegalStateException("Failed to detect target folder");
//    			}
    		}
    	}

    	return targetFolder;
    }

    protected File createTempFile (String prefix, String suffix) throws IOException {
    	File	destFolder=ensureTempFolderExists();
    	return File.createTempFile(prefix, suffix, destFolder);
    }

    public static final File ensureFolderExists (File folder) throws IllegalStateException {
    	if (folder == null) {
    		throw new IllegalArgumentException("No folder to ensure existence");
    	}

    	if ((!folder.exists()) && (!folder.mkdirs())) {
    		throw new IllegalStateException("Failed to create " + folder.getAbsolutePath());
    	}

    	return folder;
    }
    /**
     * @param anchor An anchor {@link Class} whose container we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     * @see #detectTargetFolder(File)
     */
    public static final File detectTargetFolder (Class<?> anchor) {
    	return detectTargetFolder(getClassContainerLocationFile(anchor));
    }
    
    /**
     * @param anchorFile An anchor {@link File) we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     */
    public static final File detectTargetFolder (File anchorFile) {
    	for (File	file=anchorFile; file != null; file=file.getParentFile()) {
    		if (!file.isDirectory()) {
    			continue;
    		}
    		
    		String	name=file.getName();
    		if ("target".equals(name) || "bin".equals(name) || "src".equals(name)) {
    			File	parent=file.getParentFile();
    			return new File(parent, "target2");
    		}
    	}

    	return null;
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link File} of the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws IllegalArgumentException If location is not a valid
     * {@link File} location
     * @see #getClassContainerLocationURI(Class)
     * @see File#File(URI) 
     */
    public static File getClassContainerLocationFile (Class<?> clazz)
            throws IllegalArgumentException {
        try {
            URI uri=getClassContainerLocationURI(clazz);
            return (uri == null) ? null : new File(uri);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URI} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws URISyntaxException if location is not a valid URI
     * @see #getClassContainerLocationURL(Class)
     */
    public static URI getClassContainerLocationURI (Class<?> clazz) throws URISyntaxException {
        URL url=getClassContainerLocationURL(clazz);
        return (url == null) ? null : url.toURI();
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URL} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     */
    public static URL getClassContainerLocationURL (Class<?> clazz) {
        ProtectionDomain    pd=clazz.getProtectionDomain();
        CodeSource          cs=(pd == null) ? null : pd.getCodeSource();
        URL					url=(cs == null) ? null : cs.getLocation();
        if (url == null) {
        	ClassLoader	cl=getDefaultClassLoader(clazz);
        	String		className=clazz.getName().replace('.', '/') + ".class";
        	if ((url=cl.getResource(className)) == null) {
        		return null;
        	}
        	
        	String	srcForm=getURLSource(url);
        	if (LangUtil.isEmpty(srcForm)) {
        		return null;
        	}

        	try {
        		url = new URL(srcForm);
        	} catch(MalformedURLException e) {
        		throw new IllegalArgumentException("getClassContainerLocationURL(" + clazz.getName() + ")"
        										  + "Failed to create URL=" + srcForm + " from " + url.toExternalForm()
        										  + ": " + e.getMessage());
        	}
        }

        return url;
    }
    /**
     * @param anchor An &quot;anchor&quot; {@link Class} to be used in case
     * no thread context loader is available
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load the anchor class.
     *      </LI>
     * </UL>
     * @throws IllegalArgumentException if no anchor class provided (regardless of
     * whether it is used or not) 
     */
    public static ClassLoader getDefaultClassLoader(Class<?> anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("No anchor class provided");
        }

        Thread      t=Thread.currentThread();
        ClassLoader cl=t.getContextClassLoader();
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = anchor.getClassLoader();
        }

        if (cl == null) {	// no class loader - assume system
        	cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
        
    }
    public static final String getURLSource (File file) {
    	return getURLSource((file == null) ? null : file.toURI());
    }

    public static final String getURLSource (URI uri) {
    	return getURLSource((uri == null) ? null : uri.toString());
    }

    /**
     * @param url The {@link URL} value - ignored if <code>null</code>
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     * @see #getURLSource(String)
     */
    public static final String getURLSource (URL url) {
    	return getURLSource((url == null) ? null : url.toExternalForm());
    }
    
    /**
     * @param externalForm The {@link URL#toExternalForm()} string - ignored if
     * <code>null</code>/empty
     * @return The URL(s) source path where {@link #JAR_URL_PREFIX} and
     * any sub-resource are stripped
     */
    public static final String getURLSource (String externalForm) {
		String	url=externalForm;
    	if (LangUtil.isEmpty(url)) {
    		return url;
    	}

    	url = stripJarURLPrefix(externalForm);
    	if (LangUtil.isEmpty(url)){
    		return url;
    	}
    	
    	int	sepPos=url.indexOf(RESOURCE_SUBPATH_SEPARATOR);
    	if (sepPos < 0) {
    		return adjustURLPathValue(url);
    	} else {
    		return adjustURLPathValue(url.substring(0, sepPos));
    	}
    }

    /**
     * @param path A URL path value
     * @return The path after stripping any trailing '/' provided the path
     * is not '/' itself
     */
    public static final String adjustURLPathValue(final String path) {
        final int   pathLen=LangUtil.isEmpty(path) ? 0 : path.length();
        if ((pathLen <= 1) || (path.charAt(pathLen - 1) != '/')) {
            return path;
        }

        return path.substring(0, pathLen - 1);
    }

    public static final String adjustURLPathValue(URL url) {
        return adjustURLPathValue((url == null) ? null : url.getPath());
    }

	public static String stripJarURLPrefix(String externalForm) {
		String	url=externalForm;
    	if (LangUtil.isEmpty(url)) {
    		return url;
    	}

    	if (url.startsWith(JAR_URL_PREFIX)) {
    		return url.substring(JAR_URL_PREFIX.length());
    	}    	
    	
    	return url;
	}

    protected static final void writeIndex (File indexFile, IndexEntry ... entries) throws IOException {
        writeIndex(indexFile, LangUtil.isEmpty(entries) ? Collections.<IndexEntry>emptyList() : Arrays.asList(entries));
    }

    protected static final void writeIndex (File indexFile, Collection<? extends IndexEntry> entries) throws IOException {
        File    indexDir=indexFile.getParentFile();
        if ((!indexDir.exists()) && (!indexDir.mkdirs())) {
            throw new IOException("Failed to create path to " + indexFile.getAbsolutePath());
        }

        int             numEntries=LangUtil.isEmpty(entries) ? 0 : entries.size();
        IndexEntry[]    entryValues=(numEntries <= 0) ? null : entries.toArray(new IndexEntry[numEntries]);
        // if no entries, simply delete the index file
        if (LangUtil.isEmpty(entryValues)) {
            if (indexFile.exists() && (!indexFile.delete())) {
                throw new StreamCorruptedException("Failed to clean up index file at " + indexFile.getAbsolutePath());
            }

            return;
        }

        ObjectOutputStream oos=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile), 4096));
        try {
            oos.writeObject(entryValues);
        } finally {
            oos.close();
        }
    }

    public static final void assertArrayEquals (String msg, byte[] expected, byte[] actual) {
    	int	eLen=LangUtil.isEmpty(expected) ? 0 : expected.length;
    	int	aLen=LangUtil.isEmpty(actual) ? 0 : expected.length;
    	assertEquals(msg + "[mismatched length]", eLen, aLen);

    	for (int	index=0; index < eLen; index++) {
    		byte	eb=expected[index], ab=actual[index];
    		if (eb != ab) {
    			fail(msg + ": Mismatched value at index=" + index
    			   + " - " + ab + " instead of " + eb
    			   + ": expected=" + Arrays.toString(expected) + ", actual=" + Arrays.toString(actual));
    		}
    	}
    }
}
