/* *******************************************************************
 * Copyright (c) 2001-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.tools.ant.taskdefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.aspectj.bridge.CountingMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.aspectj.tools.ajc.Main.MessagePrinter;


/**
 * This runs the AspectJ 1.1 compiler.
 * @since AspectJ 1.1, Ant 1.5
 */
public class AjcTask extends MatchingTask {


	/** @return true if readable and ends with [.java|.aj] */
    protected static boolean isSrcFile(File file) {
        return ((null != file) && file.canRead()
        	&& (file.getName().endsWith(".java")
        		|| file.getName().endsWith(".aj")));
    }
    
    /** valid -X[...] options other than -Xlint variants */
    private static final List VALID_XOPTIONS;

	/** valid warning (-warn:[...]) variants */
    private static final List VALID_WARNINGS;
    
	/** valid debugging (-g:[...]) variants */
    private static final List VALID_DEBUG;

	/** 
	 * -Xlint variants (error, warning, ignore)
	 * @see org.aspectj.weaver.Lint 
	 */
    private static final List VALID_XLINT;
    
//	/** -Xlint variants 
//	 * @see org.aspectj.weaver.Lint 
//	 */
//    private static final List VALID_XLINT_TAGS;
    
    static {
        String[] xs = new String[] 
            {   "serializableAspects", "incrementalFile"
            	//, "targetNearSource", "OcodeSize",
                 };
        VALID_XOPTIONS = Collections.unmodifiableList(Arrays.asList(xs));

        xs = new String[]
        	{"constructorName", "packageDefaultMethod", "deprecation",
        		"maskedCatchBlocks", "unusedLocals", "unusedArguments",
        		"unusedImports", "syntheticAccess", "assertIdentifier" };
        VALID_WARNINGS = Collections.unmodifiableList(Arrays.asList(xs));

        xs = new String[] {"none", "lines", "vars", "source" };
        VALID_DEBUG = Collections.unmodifiableList(Arrays.asList(xs));
        
        
        xs = new String[] { "error", "warning", "ignore"};
        VALID_XLINT = Collections.unmodifiableList(Arrays.asList(xs));
        
//        xs = new String[] 
//        { "invalidAbsoluteTypeName", "invalidWildcardTypeName",
//        	"unresolvableMember" };
//        VALID_XLINT_TAGS = Collections.unmodifiableList(Arrays.asList(xs));
    }
	// ---------------------------- state and Ant interface thereto
	
	// ------- single entries dumped into cmd
    protected Commandline cmd;
	
	// ------- lists resolved in addListArgs() at execute() time
    private Path srcdir;
    private Path injars;
    private Path classpath;
    private Path aspectpath;
    private Path bootclasspath; // XXX supported?
    private List argfiles;
    private List ignored;
    private Path sourceRoots;
    
    // also note MatchingTask grabs source files...

	// ------- interpreted here
    private boolean verbose;
    private boolean failonerror;
    
    AjcTask() {
    	reset();
    }

	/** to use this same Task more than once */
    public void reset() {
    	cmd = new Commandline();
    	srcdir = null;
    	injars = null;
    	classpath = null;
    	aspectpath = null;
    	bootclasspath = null;
    	argfiles = null;
    	ignored = new ArrayList();
		sourceRoots = null;    	
    }

	// ---- private, but delegates of all public setters
    protected void addFlag(String flag, boolean doAdd) {
        if (doAdd) {
        	cmd.addArguments(new String[] {flag});
        }
    }
    
	protected void ignore(String ignored) {
		this.ignored.add(ignored + " at " + getLocation());
	}
    protected void addFlagged(String flag, String argument) {
        cmd.addArguments(new String[] {flag, argument});
    }

    protected String validCommaList(String list, List valid, String label) {
    	return validCommaList(list, valid, label, valid.size());
    }
    
    protected String validCommaList(String list, List valid, String label, int max) {
    	StringBuffer result = new StringBuffer();
    	StringTokenizer st = new StringTokenizer(list, ",");
		int num = 0;
    	while (st.hasMoreTokens()) {
			String token = st.nextToken().trim();
			num++;
			if (num > max) {
				ignore("too many entries for -" 
					+ label 
					+ ": " 
					+ token); 
				break;
			}
			if (!valid.contains(token)) {
				ignore("bad commaList entry for -" 
					+ label 
					+ ": " 
					+ token); 
			} else {
				if (0 < result.length()) {
					result.append(",");
				}
				result.append(token);
			}
    	}
    	return (0 == result.length() ? null : result.toString());
    }
    
