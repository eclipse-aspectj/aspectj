/* *******************************************************************
 * Copyright (c) 2000-2001 Xerox Corporation. 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.aspectj.tools.ajc.Main.MessagePrinter;
import org.aspectj.util.FileUtil;

/**
 * Main taskdef class for the AspectJ 1.0 compiler, <code>ajc</code>.
 * See the README and examples/build.xml for more information.
 */
public class Ajc10 extends MatchingTask {

    private static final List<String> VALID_XOPTIONS;
    static {
        String[] xs = new String[] 
            {   "lint", "serializableAspects", "targetNearSource", "OcodeSize",
                "incrementalFile" };
        VALID_XOPTIONS = Collections.unmodifiableList(Arrays.asList(xs));
    }
    //protected boolean version;
    protected boolean source14;
    protected Set ignoredOptions;
    protected Commandline cmd;
    protected Commandline vmcmd;
    private int threads = -1;
    private File destdir;
    private File workingdir;
    private Path internalclasspath;
    private Path classpath;
    private Path bootclasspath;
    private Path extdirs;
    private Path srcdir;
    private List argfiles;
    private boolean fork;
    private boolean failonerror;
    private boolean verbose;
	private String encoding;
	private String source;

    public Ajc10() {
        reset();
    }

    // ------------------------- options 
    // find option types and whether ignored:
    //   sed -n '/void set/p' Ajc.java | sed 's/.*\/\/ //' | sort -u
    //   sed -n '/ignoredOptions/d;/ignored/p' Ajc.java 
    // each option may be "ignored" and is one+ of:
    //   ajc-old             used to be an ajc option
    //   ajc-only            only an option for ajc, not javac
    //   ajc-taskdef only    only an option for ajc taskdef, not ajc
    //   javac-also          also an option in javac
    //   eajc-also

    // ------------------------- options in order per ajc output

    public void setVerbose(boolean verbose) {  // javac-also eajc-also docDone
        setif(verbose, "-verbose");
        this.verbose = verbose;
    }

    public void setVersion(boolean version) {  // javac-also eajc-also docDone
        // let the compiler handle it
        if (version) {
            setif(true, "-version");
        }
        //this.version = version;
    }
    
    public void setNocomments(boolean nocomments) { // ajc-only not-eajc docDone
        if (nocomments) {
            ignore("-nocomments");
        }
        //setif(nocomments, "-nocomments");
    }

    public void setEmacssym(boolean input) {  // ajc-only  eajc-also docDone
        setif(input, "-emacssym"); 
    }

    public void setUsejavac(boolean input) {  // ajc-only not-eajc docDone
        if (input) {
            ignore("-usejavac");
        }
        //setif(input, "-usejavac"); 
    }

    public void setPreprocess(boolean preprocess) { // ajc-only not-eajc docDone
        if (preprocess) {
            ignore("-preprocess");
            //setif(preprocess, "-preprocess");
        }
    }

    public void setWorkingdir(String workingdir) { // ajc-only not-eajc ocDone
        ignore("-workingdir");
        //this.workingdir = project.resolveFile(workingdir);
    }

    public void setDestdir(String destdir) { // javac-also eajc-also  docDone
        this.destdir = project.resolveFile(destdir);
    }

    public void setOptimize(boolean optimize) { // javac-also ignored docDone
        setif(optimize, "-O");
    }
   

    public void setClasspath(Path classpath) {  // javac-also eajc-also docDone
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }
    public Path createClasspath() {  // javac-also docDone
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }        
    
    public void setClasspathref(Reference classpathref) {  // javac-also docDone
        createClasspath().setRefid(classpathref);
    }
    
    public void setBootclasspath(Path bootclasspath) {  // javac-also not-eajc docDone
        ignore("bootclasspath"); // XXX may jury-rig
//        if (this.bootclasspath == null) {
//            this.bootclasspath = bootclasspath;
//        } else {
//            this.bootclasspath.append(bootclasspath);
//        }
    }
    public Path createBootclasspath() {  // javac-also not-eajc docDone
        ignore("bootclasspath"); // XXX may jury-rig
        if (bootclasspath == null) {
            bootclasspath = new Path(project);
        }
        return bootclasspath.createPath();
    }    
    
    public void setBootclasspathref(Reference bootclasspathref) {  // javac-also not-eajc docDone
        ignore("bootclasspath"); // XXX may jury-rig
//        createBootclasspath().setRefid(bootclasspathref);
    }
    
    public void setExtdirs(Path extdirs) {  // javac-also not-eajc docDone
        ignore("-extdirs");
//        if (this.extdirs == null) {
//            this.extdirs = extdirs;
//        } else {
//            this.extdirs.append(extdirs);
//        }
    }

