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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ZipFileSet;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.aspectj.tools.ajc.Main.MessagePrinter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * This runs the AspectJ 1.1 compiler, 
 * supporting all the command-line options.
 * It can also complete the output in
 * the destination directory or output jar
 * by copying non-.class files from all input jars
 * or copying resources from source root directories.
 * When copying anything to the output jar, 
 * this will pass the AspectJ
 * compiler a path to a different temporary output jar file,
 * the contents of which will be copied along with any
 * resources to the actual output jar.
 * @since AspectJ 1.1, Ant 1.5
 */
public class AjcTask extends MatchingTask {
            
    private static final File DEFAULT_DESTDIR = new File(".");
    
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
        
    }
	// ---------------------------- state and Ant interface thereto
    private boolean verbose;
    private boolean failonerror;
	
	// ------- single entries dumped into cmd
    protected Commandline cmd;
	
	// ------- lists resolved in addListArgs() at execute() time
    private Path srcdir;
    private Path injars;
    private Path classpath;
    private Path aspectpath;
    private Path argfiles;
    private List ignored;
    private Path sourceRoots;

    private IMessageHolder messageHolder;

    // -------- resource-copying
    /** true if copying injar non-.class files to the output jar */
    private boolean copyInjars;
    
    /** non-null if copying all source root files but the filtered ones */
    private String sourceRootCopyFilter;
    
    /** directory sink for classes */
    private File destDir;
    
    /** zip file sink for classes */
    private File outjar;

    /** 
     * When possibly copying resources to the output jar,
     * pass ajc a fake output jar to copy from,
     * so we don't change the modification time of the output jar
     * when copying injars into the actual outjar.
     */
    private File tmpOutjar;

    private boolean executing;

    // also note MatchingTask grabs source files...
    
    public AjcTask() {
    	reset();
    }

	/** to use this same Task more than once */
    public void reset() {
        // need declare for "all fields initialized in ..."
        verbose = false;
        failonerror = false;
    	cmd = new Commandline();
    	srcdir = null;
    	injars = null;
    	classpath = null;
    	aspectpath = null;
    	argfiles = null;
    	ignored = new ArrayList();
		sourceRoots = null;
        copyInjars = false;
        sourceRootCopyFilter = null;
        destDir = DEFAULT_DESTDIR;
        outjar = null;
        tmpOutjar = null;
        executing = false;
    }

    protected void ignore(String ignored) {
        this.ignored.add(ignored + " at " + getLocation());
    }
    
    //---------------------- option values

    protected void addFlag(String flag, boolean doAdd) {
        if (doAdd) {
        	cmd.addArguments(new String[] {flag});
        }
    }
    
    protected void addFlagged(String flag, String argument) {
        cmd.addArguments(new String[] {flag, argument});
    }

    // used by entries with internal commas
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
    
    public void setIncremental(boolean incremental) {  
        addFlag("-incremental", incremental);
    }

    public void setHelp(boolean help) {  
        addFlag("-help", help);
    }

    public void setVersion(boolean version) {  
    	addFlag("-version", version);
    }

    public void setXNoweave(boolean noweave) {  
        addFlag("-Xnoweave", noweave);
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
	 * -Xlintfile {lint.properties} - enable or disable specific forms 
	 * of -Xlint messages based on a lint properties file
	 *  (default is 
	 * <code>org/aspectj/weaver/XLintDefault.properties</code>)
	 * @param xlintFile the File with lint properties
	 */
    public void setXlintfile(File xlintFile) { 
        addFlagged("-Xlintfile", xlintFile.getAbsolutePath());
    }

    public void setPreserveAllLocals(boolean preserveAllLocals) {  
        addFlag("-preserveAllLocals", preserveAllLocals);
    }

    public void setNoImportError(boolean noImportError) {  
        addFlag("-noImportError", noImportError);
    }

    public void setEncoding(String encoding) {   
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
        if (DEFAULT_DESTDIR != destDir) {
            String e = "specifying both output jar ("
                + file 
                + ") and destination dir ("
                + destDir
                + ")";
            throw new BuildException(e);
        }
        outjar = file;
    }

    public void setDestdir(File dir) {
        if (null != outjar) {
            String e = "specifying both output jar ("
                + outjar 
                + ") and destination dir ("
                + dir
                + ")";
            throw new BuildException(e);
        }
        addFlagged("-d", dir.getAbsolutePath());
        destDir = dir;        
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
    /**
     * Flag to copy all non-.class contents of injars
     * to outjar after compile completes.
     * Requires both injars and outjar.
     * @param doCopy
     */
    public void setCopyInjars(boolean doCopy){
        this.copyInjars = doCopy;
    }
    /**
     * Option to copy all files from
     * all source root directories
     * except those specified here.
     * If this is specified and sourceroots are specified,
     * then this will copy all files except 
     * those specified in the filter pattern.
     * Requires sourceroots.
     * 
     * @param filter a String acceptable as an excludes
     *        filter for an Ant Zip fileset.
     */
    public void setSourceRootCopyFilter(String filter){
        this.sourceRootCopyFilter = filter;
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

    /** direct API for testing */
    void setMessageHolder(IMessageHolder holder) {
        this.messageHolder = holder;
    }
    
    /** 
     * Setup custom message handling.
     * @param className the String fully-qualified-name of a class
     *          reachable from this object's class loader,
     *          implementing IMessageHolder, and 
     *          having a public no-argument constructor.
     * @throws BuildException if unable to create instance of className
     */
    public void setMessageHolderClass(String className) {
        try {
            Class mclass = Class.forName(className);
            IMessageHolder holder = (IMessageHolder) mclass.newInstance();
            setMessageHolder(holder);
        } catch (Throwable t) {
            String m = "unable to instantiate message holder: " + className;
            throw new BuildException(m, t);
        }
    }

    //---------------------- Path lists

    /**
     * Add path to source path and return result.
     * @param source the Path to add to - may be null
     * @param toAdd the Path to add - may not be null
     * @return the Path that results
     */
    protected Path incPath(Path source, Path toAdd) {
        if (null == source) {
            return toAdd;        
        } else {
            source.append(toAdd);
            return source;
        }
    }

    public void setSourcerootsref(Reference ref) {
        createSourceRoots().setRefid(ref);
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
	
    public void setInjarsref(Reference ref) {
        createInjars().setRefid(ref);
    }
    
    public void setInjars(Path path) {
        injars = incPath(injars, path);
    }

    public Path createInjars() {
        if (injars == null) {
            injars = new Path(project);
        }
        return injars.createPath();
    }        
    
    public void setClasspathref(Reference classpathref) {
        createClasspath().setRefid(classpathref);
    }
        
    public void setClasspath(Path path) {
        classpath = incPath(classpath, path);
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }        
    
    public void setAspectpathref(Reference ref) {
        createAspectpath().setRefid(ref);
    }

    public void setAspectpath(Path path) {
        aspectpath = incPath(aspectpath, path);
    }

    public Path createAspectpath() {
        if (aspectpath == null) {
            aspectpath = new Path(project);
        }
        return aspectpath.createPath();
    }        

    public void setSrcDir(Path path) {
        srcdir = incPath(srcdir, path);
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

    public void setArgfilesref(Reference ref) {
        createArgfiles().setRefid(ref);
    }
    
    public void setArgfiles(Path path) { // ajc-only eajc-also docDone
        argfiles = incPath(argfiles, path);
    }

    public Path createArgfiles() {
        if (argfiles == null) {
            argfiles = new Path(project);
        }
        return argfiles.createPath();
    }
            
    // ------------------------------ run
    /**
     * Compile using ajc per settings.
     * This prints the messages in verbose or terse form
     * unless an IMessageHolder was set using setMessageHolder.
     * This also renders Compiler exceptions with our header to System.err
     * an rethrows a BuildException to be ignored.
     * @exception BuildException if the compilation has problems
     *             or if there were compiler errors and failonerror is true.
     */
    public void execute() throws BuildException {
        if (executing) {
            throw new IllegalStateException("already executing");
        } else {
            executing = true;
        }
    	IMessageHolder holder = messageHolder;
    	int numPreviousErrors;
    	if (null == holder) {
    		MessageHandler mhandler = new MessageHandler(true);
	    	final IMessageHandler delegate 
	    		= verbose ? MessagePrinter.VERBOSE: MessagePrinter.TERSE;
  			mhandler.setInterceptor(delegate);
  			if (!verbose) {
  				mhandler.ignore(IMessage.INFO);
  			}
  			holder = mhandler;
    		numPreviousErrors = 0;
    	} else {
    		numPreviousErrors = holder.numMessages(IMessage.ERROR, true);
    	}
        try {
        	if (0 < ignored.size()) {
				for (Iterator iter = ignored.iterator(); iter.hasNext();) {
					log("ignored: " + iter.next(), Project.MSG_INFO);					
				}
        	}
            Main main = new Main();
            main.setHolder(holder);
            main.setCompletionRunner(new Runnable() {
                public void run() {
                    doCompletionTasks();
                }
            });
            if (null != outjar) {
                if (copyInjars || (null != sourceRootCopyFilter)) {
                    String path = outjar.getAbsolutePath();
                    int len = FileUtil.zipSuffixLength(path);
                    if (len < 1) {
                        log("not copying injars - weird outjar: " + path);
                    } else {
                        path = path.substring(0, path.length()-len) + ".tmp.jar";
                        tmpOutjar = new File(path);
                    }
                }
                if (null == tmpOutjar) {                
                    addFlagged("-outjar", outjar.getAbsolutePath());        
                } else {
                    addFlagged("-outjar", tmpOutjar.getAbsolutePath());        
                }
            }
            
            addListArgs();
            String[] args = cmd.getArguments();
	        if (verbose) {
	        	log("ajc " + Arrays.asList(args), Project.MSG_VERBOSE);
	        }
            main.runMain(args, false);

			if (failonerror) {
				int errs = holder.numMessages(IMessage.ERROR, false);
				errs -= numPreviousErrors;
				if (0 < errs) {
					// errors should already be printed by interceptor
					throw new BuildException(errs + " errors"); 
				}
			} 
            // Throw BuildException if there are any fail or abort
            // messages.
            // The BuildException message text has a list of class names
            // for the exceptions found in the messages, or the
            // number of fail/abort messages found if there were
            // no exceptions for any of the fail/abort messages.
            // The interceptor message handler should have already
            // printed the messages, including any stack traces.
            {
                IMessage[] fails = holder.getMessages(IMessage.FAIL, true);
                if (!LangUtil.isEmpty(fails)) {
                    StringBuffer sb = new StringBuffer();
                    String prefix = "fail due to ";
                    for (int i = 0; i < fails.length; i++) {
                        Throwable t = fails[i].getThrown();
                        if (null != t) {
                            sb.append(prefix);
                            sb.append(LangUtil.unqualifiedClassName(t.getClass()));
                            prefix = ", ";
                        }
                    }
                    if (0 < sb.length()) {
                        sb.append(" rendered in messages above.");
                    } else {
                        sb.append(fails.length 
                                  + " fails/aborts (no exceptions)");
                    }
                    throw new BuildException(sb.toString());
			    }
            }
        } catch (BuildException e) {
            throw e;
        } catch (Throwable x) {
        	System.err.println(Main.renderExceptionForUser(x));        	
            throw new BuildException("IGNORE -- See " 
            	+ LangUtil.unqualifiedClassName(x) 
            	+ " rendered to System.err");
        } finally {
            executing = false;
            if (null != tmpOutjar) {
                tmpOutjar.delete();
            }
        }        
    }
    
    // ------------------------------ setup and reporting
    /** 
     */
	protected void addListArgs() throws BuildException {
		
        if (classpath != null) {
            addFlagged("-classpath", classpath.toString());
        }
        if (aspectpath != null) {
            addFlagged("-aspectpath", aspectpath.toString());
        }
        if (injars != null) {
            addFlagged("-injars", injars.toString());
        }
        if (sourceRoots != null) {
            addFlagged("-sourceroots", sourceRoots.toString());
        }
        if (argfiles != null) {
            String[] files = argfiles.list();
            for (int i = 0; i < files.length; i++) {
                File argfile = project.resolveFile(files[i]);
                if (check(argfile, files[i], false, location)) {
		            addFlagged("-argfile", argfile.getAbsolutePath());
                }
            }
        }
        if (srcdir != null) {
            // todo: ignore any srcdir if any argfiles and no explicit includes
            String[] dirs = srcdir.list();
            for (int i = 0; i < dirs.length; i++) {
                File dir = project.resolveFile(dirs[i]);
                check(dir, dirs[i], true, location);
                String[] files = getDirectoryScanner(dir).getIncludedFiles();
                for (int j = 0; j < files.length; j++) {
                    File file = new File(dir, files[j]);
                    if (FileUtil.hasSourceSuffix(file)) {
                        cmd.createArgument().setFile(file);
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
    
    /** 
     * Called when compile or incremental compile is completing,
     * this completes the output jar or directory
     * by copying resources if requested.
     * Note: this is a callback run synchronously by the compiler.
     * That means exceptions thrown here are caught by Main.run(..)
     * and passed to the message handler.
     */
    protected void doCompletionTasks() {
        if (!executing) {
            throw new IllegalStateException("should be executing");
        }
        if (null != outjar) {
            completeOutjar();
        } else {
            completeDestdir();
        }
    }
    
    /** 
     * Complete the destination directory
     * by copying resources from the source root directories
     * (if the filter is specified)
     * and non-.class files from the input jars 
     * (if XCopyInjars is enabled).
     */
    private void completeDestdir() {
        if (!copyInjars && (null == sourceRootCopyFilter)) {
            return;
        } else if (!destDir.canWrite()) {
            String s = "unable to copy resources to destDir: " + destDir;
            throw new BuildException(s);
        }
        final Project project = getProject();
        if (copyInjars) {
            String taskName = getTaskName() + " - unzip";
            String[] paths = injars.list();
            if (!LangUtil.isEmpty(paths)) {
                PatternSet patternSet = new PatternSet();
                patternSet.setProject(project);        
                patternSet.setIncludes("**/*");
                patternSet.setExcludes("**/*.class");  
                for (int i = 0; i < paths.length; i++) {
                    Expand unzip = new Expand();
                    unzip.setProject(project);
                    unzip.setTaskName(taskName);
                    unzip.setDest(destDir);
                    unzip.setSrc(new File(paths[i]));
                    unzip.addPatternset(patternSet);
                    unzip.execute();
                }
            }
        }
        if (null != sourceRootCopyFilter) {
            String[] paths = sourceRoots.list();
            if (!LangUtil.isEmpty(paths)) {
                Copy copy = new Copy();
                copy.setProject(project);
                copy.setTodir(destDir);
                for (int i = 0; i < paths.length; i++) {
                    FileSet fileSet = new FileSet();
                    fileSet.setDir(new File(paths[i]));
                    fileSet.setIncludes("**/*");
                    fileSet.setExcludes(sourceRootCopyFilter);  
                    copy.addFileset(fileSet);
                }
                copy.execute();
            }
        }        
    }
    
    /** 
     * Complete the output jar
     * by copying resources from the source root directories
     * if the filter is specified.
     * and non-.class files from the input jars if enabled.
     */
    private void completeOutjar() {
        if (((null == tmpOutjar) || !tmpOutjar.canRead()) 
            || (!copyInjars && (null == sourceRootCopyFilter))) {
            return;
        }
        Zip zip = new Zip();
        Project project = getProject();
        zip.setProject(project);        
        zip.setTaskName(getTaskName() + " - zip");
        zip.setDestFile(outjar);
        ZipFileSet zipfileset = new ZipFileSet();
        zipfileset.setProject(project);        
        zipfileset.setSrc(tmpOutjar);
        zipfileset.setIncludes("**/*.class");
        zip.addZipfileset(zipfileset);
        if (copyInjars) {
            String[] paths = injars.list();
            if (!LangUtil.isEmpty(paths)) {
                for (int i = 0; i < paths.length; i++) {
                    File jarFile = new File(paths[i]);
                    zipfileset = new ZipFileSet();
                    zipfileset.setProject(project);
                    zipfileset.setSrc(jarFile);
                    zipfileset.setIncludes("**/*");
                    zipfileset.setExcludes("**/*.class");  
                    zip.addZipfileset(zipfileset);
                }
            }
        }
        if (null != sourceRootCopyFilter) {
            String[] paths = sourceRoots.list();
            if (!LangUtil.isEmpty(paths)) {
                for (int i = 0; i < paths.length; i++) {
                    File srcRoot = new File(paths[i]);
                    FileSet fileset = new FileSet();
                    fileset.setProject(project);
                    fileset.setDir(srcRoot);
                    fileset.setIncludes("**/*");
                    fileset.setExcludes(sourceRootCopyFilter);  
                    zip.addFileset(fileset);
                }
            }
        }        
        zip.execute();
    }
}