	// -------------- Ant interface
    public void setIncremental(boolean incremental) {  
        addFlag("-incremental", incremental);
    }

    public void setHelp(boolean help) {  
        addFlag("-help", help);
    }

    public void setVersion(boolean version) {  
    	addFlag("-version", version);
    }

    public void setNoweave(boolean noweave) {  
        addFlag("-noweave", noweave);
    }

    public void setNowarn(boolean nowarn) {  
        addFlag("-nowarn", nowarn);
    }

    public void setDeprecation(boolean deprecation) {  
        addFlag("-deprecation", deprecation);
    }

    public void setWarn(String warnings) {
    	warnings = validCommaList(warnings, VALID_WARNINGS, "warn");
        addFlag("-warn:" + warnings, (null != warnings));
    }

    public void setDebug(boolean debug) {
        addFlag("-g", debug);
    }
    
    public void setDebugLevel(String level) {
    	level = validCommaList(level, VALID_DEBUG, "g");
        addFlag("-g:" + level, (null != level));
    }

    public void setEmacssym(boolean emacssym) {
        addFlag("-emacssym", emacssym);
    }

	/** 
	 * -Xlint - set default level of -Xlint messages to warning
	 * (same as </code>-Xlint:warning</code>)
	 */
	public void setXlintwarnings(boolean xlintwarnings) {
        addFlag("-Xlint", xlintwarnings);
	}
	
	/** -Xlint:{error|warning|info} - set default level for -Xlint messages
	 * @param xlint the String with one of error, warning, ignored 
	 */
    public void setXlint(String xlint) {
    	xlint = validCommaList(xlint, VALID_XLINT, "Xlint", 1);
        addFlag("-Xlint:" + xlint, (null != xlint));
    }

	/** 
	 * -Xlint:file={lint.properties} - enable or disable specific forms 
	 * of -Xlint messages based on a lint properties file
	 *  (default is 
	 * <code>org/aspectj/weaver/XLintDefault.properties</code>)
	 * @param xlintFile the File with lint properties
	 */
    public void setXlintfile(File xlintFile) { 
        String flag = "-Xlintfile:" + xlintFile.getAbsolutePath();
        addFlag(flag, true);
    }

    public void setPreserveAllLocals(boolean preserveAllLocals) {  
        addFlag("-preserveAllLocals", preserveAllLocals);
    }

    public void setNoImportError(boolean noImportError) {  
        addFlag("-noImportError", noImportError);
    }

    public void setEncoding(String encoding) {   // XXX encoding unvalidated
        addFlagged("-encoding", encoding);
    }

    public void setLog(File file) {
        addFlagged("-log", file.getAbsolutePath());        
    }
    
    public void setProceedOnError(boolean proceedOnError) {  
        addFlag("-proceedOnError", proceedOnError);
    }

    public void setVerbose(boolean verbose) {  
        addFlag("-verbose", verbose);
        this.verbose = verbose;
    }

    public void setReferenceInfo(boolean referenceInfo) {  
        addFlag("-referenceInfo", referenceInfo);
    }

    public void setProgress(boolean progress) {  
        addFlag("-progress", progress);
    }

    public void setTime(boolean time) {  
        addFlag("-time", time);
    }

    public void setNoExit(boolean noExit) {  
        addFlag("-noExit", noExit);
    }

    public void setFailonerror(boolean failonerror) {  
        this.failonerror = failonerror;
    }

	// ----------------
    public void setTagFile(File file) {
        addFlagged(Main.CommandController.TAG_FILE_OPTION,
	        file.getAbsolutePath());        
    }
    
    public void setOutjar(File file) {
        addFlagged("-outjar", file.getAbsolutePath());        
    }

    public void setDestdir(File dir) {
        addFlagged("-d", dir.getAbsolutePath());        
    }
    
    public void setTarget(String either11or12) {
    	if ("1.1".equals(either11or12)) {
    		addFlagged("-target", "1.1");
    	} else if ("1.2".equals(either11or12)) {
    		addFlagged("-target", "1.2");
    	} else {
    		ignore("-target " + either11or12);
    	}   		
    }
    
