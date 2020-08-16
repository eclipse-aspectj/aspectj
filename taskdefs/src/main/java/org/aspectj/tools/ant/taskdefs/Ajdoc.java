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
 *     2006, Arno Schmidmeier, (reactivated the source and removed deprecated calls)
 * ******************************************************************/


package org.aspectj.tools.ant.taskdefs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Javadoc.AccessType;
import org.apache.tools.ant.taskdefs.Javadoc.Html;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.aspectj.tools.ajdoc.Main;

/**
 * A task to run the ajdoc tool.
 *
 * @author Jeff Palm
 */
public class Ajdoc extends MatchingTask {
    /** The name of ajdoc class we're using. */
    public final static String AJDOC_CLASSNAME = "org.aspectj.tools.ajdoc.Main";

    /** If true, ignore fork param and use FORCE_FORK_VALUE  */
    public final static boolean FORCE_FORK = false;

    /** value used as fork when FORCE_FORK is true  */
    public final static boolean FORCE_FORK_VALUE = true;

    protected Commandline cmd;
    protected Commandline vmcmd;
    private Path sourcepath;
    private File destdir;
    private Collection<String> sourcefiles;
    private Collection<String> packagenames;
    private File packageList;
    private Path bootclasspath;
    private Path extdirs;
    private Path classpath;
    private Path internalclasspath;
    private List<File> argfiles;
    private Path docletpath;
    private Collection<Link> links;
    private Collection<Group> groups;
    private Doclet doclet;
    private boolean failonerror;
    private boolean fork;
	private String source;
	private Html bottom;
    private Vector<FileSet> fileSets = new Vector<>();
    /** reset all to initial values - permit gc if Ajdoc is held */
    public Ajdoc() {
        reset();
    }

    protected void reset() {
        cmd = new Commandline();
        vmcmd = new Commandline();
        sourcepath  = null;
        destdir  = null ;
        sourcefiles  = null;
        packagenames  = null;
        packageList  = null;
        bootclasspath  = null;
        extdirs  = null;
        classpath  = null;
        internalclasspath  = null;
        argfiles  = null;
        docletpath  = null;
        links = new ArrayList<>();
        groups = new ArrayList<>();
        doclet  = null;
        failonerror  = false;
        fork = false;
        source = null;
        bottom = null;
    }

    protected final boolean setif(boolean b, String flag) {
        if (b) cmd.createArgument().setValue(flag);
        return b;
    }

    protected final void setfile(String flag, String file) {
        set(flag, getProject().resolveFile(file).getAbsolutePath());
    }

    protected final void set(String flag, String val) {
        setif(true, flag, val);
    }

    protected final boolean setif(boolean b, String flag, String val) {
        if (setif(b, flag)) cmd.createArgument().setValue(val);
        return b;
    }

    public void setSource(String input) {
        source = input;
    }

    public void setSourcepath(Path path) {
        if (sourcepath == null) {
            sourcepath = path;
        } else {
            sourcepath.append(path);
        }
    }

    public Path createSourcepath() {
        return sourcepath == null ?
            (sourcepath = new Path(getProject())) :
            sourcepath.createPath();
    }

    public void setSourcepathRef(Reference id) {
        createSourcepath().setRefid(id);
    }

    public void setSrcdir(Path path) { setSourcepath(path); }

    public Path createSrcdir() { return createSourcepath(); }

    public void setSrcdirRef(Reference id) { setSourcepathRef(id); }

    public void setDestdir(String destdir) {
        this.destdir = getProject().resolveFile(destdir);
    }

    public void setSourcefiles(String list) {
        (sourcefiles == null ?
         sourcefiles = new ArrayList() :
         sourcefiles).addAll(strings(list));
    }

    public void addFileset(FileSet fs) {
        fileSets.addElement(fs);
    }

    private void addFileSets() {
    	if(sourcefiles == null)
    		sourcefiles = new ArrayList<>();

        Enumeration<FileSet> e = fileSets.elements();
        while (e.hasMoreElements()) {
            FileSet fs = (FileSet) e.nextElement();
            if (!fs.hasPatterns() && !fs.hasSelectors()) {
                fs = (FileSet) fs.clone();
                fs.createInclude().setName("**/*.java");
                fs.createInclude().setName("**/*.aj");
            }
            File baseDir = fs.getDir(getProject());
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();
			for (String file : files) {
				sourcefiles.add((new File(baseDir, file)).getAbsolutePath());
			}
        }
    }

    public void setPackagenames(String list) {
        (packagenames == null ?
         packagenames = new ArrayList<>() :
         packagenames).addAll(strings(list, true));
    }

    public void setAccess(AccessType at) {
        cmd.createArgument().setValue("-" + at.getValue());
    }

    public void setPackageList(String packageList) {
        this.packageList = getProject().resolveFile(packageList);
    }

