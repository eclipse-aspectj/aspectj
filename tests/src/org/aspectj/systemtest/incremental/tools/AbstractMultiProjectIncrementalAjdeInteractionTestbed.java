package org.aspectj.systemtest.incremental.tools;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.bridge.IMessage;
import org.aspectj.testing.util.FileUtil;

public class AbstractMultiProjectIncrementalAjdeInteractionTestbed extends
		AjdeInteractionTestbed {

	public static boolean VERBOSE = false;

	protected void setUp() throws Exception {
		super.setUp();
		AjdeInteractionTestbed.VERBOSE = VERBOSE;
		AjState.FORCE_INCREMENTAL_DURING_TESTING = true;
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		AjState.FORCE_INCREMENTAL_DURING_TESTING = false;
		configureBuildStructureModel(false);
		MyBuildOptionsAdapter.reset();
	}

	public void build(String projectName) {
		constructUpToDateLstFile(projectName,"build.lst");
		build(projectName,"build.lst");
		if (AjdeInteractionTestbed.VERBOSE) printBuildReport();
	}

	public void fullBuild(String projectName) {
		constructUpToDateLstFile(projectName,"build.lst");
		fullBuild(projectName,"build.lst");
		if (AjdeInteractionTestbed.VERBOSE) printBuildReport();
	}

	private void constructUpToDateLstFile(String pname, String configname) {
		File projectBase = new File(sandboxDir,pname);
		File toConstruct = new File(projectBase,configname);
		List filesForCompilation = new ArrayList();
		collectUpFiles(projectBase,projectBase,filesForCompilation);
	
		try {
			FileOutputStream fos = new FileOutputStream(toConstruct);
			DataOutputStream dos = new DataOutputStream(fos);
			for (Iterator iter = filesForCompilation.iterator(); iter.hasNext();) {
				String file = (String) iter.next();
				dos.writeBytes(file+"\n");
			}
			dos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void checkForError(String anError) {
		List messages = MyTaskListManager.getErrorMessages();
		for (Iterator iter = messages.iterator(); iter.hasNext();) {
			IMessage element = (IMessage) iter.next();
			if (element.getMessage().indexOf(anError)!=-1) return;
		}
		fail("Didn't find the error message:\n'"+anError+"'.\nErrors that occurred:\n"+MyTaskListManager.getErrorMessages());
	}

	private void collectUpFiles(File location, File base, List collectionPoint) {
		String contents[] = location.list();
		if (contents==null) return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(location,string);
			if (f.isDirectory()) {
				collectUpFiles(f,base,collectionPoint);
			} else if (f.isFile() && (f.getName().endsWith(".aj") || f.getName().endsWith(".java"))) {
				String fileFound;
				try {
					fileFound = f.getCanonicalPath();
					String toRemove  = base.getCanonicalPath();
					if (!fileFound.startsWith(toRemove)) throw new RuntimeException("eh? "+fileFound+"   "+toRemove);
					collectionPoint.add(fileFound.substring(toRemove.length()+1));//+1 captures extra separator
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Fill in the working directory with the project base files,
	 * from the 'base' folder.
	 */
	protected void initialiseProject(String p) {
		File projectSrc=new File(testdataSrcDir+File.separatorChar+p+File.separatorChar+"base");
		File destination=new File(getWorkingDir(),p);
		if (!destination.exists()) {destination.mkdir();}
		copy(projectSrc,destination);//,false);
	}

	/**
	 * Copy the contents of some directory to another location - the
	 * copy is recursive.
	 */
	protected void copy(File from, File to) {
		String contents[] = from.list();
		if (contents==null) return;
		for (int i = 0; i < contents.length; i++) {
			String string = contents[i];
			File f = new File(from,string);
			File t = new File(to,string);
			
			if (f.isDirectory() && !f.getName().startsWith("inc")) {
				t.mkdir();
				copy(f,t);
			} else if (f.isFile()) {
				StringBuffer sb = new StringBuffer();
				//if (VERBOSE) System.err.println("Copying "+f+" to "+t);
				FileUtil.copyFile(f,t,sb);
				if (sb.length()!=0) { System.err.println(sb.toString());}
			} 
		}
	}

}