    /** 
     * Language compliance level.
     * If not set explicitly, eclipse default holds.
     * @param either13or14 either "1.3" or "1.4"
     */
    public void setCompliance(String either13or14) {
    	if ("1.3".equals(either13or14)) {
    		addFlag("-1.3", true);
    	} else if ("1.4".equals(either13or14)) {
    		addFlag("-1.4", true);
    	} else {
    		ignore(either13or14 + "[compliance]");
    	}   		
    }
    
    /** 
     * Source compliance level.
     * If not set explicitly, eclipse default holds.
     * @param either13or14 either "1.3" or "1.4"
     */
    public void setSource(String either13or14) {
    	if ("1.3".equals(either13or14)) {
    		addFlagged("-source", "1.3");
    	} else if ("1.4".equals(either13or14)) {
    		addFlagged("-source", "1.4");
    	} else {
    		ignore("-source " + either13or14);
    	}   		
    }
    //---------------------- accumulate these lists
    public void setSourceRootsList(String commaList) {
    	StringTokenizer st = new StringTokenizer(commaList, ",");
    	while (st.hasMoreTokens()) {
    		String token = st.nextToken().trim();
    		if (0 == token.length()) {
    			ignore("empty source root found");
    		}
    		File srcRoot = new File(token);
    		if (srcRoot.canRead() && srcRoot.isDirectory()) {
    			Path path = new Path(getProject(), srcRoot.getPath());
    			setSourceRoots(path);
    		} else {
    			ignore("source root not found: " + srcRoot);
    		}
    	}
    }

    public void setSourceRoots(Path roots) {
        if (this.sourceRoots == null) {
            this.sourceRoots = roots;
        } else {
            this.sourceRoots.append(roots);
        }
    }