    public void setClasspath(Path path) {
        if (classpath == null) {
            classpath = path;
        } else {
            classpath.append(path);
        }
    }

    public Path createClasspath() {
        return (classpath == null ?
                classpath = new Path(getProject()) :
                classpath).createPath();
    }

    public void setClasspathref(Reference id) {
        createClasspath().setRefid(id);
    }

    public void setBootclasspath(Path path) { // todo: unsupported
        if (bootclasspath == null) {
            bootclasspath = path;
        } else {
         bootclasspath.append(path);
        }
    }

    public Path createBootclasspath() { // todo: unsupported
        return (bootclasspath == null ?
                bootclasspath = new Path(getProject()) :
                bootclasspath).createPath();
    }

    public void setBootclasspathref(Reference bootclasspathref) { // todo: unsupported
        createBootclasspath().setRefid(bootclasspathref);
    }

    public void setInternalclasspath(Path internalclasspath) {
        if (this.internalclasspath == null) {
            this.internalclasspath = internalclasspath;
        } else {
            this.internalclasspath.append(internalclasspath);
        }
    }

    public Path createInternalclasspath() {
        if (internalclasspath == null) {
            internalclasspath = new Path(getProject());
        }
        return internalclasspath.createPath();
    }


    public void setInternalclasspathref(Reference internalclasspathref) {
        createInternalclasspath().setRefid(internalclasspathref);
    }

    public void setExtdirs(Path path) {
        if (extdirs == null) {
            extdirs = path;
        } else {
            extdirs.append(path);
        }
    }

    public List<File> createArgfiles() {
        return (argfiles == null ?
                argfiles = new ArrayList<>() :
                argfiles);
    }

    public void setArgfile(String argfile) {
        createArgfiles().add(getProject().resolveFile(argfile));
    }

    public void setArgfiles(String argfiles) {
        createArgfiles().addAll(files(argfiles));
    }

    public void setOverview(String overview) {
        setfile("-overview", overview);
    }

    public void setPublic(boolean b) {
        setif(b, "-public");
    }

    public void setPackage(boolean b) {
        setif(b, "-package");
    }

    public void setProtected(boolean b) {
        setif(b, "-protected");
    }

    public void setPrivate(boolean b) {
        setif(b, "-private");
    }

    public void setOld(boolean old) {
        setif(old, "-old");
    }

    public void setAuthor(boolean author) {
        setif(author, "-author");
    }

    public void setSplitindex(boolean splitindex) {
        setif(splitindex, "-splitindex");
    }

    public void setWindowtitle(String windowtitle) {
        set("-windowtitle", windowtitle);
    }

    public void setDoctitle(String doctitle) {
        set("-doctitle", doctitle);
    }

    public void setHeader(String header) {
        set("-header", header);
    }

    public void setFooter(String footer) {
        set("-footer", footer);
    }

    public void setBottom(String bottom) {
        Html html = new Html();
        html.addText(bottom);
        addBottom(html);
    }

    public void addBottom(Html text) {
        bottom = text;
    }

    public void setVerbose(boolean b) {
        setif(b, "-verbose");
    }

    public void setVersion(boolean b) {
        setif(b, "-version");
    }

    public void setUse(boolean b) {
        setif(b, "-use");
    }

    public void setStandard(boolean b) {
        setif(b, "-standard");
    }

    public class Link {
        protected String href;
        protected boolean offline;
        protected String packagelistLoc;
        public Link() {}
        public void setHref(String href) {
            this.href = href;
        }
        public void setOffline(boolean offline) {
            this.offline = offline;
        }
        public void setPackagelistLoc(String packagelistLoc) {
            this.packagelistLoc = packagelistLoc;
        }
    }

    public void setLink(String href) {
        createLink().setHref(href);
    }

    public Link createLink() {
        Link link = new Link();
        links.add(link);
        return link;
    }

    public void setLinkoffline(String linkoffline) {
        Link link = createLink();
        int ispace = linkoffline.indexOf(" ");
        if (ispace == -1) {
            throw new BuildException("linkoffline usage: <url> <url2>!", getLocation());
        }
        link.setHref(linkoffline.substring(0, ispace).trim());
        link.setPackagelistLoc(linkoffline.substring(ispace+1).trim());
    }

    public class Group {
        private String title;
        private List<String> packages;
        public Group() {}
        public void setTitle(String title) {
            this.title = title;
        }
        public final void setPackages(String packages) {
            setPackagenames(packages);
        }
        public void setPackagenames(String packages) {
            this.packages = strings(packages);
        }
    }

