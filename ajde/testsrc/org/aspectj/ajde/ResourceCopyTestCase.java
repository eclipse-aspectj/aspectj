/*
 * Created on 31-Jul-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.aspectj.ajde;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.aspectj.util.FileUtil;

/**
 * @author websterm
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ResourceCopyTestCase extends AjdeTestCase {

	public static final String PROJECT_DIR = "bug-36071"; 
	public static final String srcDir = PROJECT_DIR + "/src"; 
	public static final String binDir = PROJECT_DIR + "/bin"; 

	public static final String injar1 = "testdata/bug-40943/input1.jar"; 
	public static final String injar2 = "testdata/bug-40943/input2.jar"; 
	public static final String outjar = "testdata/bug-40943/output.jar"; 

	/**
	 * Constructor for JarResourceCopyTestCase.
	 * @param arg0
	 */
	public ResourceCopyTestCase(String arg0) {
		super(arg0);
	}

	/*
	 * Ensure the output directpry in clean
	 */	
	protected void setUp() throws Exception {
		super.setUp(PROJECT_DIR);
		FileUtil.deleteContents(new File(binDir));
	}
	
	public void testSrcToBin () {
		doSynchronousBuild("config.lst");
		assertTrue(!Ajde.getDefault().getTaskListManager().hasWarning());
		assertTrue(new java.io.File("testdata/bug-36071").getAbsolutePath(), compareDirs("src", "bin"));
	}
	
//	public void testInjarsToBin () {
//		List args = new ArrayList();
//		args.add("-injars");
//		args.add(injar1);
//		
//		args.add("-d");
//		args.add(binDir);
//
//		args.add("-classpath");
//		args.add("../runtime/bin");
//	
//		args.add("testdata/bug-40943/aspects/Logging.java");
//	
//		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
//
//		assertTrue(new java.io.File("testdata/bug-40943").getAbsolutePath(),compareJarToDirs(injar1,binDir));
//	}
//
//	public void testInjarsToOutjar () {
//		List args = new ArrayList();
//		args.add("-injars");
//		args.add(injar1);
//		
//		args.add("-outjar");
//		args.add(outjar);
//
//		args.add("-classpath");
//		args.add("../runtime/bin");
//	
//		args.add("testdata/bug-40943/aspects/Logging.java");
//	
//		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
//
//		assertTrue(new java.io.File("testdata/bug-40943").getAbsolutePath(),compareJars(injar1,outjar));
//	}
//	
//
//	public void test2InjarsToOutjar () {
//		System.err.println("? test2InjarsToOutjar()");
//		List args = new ArrayList();
//		args.add("-injars");
//		args.add(injar1 + ";" + injar2);
//		
//		args.add("-outjar");
//		args.add(outjar);
//
//		args.add("-classpath");
//		args.add("../runtime/bin");
//	
//		args.add("testdata/bug-40943/aspects/Logging.java");
//	
//		CommandTestCase.runCompiler(args, CommandTestCase.NO_ERRORS);
//
//		assertTrue(new java.io.File("testdata/bug-40943").getAbsolutePath(),compareJars(injar1,outjar));
//	}
	
	/*
	 * Ensure -outjar conatins all non-Java resouces from injars
	 */
	public boolean compareDirs (String indirName, String outdirName) {
		File srcBase = openFile(indirName);
		File binBase = openFile(outdirName);
		File[] fromResources = FileUtil.listFiles(srcBase,aspectjResourceFileFilter);
		File[] toResources = FileUtil.listFiles(binBase,aspectjResourceFileFilter);

		HashSet resources = new HashSet();		
		for (int i = 0; i < fromResources.length; i++) {
			resources.add(FileUtil.normalizedPath(fromResources[i],srcBase));
		}
		
		for (int i = 0; i < toResources.length; i++) {
			String fileName = FileUtil.normalizedPath(toResources[i],binBase);
			boolean b = resources.remove(fileName);
			assertTrue(fileName,b);
		}
		
		assertTrue(resources.toString(), resources.isEmpty());
		return true;
	}	
	
	/*
	 * Ensure -outjar conatins all non-Java resouces from injars
	 */
	public boolean compareJarToDirs (String injarName , String outdirName) {
		File baseDir = new File(binDir);
		System.err.println("? compareJarToDirs() baseDir='" + baseDir + "'");
		File[] files = FileUtil.listFiles(baseDir,aspectjResourceFileFilter);
		for (int i = 0; i < files.length; i++) {
			System.err.println("? compareJarToDirs() name='" + files[i] + "'");
		}
		
		return false;
	}	
	
	/*
	 * Ensure -outjar conatins all non-Java resouces from injars
	 */
	public boolean compareJars (String injarName , String outjarName) {
	
		HashSet resources = new HashSet();
	
		try {	
			File injarFile = new File(injarName);
			File outjarFile = new File(outjarName);
			assertTrue("outjar older than injar",(outjarFile.lastModified() > injarFile.lastModified()));
			
			ZipInputStream injar = new ZipInputStream(new java.io.FileInputStream(injarFile));
			ZipEntry entry;
			while (null != (entry = injar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {
					resources.add(fileName);
				}
				injar.closeEntry();
			}
			injar.close();

			ZipInputStream outjar = new ZipInputStream(new java.io.FileInputStream(outjarFile));
			while (null != (entry = outjar.getNextEntry())) {
				String fileName = entry.getName();
				if (!fileName.endsWith(".class")) {
					boolean b = resources.remove(fileName);
					assertTrue(fileName,b);
				}
				outjar.closeEntry();
			}
			outjar.close();

			assertTrue(resources.toString(),resources.isEmpty());
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
		
		return true;
	}
    
	public static final FileFilter aspectjResourceFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return !name.endsWith(".class") && !name.endsWith(".java") && !name.endsWith(".aj");
		}
	};

}
