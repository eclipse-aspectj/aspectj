/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.testing;

import org.aspectj.tools.ajc.AjcTestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Element that allow to run an abritrary Ant target in a sandbox.
 * <p/>
 * Such a spec is used in a "<ajc-test><ant file="myAnt.xml" [target="..."] [verbose="true"]/> XML element.
 * The "target" is optional. If not set, default myAnt.xml target is used.
 * The "file" file is looked up from the <ajc-test dir="..."/> attribute.
 * If "verbose" is set to "true", the ant -v output is piped, else nothing is reported except errors.
 * <p/>
 * The called Ant target benefits from 2 implicit variables:
 * "${aj.sandbox}" points to the test current sandbox folder.
 * "aj.path" is an Ant refid on the classpath formed with the sandbox folder + ltw + the AjcTestCase classpath
 * (ie usually aspectjrt, junit, and testing infra)
 * <p/>
 * Element "<stdout><line text="..">" and "<stderr><line text="..">" can be used. For now a full match
 * is performed on the output of the runned target only (not the whole Ant invocation). This is experimental
 * and advised to use a "<junit>" task instead or a "<java>" whose main that throws some exception upon failure.
 *
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AntSpec implements ITestStep {
    
    private final static String DEFAULT_LTW_CLASSPATH_ENTRIES =
            ".." + File.separator + "asm/bin"
            + File.pathSeparator + ".." + File.separator + "bridge/bin"
            + File.pathSeparator + ".." + File.separator + "loadtime/bin"
            + File.pathSeparator + ".." + File.separator + "loadtime5/bin"
            + File.pathSeparator + ".." + File.separator + "weaver/bin"
            + File.pathSeparator + ".." + File.separator + "lib/bcel/bcel.jar";


    private boolean m_verbose = false;
    private AjcTest m_ajcTest;
    private OutputSpec m_stdErrSpec;
    private OutputSpec m_stdOutSpec;
    private String m_antFile;
    private String m_antTarget;

    public void execute(AjcTestCase inTestCase) {
        final String failMessage = "test \"" + m_ajcTest.getTitle() + "\" failed: ";

        File buildFile = new File(m_ajcTest.getDir() + File.separatorChar + m_antFile);
        if (!buildFile.exists()) {
            AjcTestCase.fail(failMessage + "no such Ant file " + buildFile.getAbsolutePath());
        }
        Project p = new Project();
        final StringBuffer stdout = new StringBuffer();
        final StringBuffer stderr = new StringBuffer();
        try {
            // read the Ant file
            p.init();
            p.setUserProperty("ant.file", buildFile.getAbsolutePath());
            // setup aj.sandbox
            p.setUserProperty("aj.sandbox", inTestCase.getSandboxDirectory().getAbsolutePath());
            // setup aj.dir "modules" folder
            p.setUserProperty("aj.root", new File("..").getAbsolutePath());
			
            // create the test implicit path aj.path that contains the sandbox + regular test infra path
            Path path = new Path(p, inTestCase.getSandboxDirectory().getAbsolutePath());
            populatePath(path, DEFAULT_LTW_CLASSPATH_ENTRIES);
            populatePath(path, AjcTestCase.DEFAULT_CLASSPATH_ENTRIES);
            p.addReference("aj.path", path);

            ProjectHelper helper = ProjectHelper.getProjectHelper();
            helper.parse(p, buildFile);

            // use default target if no target specified
            if (m_antTarget == null) {
                m_antTarget = p.getDefaultTarget();
            }

            // make sure we listen for failure
            DefaultLogger consoleLogger = new DefaultLogger() {
                public void buildFinished(BuildEvent event) {
                    super.buildFinished(event);
                    if (event.getException() != null) {
                        AjcTestCase.fail(failMessage + "failure " + event.getException());
                    }
                }
                public void targetFinished(BuildEvent event) {
                    super.targetFinished(event);
                    if (event.getException() != null) {
                        AjcTestCase.fail(failMessage + "failure in '" + event.getTarget() + "' " + event.getException());
                    }
                }
                public void messageLogged(BuildEvent event) {
                    super.messageLogged(event);
                    if (event.getTarget() != null && event.getPriority() >= Project.MSG_INFO && m_antTarget.equals(event.getTarget().getName())) {
                        if (event.getException() == null) {
                            stdout.append(event.getMessage()).append('\n');
                        } else {
                            stderr.append(event.getMessage());
                        }
                    }
                }
            };
            consoleLogger.setErrorPrintStream(System.err);
            consoleLogger.setOutputPrintStream(System.out);
            consoleLogger.setMessageOutputLevel(m_verbose?Project.MSG_VERBOSE:Project.MSG_ERR);
            p.addBuildListener(consoleLogger);
        } catch (Throwable t) {
            AjcTestCase.fail(failMessage + "invalid Ant script :" + t.toString());
        }
        try {
            p.fireBuildStarted();
            p.executeTarget(m_antTarget);
            p.fireBuildFinished(null);
        } catch (BuildException e) {
            p.fireBuildFinished(e);
        } catch (Throwable t) {
            AjcTestCase.fail(failMessage + "error when invoking target :" + t.toString());
        }

        // match lines
        //TODO AV experimental: requires full match so rather useless.
        // if someone needs it, we ll have to impl a "include / exclude" stuff
        if (m_stdOutSpec != null)
            m_stdOutSpec.matchAgainst(stdout.toString());
        // match lines
        if (m_stdErrSpec != null)
            m_stdErrSpec.matchAgainst(stdout.toString());
    }

    public void addStdErrSpec(OutputSpec spec) {
        if (m_stdErrSpec!=null)
            throw new UnsupportedOperationException("only one 'stderr' allowed in 'ant'");
        m_stdErrSpec = spec;
    }

    public void addStdOutSpec(OutputSpec spec) {
        if (m_stdOutSpec!=null)
            throw new UnsupportedOperationException("only one 'stdout' allowed in 'ant'");
        m_stdOutSpec = spec;
    }

    public void setVerbose(String verbose) {
        if (verbose != null && "true".equalsIgnoreCase(verbose)) {
            m_verbose = true;
        }
    }

    public void setFile(String file) {
        m_antFile = file;
    }

    public void setTarget(String target) {
        m_antTarget = target;
    }

    public void addExpectedMessage(ExpectedMessageSpec message) {
        throw new UnsupportedOperationException("don't use 'message' in 'ant' specs.");
    }

    public void setBaseDir(String dir) {
        ;
    }

    public void setTest(AjcTest test) {
        m_ajcTest = test;
    }

    private static void populatePath(Path path, String pathEntries) {
        StringTokenizer st = new StringTokenizer(pathEntries, File.pathSeparator);
        while (st.hasMoreTokens()) {
            path.setPath(new File(st.nextToken()).getAbsolutePath());
        }
    }
}