    public Path createExtdirs() {  // javac-also not-eajc docDone
        ignore("-extdirs");
        if (extdirs == null) {
            extdirs = new Path(project);
        }
        return extdirs.createPath();
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

    public void setEncoding(String encoding) { // ignored eajc-also docDone
        // XXX add support
        //ignore("encoding");
        this.encoding = encoding;
    }

    public void setSource(String input) {    // javac-also (Ant 1.4) eajc-also docDone
        source = input;
        //source14 = "1.4".equals(input); // XXX todo
    }

    public void setLenient(boolean input) {  // ajc-only not-eajc docDone
        ignore("-lenient");
        //setif(input, "-lenient"); 
    }

    public void setStrict(boolean input) {  // ajc-only not-eajc docDone
        ignore("-strict");
        //setif(input, "-strict"); 
    }

    public void setPorting(boolean input) {  // ajc-only not-eajc docDone
        ignore("-porting");
        //setif(input, "-porting"); 
    }

    public void setX(String input) {  // ajc-only eajc-also docDone
        StringTokenizer tokens = new StringTokenizer(input, ",", false);
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (1 < token.length()) {
                if (VALID_XOPTIONS.contains(token)) {
                    setif(true, "-X" + token); 
                } else {
                    ignore("-X" + token);
                }
            }
        }
    }

    // ------------------------- vestigial
    public void setThreads(int threads) { // ajc-old docDone ignored
        ignore("-threads");
        //this.threads = threads;
    }
    
    public void setDumpstack(boolean dumpstack) { // ajc-old
        ignore("-dumpstack");
        //setif(dumpstack, "-dumpstack");
    }

    // ------------------------- specific to taskdef

    public void setInternalclasspath(Path internalclasspath) { // ajc-taskdef only
        if (this.internalclasspath == null) {
            this.internalclasspath = internalclasspath;
        } else {
            this.internalclasspath.append(internalclasspath);
        }
    }
    public Path createInternalclasspath() { // ajc-taskdef only
        if (internalclasspath == null) {
            internalclasspath = new Path(project);
        }
        return internalclasspath.createPath();
    }        
    
    public void setInternalclasspathref(Reference internalclasspathref) { // ajc-taskdef only
        createInternalclasspath().setRefid(internalclasspathref);
    }

     public void setSrcdir(Path srcdir) { // javac-also eajc-also docDone
        if (this.srcdir == null) {
            this.srcdir = srcdir;
        } else {
            this.srcdir.append(srcdir);
        }
    }

    public Path createSrc() { // javac-also eajc-also docDone
        return createSrcdir();
    }

    public Path createSrcdir() { // javac-also eajc-also docDone
        if (srcdir == null) {
            srcdir = new Path(project);
        }
        return srcdir.createPath();
    }

    public void setFork(boolean fork) { // ajc-only not-eajc docDone
        ignore("fork");
        //this.fork = fork;
    }

    public void setFailonerror(boolean failonerror) {  // javac-also docDone
        this.failonerror = failonerror;
    }

    public void setMaxmemory(String max) { // ajc-taskdef only docDone
        ignore("-maxmemory");
        // we do not run under 1.1 anyway...
//        createJvmarg().setValue((Project.getJavaVersion().
//                                 startsWith("1.1") ?
//                                 "-mx" : "-Xmx") +max);
    }

    public void setJvmarg(String input) {  // ajc-taskdef only docDone
        ignore("jvmarg"); // XXX fork
        //vmcmd.createArgument().setValue(input);
    }

    public Commandline.Argument createJvmarg() { // ajc-taskdef only docDone
        ignore("jvmarg"); // XXX fork
        return vmcmd.createArgument();
    }

    // ------------------------- javac task compatibility
    public void setNosymbols(boolean nosymbols) { // todo remove?
        ignore("-nosymbols");
        //setif(nosymbols, "-nosymbols");
    }
    
    public void setDebug(boolean debug) { // javac-also eajc-also docDone
        setif(debug, "-g"); // todo: changed from -debug
    }
    
    public void setDeprecation(boolean deprecation) { // javac-also eajc-also docDone
        setif(deprecation, "-deprecation"); // XXX eajc: also "warn:deprecation"
    }

    // ------------------------- javac task compatibility - ignored
    public void setTarget(String target) {  // javac-also ignored docDone
        ignore("target"); // todo: ajc accepts but doesn't use - pass in?
    }
    public void setDepend(String depend) {  // javac-also ignored docDone
        ignore("depend");
    }
    public void setIncludeantruntime(boolean includeAntruntime) {  // javac-also ignored docDone
        ignore("includeantruntime");
    }
    public void setIncludejavaruntime(boolean includeJavaruntime ) {  // javac-also ignored docDone
        ignore("includeJavaruntime");
    }

