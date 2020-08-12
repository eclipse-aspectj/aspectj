/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation
 *     Helen Hawkins  Converted to new interface (bug 148190)   
 * ******************************************************************/

package org.aspectj.testing.ajde;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajde.core.IBuildMessageHandler;
import org.aspectj.ajde.core.IBuildProgressMonitor;
import org.aspectj.ajde.core.ICompilerConfiguration;
import org.aspectj.ajde.core.IOutputLocationManager;
import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.harness.bridge.Globals;
import org.aspectj.util.FileUtil;

/**
 * This re-uses the same config file to setup ajde so that recompiles appear to be of the same configuration.
 * 
 * @since Java 1.3 (uses dynamic proxies)
 */
public class CompileCommand implements ICommand {
	// time out waiting for build at three minutes
	long MAX_TIME = 180 * 1000;
	// this proxy ignores calls
	InvocationHandler proxy = new VoidInvocationHandler();
	InvocationHandler loggingProxy = new LoggingInvocationHandler();
	MyMessageHandler myHandler = new MyMessageHandler();
	long endTime;
	boolean buildNextFresh;
	File tempDir;

	private AjCompiler compiler;

	/**
	 * Clients call this before repeatCommand as a one-shot request for a full rebuild of the same configuration. (Requires a
	 * downcast from ICommand to CompileCommand.)
	 */
	public void buildNextFresh() {
		buildNextFresh = true;
	}

	// --------- ICommand interface
	public boolean runCommand(String[] args, IMessageHandler handler) {
		setup(args);
		myHandler.start();
		long startTime = System.currentTimeMillis();
		try {
			compiler.buildFresh();
		} finally {
			runCommandCleanup();
		}
		return !myHandler.hasError();
	}

	public boolean repeatCommand(IMessageHandler handler) {
		myHandler.start();
		long startTime = System.currentTimeMillis();
		// System.err.println("recompiling...");
		if (buildNextFresh) {
			buildNextFresh = false;
			compiler.buildFresh();
		} else {
			compiler.build();
		}
		return !myHandler.hasError();
	}

	void runCommandCleanup() {
		if (null != tempDir) {
			FileUtil.deleteContents(tempDir);
			tempDir.delete();
		}
	}

	// set by build progress monitor when done
	void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	private void setup(String[] args) {
		File config = writeConfig(args);
		if (null == config) {
			throw new Error("unable to write config file");
		}
		IBuildProgressMonitor buildProgressMonitor = new MyBuildProgressMonitor();
		String classesDir = "../testing/bin/classes";
		for (int i = 0; i < args.length; i++) {
			if ("-d".equals(args[i]) && ((1 + i) < args.length)) {
				classesDir = args[1 + i];
				break;
			}
		}
		MyCompilerConfig compilerConfig = new MyCompilerConfig();
		compiler = new AjCompiler("blah", compilerConfig, buildProgressMonitor, myHandler);
	}

