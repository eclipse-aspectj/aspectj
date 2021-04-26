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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.aspectj.tools.ajc.AjcTestCase;

import static org.aspectj.util.LangUtil.is16VMOrGreater;

/**
 * Element that allow to run an abritrary Ant target in a sandbox.
 * <p/>
 * Such a spec is used in a {@code <ajc-test><ant file="myAnt.xml" [target="..."] [verbose="true"]/>} XML element. The
 * {@code target} is optional. If not set, default <i>myAnt.xml</i> target is used. The {@code file} is looked up from
 * the {@code <ajc-test dir="..."/>} attribute. If @{code verbose} is set to {@code true}, the {@code ant -v} output is
 * piped, else nothing is reported except errors.
 * <p/>
 * The called Ant target benefits from some implicit variables:
 * <ul>
 *   <li>{@code ${aj.sandbox}} points to the test current sandbox folder.</li>
 *   <li>
 *     {@code ${aj.path}} is an Ant refid on the classpath formed with the sandbox folder + ltw + the AjcTestCase
 *     classpath (i.e. usually aspectjrt, junit, and testing infra).
 *   </li>
 *   <li>
 *     For Java 16+, {@code ${aj.addOpensKey}} and {@code ${aj.addOpensValue}} together add {@code --add-opens} and
 *     {@code java.base/java.lang=ALL-UNNAMED} as JVM parameters. They have to be used together and consecutively in
 *     this order as {@code jvmarg} parameter tags inside the {@code java} Ant task.
 *   </li>
 * </ul>
 * <p/>
 * Element {@code <stdout><line text="..">} and {@code <stderr><line text="..">} can be used. For now, a full match is
 * performed on the output of the runned target only (not the whole Ant invocation). This is experimental and you are
 * advised to use a {@code <junit>} task instead or a {@code <java>} whose main throws some exception upon failure.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AntSpec implements ITestStep {

	public static String outputFolders(String... modules) {
		StringBuilder s = new StringBuilder();
		for (String module: modules) {
			s.append(File.pathSeparator + ".." +File.separator + module + File.separator + "target" + File.separator + "classes");
		}
		return s.toString();
	}


	// ALSO SEE AJC
	private final static String DEFAULT_LTW_CLASSPATH_ENTRIES =
			outputFolders("asm", "bridge", "loadtime", "weaver", "org.aspectj.matcher", "bcel-builder");

	private boolean m_verbose = false;
	private AjcTest m_ajcTest;
	private OutputSpec m_stdErrSpec;
	private OutputSpec m_stdOutSpec;
	private String m_antFile;
	private String m_antTarget;

	public void execute(final AjcTestCase inTestCase) {
		final String failMessage = "test \"" + m_ajcTest.getTitle() + "\" failed: ";

		File buildFile = new File(m_ajcTest.getDir() + File.separatorChar + m_antFile);
		if (!buildFile.exists()) {
			AjcTestCase.fail(failMessage + "no such Ant file " + buildFile.getAbsolutePath());
		}
		Project p = new Project();
		final StringBuffer stdout = new StringBuffer();
		final StringBuffer stderr = new StringBuffer();
		final StringBuffer verboseLog = new StringBuffer();
		try {
			// read the Ant file
			p.init();
			p.setUserProperty("ant.file", buildFile.getAbsolutePath());
			// setup aj.sandbox
			p.setUserProperty("aj.sandbox", inTestCase.getSandboxDirectory().getAbsolutePath());
			// setup aj.dir "modules" folder
			p.setUserProperty("aj.root", new File("..").getAbsolutePath());

			// On Java 16+, LTW no longer works without this parameter. Add the argument here and not in AjcTestCase::run,
			// because even if 'useLTW' and 'useFullLTW' are not set, we might in the future have tests for weaver attachment
			// during runtime. See also docs/dist/doc/README-187.html.
			//
			// Attention: Ant 1.6.3 under Linux neither likes "" (empty string) nor " " (space), on Windows it would not be
			// a problem. So we use "_dummy" Java system properties, even though they pollute the command line.
			p.setUserProperty("aj.addOpensKey", is16VMOrGreater() ? "--add-opens" : "-D_dummy");
			p.setUserProperty("aj.addOpensValue", is16VMOrGreater() ? "java.base/java.lang=ALL-UNNAMED" : "-D_dummy");

			// create the test implicit path aj.path that contains the sandbox + regular test infra path
			Path path = new Path(p, inTestCase.getSandboxDirectory().getAbsolutePath());
			populatePath(path, DEFAULT_LTW_CLASSPATH_ENTRIES);
			populatePath(path, AjcTestCase.DEFAULT_CLASSPATH_ENTRIES);
			p.addReference("aj.path", path);
			p.setBasedir(buildFile.getAbsoluteFile().getParent());
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

						try {
							File antout = new File(inTestCase.getSandboxDirectory().getAbsolutePath(), "antout");
							if (antout.exists()) {
								stdout.append("Forked java command stdout:\n");
								System.out.println("Forked java command stdout:");
								FileReader fr = new FileReader(antout);
								BufferedReader br = new BufferedReader(fr);
								String line = br.readLine();
								while (line != null) {
									stdout.append(line).append("\n");
									System.out.println(stdout);
									line = br.readLine();
								}
								fr.close();
							}

							File anterr = new File(inTestCase.getSandboxDirectory().getAbsolutePath(), "anterr");
							if (anterr.exists()) {
								stdout.append("Forked java command stderr:\n");
								System.out.println("Forked java command stderr:");
								FileReader fr = new FileReader(anterr);
								BufferedReader br = new BufferedReader(fr);
								String line = br.readLine();
								while (line != null) {
									stdout.append(line).append("\n");
									System.out.println(stdout);
									line = br.readLine();
								}
								fr.close();
							}
						} catch (Exception e) {
							String exceptionMessage = "Exception whilst loading forked java task output " + e.getMessage() + "\n";
							System.out.println(exceptionMessage);
							e.printStackTrace();
							stdout.append(exceptionMessage);
						}

						// AjcTestCase.fail(failMessage + "failure " + event.getException());
						AjcTestCase.fail(event.getException() + "\n" + verboseLog + stdout + stderr);
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

					Target target = event.getTarget();
					if (target != null && m_antTarget.equals(target.getName()) && event.getSource() instanceof Java)
						switch (event.getPriority()) {
						case Project.MSG_INFO:
							stdout.append(event.getMessage()).append('\n');
							break;
						case Project.MSG_WARN:
							stderr.append(event.getMessage()).append('\n');
							break;
						case Project.MSG_VERBOSE:
							verboseLog.append(event.getMessage()).append('\n');
							break;
						}
				}
			};
			consoleLogger.setErrorPrintStream(System.err);
			consoleLogger.setOutputPrintStream(System.out);
			consoleLogger.setMessageOutputLevel(m_verbose ? Project.MSG_VERBOSE : Project.MSG_ERR);
			p.addBuildListener(consoleLogger);
		} catch (Throwable t) {
			AjcTestCase.fail(failMessage + "invalid Ant script :" + t.toString());
		}
		try {
			p.setProperty("verbose", "true");
			p.fireBuildStarted();
			p.executeTarget(m_antTarget);
			p.fireBuildFinished(null);
		} catch (BuildException e) {
			p.fireBuildFinished(e);
		} catch (Throwable t) {
			AjcTestCase.fail(failMessage + "error when invoking target :" + t.toString());
		}

		/* See if stdout/stderr matches test specification */
		if (m_stdOutSpec != null) {
			m_stdOutSpec.matchAgainst(stdout.toString());
		}
		if (m_stdErrSpec != null) {
			String stderr2 = stderr.toString();
			// Working around this ridiculous message that still comes out of Java7 builds:
			if (stderr2.contains("Class JavaLaunchHelper is implemented in both") && stderr2.indexOf('\n')!=-1) {
				stderr2 = stderr2.replaceAll("objc\\[[0-9]*\\]: Class JavaLaunchHelper is implemented in both [^\n]*\n","");
			}
			// JDK 11 is complaining about illegal reflective calls - temporary measure ignore these - does that get all tests passing and this is the last problem?
			if (stderr2.contains("WARNING: Illegal reflective access using Lookup on org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor")) {
//				WARNING: An illegal reflective access operation has occurred
//				WARNING: Illegal reflective access using Lookup on org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor (file:/Users/aclement/gits/org.aspectj/loadtime/bin/) to class java.lang.ClassLoader
//				WARNING: Please consider reporting this to the maintainers of org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor
//				WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
//				WARNING: All illegal access operations will be denied in a future release

				stderr2 = stderr2.replaceAll("WARNING: An illegal reflective access operation has occurred\n","");
				stderr2 = stderr2.replaceAll("WARNING: Illegal reflective access using Lookup on org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor[^\n]*\n","");
				stderr2 = stderr2.replaceAll("WARNING: Please consider reporting this to the maintainers of org.aspectj.weaver.loadtime.ClassLoaderWeavingAdaptor\n","");
				stderr2 = stderr2.replaceAll("WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations\n","");
				stderr2 = stderr2.replaceAll("WARNING: All illegal access operations will be denied in a future release\n","");
			}
			// J12: Line can start with e.g."OpenJDK 64-Bit Server VM" or "Java HotSpot(TM) 64-Bit Server VM". Therefore,
			// we have to match a substring instead of a whole line
			stderr2 = stderr2.replaceAll("[^\n]+ warning: Archived non-system classes are disabled because the java.system.class.loader property is specified .*org.aspectj.weaver.loadtime.WeavingURLClassLoader[^\n]+\n?","");
			m_stdErrSpec.matchAgainst(stderr2);
		}
	}

	public void addStdErrSpec(OutputSpec spec) {
		if (m_stdErrSpec != null)
			throw new UnsupportedOperationException("only one 'stderr' allowed in 'ant'");
		m_stdErrSpec = spec;
	}

	public void addStdOutSpec(OutputSpec spec) {
		if (m_stdOutSpec != null)
			throw new UnsupportedOperationException("only one 'stdout' allowed in 'ant'");
		m_stdOutSpec = spec;
	}

	public void setVerbose(String verbose) {
		if ("true".equalsIgnoreCase(verbose)) {
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