    // ------------------------- other state methods
    
    protected final void ignore(String attribute) {
        ignoredOptions.add(attribute);
    }

    public void backdoorSetFile(File file) {
        if (null != file) {
            cmd.createArgument().setFile(file);
        }
    }
    
    /** reset variables to permit gc */
    public void reset() {
        //version = false;
        source14 = false;
        ignoredOptions = new HashSet();
        cmd = new Commandline();
        vmcmd = new Commandline();
        threads = -1;
        destdir = null;
        workingdir = null;
        internalclasspath = null;
        classpath = null;
        bootclasspath = null;
        extdirs = null;
        srcdir = null;
        argfiles = null;
        fork = false;
        failonerror = true;
        encoding = null;
        source = null;
    }

    protected final void setif(boolean b, String flag) {
        if (b) cmd.createArgument().setValue(flag);
    }

    // ------------------------- operational methods

    @Override
	public void execute() throws BuildException {
        if (srcdir == null && argfiles == null) {
            throw new BuildException("one of srcdir or argfiles must be set!",
                                     location);
        }
//        if (threads != -1) {
//            cmd.createArgument().setValue("-threads");
//            cmd.createArgument().setValue(threads+"");
//        }
//        if (workingdir != null) {
//            cmd.createArgument().setValue("-workingdir");
//            cmd.createArgument().setFile(workingdir);
//        }
        if (destdir != null) {
            cmd.createArgument().setValue("-d");
            cmd.createArgument().setFile(destdir);
        }
        if (classpath != null) {
            cmd.createArgument().setValue("-classpath");
            cmd.createArgument().setPath(classpath);
        }
        if (bootclasspath != null) {
            cmd.createArgument().setValue("-bootclasspath");
            cmd.createArgument().setPath(bootclasspath);
        }
        if (extdirs != null) {
            cmd.createArgument().setValue("-extdirs");
            cmd.createArgument().setPath(extdirs);
        }
        if (null != encoding) {
            cmd.createArgument().setValue("-encoding");
            cmd.createArgument().setValue(encoding);
        }
        if (null != source) {
            cmd.createArgument().setValue("-source");
            cmd.createArgument().setValue(source);
        }

        int numargfiles = 0;
        if (argfiles != null) {
			for (Object o : argfiles) {
				String name = o + "";
				File argfile = project.resolveFile(name);
				if (check(argfile, name, false, location)) {
					cmd.createArgument().setValue("-argfile");
					cmd.createArgument().setFile(argfile);
					numargfiles++;
				}
			}
        }
        int numfiles = 0;
        if (srcdir != null) {
            // todo: ignore any srcdir if any argfiles and no explicit includes
            String[] dirs = srcdir.list();
			for (String value : dirs) {
				File dir = project.resolveFile(value);
				check(dir, value, true, location);
				String[] files = getDirectoryScanner(dir).getIncludedFiles();
				for (String s : files) {
					File file = new File(dir, s);
					if (FileUtil.hasSourceSuffix(file)) {
						cmd.createArgument().setFile(file);
						numfiles++;
					}
				}
			}
        }
        if ((null != ignoredOptions) && (ignoredOptions.size() > 0)) {
            log("The following attributes were ignored " + ignoredOptions,
                Project.MSG_WARN);
            if (ignoredOptions.contains("-preprocess")) {
                throw new BuildException("preprocess no longer supported");
            }
        }
        log("Compiling " + numfiles + " source and " +
            + numargfiles + " arg files" 
            + (null == destdir ? "" : " to " + destdir.getPath()), Project.MSG_INFO);

         // here is the actual invocation
        //int result = (fork || (internalclasspath != null)) ? fork() : spoon();
        if (fork || (internalclasspath != null))  {
        	log("WARNING: fork not supported", Project.MSG_WARN);
        }
        int result = spoon();
        if (result != 0) {
            String msg = "Compilation error: " + result;
            if (failonerror) {
                reset();
                throw new BuildException(msg);
            } else {
                log(msg, Project.MSG_WARN);
            }
        }
        reset(); // see throw above
    }

// now leaving version to compiler - remove
//    protected void version(Path classpath) {
//        try {
//            Class main = findClass("org.aspectj.tools.ajc.Main",
//                                   classpath);
//            Method printVersion = main.getDeclaredMethod("printVersion",
//                                                         new Class[]{});
//            printVersion.setAccessible(true);
//            printVersion.invoke(main.newInstance(), new Object[]{});
//        } catch (Exception e) {}
//    }
//
//    protected Class findClass(String classname, Path classpathPath) {
//        String classpath = (classpathPath != null ?
//                            classpathPath+"" : "").trim();
//        if (classpath.length() == 0) {
//            try {
//                return Class.forName(classname);
//            } catch (ClassNotFoundException e){}
//        }
//        List urls = new ArrayList();
//        for (StringTokenizer t = new StringTokenizer(classpath,
//                                                     File.pathSeparator);
//             t.hasMoreTokens();) {
//            File f = new File(t.nextToken().trim());
//            try {
//                if (f.exists()) {
//                    URL url = f.toURL();
//                    if (url != null) urls.add(url);
//                }
//            } catch (MalformedURLException e) {}
//        }
//        if (urls.size() == 0) return null;
//        try {
//            return new URLClassLoader((URL[])urls.toArray
//                                      (new URL[urls.size()]),
//                                      null).loadClass(classname);
//        } catch (ClassNotFoundException e) {}
//        return null;
//    }

// unused now, but leave in
//    protected int fork() throws BuildException {
//        Java java = (Java)project.createTask("java");
//        java.setTaskName(getTaskName());
//        Path compileClasspath;
//        if (internalclasspath != null) {
//            compileClasspath = internalclasspath;
//            compileClasspath.append(Path.systemClasspath);
//        } else {
//            compileClasspath = Path.systemClasspath;
//        }
//        //if (version) version(compileClasspath);
//        java.setClasspath(compileClasspath);
//        java.setClassname(FALSE_MAIN);
//        String[] args;
//        args = cmd.getArguments();
//        for (int i = 0; i < args.length; i++) {
//            java.createArg().setValue(args[i]);
//        }
//        args = vmcmd.getArguments();
//        for (int i = 0; i < args.length; i++) {
//            java.createJvmarg().setValue(args[i]);
//        }
//        java.setFork(fork);
//        // java handles its own verbose logging when forking
//        return java.executeJava();
//    }