    public void setGroup(String str) {
        for (StringTokenizer t = new StringTokenizer(str, ",", false);
             t.hasMoreTokens();) {
            String s = t.nextToken().trim();
            int ispace = s.indexOf(' ');
            if (ispace != -1) {
                Group group = createGroup();
                group.setTitle(s.substring(0, ispace));
                group.setPackagenames(s.substring(ispace+1));
            } else {
                throw new BuildException
                    ("group usage: group=[<title> <pkglist>]*", getLocation());
            }
        }
    }

    public Group createGroup() {
        Group group = new Group();
        groups.add(group);
        return group;
    }

    public void setNodeprecated(boolean nodeprecated) {
        setif(nodeprecated, "-nodeprecated");
    }

    public void setNodeprecatedlist(boolean nodeprecatedlist) {
        setif(nodeprecatedlist, "-nodeprecatedlist");
    }

    public void setNotree(boolean notree) {
        setif(notree, "-notree");
    }

    public void setNoindex(boolean noindex) {
        setif(noindex, "-noindex");
    }

    public void setNohelp(boolean nohelp) {
        setif(nohelp, "-nohelp");
    }

    public void setNonavbar(boolean nonavbar) {
        setif(nonavbar, "-nonavbar");
    }

    public void setSerialwarn(boolean serialwarn) {
        setif(serialwarn, "-serialwarn");
    }

    public void setHelpfile(String helpfile) {
        setfile("-helpfile", helpfile);
    }

    public void setStylesheetfile(String stylesheetfile) {
        setfile("-stylesheetfile", stylesheetfile);
    }

    public void setCharset(String charset) { set("-charset", charset); }

    public void setDocencoding(String docencoding) {
        set("-docencoding", docencoding);
    }

    public void setDoclet(String doclet) {
        createDoclet().setName(doclet);
    }

    public static class Param {
        protected String name;
        protected String value;
        public Param() {}
        public void setName(String name) { this.name  = name; }
        public void setValue(String value) { this.value = value; }
    }

    public class Doclet {
        protected String name;
        protected Path path;
        protected List<Param> params = new ArrayList<>();
        public Doclet() {}
        public void setName(String name) {
            this.name = name;
        }
        public void setPath(Path path) {
            if (this.path == null) {
                this.path = path;
            } else {
                this.path.append(path);
            }
        }
        public Path createPath() {
            return (path == null ?
                    path = new Path(getProject()) :
                    path).createPath();
        }
        public Param createParam() {
            Param param = new Param();
            params.add(param);
            return param;
        }
    }

    public Doclet createDoclet() {
        if (doclet != null) {
            throw new BuildException("Only one doclet is allowed!");
        }
        return doclet = new Doclet();
    }

    public void setDocletpath(Path path) {
        (docletpath == null ?
         docletpath = path :
         docletpath).append(path);
    }

    public Path createDocletpath() {
        return docletpath == null ?
            (docletpath = new Path(getProject())) :
            docletpath.createPath();
    }

    public void setDocletpathRef(Reference id) {
        createDocletpath().setRefid(id);
    }

    public void setAdditionalparam(String additionalparam) {
        cmd.createArgument().setLine(additionalparam);
    }

    public void setFailonerror(boolean failonerror) {
        this.failonerror = failonerror;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }

    public Commandline.Argument createJvmarg() {
        return vmcmd.createArgument();
    }

    public void setMaxmemory(String max) {
        createJvmarg().setValue((Project.getJavaVersion().
                                 startsWith("1.1") ?
                                 "-mx" : "-Xmx") +max);
    }

