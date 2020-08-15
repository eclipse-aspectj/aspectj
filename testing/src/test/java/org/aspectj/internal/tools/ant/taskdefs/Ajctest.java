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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
//import java.util.Collection;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.StyleContext;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;
//import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.StreamPumper;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.aspectj.util.LangUtil;

@SuppressWarnings("deprecation")
public class Ajctest extends Task implements PropertyChangeListener {
    private static Ajctest CURRENT_AJCTEST;

    // todo shutdown hook assumes one task per VM
    public Ajctest() {
        super();
        CURRENT_AJCTEST = this;
    }

    private static boolean firstTime = true;

    public final PropertyChangeSupport bean = new PropertyChangeSupport(this);

    {
        bean.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if      ("ajdoc.good".equals(name))  ajdocStats.goods++;
        else if ("ajc.good".equals(name))    ajcStats.goods++;
        else if ("run.good".equals(name))    runStats.goods++;
        if      ("ajdoc.fail".equals(name))  ajdocStats.fails++;
        else if ("ajc.fail".equals(name))    ajcStats.fails++;
        else if ("run.fail".equals(name))    runStats.fails++;
    }

    private void fire(String prop, Object oldval, Object newval) {
        bean.firePropertyChange(prop, oldval, newval);
    }

    private void fire(String prop) {
        fire(prop, "dummy-old", "dummy-new");
    }

    private static boolean dumpresults = false;
    private Stats ajdocStats = new Stats();
    private Stats ajcStats   = new Stats();
    private Stats runStats   = new Stats();
//    private Stats errorStats   = new Stats();
    private static final String NO_TESTID = "NONE";
    private File workingdir = new File("ajworkingdir"); //XXX

    //fields
    private String testId = NO_TESTID;
    private List<Argument> args = new Vector<>();
    private List<Testset> testsets = new Vector<>();
    private Path classpath;
    private Path internalclasspath;
    private File destdir;
    private File dir;
    private File errorfile;
    private List<Run> testclasses = new Vector<>();
    private boolean nocompile;
    private Ajdoc ajdoc = null;
    private boolean noclean;
    private boolean noverify;
    private List<String> depends = new Vector<>();
    //end-fields

    public Argfile createArgfile() {
        return createTestset().createArgfile();
    }

    public void setNoverify(boolean input) {
        if (input != noverify) noverify = input;
    }

    public void setOwningTarget(Target target) {
        super.setOwningTarget(target);
        if (null != target) {
            //setTestId(target.getName());
        }
    }

    public void setTestId(String str) {
        if ((null != str) && (0 < str.trim().length())) {
            testId = str;
        }
    }

    public void setArgs(String str) {
        if (str == null || str.length() < 1) return;
        StringTokenizer tok = new StringTokenizer(str, ",", false);
        while (tok.hasMoreTokens()) {
            String name = tok.nextToken().trim();
            if (0 < name.length()) {
              parse(name.startsWith("J") ? createJarg() : createArg(), name);
            }
        }
    }

    private void parse(Argument arg, String name) {
        int itilde = name.lastIndexOf("~");
        if (itilde != -1) {
            name = name.substring(0, itilde) + name.substring(itilde+1);
        }
        int ieq = name.lastIndexOf("=");
        int icolon  = name.lastIndexOf(":");
        int ileft = name.lastIndexOf("[");
        int iright = name.lastIndexOf("]");
        boolean always = true;
        String rest = "";
        String newName = name;
        if (ieq != -1) {
            rest = name.substring(ieq+1);
            newName = name.substring(0, ieq);
            always = true;
        } else if (icolon != -1) {
            rest = name.substring(icolon+1);
            newName = name.substring(0, icolon);
            always = false;
        } else if (ileft != -1) {
            newName = name.substring(0, ileft);
            always = true;
        }
        String values =  ileft == -1 ? rest :
            name.substring(ileft+1, iright > ileft ? iright : rest.length()-1);
        String value = null;
        if (itilde != -1) {
            String prop = project.getUserProperty(values);
            if (prop == null) {
                prop = project.getProperty(values);
            }
            if (prop != null) {
                value = prop;
            }
        }
        if (value != null) {
            arg.setValue(value);
        } else {
            arg.setValues(values);
        }
        arg.setName(newName);
        arg.setAlways(always);
    }

    public Argument createJarg() {
        Argument arg = new Argument(true);
        args.add(arg);
        return arg;
    }


    public Argument createArg() {
        Argument arg = new Argument(false);
        args.add(arg);
        return arg;
    }