    /** utility to render String[] for logging */
    public static String render(String[] args) {
        if (null == args) return "";
        StringBuffer sb = new StringBuffer();
		for (String arg : args) {
			sb.append(arg);
			sb.append(" ");
		}
        return sb.toString();
    }

    protected int spoon() throws BuildException {
        //if (version) version(null);
        int result = -1;
        final IMessageHolder holder;
        {
	    	MessageHandler handler = new MessageHandler();
	    	if (!verbose) {
	  			handler.ignore(IMessage.INFO);
	  		}
	    	final IMessageHandler delegate 
	    		= verbose ? MessagePrinter.VERBOSE: MessagePrinter.TERSE;
			handler.setInterceptor(delegate);
			holder = handler;
        }
        try {
            String[] args = cmd.getCommandline();
            // XXX avoid rendering if not verbosely logging?
            log("Running  in-process using " 
                + Ajc10.render(cmd.getCommandline()), Project.MSG_VERBOSE);
        	
        	Main main = new Main();
        	main.run(args, holder);
        	int errs = holder.numMessages(IMessage.ERROR, true);
        	if (0 < errs) {
        		result = 1;
        	} else {
                result = 0;
            }
        } catch (Throwable t) {
            while (t instanceof AbortException) {
                // check for "just quit -- message printed already"
                if (((AbortException)t).isSilent()) { 
                    t = null;
                    break;
                }
                IMessage m = ((AbortException) t).getIMessage();
                if (null == m) {
                    break;
                } else {
                    Throwable tt = m.getThrown();
                    if (null != tt) {
                        t = tt;   
                    } else {
                        break;
                    }
                }
            }
            if (null != t) {
                // t.printStackTrace(); // let recipient print
                throw new BuildException("Compiler failure", t, location);
            }
        } finally {
        	// now printing messages as we go, above
//			IMessage.Kind level = (verbose ? IMessage.INFO : IMessage.WARNING);
//        	if (0 < holder.numMessages(level, true)) {
//        		final String prefix = "";
//        		final boolean printSummary = false;
//        		MessageUtil.print(System.err, 
//        			holder, 
//        			prefix, 
//        			MessageUtil.MESSAGE_ALL,
//        			(verbose ? MessageUtil.PICK_INFO_PLUS : MessageUtil.PICK_WARNING_PLUS),
//        			printSummary);
//        	}
        }
        return result;
    }

    protected final boolean check(File file, String name,
                                  boolean isDir, Location loc) {
        loc = loc != null ? loc : location;
        if (file == null) {
            throw new BuildException(name + " is null!", loc);
        }
        if (!file.exists()) {
            throw new BuildException(file + "doesn't exist!", loc);
        }
        if (isDir ^ file.isDirectory()) {
            String e = file + " should" + (isDir ? "" : "n't")  +
                " be a directory!";
            throw new BuildException(e, loc);
        }
        return true;
    }
}