    public void execute() throws BuildException {
        if (sourcepath == null && argfiles == null && fileSets.size() == 0) {
            throw new BuildException("one of sourcepath or argfiles must be set!",
            		getLocation());
        }
        if (sourcepath != null) {
            cmd.createArgument().setValue("-sourcepath");
            cmd.createArgument().setPath(sourcepath);
        }
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
        if (source != null) {
            cmd.createArgument().setValue("-source");
            cmd.createArgument().setValue(source);
        }
        if (bottom != null) {
        	cmd.createArgument().setValue("-bottom");
        	cmd.createArgument().setValue(getProject().replaceProperties(bottom.getText()));
        }

        for (Link link: links) {
            if (link.href == null) {
                throw new BuildException("Link href cannot be null!", getLocation());
            }
            if (link.packagelistLoc != null) {
                cmd.createArgument().setValue("-linkoffline");
                cmd.createArgument().setValue(link.href);
                cmd.createArgument().setValue(link.packagelistLoc);
            } else {
                cmd.createArgument().setValue("-link");
                cmd.createArgument().setValue(link.href);
            }
        }
        if (doclet != null) {
            if (doclet.name == null) {
                throw new BuildException("Doclet name cannot be null!", getLocation());
            }
            cmd.createArgument().setValue("-doclet");
            cmd.createArgument().setValue(doclet.name);
            if (doclet.path != null) {
                cmd.createArgument().setValue("-docletpath");
                cmd.createArgument().setPath(doclet.path);
            }
			for (Param param : doclet.params) {
				if (param.name == null) {
					throw new BuildException("Doclet params cannot be null!",
							getLocation());
				}
				cmd.createArgument().setValue(param.name);
				if (param.value == null) {
					cmd.createArgument().setValue(param.value);
				}
			}
        }
        Map<String,List<String>> groupMap = new HashMap<>();
        for (Group group: groups) {
            if (group.title == null) {
                throw new BuildException("Group names cannot be null!",
                                         getLocation());
            }
            if (group.packages == null) {
                throw new BuildException("Group packages cannot be null!",
                                         getLocation());
            }
            List<String> packages = groupMap.get(group.title);
            if (packages == null) {
                packages = new ArrayList<>();
            }
            packages.addAll(group.packages);
            groupMap.put(group.title, packages);
        }
        for (String title: groupMap.keySet()) {
            List<String> packages = groupMap.get(title);
            String pkgstr = "";
            for (Iterator<String> j = packages.iterator(); j.hasNext();) {
                pkgstr += j.next();
                if (j.hasNext()) pkgstr += ",";
            }
            cmd.createArgument().setValue("-group");
            cmd.createArgument().setValue(title);
            cmd.createArgument().setValue(pkgstr);
        }
        if (argfiles != null) {
			for (File file : argfiles) {
				String name = file + "";
				File argfile = getProject().resolveFile(name);
				if (check(argfile, name, false, getLocation())) {
					cmd.createArgument().setValue("-argfile");
					cmd.createArgument().setFile(argfile);
				}
			}
        }
        if (packageList != null) {
            cmd.createArgument().setValue("@" + packageList);
        }
        if (null != packagenames) {
			for (String packagename : packagenames) {
				cmd.createArgument().setValue(packagename);
			}
        }
        // support for include parameter as a MatchingTask
        int numfiles = 0;
        if (sourcepath != null) {
            String[] dirs = sourcepath.list();
			for (String value : dirs) {
				File dir = getProject().resolveFile(value);
				check(dir, value, true, getLocation());
				String[] files = getDirectoryScanner(dir).getIncludedFiles();
				for (String s : files) {
					File file = new File(dir, s);
					if (file.getName().endsWith(".java")
							|| file.getName().endsWith(".aj")) {
						cmd.createArgument().setFile(file);
						numfiles++;
					}
				}
			}
        }

    	addFileSets();
        if (sourcefiles != null) {
			for (String sourcefile : sourcefiles) {
				// let ajdoc resolve sourcefiles relative to sourcepath,
				cmd.createArgument().setValue(sourcefile);
			}
        }
        // XXX PR682 weak way to report errors - need to refactor
        int result = compile();
        if (failonerror && (0 != result)) {
            throw new BuildException("Ajdoc failed with code " + result);
        }
        reset();
    }

    protected int compile() throws BuildException { // todo: doc that fork is ignored
        try {
            String[] args = cmd.getArguments();
            if (fork) {
                log("Warning: fork is ignored ", Project.MSG_WARN);
            }
            Main.main(args);
            if (Main.hasAborted())
            	return 1;
            else
            	return 0;
        } catch (Throwable t) {
            throw new BuildException(t);
        }
    }

    protected interface Mapper<T> {
        T map(String str);
    }

    protected final <T> List<T> list(String str, Mapper<T> mapper) {
        if (str == null) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (StringTokenizer t = new StringTokenizer(str, ",", false);
             t.hasMoreTokens();) {
            list.add(mapper.map(t.nextToken().trim()));
        }
        return list;
    }

    protected final List<File> files(String str) {
        return list(str, new Mapper<File>() {
                public File map(String s) {
                    return getProject().resolveFile(s);
                }
            });
    }

    protected final List<String> strings(String str) {
        return strings(str, false);
    }

    protected final List<String> strings(String str, final boolean filterSlashes) {
        return list(str, new Mapper<String>() {
                public String map(String s) {
                    return filterSlashes ? filterSlashes(s) : s;
                }
            });
    }

    protected final String filterSlashes(String str) {
        if (str == null) return null;
        return str.
            replace('/', '.').
            replace('\\', '.').
            replace(File.separatorChar, '.');
    }

    protected final boolean check(File file, String name,
                                  boolean isDir, Location loc) {
        loc = loc != null ? loc : getLocation();
        if (file == null) {
            throw new BuildException(name + " is null!", loc);
        }
        if (!file.exists()) {
            throw new BuildException(file + "doesn't exist!", loc);
        }
        if (isDir ^ file.isDirectory()) {
            throw new BuildException(file + " should" +
                                     (!isDir ? "n't" : "") +
                                     " be a directory!", loc);
        }
        return true;
    }
}