    public void setClasspath(Path path) {
        if (classpath == null) {
            classpath = path;
        } else {
            classpath.append(path);
        }
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void setInternalclasspath(Path path) {
        if (internalclasspath == null) {
            internalclasspath = path;
        } else {
            internalclasspath.append(path);
        }
    }

    public Path createInternalclasspath() {
        if (internalclasspath == null) {
            internalclasspath = new Path(project);
        }
        return internalclasspath.createPath();
    }

    public void setInternalclasspathRef(Reference r) {
        createInternalclasspath().setRefid(r);
    }

    public void setDestdir(String destdir) {
        this.destdir = project.resolveFile(destdir);
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public void setErrorfile(File errorfile) {
        this.errorfile = errorfile;
    }

    public Run createJava() {
		Run testclass = new Run(project);
        testclasses.add(testclass);
        return testclass;
    }

    public void setClasses(String str) {
        for (StringTokenizer t = new StringTokenizer(str, ", ", false);
             t.hasMoreTokens();) {
            createJava().setClassname(t.nextToken().trim());
        }
    }

    public void setTestclass(String testclass) {
        createJava().setClassname(testclass);
    }

    public void setAjdoc(boolean b) {
        if (b && ajdoc == null) {
            createAjdoc();
        } else if (!b) {
            ajdoc = null;
        }
    }

    public void setAjdocargs(String str) {
        createAjdoc();
        for (StringTokenizer t = new StringTokenizer(str, ", ", false);
             t.hasMoreTokens();) {
            ajdoc.createArg().setValue(t.nextToken().trim());
        }
    }

    public void addAjdoc(Ajdoc ajdoc) {
        this.ajdoc = ajdoc;
    }

    public Ajdoc createAjdoc() {
        return ajdoc = new Ajdoc();
    }

    public static class Argument {
        private String name;
        private List<String> values = new Vector<>();
        private boolean always = true;
        final boolean isj;
        public Argument(boolean isj) {
            this.isj = isj;
        }
        public void setName(String str) {
            this.name = str.startsWith("-") ? str :
                ("-" + (str.startsWith("J") ? str.substring(1) : str));
        }
        public void setValues(String str) {
            values = new Vector<>();
            StringTokenizer tok = new StringTokenizer(str, ", ", false);
            while (tok.hasMoreTokens()) {
                values.add(tok.nextToken().trim());
            }
        }
        public void setValue(String value) {
            (values = new Vector<>()).add(value);
        }
        public void setAlways(boolean always) {
            this.always = always;
        }
        public String toString() { return name + ":" + values; }
    }

    public void setNocompile(boolean nocompile) {
        this.nocompile = nocompile;
    }

    private static class Stats {
        int goods = 0;
        int fails = 0;
    }

    private static class Arg {
        final String name;
        final String value;
        final boolean isj;
        Arg(String name, String value, boolean isj) {
            this.name = name;
            this.value = value;
            this.isj = isj;
        }
        public String toString() {
            return name + (!"".equals(value) ? " " + value : "");
        }
    }

    public Testset createTestset() {
        Testset testset = new Testset();
        testsets.add(testset);
        return testset;
    }

    public void setNoclean(boolean noclean) {
        this.noclean = noclean;
    }

    public void setDepends(String depends) {
        for (StringTokenizer t = new StringTokenizer(depends, ", ", false);
             t.hasMoreTokens();) {
            this.depends.add(t.nextToken().trim());
        }
    }
    //end-methods

    public static class Argfile {
        private String name;
        public void setName(String name) { this.name = name; }
    }

    public class Ajdoc {
        private Commandline cmd = new Commandline();
        public Commandline.Argument createArg() { return cmd.createArgument(); }
        public Commandline getCommandline() { return cmd; }
        public String toString() { return cmd + ""; }
    }

    public class Testset extends FileSet {
        private List<Argfile> argfileNames = new Vector<>();
        public List<File> argfiles;
        public List<File> files;
        public List<Argument> args = new Vector<>();
        public String classname;
        private boolean havecludes = false;
        private List<Run> testclasses = new Vector<>();
        private Path classpath;
        private Path internalclasspath;
        private Ajdoc ajdoc = null;
        private boolean fork = false;
        private boolean noclean;
        private List<String> depends = new Vector<>();
        public String toString() {
            String str = "";
            if (files.size() > 0) {
                str += "files:" + "\n";
				for (File file : files) {
					str += "\t" + file + "\n";
				}
            }
            if (argfiles.size() > 0) {
                str += "argfiles:" + "\n";
				for (File argfile : argfiles) {
					str += "\t" + argfile + "\n";
				}
            }
            if (args.size() > 0) {
                str += "args:" + "\n";
				for (Argument arg : args) {
					str += "\t" + arg + "\n";
				}
            }
            if (testclasses.size() > 0) {
                str += "classes:" + "\n";
				for (Run testclass : testclasses) {
					str += "\t" + testclass + "\n";
				}
            }
            return str;
        }
        public void setIncludes(String includes) {
            super.setIncludes(includes);
            havecludes = true;
        }
        public void setExcludes(String excludes) {
            super.setExcludes(excludes);
            havecludes = true;
        }
        public void setIncludesfile(File includesfile) {
            super.setIncludesfile(includesfile);
            havecludes = true;
        }
        public void setExcludesfile(File excludesfile) {
            super.setExcludesfile(excludesfile);
            havecludes = true;
        }

        public void setArgfile(String name) {
            createArgfile().setName(name);
        }

        public void setArgfiles(String str) {
            StringTokenizer tok = new StringTokenizer(str, ", ", false);
            while (tok.hasMoreTokens()) {
                createArgfile().setName(tok.nextToken().trim());
            }

        }
        public Argfile createArgfile() {
            Argfile argfile = new Argfile();
            argfileNames.add(argfile);
            return argfile;
        }
        public Run createJava() {
            // See crashing note
            //Run testclass = new Run();
            Run testclass = new Run(project);
            this.testclasses.add(testclass);
            return testclass;
        }
        public void addJava(Run run) {
            this.testclasses.add(run);
        }
        public void setJava(String str) {
            StringTokenizer t = new StringTokenizer(str, " ");
            Run run = createJava();
            run.setClassname(t.nextToken().trim());
            while (t.hasMoreTokens()) {
                run.createArg().setValue(t.nextToken().trim());
            }
        }
        public void setTestclass(String testclass) {
            createJava().setClassname(testclass);
        }

        public void setClasses(String str) {
            for (StringTokenizer t = new StringTokenizer(str, ", ", false);
                 t.hasMoreTokens();) {
                createJava().setClassname(t.nextToken().trim());
            }
        }
        public void setClasspath(Path path) {
            if (classpath == null) {
                classpath = path;
            } else {
                classpath.append(path);
            }
        }

        public Path createClasspath() {
            if (classpath == null) {
                classpath = new Path(project);
            }
            return classpath.createPath();
        }

        public void setClasspathRef(Reference r) {
            createClasspath().setRefid(r);
        }
        public void setInternalclasspath(Path path) {
            if (internalclasspath == null) {
                internalclasspath = path;
            } else {
                internalclasspath.append(path);
            }
        }

        public Path createInternalclasspath() {
            if (internalclasspath == null) {
            internalclasspath = new Path(project);
            }
            return internalclasspath.createPath();
        }

        public void setInternalclasspathRef(Reference r) {
            createInternalclasspath().setRefid(r);
        }

        public void setAjdoc(boolean b) {
            if (b && ajdoc == null) {
                createAjdoc();
            } else if (!b) {
                this.ajdoc = null;
            }
        }
        public Ajdoc getAjdoc() { return this.ajdoc; }
        public void setAjdocargs(String str) {
            createAjdoc();
            for (StringTokenizer t = new StringTokenizer(str, ", ", false);
                     t.hasMoreTokens();) {
                this.ajdoc.createArg().setValue(t.nextToken().trim());
            }
        }
        public void addAjdoc(Ajdoc ajdoc) {
            this.ajdoc = ajdoc;
        }
        public Ajdoc createAjdoc() {
            return this.ajdoc = new Ajdoc();
        }
        public void setFork(boolean fork) {
            this.fork = fork;
        }
        public void setNoclean(boolean noclean) {
            this.noclean = noclean;
        }
        public void setDepends(String depends) {
            for (StringTokenizer t = new StringTokenizer(depends, ", ", false);
                 t.hasMoreTokens();) {
                this.depends.add(t.nextToken().trim());
            }
        }
        //end-testset-methods

        public void resolve() throws BuildException {
            if (dir != null) this.setDir(dir);
            File src = getDir(project);
            argfiles = new Vector<>();
            files = new Vector<>();
			for (Argfile argfileName : argfileNames) {
				String name = argfileName.name;
				File argfile = new File(src, name);
				if (check(argfile, name, location)) argfiles.add(argfile);
			}
            if (havecludes || argfiles.size() <= 0) {
                String[] filenames =
                    getDirectoryScanner(project).getIncludedFiles();
				for (String name : filenames) {
					if (name.endsWith(".java")) {
						File file = new File(src, name);
						if (check(file, name, location)) files.add(file);
					}
				}
            }
			for (Run run : Ajctest.this.testclasses) {
				this.testclasses.add(run);
			}
            if (this.classpath == null) {
                setClasspath(Ajctest.this.classpath);
            }
            if (this.internalclasspath == null) {
                setInternalclasspath(Ajctest.this.internalclasspath);
            }
            if (this.ajdoc == null) {
                this.ajdoc = Ajctest.this.ajdoc;
            }
            if (this.fork) {
				for (Run testclass : this.testclasses) {
					testclass.setFork(fork);
				}
            }
            if (!this.noclean) {
                this.noclean = Ajctest.this.noclean;
            }
            this.depends.addAll(Ajctest.this.depends);
        }
        private boolean check(File file, String name, Location loc)
            throws BuildException {
            loc = loc != null ? loc : location;
            if (file == null) {
                throw new BuildException
                    ("file " + name + " is null!", loc);
            }
            if (!file.exists()) {
                throw new BuildException
                    ("file " + file + " with name " + name +
                     " doesn't exist!", loc);
            }
            return true;
        }
        public void setArgs(String str) {
            if (str == null || str.length() < 1) return;
            StringTokenizer tok = new StringTokenizer(str, ",", false);
            while (tok.hasMoreTokens()) {
                String name = tok.nextToken().trim();
                parse(name.startsWith("J") ? createJarg() : createArg(), name);
            }
        }

        public Argument createJarg() {
            Argument arg = new Argument(true);
            args.add(arg);
            return arg;
        }

        public Argument createArg() {
            Argument arg = new Argument(false);
            args.add(arg);
            return arg;
        }
    }

    private void prepare() throws BuildException {

    }

    private void finish() throws BuildException {
        if (errors.size() > 0) {
            log("");
            log("There " + w(errors) + " " + errors.size() + " errors:");
            for (int i = 0; i < errors.size(); i++) {
                log(" ", errors.get(i), i);
            }
        }
        allErrors.addAll(errors);
    }

    private void log(String space, Failure failure, int num) {
        String number = "[" + num + "] ";
        log(enough(number, 60, '-'));
        for (int i = number.length()-1; i > 0; i--) space += " ";
        log(space, failure.testset.files, "files:");
        log(space, failure.testset.argfiles, "argfiles:");
        log(space, failure.args, "args:");
        log(space + "msgs:" + failure.msgs);
    }


    private String enough(String str, int size, char filler) {
        while (str.length() < size) str += filler;
        return str;
    }


    private void log(String space, List<?> list, String title) {
        if (list == null || list.size() < 1) return;
        log(space + title);
		for (Object o : list) {
			log(space + "  " + o);
		}
    }

    private void execute(Testset testset, List<Arg> args) throws BuildException {
        if (testset.files.size() > 0) {
            log("\tfiles:");
			for (File file : testset.files) {
				log("\t  " + file);
			}
        }
        if (testset.argfiles.size() > 0) {
            log("\targfiles:");
			for (File file : testset.argfiles) {
				log("\t  " + file);
			}
        }
        if (args.size() > 0) {
            log("\targs:");
			for (Arg arg : args) {
				log("\t  " + arg);
			}
        }
        if (testset.testclasses.size() > 0) {
            log("\tclasses:");
			for (Run testclass : testset.testclasses) {
				log("\t  " + testclass);
			}
        }
        if (!testset.noclean &&
            (!isSet("noclean") && !isSet("nocompile"))) {
            delete(destdir);
            make(destdir);
        }
        delete(workingdir);
        make(workingdir);
		for (String depend : testset.depends) {
			String target = depend + "";
			// todo: capture failures here?
			project.executeTarget(target);
		}
        int exit;
        if (!isSet("nodoc") && testset.ajdoc != null) {
            log("\tdoc... " + testset.ajdoc);
            AjdocWrapper ajdoc = new AjdocWrapper(testset, args);
            if ((exit = ajdoc.run()) != 0) {
                post(testset, args, ajdoc.msgs, exit, "ajdoc");
            } else {
                fire("ajdoc.good");
            }
            fire("ajdoc.done");
            log("\tdone with ajdoc.");
        }
        boolean goodCompile = true;
        if (!isSet("nocompile") && !nocompile) {
            log("\tcompile" +
                (testset.noclean ? "(boostrapped)" : "") + "...");
            //AjcWrapper ajc = new AjcWrapper(testset, args);
            JavaCommandWrapper ajc;
            // XXX dependency on Ant property ajctest.compiler
            final String compiler = getAntProperty("ajctest.compiler");
            if ("eclipse".equals(compiler) || "eajc".equals(compiler)) {
                ajc = new EAjcWrapper(testset, args);
            } else if ((null == compiler) || "ajc".equals(compiler)) {
                ajc = new AjcWrapper(testset, args);
            } else if ("javac".equals(compiler)) {
                throw new Error("javac not supported");
                //ajc = new JavacWrapper(testset, args);
            } else {
                throw new Error("unknown compiler: " + compiler);
            }

            System.out.println("using compiler: " + ajc);
            try {
                if ((exit = ajc.run()) != 0) {
                    post(testset, args, ajc.msgs, exit, "ajc");
                    goodCompile = false;
                } else {
                    fire("ajc.good");
                }
                fire("ajc.done");
            } catch (Throwable ___) {
                post(testset, args, ___+"", -1, "ajc");
                goodCompile = false;
            }
        }
        if (!goodCompile) {
            post(testset, new Vector(),
                 "couldn't run classes " + testset.testclasses +
                 "due to failed compile",
                 -1, "run");

        } else if (!isSet("norun")) {
			for (Run testclass : testset.testclasses) {
				log("\ttest..." + testclass.classname());
				if (null != destdir) {
					testclass.setClassesDir(destdir.getAbsolutePath());
				}
				if ((exit = testclass.executeJava()) != 0) {
					post(testset, new Vector(), testclass.msgs, exit, "run");
				} else {
					fire("run.good");
				}
				fire("run.done");
			}
        }
        log("");
    }

    public void execute() throws BuildException {
        gui(this);
        dumpresults = isSet("dumpresults");
        prepare();
        log(testsets.size() + " testset" + s(testsets),
            Project.MSG_VERBOSE);
        Map<Testset,List<List<Arg>>> testsetToArgcombo = new HashMap<>();
        List<Integer> argcombos = new Vector<>();
        for (Testset testset: testsets) {
            testset.resolve();
            List<Argument> bothargs = new Vector<>(args);
            bothargs.addAll(testset.args);
            List<List<Arg>> argcombo = argcombo(bothargs);
            argcombos.add(argcombo.size());
            testsetToArgcombo.put(testset, argcombo);
        }
        while (!testsetToArgcombo.isEmpty()) {
            int testsetCounter = 1;
            for (Iterator<Testset> iter = testsets.iterator(); iter.hasNext(); testsetCounter++) {
                Testset testset = iter.next();
                List<List<Arg>> argcombo = testsetToArgcombo.get(testset);
                if (argcombo.size() == 0) {
                    testsetToArgcombo.remove(testset);
                    continue;
                }
                List<Arg> args = argcombo.remove(0);
                final String startStr = "Testset " + testsetCounter + " of " + testsets.size();
                String str = startStr + " / Combo " + testsetCounter + " of " + argcombos.size();
                log("---------- " + str + " ----------");
                execute(testset, args);
            }
        }

//          for (Iterator iter = testsets.iterator(); iter.hasNext(); _++) {
//              Testset testset = (Testset)iter.next();
//              testset.resolve();
//              List bothargs = new Vector(args);
//              bothargs.addAll(testset.args);
//              int __ = 1;
//              List argcombo = argcombo(bothargs);
//              log(argcombo.size() + " combination" + s(argcombo),
//                  Project.MSG_VERBOSE);
//              final String startStr = "Testset " + _ + " of " + testsets.size();
//              for (Iterator comboiter = argcombo.iterator();
//                   comboiter.hasNext(); __++) {
//                  List args = (List)comboiter.next();
//                  execute(testset, args);
//                  log("");
//              }
//          }
        finish();
    }

    private void delete(File dir) throws BuildException {
        Delete delete = (Delete)project.createTask("delete");
        delete.setDir(dir);
        delete.execute();
    }

    private void make(File dir) throws BuildException {
        Mkdir mkdir = (Mkdir)project.createTask("mkdir");
        mkdir.setDir(dir);
        mkdir.execute();
    }

    private String getAntProperty(String name) {
        String uprop = project.getUserProperty(name);
        if (null == uprop) {
            uprop = project.getProperty(name);
        }
        return uprop;
    }

    private boolean isSet(String name) {
        String uprop = project.getUserProperty(name);
        if (uprop == null ||
            "no".equals(uprop) ||
            "false".equals(uprop)) return false;
        String prop = project.getProperty(name);
        if (prop == null ||
            "no".equals(prop) ||
            "false".equals(prop)) return false;
        return true;
    }

    /**
     * Interpose Wrapper class to catch and report exceptions
     * by setting a positive value for System.exit().
     * (In some cases it seems that Exceptions are not being reported
     *  as errors in the tests.)
     * This forces the VM to fork.  A forked VM is required for
     * two reasons:
     * (1) The wrapper class may have been defined by a different
     * class loader than the target class, so it would not be able
     * to load the target class;
     * <p>
     * (2) Since the wrapper class is generic, we have to pass in
     * the name of the target class.  I choose to do this using
     * VM properties rather than hacking up the arguments.
     * <p>todo: relies on name/value of property "taskdef.jar"
     *    to add jar with wrapper to invoking classpath.
     * <p>
     * It is beneficial for another reason:
     * (3) The wrapper class can be asked to test-load all classes
     *     in a classes dir, by setting a VM property.  This class
     *     sets up the property if the value is defined using
     *     <code>setClassesDir(String)</code>
     * <p>todo: if more tunnelling, generalize and parse.
     */
    public class RunWrapper extends Java {
        public final Class LINK_WRAPPER_CLASS = MainWrapper.class;
        /** tracked in MainWrapper.PROP_NAME */ // todo: since reflective, avoid direct
        public final String PROP_NAME = "MainWrapper.classname";
        /** tracked in MainWrapper.CLASSDIR_NAME */
        public final String CLASSDIR_NAME = "MainWrapper.classdir";
        public final String WRAPPER_CLASS
            = "org.aspectj.internal.tools.ant.taskdefs.MainWrapper";
        private String classname;
        protected String classesDir;
        /** capture classname here, replace with WRAPPER_CLASS */
        public void setClassname(String classname) {
            super.setClassname(WRAPPER_CLASS);
            this.classname = classname;
        }

        /**
         * Setup the requirements for the wrapper class:
         * <li>fork to get classpath and VM properties right</li>
         * <li>set VM property</li>
         * <li>add ${ajctest.wrapper.jar} (with wrapper class) to the classpath</li>
         */
        private void setup() {
            setFork(true);
            Commandline.Argument cname = createJvmarg();
            cname.setValue("-D"+PROP_NAME+"="+classname);
            if (!noverify) {
                cname = createJvmarg();
                cname.setValue("-Xfuture"); // todo: 1.2 or later..
            }
            if (null != classesDir) {
                cname = createJvmarg();
                cname.setValue("-D"+CLASSDIR_NAME+"="+classesDir);
            }
            // todo dependence on name/setting of ajctest.wrapper.jar
            String value =  project.getProperty("ajctest.wrapper.jar");
            if (null != value) {
                Path wrapperPath = new Path(project, value);
               RunWrapper.this.createClasspath().append(wrapperPath);
            }
        }

        /** do setup, then super.execute() */
        public int executeJava() {
            setup();
            int result = super.executeJava();
            // snarf - also load all classes?
            return result;
        }

        /** set directory to scan for classes */
        public void setClassesDir(String dir) {
            classesDir = dir;
        }
    }

    public class Run extends RunWrapper {
        //public class Run extends Java
        private Path bootclasspath;
        public void setBootbootclasspath(Path path) {
            if (bootclasspath == null) {
                bootclasspath = path;
            } else {
                bootclasspath.append(path);
            }
        }
        public Path createBootbootclasspath() {
            if (bootclasspath == null) bootclasspath = new Path(this.project);
            return bootclasspath.createPath();
        }
        public void setBootbootclasspathRef(Reference r) {
            createBootbootclasspath().setRefid(r);
        }
        private Path bootclasspatha;
        public void setBootbootclasspatha(Path path) {
            if (bootclasspatha == null) {
                bootclasspatha = path;
            } else {
                bootclasspatha.append(path);
            }
        }
        public Path createBootbootclasspatha() {
            if (bootclasspatha == null) bootclasspatha = new Path(this.project);
            return bootclasspatha.createPath();
        }
        public void setBootbootclasspathaRef(Reference r) {
            createBootbootclasspatha().setRefid(r);
        }
        private Path bootclasspathp;
        public void setBootbootclasspathp(Path path) {
            if (bootclasspathp == null) {
                bootclasspathp = path;
            } else {
                bootclasspathp.append(path);
            }
        }
        public Path createBootbootclasspathp() {
            if (bootclasspathp == null) bootclasspathp = new Path(this.project);
            return bootclasspathp.createPath();
        }
        public void setBootbootclasspathpRef(Reference r) {
            createBootbootclasspathp().setRefid(r);
        }
        public Run(Project project) {
            super();
            //this.project = Ajctest.this.project;
            this.setTaskName("ajcjava");
            this.project = project;
        }
        public String msgs = "";
        public int executeJava() {
            Path cp = Ajctest.this.classpath != null ? Ajctest.this.classpath :
                new Path(this.project, destdir.getAbsolutePath());
            cp.append(Path.systemClasspath);
            this.setClasspath(cp);
            if (bootclasspath != null) {
                setFork(true);
                createJvmarg().setValue("-Xbootclasspath:" + bootclasspath);
            }
            if (bootclasspatha != null) {
                setFork(true);
                createJvmarg().setValue("-Xbootclasspath/a:" + bootclasspatha);
            }
            if (bootclasspathp != null) {
                setFork(true);
                createJvmarg().setValue("-Xbootclasspath/p:" + bootclasspathp);
            }
            int exit = -1;
            // todo: add timeout feature todo: this or below?
            try {
                exit = super.executeJava();
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                PrintWriter out = new PrintWriter(sw);
                t.printStackTrace(out);
                msgs = sw.toString();
                out.close();
                // todo: return exit code
            }
            return exit;
        }
        public String _classname;
        public String classname() { return _classname; }
        public void setClassname(String classname) {
            super.setClassname(_classname = classname);
        }
        public String toString() { return _classname; }
    }
    // class Run
    // todo: need to run in a wrapper which report non-zero int on exception
    // todo: unused method? see executeJava above.
//    private int java(String classname, Collection args) throws BuildException {
//        Java java = (Java)project.createTask("java");
//        java.setClassname(classname);
//        for (Iterator i = args.iterator(); i.hasNext();) {
//            Object o = i.next();
//            Commandline.Argument arg = java.createArg();
//            if (o instanceof File) {
//                arg.setFile((File)o);
//            } else if (o instanceof Path) {
//                arg.setPath((Path)o);
//            } else {
//                arg.setValue(o+"");
//            }
//        }
//        return java.executeJava();
//    }

    private static List allErrors = new Vector();
    private List<Failure> errors = new Vector<>();

    private void post(Testset testset, List args,
                      String msgs, int exit, String type) {
        errors.add(new Failure(testset, args, msgs, exit, type, testId));
        fire(type + ".fail");
    }

    private static long startTime;
    private static long stopTime;

    private static String date(long time) {
        return DateFormat.getDateTimeInstance
            (DateFormat.FULL, DateFormat.FULL).
            format(new Date(time));
    }

    static {
        final PrintStream err = System.err;
        startTime = System.currentTimeMillis();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                private String ms(long start, long stop) {
                    long rest = Math.abs(stop-start) / 1000;
                    long days = rest / 86400;
                    long hours = (rest -= days*86400) / 3600;
                    long mins = (rest -= hours*3600) / 60;
                    long secs = (rest -= mins*60);
                    boolean req = false;
                    String str = "";
                    if (req || days > 0) {
                        req = true;
                        str += days + " day" + (days != 1 ? "s" : "") + " ";
                    }
                    if (req || hours > 0) {
                        req = true;
                        str += hours + " hour" + (hours != 1 ? "s" : "") + " ";
                    }
                    if (req || mins > 0) {
                        req = true;
                        str += mins + " minute" + (mins != 1 ? "s" : "") + " ";
                    }
                    str += secs + " second" + (secs != 1 ? "s" : "") + " ";
                    return str;
                }

                public void run() {
                    Ajctest current = CURRENT_AJCTEST;
                    String oneLine = "warning: oneLine not set.";
                    String multiLine = "warning: multiLine not set.";

                    // setup oneLine
                    if (null == current) {
                        oneLine = "\nRESULT=\"ERROR\" null ACJTEST";
                    } else {
                        StringBuffer sb = new StringBuffer("\n");
                        int errs = Ajctest.allErrors.size();
                        int allFails = errs
                            + current.ajdocStats.fails
                            + current.ajcStats.fails
                            + current.runStats.fails;
                        if (1 > allFails) {
                            sb.append("RESULT=\"PASS\"\terrors=\"");
                        } else {
                            sb.append("RESULT=\"FAIL\"\terrors=\"");
                        }
                        sb.append(""+errs);
                        sb.append("\"\tajdoc.pass=\"");
                        sb.append(""+current.ajdocStats.goods);
                        sb.append("\"\tajdoc.fail=\"");
                        sb.append(""+current.ajdocStats.fails);
                        sb.append("\"\tajc.pass=\"");
                        sb.append(""+current.ajcStats.goods);
                        sb.append("\"\tajc.fail=\"");
                        sb.append(""+current.ajcStats.fails);
                        sb.append("\"\trun.pass=\"");
                        sb.append(""+current.runStats.goods);
                        sb.append("\"\trun.fail=\"");
                        sb.append(""+current.runStats.fails);
                        sb.append("\"\ttestId=\"");
                        sb.append(current.testId);
                        sb.append("\"\tproject=\"");
                        Project p = current.getProject();
                        if (null != p) sb.append(p.getName());
                        sb.append("\"\tfile=\"");
                        sb.append(""+current.getLocation());
                        sb.append("\"");
                        oneLine = sb.toString();
                    }

                    // setup multiLine
                    {
                        stopTime = System.currentTimeMillis();
                        String str = "";
                        str += "\n";
                        str +=
                            "===================================" +
                            "===================================" + "\n";
                        str += "Test started : " + date(startTime) + "\n";
                        str += "Test ended   : " + date(stopTime) + "\n";
                        str += "Total time   : " + ms(startTime, stopTime) + "\n";
                        str +=
                            "------------------------------" +
                            " Summary " +
                            "------------------------------" + "\n";
                        str += "Task\tPassed\tFailed" + "\n";
                        Object[] os = new Object[] {
                            "ajdoc", current.ajdocStats.goods+"",   current.ajdocStats.fails+"",
                            "ajc",   current.ajcStats.goods  +"",   current.ajcStats.fails  +"",
                            "run",   current.runStats.goods  +"",   current.runStats.fails  +"",
                        };
                        for (int i = 0; i < os.length; i += 3) {
                            str += os[i] + "\t" + os[i+1] + "\t" + os[i+2] + "\n";
                        }
                        if (allErrors.size() > 0) {
                            str += "" + "\n";
                            str +=
                                "There " + w(allErrors) + " " +
                                allErrors.size() + " error" +
                                s(allErrors) + ":" + "\n";
                            for (int i = 0; i < allErrors.size(); i++) {
                                Failure failure = (Failure)allErrors.get(i);
                                str +=
                                    "---------- Error " + i + " [" +
                                    failure.testId + "]" +
                                    " ------------------------------" + "\n";
                                str += " " + failure + "\n\n";
                            }
                        } else {
                            str += "No errors." + "\n";
                        }
                        str += "--------------------------" +
                            " End of Summary " +
                            "---------------------------" + "\n";
                        multiLine = str;
                    }

                    // print both multiLine and oneLine
                    err.println(multiLine);
                    err.println(oneLine);
                    if (dumpresults && (allErrors.size() +
                                        current.ajdocStats.fails +
                                        current.ajcStats.fails   +
                                        current.runStats.fails) > 0) {
                        String date = date(System.currentTimeMillis());
                        String filename = "ajc-errors";
                        for (StringTokenizer t = new StringTokenizer(date, ",: ");
                             t.hasMoreTokens();) {
                            filename += "-" + t.nextToken().trim();
                        }
                        filename += ".txt";
                        PrintWriter out = null;
                        File file = new File(filename);
                        System.err.println("dumping results to " + file);
                        try {
                            out = new PrintWriter(new FileWriter(file));
                            out.println(multiLine);
                            out.println(oneLine);
                            System.err.println("dumped results to " + file);
                        } catch (IOException ioe) {
                            if (out != null) out.close();
                        }
                    }
                }
            }));
    }

    private static String w(List list) { return a(list, "were", "was"); }
    private static String s(List list) { return a(list, "s", ""); }

    private static String a(List list, String some, String one) {
        return list == null || list.size() != 1 ? some : one;
    }

    static class Failure {
        public final Testset testset;
        public final List args;
        public final String msgs;
        public final int exit;
        public final String type;
        public final long time;
        public final String testId;
        public Failure(Testset testset, List args,
                       String msgs, int exit, String type,
                       String testId) {
            this.testset = testset;
            this.args = args;
            this.msgs = msgs;
            this.exit = exit;
            this.type = type;
            this.time = System.currentTimeMillis();
            this.testId = testId;
        }
        public String toString() {
            String str = "testId:" + testId+ "\n";
            str += "type:" + type + "\n";
            str += testset + "\n";
            if (args.size() > 0) {
                str += " args: " + args + "\n";
            }
            str += " msgs:" + msgs + "\n";
            str += " exit:" + exit;
            return str;
        }
    }

    private List<List<Arg>> argcombo(List<Argument> arguments) {
        List<Argument> combos = new Vector<>();
        List<Arg> always = new Vector<>();
		for (Argument arg : arguments) {
			if (arg.values.size() == 0) arg.values.add("");
			if (!arg.always && !arg.values.contains(null)) arg.values.add(null);
			if (arg.values.size() > 0) {
				combos.add(arg);
			} else if (arg.always) {
				always.add(new Arg(arg.name, arg.values.get(0) + "", arg.isj));
			}
		}
        List<List<Arg>> argcombo = combinations(combos);
		for (Arg arg : always) {
			for (List<Arg> argList : argcombo) {
				argList.add(arg);
			}
		}
        return argcombo;
    }

    private abstract class ExecWrapper {
        public String msgs;
        public int run() {
            return run(createCommandline());
        }
        protected abstract Commandline createCommandline();
        protected final int run(Commandline cmd) {
            Process process = null;
            int exit = Integer.MIN_VALUE;
            final StringBuffer buf = new StringBuffer();
            Thread errPumper = null;
            Thread outPumper = null;
            try {
                log("\tcalling " + cmd, Project.MSG_VERBOSE);
                process = Runtime.getRuntime().exec(cmd.getCommandline());
                OutputStream os = new OutputStream() {
                        StringBuffer sb = new StringBuffer();
                        public void write(int b) throws IOException {
                            final char c = (char)b;
                            buf.append(c);
                            if (c != '\n') {
                                sb.append(c);
                            } else {
                                System.err.println(sb.toString());
                                sb = new StringBuffer();
                            }
                        }
                    };
                OutputStream los = new LogOutputStream(Ajctest.this,
                                                       Project.MSG_INFO);
                outPumper = new Thread(new StreamPumper(process.getInputStream(),
                                                        los));
                errPumper = new Thread(new StreamPumper(process.getErrorStream(),
                                                        os));
                outPumper.setDaemon(true);
                errPumper.setDaemon(true);
                outPumper.start();
                errPumper.start();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
                //throw e;
            } finally {
                try {
                    if (outPumper != null) outPumper.join();
                    if (errPumper != null) errPumper.join();
                } catch (InterruptedException ie) {
                } finally {
                    outPumper = null;
                    errPumper = null;
                }
                exit = process.exitValue();
                msgs = buf.toString();
                if (exit != 0) {
                    log("Test failed with exit value: " + exit);
                } else {
                    log("Success!", Project.MSG_VERBOSE);
                }
                if (process != null) process.destroy();
                process = null;
                System.err.flush();
                System.out.flush();
            }
            return exit;
        }
    }

    private class AjcWrapper extends JavaCommandWrapper {
        public AjcWrapper(Testset testset, List args) {
            super(testset, args, false);
            if (testset.noclean) {
                setExtraclasspath(new Path(project,
                                           destdir.getAbsolutePath()));
            }
        }
        String getMainClassName() {
            return "org.aspectj.tools.ajc.Main";
        }
    }

    private class EAjcWrapper extends JavaCommandWrapper {
        public EAjcWrapper(Testset testset, List args) {
            super(testset, args, false);
            if (testset.noclean) {
                setExtraclasspath(new Path(project,
                                           destdir.getAbsolutePath()));
            }
        }
        String getMainClassName() {
            return "org.aspectj.ajdt.ajc.Main";
        }
    }

    static List ajdocArgs(List args) {
        List newargs = new Vector();
        for (Iterator i = args.iterator(); i.hasNext();) {
            String arg = i.next() + "";
            if (arg.startsWith("-X")) {
                newargs.add(arg);
            } else if (arg.equals("-public")    ||
                       arg.equals("-package")   ||
                       arg.equals("-protected") ||
                       arg.equals("-private")) {
                newargs.add(arg);
            } else if (arg.equals("-d")             ||
                       arg.equals("-classpath")     ||
                       arg.equals("-cp")            ||
                       arg.equals("-sourcepath")    ||
                       arg.equals("-bootclasspath") ||
                       arg.equals("-argfile")) {
                newargs.add(arg);
                newargs.add(i.next()+"");
            } else if (arg.startsWith("@")) {
                newargs.add(arg);
            }
        }
        return newargs;
    }

    private class AjdocWrapper extends JavaCommandWrapper {
        public AjdocWrapper(Testset testset, List args) {
            super(testset, ajdocArgs(args), true);
            String[] cmds = testset.getAjdoc().getCommandline().getCommandline();
			Collections.addAll(this.args, cmds);
        }
        String getMainClassName() {
            return "org.aspectj.tools.ajdoc.Main";
        }
    }

    private abstract class JavaCommandWrapper extends ExecWrapper {
        abstract String getMainClassName();
        protected Testset testset;
        protected List args;
        protected boolean needsClasspath;
        protected Path extraClasspath;

        public JavaCommandWrapper(Testset testset, List args,
                                  boolean needsClasspath) {
            this.testset = testset;
            this.args = args;
            this.needsClasspath = needsClasspath;
            this.extraClasspath = testset.internalclasspath;
        }
        public void setExtraclasspath(Path extraClasspath) {
            this.extraClasspath = extraClasspath;
        }

        public String toString() {
            return LangUtil.unqualifiedClassName(getClass())
                + "(" + getMainClassName() + ")";
        }

        protected Commandline createCommandline() {
            Commandline cmd = new Commandline();
            cmd.setExecutable("java");
            cmd.createArgument().setValue("-classpath");
            Path cp = null;
            if (extraClasspath != null) {
                cp = extraClasspath;
            }
            if (extraClasspath == null) {
                Path aspectjBuildDir =
                    new Path(project,
                             project.getProperty("ajctest.pathelement"));
                // todo: dependency on ant script variable name ajctest.pathelement
                if (cp == null) cp = aspectjBuildDir;
                else cp.append(aspectjBuildDir);
            }
            if (cp == null) {
                cp = Path.systemClasspath;
            } else {
                cp.append(Path.systemClasspath);
            }
            cmd.createArgument().setPath(cp);
			for (Object item : args) {
				Arg arg = (Arg) item;
				if (arg.isj) {
					cmd.createArgument().setValue(arg.name);
					if (!arg.value.equals("")) {
						cmd.createArgument().setValue(arg.value);
					}
				}
			}
            cmd.createArgument().setValue(getMainClassName());
            boolean alreadySetDestDir = false;
            boolean alreadySetClasspath = false;
			for (Object o : args) {
				Arg arg = (Arg) o;
				if (!arg.isj) {
					cmd.createArgument().setValue(arg.name);
					if (arg.name.equals("-d")) {
						setDestdir(arg.value + "");
						alreadySetDestDir = true;
					}
					if (arg.name.equals("-classpath")) {
						alreadySetClasspath = true;
					}
					if (!arg.value.equals("")) {
						cmd.createArgument().setValue(arg.value);
					}
				}
			}
            if (destdir == null) {
                setDestdir(".");
            }
            if (!alreadySetDestDir) {
                cmd.createArgument().setValue("-d");
                cmd.createArgument().setFile(destdir);
            }
            if (!alreadySetClasspath && testset.classpath != null) {
                cmd.createArgument().setValue("-classpath");
                cmd.createArgument().setPath(testset.classpath);
            } else if (needsClasspath) {
                Path _cp = Ajctest.this.classpath != null ? Ajctest.this.classpath :
                    new Path(project, destdir.getAbsolutePath());
                _cp.append(Path.systemClasspath);
                cmd.createArgument().setValue("-classpath");
                cmd.createArgument().setPath(_cp);
            }
			for (File value : testset.files) {
				cmd.createArgument().setFile(value);
			}
			for (File file : testset.argfiles) {
				cmd.createArgument().setValue("-argfile");
				cmd.createArgument().setFile(file);
			}
            return cmd;
        }
    }

    /** implement invocation of ajc */