	private File writeConfig(String[] args) {
		tempDir = FileUtil.getTempDir("CompileCommand");
		File result = new File(tempDir, "config.lst");
		OutputStream out = null;
		try {
			out = new FileOutputStream(result);
			PrintStream outs = new PrintStream(out, true);
			for (String arg : args) {
				outs.println(arg);
			}
			return result;
		} catch (IOException e) {
			return null;
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	// private Object makeLoggingProxy(Class interfac) {
	// return Proxy.newProxyInstance(
	// interfac.getClassLoader(),
	// new Class[] { interfac },
	// loggingProxy);
	// }

	private Object makeProxy(Class interfac) {
		return Proxy.newProxyInstance(interfac.getClassLoader(), new Class[] { interfac }, proxy);
	}
}

class MyMessageHandler implements IBuildMessageHandler {

	boolean hasError;
	boolean hasWarning;

	private MessageHandler messageHandler = new MessageHandler(false);

	public boolean handleMessage(IMessage message) throws AbortException {
		maintainHasWarning(message.getKind());
		return messageHandler.handleMessage(message);
	}

	private void maintainHasWarning(IMessage.Kind kind) {
		if (!hasError) {
			if (IMessage.ERROR.isSameOrLessThan(kind)) {
				hasError = true;
				hasWarning = true;
			}
		}
		if (!hasWarning && IMessage.WARNING.isSameOrLessThan(kind)) {
			hasWarning = true;
		}
	}

	public boolean hasWarning() {
		return hasWarning;
	}

	public boolean hasError() {
		return hasError;
	}

	public void start() {
		hasWarning = false;
		hasError = false;
		messageHandler.init(true);
	}

	public void dontIgnore(Kind kind) {
		messageHandler.dontIgnore(kind);
	}

	public void ignore(Kind kind) {
		messageHandler.ignore(kind);
	}

	public boolean isIgnoring(Kind kind) {
		return messageHandler.isIgnoring(kind);
	}

}

class MyBuildProgressMonitor implements IBuildProgressMonitor {

	public void begin() {
	}

	public void finish(boolean wasFullBuild) {
	}

	public boolean isCancelRequested() {
		return false;
	}

	public void setProgress(double percentDone) {
	}

	public void setProgressText(String text) {
	}

}

class VoidInvocationHandler implements InvocationHandler {
	public Object invoke(Object me, Method method, Object[] args) throws Throwable {
		// System.err.println("Proxying"
		// // don't call toString on self b/c proxied
		// // + " me=" + me.getClass().getName()
		// + " method=" + method
		// + " args=" + (LangUtil.isEmpty(args)
		// ? "[]" : Arrays.asList(args).toString()));
		return null;
	}
}

class LoggingInvocationHandler implements InvocationHandler {
	public Object invoke(Object me, Method method, Object[] args) throws Throwable {
		System.err.println("Proxying " + render(method, args));
		return null;
	}

	public static String render(Class c) {
		if (null == c) {
			return "(Class) null";
		}
		String result = c.getName();
		if (result.startsWith("java")) {
			int loc = result.lastIndexOf(".");
			if (-1 != loc) {
				result = result.substring(loc + 1);
			}
		}
		return result;
	}

	public static String render(Method method, Object[] args) {
		StringBuffer sb = new StringBuffer();
		sb.append(render(method.getReturnType()));
		sb.append(" ");
		sb.append(method.getName());
		sb.append("(");
		Class[] parmTypes = method.getParameterTypes();
		int parmTypesLength = (null == parmTypes ? 0 : parmTypes.length);
		int argsLength = (null == args ? 0 : args.length);
		boolean doType = (parmTypesLength == argsLength);
		for (int i = 0; i < argsLength; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			if (doType) {
				sb.append("(");
				sb.append(render(parmTypes[i]));
				sb.append(") ");
			}
			if (null == args[i]) {
				sb.append("null");
			} else { // also don't recurse into proxied toString?
				sb.append(args[i].toString());
			}
		}
		sb.append(")");
		return sb.toString();
	}
}

class MyCompilerConfig implements ICompilerConfiguration {

	private Set inpath;
	private Set aspectPath;
	private String outJar;
	private IOutputLocationManager locationMgr;

	public Set getAspectPath() {
		return aspectPath;
	}

	public void setAspectPath(Set path) {
		aspectPath = path;
	}

	public String getClasspath() {
		return Globals.S_aspectjrt_jar;
	}

	public Set getInpath() {
		return inpath;
	}

	public void setInpath(Set input) {
		inpath = input;
	}

	public Map getJavaOptionsMap() {
		return JavaOptions.getDefaultJavaOptions();
	}

	public List getProjectXmlConfigFiles() {
		return Collections.EMPTY_LIST;
	}

	public String getOutJar() {
		return outJar;
	}

	public void configurationRead() {
	}

	public void setOutJar(String input) {
		outJar = input;
	}

	public IOutputLocationManager getOutputLocationManager() {
		if (locationMgr == null) {
			locationMgr = new MyOutputLocationManager();
		}
		return locationMgr;
	}

	public String getNonStandardOptions() {
		return null;
	}

	public List getProjectSourceFiles() {
		return null;
	}

	public List getProjectSourceFilesChanged() {
		return null;
	}

	public Map getSourcePathResources() {
		return null;
	}

	public int getConfigurationChanges() {
		return ICompilerConfiguration.EVERYTHING;
	}

	public List getClasspathElementsWithModifiedContents() {
		return null;
	}

	public String getProjectEncoding() {
		return null;
	}

	public String getProcessor() {
		return null;
	}

	public String getProcessorPath() {
		return null;
	}

	@Override
	public String getModulepath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getModuleSourcepath() {
		// TODO Auto-generated method stub
		return null;
	}

}

class MyOutputLocationManager implements IOutputLocationManager {

	public List getAllOutputLocations() {
		return null;
	}

	public File getDefaultOutputLocation() {
		return null;
	}

	public File getOutputLocationForClass(File compilationUnit) {
		return null;
	}

	public File getOutputLocationForResource(File resource) {
		return null;
	}

	public String getUniqueIdentifier() {
		return null;
	}

	public Map getInpathMap() {
		return Collections.EMPTY_MAP;
	}

	public String getSourceFolderForFile(File sourceFile) {
		return null;
	}

	public void reportFileWrite(String outputfile, int filetype) {
	}

	public void reportFileRemove(String outputfile, int filetype) {
	}

	public int discoverChangesSince(File dir, long buildtime) {
		return 0;
	}

	public String getProjectEncoding() {
		return null;
	}

}