    public Path createSourceRoots() {
        if (sourceRoots == null) {
            sourceRoots = new Path(project);
        }
        return sourceRoots.createPath();
    }        
	
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }        
    
    public void setInjars(Path injars) {
        if (this.injars == null) {
            this.injars = injars;
        } else {
            this.injars.append(injars);
        }
    }

    public Path createInjars() {
        if (injars == null) {
            injars = new Path(project);
        }
        return injars.createPath();
    }        
    
    public void setAspectpath(Path aspectpath) {
        if (this.aspectpath == null) {
            this.aspectpath = aspectpath;
        } else {
            this.aspectpath.append(aspectpath);
        }
    }

    public Path createSrc() {
        return createSrcdir();
    }

    public Path createSrcdir() {
        if (srcdir == null) {
            srcdir = new Path(project);
        }
        return srcdir.createPath();
    }

    public Path createAspectpath() {
        if (aspectpath == null) {
            aspectpath = new Path(project);
        }
        return aspectpath.createPath();
    }        

    public void setClasspathref(Reference classpathref) {
        createClasspath().setRefid(classpathref);
    }
    
    public void setBootclasspath(Path bootclasspath) {
        if (this.bootclasspath == null) {
            this.bootclasspath = bootclasspath;
        } else {
            this.bootclasspath.append(bootclasspath);
        }
    }
    public Path createBootclasspath() {
        if (bootclasspath == null) {
            bootclasspath = new Path(project);
        }
        return bootclasspath.createPath();
    }    
    
    public void setBootclasspathref(Reference bootclasspathref) {
        createBootclasspath().setRefid(bootclasspathref);
    }

    public void setArgfile(File argfile) { // ajc-only eajc-also docDone
        if (argfiles == null) {
            argfiles = new Vector();
        }
        argfiles.add(argfile);
    }

    public void setArgfiles(String argfiles) { // ajc-only eajc-also docDone
        StringTokenizer tok = new StringTokenizer(argfiles, ", ", false);
        if (tok.hasMoreTokens() && this.argfiles == null) {
            this.argfiles = new Vector();
        }
        while (tok.hasMoreTokens()) {
            this.argfiles.add(project.resolveFile(tok.nextToken().trim()));
        }
    }

    public void setX(String input) {  // ajc-only eajc-also docDone
        StringTokenizer tokens = new StringTokenizer(input, ",", false);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (1 < token.length()) {
                if (VALID_XOPTIONS.contains(token)) {
                	addFlag("-X" + token, true); 
                } else {
                    ignore("-X" + token);
                }
            }
        }
    }
    // ---------------------------- test state and interface thereto
    
    private IMessageHolder messageHolder;
        
    void setMessageHolder(IMessageHolder holder) {
        this.messageHolder = holder;
    }
    
    /** 
     * Setup custom message handling.
     * @param className the String fully-qualified-name of a class
     *          reachable from this object's class loader,
     *          implementing IMessageHolder, and 
     *          having a public no-argument constructor.
     */
    public void setMessageHolderClass(String className) {
    	try {
    		Class mclass = Class.forName(className);
    		IMessageHolder holder = (IMessageHolder) mclass.newInstance();
    		setMessageHolder(holder);
    	} catch (Throwable t) { // XXX better message
    		ignore("unable to load message holder class " + className
    			+ t.getClass().getName() + ": " + t.getMessage());
    	}
    }
    
    // ------------------------------ run
    /**
     * Compile using ajc per settings.
     * This prints the messages in verbose or terse form
     * unless an IMessageHolder was set using setMessageHolder.
     * @exception BuildException if the compilation has problems
     *             or if there were compiler errors and failonerror is true.
     */
    public void execute() throws BuildException {
    	IMessageHolder holder = messageHolder;
    	int numPreviousErrors;
    	if (null == holder) {
    		MessageHandler mhandler = new MessageHandler(true);
	    	final IMessageHandler delegate 
	    		= verbose ? MessagePrinter.VERBOSE: MessagePrinter.TERSE;
  			mhandler.setInterceptor(delegate);
  			holder = mhandler;		
    		numPreviousErrors = 0;
    	} else {
    		numPreviousErrors = holder.numMessages(IMessage.ERROR, true);
    	}
        
        try {
        	if (0 < ignored.size()) {
				for (Iterator iter = ignored.iterator(); iter.hasNext();) {
					log("ignored: " + iter.next(), project.MSG_INFO);					
				}
        	}
            Main main = new Main();
            main.setHolder(holder);
            addListArgs();
            String[] args = cmd.getArguments();
	        if (verbose) {
	        	log("ajc " + Arrays.asList(args), project.MSG_VERBOSE);
	        }
            main.runMain(args, false);
            
			if (failonerror) {
				int errs = holder.numMessages(IMessage.ERROR, true);
				errs -= numPreviousErrors;
				if (0 < errs) {
					// errors should already be printed by interceptor
					throw new BuildException(errs + " errors"); 
				}
			}
        } catch (BuildException e) {
            throw e;
        } catch (Throwable x) {
            throw new BuildException("Thrown: ", x);
        }
    }
    
    // ------------------------------ setup and reporting
    /** 
     * @return String[] of command-line arguments
     * @throws BuildException if tagFile or sourceRoots invalid
     */
	protected void addListArgs() throws BuildException {
		
        if (classpath != null) {
            addFlagged("-classpath", classpath.toString());
        }
        if (aspectpath != null) {
            addFlagged("-aspectpath", aspectpath.toString());
        }
        if (bootclasspath != null) {
            addFlagged("-bootclasspath", bootclasspath.toString());
        }
        if (injars != null) {
            addFlagged("-injars", injars.toString());
        }
        if (sourceRoots != null) {
            addFlagged("-sourceroots", sourceRoots.toString());
        }
        int numargfiles = 0;
        if (argfiles != null) {
            for (Iterator i = argfiles.iterator(); i.hasNext();) {
                String name = i.next()+"";
                File argfile = project.resolveFile(name);
                if (check(argfile, name, false, location)) {
		            addFlagged("-argfile", argfile.getAbsolutePath());
                    numargfiles++;    
                }
            }
        }
        int numSources = 0;
        if (srcdir != null) {
            // todo: ignore any srcdir if any argfiles and no explicit includes
            String[] dirs = srcdir.list();
            for (int i = 0; i < dirs.length; i++) {
                File dir = project.resolveFile(dirs[i]);
                check(dir, dirs[i], true, location);
                String[] files = getDirectoryScanner(dir).getIncludedFiles();
                for (int j = 0; j < files.length; j++) {
                    File file = new File(dir, files[j]);
                    if (isSrcFile(file)) {
                        cmd.createArgument().setFile(file);
                        numSources++;
                    }
                }
            }
        }
	}    
	    
    protected final boolean check(File file, String name,
                                  boolean isDir, Location loc) {
        loc = loc != null ? loc : location;
        if (file == null) {
            throw new BuildException(name + " is null!", loc);
        }
        if (!file.exists()) {
            throw new BuildException(file + " doesn't exist!", loc);
        }
        if (isDir ^ file.isDirectory()) {
            String e = file + " should" + (isDir ? "" : "n't")  +
                " be a directory!";
            throw new BuildException(e, loc);
        }
        return true;
    }
}