//    private void java(Testset testset, List args) throws BuildException {
//        Java java = (Java)project.createTask("java");
//        java.setClassname("org.aspectj.tools.ajc.Main");
//        if (classpath != null) {
//            java.setClasspath(classpath);
//        }
//        for (Iterator iter = args.iterator(); iter.hasNext();) {
//            Arg arg = (Arg)iter.next();
//            if (arg.isj) {
//                java.createJvmarg().setValue(arg.name);
//                if (!arg.value.equals("")) {
//                    java.createJvmarg().setValue(arg.value);
//                }
//            }
//        }
//        for (Iterator iter = args.iterator(); iter.hasNext();) {
//            Arg arg = (Arg)iter.next();
//            if (!arg.isj) {
//                java.createArg().setValue(arg.name);
//                if (!arg.value.equals("")) {
//                    java.createArg().setValue(arg.value);
//                }
//            }
//        }
//        for (Iterator iter = testset.files.iterator(); iter.hasNext();) {
//            java.createArg().setFile((File)iter.next());
//        }
//        for (Iterator iter = testset.argfiles.iterator(); iter.hasNext();) {
//            java.createArg().setValue("-argfile");
//            java.createArg().setFile((File)iter.next());
//        }
//        java.setFork(true);
//        java.execute();
//    }
//
//    private void exec(Testset testset, List args) throws BuildException {
//        ExecTask exec = (ExecTask)project.createTask("exec");
//        exec.setExecutable("java");
//        if (classpath != null) {
//            exec.createArg().setValue("-classpath");
//            exec.createArg().setPath(classpath);
//        }
//        for (Iterator iter = args.iterator(); iter.hasNext();) {
//            Arg arg = (Arg)iter.next();
//            if (arg.isj) {
//                exec.createArg().setValue(arg.name);
//                if (!arg.value.equals("")) {
//                    exec.createArg().setValue(arg.value);
//                }
//            }
//        }
//        exec.createArg().setValue("org.aspectj.tools.ajc.Main");
//        for (Iterator iter = args.iterator(); iter.hasNext();) {
//            Arg arg = (Arg)iter.next();
//            if (!arg.isj) {
//                exec.createArg().setValue(arg.name);
//                if (!arg.value.equals("")) {
//                    exec.createArg().setValue(arg.value);
//                }
//            }
//        }
//        for (Iterator iter = testset.files.iterator(); iter.hasNext();) {
//            exec.createArg().setFile((File)iter.next());
//        }
//        for (Iterator iter = testset.argfiles.iterator(); iter.hasNext();) {
//            exec.createArg().setValue("-argfile");
//            exec.createArg().setFile((File)iter.next());
//        }
//        exec.execute();
//    }
//
    public void handle(Throwable t) {
        log("handling " + t);
        if (t != null) t.printStackTrace();
        log("done handling " + t);
    }

    private List<List<Arg>> combinations(List<Argument> arglist) {
        List<List<Arg>> result = new Vector<>();
        result.add(new Vector<>());
		for (Argument arg : arglist) {
			int N = result.size();
			for (int i = 0; i < N; i++) {
				List<Arg> to = result.remove(0);
				for (String s : arg.values) {
					List<Arg> newlist = new Vector<>(to);
					Object val = s;
					if (val != null) newlist.add(new Arg(arg.name, val + "", arg.isj));
					result.add(newlist);
				}
			}
		}
        return result;
    }

    /////////////////////// GUI support //////////////////////////////
    private static Gui gui;

    private static void gui(Ajctest ajc) {
        if (firstTime && ajc.isSet("gui")) {
            JFrame f = new JFrame("AspectJ Test Suite");
            f.getContentPane().add(BorderLayout.CENTER, gui = new Gui());
            f.pack();
            f.setVisible(true);
        }
        if (gui != null) {
            ajc.bean.addPropertyChangeListener(gui);
        }
        firstTime = false;
    }

    private static class Gui extends JPanel implements PropertyChangeListener {
        private FailurePanel fail = new FailurePanel();
        private TablePanel table = new TablePanel();
        private StatusPanel status = new StatusPanel();
        public Gui() {
            super(new BorderLayout());
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split.setPreferredSize(new Dimension(500, 300));
            split.add(JSplitPane.BOTTOM, fail);
            split.add(JSplitPane.TOP, table);
            split.setDividerLocation(200);
            add(BorderLayout.CENTER, split);
            add(BorderLayout.SOUTH, status);
            setPreferredSize(new Dimension(640, 680));
        }
        public void propertyChange(PropertyChangeEvent evt) {
            String name = evt.getPropertyName();
            if ("ajdoc.good".equals(name)) {
                status.ajc.goods.inc();
            } else if ("ajc.good".equals(name)) {
                status.ajc.goods.inc();
            } else if ("run.good".equals(name)) {
                status.runs.goods.inc();
            }
            if ("ajdoc.done".equals(name)) {
                status.ajc.total.inc();
            } else if ("ajc.done".equals(name)) {
                status.ajc.total.inc();
            } else if ("run.done".equals(name)) {
                status.runs.total.inc();
            }
            if ("ajdoc.fail".equals(name)) {
                status.ajc.fails.inc();
            } else if ("ajc.fail".equals(name)) {
                status.ajc.fails.inc();
            } else if ("run.fail".equals(name)) {
                status.runs.fails.inc();
            }
        }

        private abstract static class TitledPanel extends JPanel {
            public TitledPanel(LayoutManager layout, String title) {
                super(layout);
                setBorder(BorderFactory.createTitledBorder(title));
            }
        }

        private static class StatusPanel extends TitledPanel {
            StatusInnerPanel ajdoc = new StatusInnerPanel("Ajdoc");
            StatusInnerPanel runs  = new StatusInnerPanel("Runs");
            StatusInnerPanel ajc   = new StatusInnerPanel("Ajc");

            public StatusPanel() {
                super(new FlowLayout(), "Status");
                add(ajdoc);
                add(runs);
                add(ajc);
            }

            private static class StatusInnerPanel extends TitledPanel {
                IntField goods = new IntField(5, Color.green.darker());
                IntField fails = new IntField(5, Color.red.darker());
                IntField total = new IntField(5, Color.blue.darker());
                public StatusInnerPanel(String str) {
                    super(null, str);
                    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                    Object[] os = new Object[] {
                        "Passed", goods,
                        "Failed", fails,
                        "Totals", total,
                    };
                    for (int i = 0; i < os.length; i += 2) {
                        JPanel p = p();
                        p.add(new JLabel(os[i]+":"));
                        p.add((Component)os[i+1]);
                        this.add(p);
                    }
                }

                private JPanel p() {
                    JPanel p = new JPanel();
                    p.setLayout(new FlowLayout(FlowLayout.LEFT));
                    return p;
                }

                private class IntField extends JTextField {
                    public IntField(int i, Color fg) {
                        super("0", i);
                        this.setBackground(StatusInnerPanel.this.getBackground());
                        this.setForeground(fg);
                        this.setEditable(false);
                        this.setBorder(BorderFactory.createEmptyBorder());
                    }
                    public void add(int i) {
                        setText((Integer.parseInt(getText().trim())+i)+"");
                    }
                    public void inc() { add(1); }
                }
            }
        }

        private class TablePanel extends TitledPanel {
            private DefaultTableModel model = new DefaultTableModel();
            private TJable table;
            private List failures = new Vector();
            public TablePanel() {
                super(new BorderLayout(), "Failures");
                Object[] names = new String[] {
                    "Task", "Type", "Number", "Time"
                };
				for (Object name : names) {
					model.addColumn(name);
				}
                table = new TJable(model, failures);
                this.add(new JScrollPane(table), BorderLayout.CENTER);
            }

            private class TJable extends JTable {
                private List list;
                public TJable(TableModel model, List list) {
                    super(model);
                    this.list = list;
                    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                }
                public void valueChanged(ListSelectionEvent e) {
                    super.valueChanged(e);
                    if (list == null) return;
                    int i = (e.getFirstIndex()-e.getLastIndex())/2;
                    if (list.size() > 0) {
                        Failure f = (Failure)list.get(i);
                        fail.setFailure(f);
                    }
                }
            }

            public void add(Failure f, String taskname, String type,
                            int num, long time) {
                model.addRow(new Object[]{taskname, type,
						num, date(time)});
                failures.add(f);
            }
        }

        private static class FailurePanel extends TitledPanel {
            private JTextArea msgs = new JTextArea(10,50);
            private InfoPanel info = new InfoPanel();

            public FailurePanel() {
                super(new BorderLayout(), "Failure");
                msgs.setFont(StyleContext.getDefaultStyleContext().
                             getFont("SansSerif", Font.PLAIN, 10));
                add(BorderLayout.NORTH, info);
                JScrollPane sc = new JScrollPane(msgs);
                sc.setBorder(BorderFactory.createTitledBorder("Messages"));
                add(BorderLayout.CENTER, sc);
            }

            public void setText(String str) {
                msgs.setText(str);
            }

            public void setFailure(Failure f) {
                msgs.setText(f.msgs);
                info.setText("Type"        , f.type);
                info.setText("Args"        , f.args);
                info.setText("Exit"        , f.exit+"");
                info.setText("Time"        , date(f.time));
                info.setText("Files"       , f.testset.files);
                info.setText("Classnames"  , f.testset.testclasses);
            }

            private static class InfoPanel extends JPanel {
                Map fields = new HashMap();
                public void setText(String key, Object str) {
                    ((JTextField)fields.get(key)).setText(str+"");
                }
                public InfoPanel() {
                    super(new GridBagLayout());
                    LabelFieldGBC gbc = new LabelFieldGBC();
                    Object[] os = new Object[] {
                        "Type",
                        "Args",
                        "Exit",
                        "Time",
                        "Files",
                        "Classnames",
                    };
					for (Object o : os) {
						String name = o + "";
						JLabel label = new JLabel(name + ":");
						JTextField comp = new JTextField(25);
						comp.setEditable(false);
						comp.setBackground(Color.white);
						comp.setBorder(BorderFactory.
								createBevelBorder(BevelBorder.LOWERED));
						label.setLabelFor(comp);
						fields.put(name, comp);
						add(label, gbc.forLabel());
						add(comp, gbc.forField());
					}
                    add(new JLabel(), gbc.forLastLabel());
                }
            }

            private static class LabelFieldGBC extends GridBagConstraints {
                public LabelFieldGBC() {
                    insets = new Insets(1,3,1,3);
                    gridy = RELATIVE;
                    gridheight = 1;
                    gridwidth = 1;
                }
                public LabelFieldGBC forLabel() {
                    fill = NONE;
                    gridx = 0;
                    anchor = NORTHEAST;
                    weightx = 0.0;
                    return this;
                }

                public LabelFieldGBC forLastLabel() {
                    forLabel();
                    fill = VERTICAL;
                    weighty = 1.0;
                    return this;
                }

                public LabelFieldGBC forField() {
                    fill = HORIZONTAL;
                    gridx = 1;
                    anchor = CENTER;
                    weightx = 1.0;
                    return this;
                }

                public LabelFieldGBC forLastField() {
                    forField();
                    fill = BOTH;
                    weighty = 1.0;
                    return this;
                }
            }
        }
    }

}
