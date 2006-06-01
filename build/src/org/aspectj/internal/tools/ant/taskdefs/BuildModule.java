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

 
package org.aspectj.internal.tools.ant.taskdefs;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.aspectj.internal.tools.build.BuildSpec;
import org.aspectj.internal.tools.build.Builder;

/**
 * Ant interface to build a product or module, including any required modules.
 * @see Builder
 */
public class BuildModule extends Task { // quickie hack...

    public static void main(String[] args) {
        TestBuildModule.main(args);
    }    

    private static File pathToFile(Path path) {
        if (null != path) {
            String[] list = path.list();
            if ((null == list) || (1 != list.length)) {
                throw new IllegalArgumentException("expected exactly 1 element");
            }
            return new File(list[0]);
        }
        return null;
    }
    BuildSpec buildSpec;

    public BuildModule() {
        buildSpec = new BuildSpec();
        setTaskName("ajbuild");
    }
    
    public void setModuledir(Path moduleDir) {
        buildSpec.moduleDir = pathToFile(moduleDir);
    }
    
    public void setModule(String module) { // XXX handle multiple modules, same builder
        buildSpec.module = module;
    }
    
    public void setVersion(String version) {
        buildSpec.version = version;
    }
    public void setBasedir(Path baseDir) {
        buildSpec.baseDir = pathToFile(baseDir);
    }
    
    public void setJardir(Path jarDir) {
        buildSpec.jarDir = pathToFile(jarDir);
    }
    
    public void setTrimtesting(boolean trimTesting) {
        buildSpec.trimTesting = trimTesting;
    }
    
    public void setAssembleall(boolean assembleAll) {
        buildSpec.assembleAll = assembleAll;
    }
    
    public void setRebuild(boolean rebuild) {    
        buildSpec.rebuild = rebuild;
    }
    
    public void setFailonerror(boolean failonerror) {    
        buildSpec.failonerror = failonerror;
    }
    
    public void setCreateinstaller(boolean create) {    
        buildSpec.createInstaller = create;
    }
    
    public void setVerbose(boolean verbose) {    
        buildSpec.verbose = verbose;
    }

    public void setBuildConfig(String buildConfig) {
        buildSpec.buildConfig = buildConfig;
    }

    // --------------------------------------------------------- product build
    
    public void setProductdir(Path productDir) {
        buildSpec.productDir = pathToFile(productDir);
    }
    
    public void setTempdir(Path tempDir) {
        buildSpec.tempDir = pathToFile(tempDir);
    }
    
    public void setDistdir(Path distdir) {
        buildSpec.distDir = pathToFile(distdir);
    }
        
    public void execute() throws BuildException {
        final BuildSpec buildSpec = this.buildSpec;
        this.buildSpec = new BuildSpec();
        build(buildSpec);    
    }
    
    private void build(BuildSpec buildSpec) throws BuildException {
        final boolean failonerror = buildSpec.failonerror;
        Builder builder = null;
        try  {
			// try using script first if not a product
            boolean built = false;
			if ((null == buildSpec.productDir) && (null != buildSpec.moduleDir)) { 
	            File buildScript = new File(buildSpec.moduleDir, "build.xml");  // XXXFileLiteral
	            if (buildScript.canRead()) {
	                built = buildByScript(buildSpec, buildScript);
	                if (!built) {
	                    log("unable to build " 
	                    	+ buildSpec 
	                    	+ " using script: " 
	                    	+ buildScript.getAbsolutePath());
	                }
	            } 
			}
            if (!built) {
                builder = AntBuilder.getBuilder(
                    buildSpec.buildConfig, 
                    getProject(), 
                    buildSpec.tempDir);
                if (!builder.build(buildSpec) && failonerror) {
                    Location loc = getLocation();
                    throw new BuildException("error building " + buildSpec, loc);
                }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Throwable t) {
            Location loc = getLocation();
            throw new BuildException("error building " + buildSpec, t, loc);
        } finally {
            if (null != builder) {
                builder.cleanup();
            }
        }
    }

    boolean buildByScript(BuildSpec buildSpec, File buildScript) 
        throws BuildException {
        return false;
    }
}
 