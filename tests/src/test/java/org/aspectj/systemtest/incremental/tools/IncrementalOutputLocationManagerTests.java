/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.incremental.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.IOutputLocationManager;

/**
 * OutputLocationManger tests which check whether the correct type of build has happened.
 */
public class IncrementalOutputLocationManagerTests extends AbstractMultiProjectIncrementalAjdeInteractionTestbed {

	public void testPr166580() {
		initialiseProject("PR166580");
		configureOutputLocationManager("PR166580", new MyOutputLocationManager("PR166580", 2));
		build("PR166580");
		checkWasFullBuild();
		alter("PR166580", "inc1");
		build("PR166580");
		checkWasntFullBuild();
	}

	/**
	 * Will send output from src dir 'srcX' to directory 'binX'
	 */
	private class MyOutputLocationManager implements IOutputLocationManager {

		private String projectDir;
		private int numberOfSrcDirs;
		private List<File> allOutputDirs;

		public MyOutputLocationManager(String projectName, int numberOfSrcDirs) {
			projectDir = getWorkingDir() + File.separator + projectName;
			this.numberOfSrcDirs = numberOfSrcDirs;
		}

		public void reportFileWrite(String outputfile, int filetype) {
		}

		public void reportFileRemove(String outputfile, int filetype) {
		}
		
		public Map<File,String> getInpathMap() {
			return Collections.emptyMap();
		}

		public File getOutputLocationForClass(File compilationUnit) {
			String path = compilationUnit.getAbsolutePath();
			int index = path.indexOf("src");
			String number = path.substring(index + 3, index + 4);
			File ret = new File(projectDir + File.separator + "bin" + number);
			if (!ret.exists()) {
				ret.mkdirs();
			}
			return ret;
		}

		public File getOutputLocationForResource(File resource) {
			return getOutputLocationForClass(resource);
		}

		public List<File> getAllOutputLocations() {
			if (allOutputDirs == null) {
				allOutputDirs = new ArrayList<>();
				for (int i = 0; i < numberOfSrcDirs + 1; i++) {
					File f = null;
					if (i == 0) {
						f = new File(projectDir + File.separator + "bin");
					} else {
						f = new File(projectDir + File.separator + "bin" + i);
					}
					allOutputDirs.add(f);
				}
			}
			return allOutputDirs;
		}

		public File getDefaultOutputLocation() {
			return new File(projectDir + File.separator + "bin");
		}

		public String getSourceFolderForFile(File sourceFile) {
			return null;
		}

		public int discoverChangesSince(File dir, long buildtime) {
			// TODO Auto-generated method stub
			return 0;
		}

	}

}
