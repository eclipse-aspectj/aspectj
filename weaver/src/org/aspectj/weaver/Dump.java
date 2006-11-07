/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Matthew Webster 
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.Version;
import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;
import org.aspectj.weaver.tools.Traceable;

/**
 * @author websterm
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Dump {

	public final static String DUMP_CONDITION_PROPERTY = "org.aspectj.weaver.Dump.condition";
	public final static String DUMP_DIRECTORY_PROPERTY = "org.aspectj.dump.directory";

	/* Format for unique filename based on date & time */
	private static final String FILENAME_PREFIX = "ajcore";
//	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd"); 
//	private static final DateFormat timeFormat = new SimpleDateFormat("HHmmss.SSS"); 
	private static final String FILENAME_SUFFIX = "txt";
	
	public static final String UNKNOWN_FILENAME = "Unknown";
	public static final String DUMP_EXCLUDED = "Excluded";
	public static final String NULL_OR_EMPTY = "Empty";
	
	private static Class exceptionClass;
	private static IMessage.Kind conditionKind = IMessage.ABORT;
	private static File directory = new File(".");

	private String reason;
	private String fileName;
	private PrintStream print;
	
	private static String[] savedCommandLine;
	private static List savedFullClasspath;
	private static IMessageHolder savedMessageHolder;
	
	private static Map nodes = new WeakHashMap();
	private static String lastDumpFileName = UNKNOWN_FILENAME;
	
	private static boolean preserveOnNextReset = false;
	
	private static Trace trace = TraceFactory.getTraceFactory().getTrace(Dump.class);
	
	/**
	 * for testing only, so that we can verify dump contents
	 * after compilation has completely finished
	 */
	public static void preserveOnNextReset() {
		preserveOnNextReset = true;		
	}
	
	public static void reset() {
		if (preserveOnNextReset) {
			preserveOnNextReset = false;
			return;
		}
		else {
			nodes.clear();
			savedMessageHolder = null;
		}
	}
	
	
	/*
	 * Dump methods
	 */
	public static String dump (String reason) {
		String fileName = UNKNOWN_FILENAME;
		Dump dump = null;
		try {
			dump = new Dump(reason);
			fileName = dump.getFileName();
			dump.dumpDefault();
		}
		finally {
			if (dump != null) dump.close();
		}
		return fileName;
	}
	
	public static String dumpWithException (Throwable th) {
		return dumpWithException(savedMessageHolder,th);
	}
	
	public static String dumpWithException (IMessageHolder messageHolder, Throwable th) {
		if (trace.isTraceEnabled()) trace.enter("dumpWithException",null,new Object[] {messageHolder, th});

		String fileName = UNKNOWN_FILENAME;
		Dump dump = null;
		try {
			dump = new Dump(th.getClass().getName());
			fileName = dump.getFileName();
			dump.dumpException(messageHolder,th);
		}
		finally {
			if (dump != null) dump.close();
		}
		
		if (trace.isTraceEnabled()) trace.exit("dumpWithException",fileName);
		return fileName;
	}

	public static String dumpOnExit () {
		return dumpOnExit(savedMessageHolder, false);
	}

	public static String dumpOnExit (IMessageHolder messageHolder, boolean reset) {
		if (trace.isTraceEnabled()) trace.enter("dumpOnExit",null,messageHolder);
		String fileName = UNKNOWN_FILENAME;
	
		if (!shouldDumpOnExit(messageHolder)) {
			fileName = DUMP_EXCLUDED;
		}
		else {
			Dump dump = null;
			try {
				dump = new Dump(conditionKind.toString());
				fileName = dump.getFileName();
				dump.dumpDefault(messageHolder);
			}
			finally {
				if (dump != null) dump.close();
			}
		}
		
		if (reset) messageHolder.clearMessages();

		if (trace.isTraceEnabled()) trace.exit("dumpOnExit",fileName);
		return fileName;
	}

	private static boolean shouldDumpOnExit (IMessageHolder messageHolder) {
		if (trace.isTraceEnabled()) trace.enter("shouldDumpOnExit",null,messageHolder);
		if (trace.isTraceEnabled()) trace.event("shouldDumpOnExit",null,conditionKind);
		boolean result = (messageHolder == null) || messageHolder.hasAnyMessage(conditionKind,true);
		
		if (trace.isTraceEnabled()) trace.exit("shouldDumpOnExit",result);
		return result;
	}

	/*
	 * Dump configuration
	 */
	public static void setDumpOnException (boolean b) {
		if (b) {
			exceptionClass = java.lang.Throwable.class;
		}
		else {
			exceptionClass = null;
		}
	}
	
	public static boolean setDumpDirectory (String directoryName) {
		if (trace.isTraceEnabled()) trace.enter("setDumpDirectory",null,directoryName);
		boolean success = false;

		File newDirectory = new File(directoryName);
		if (newDirectory.exists()) {
			directory = newDirectory;
			success = true;
		}
		
		if (trace.isTraceEnabled()) trace.exit("setDumpDirectory",success);
		return success;
		
	}
	
	public static boolean getDumpOnException () {
		return (exceptionClass != null);
	}

	public static boolean setDumpOnExit (IMessage.Kind condition) {
		if (trace.isTraceEnabled()) trace.event("setDumpOnExit",null,condition);

		conditionKind = condition;
		return true;
	}

	public static boolean setDumpOnExit (String condition) {
		for (Iterator i = IMessage.KINDS.iterator(); i.hasNext();) {
			IMessage.Kind kind = (IMessage.Kind)i.next();
			if (kind.toString().equals(condition)) {
				return setDumpOnExit(kind);
			}
		}
		return false;
	}
	
	public static IMessage.Kind getDumpOnExit () {
		return conditionKind; 
	}
	
	public static String getLastDumpFileName () {
		return lastDumpFileName;
	}

	/*
	 * Dump registration
	 */
	public static void saveCommandLine (String[] args) {
		savedCommandLine = new String[args.length];
		System.arraycopy(args,0,savedCommandLine,0,args.length); 
	}

	public static void saveFullClasspath (List list) {
		savedFullClasspath = list;
	}

	public static void saveMessageHolder (IMessageHolder holder) {
		savedMessageHolder = holder;
	}

	public static void registerNode (Class module, INode newNode) {
		if (trace.isTraceEnabled()) trace.enter("registerNode",null,new Object[] { module, newNode} );

		nodes.put(newNode,new WeakReference(newNode));

		if (trace.isTraceEnabled()) trace.exit("registerNode",nodes.size());
	}
	
	/*
	 * Dump methods
	 */
	private Dump (String reason) {
		if (trace.isTraceEnabled()) trace.enter("<init>",this,reason);

		this.reason = reason;
		
		openDump();
		dumpAspectJProperties();
		dumpDumpConfiguration();
		
		if (trace.isTraceEnabled()) trace.exit("<init>",this);
	}

	public String getFileName() {
		return fileName;
	}
	
	private void dumpDefault () {
		dumpDefault(savedMessageHolder);
	}
		
	private void dumpDefault (IMessageHolder holder) {
		dumpSytemProperties();
		dumpCommandLine();
		dumpFullClasspath();
		dumpCompilerMessages(holder);
		
		dumpNodes();
	}

	private void dumpNodes() {

		/*
		 * Dump registered nodes
		 */
		IVisitor dumpVisitor = new IVisitor() {

			public void visitObject (Object obj) {
				println(formatObj(obj));
			}

			public void visitList (List list) {
				println(list);
			}
		};
		Set keys = nodes.keySet();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			Object module = i.next();
//			INode dumpNode = (INode)nodes.get(module);
			INode dumpNode = (INode)module;
			println("---- " + formatObj(dumpNode) + " ----");
			try {
				dumpNode.accept(dumpVisitor); 
			}
			catch (Exception ex) {
				trace.error(formatObj(dumpNode).toString(),ex);
			}
		}
	}
	
	private void dumpException (IMessageHolder messageHolder, Throwable th) {
		println("---- Exception Information ---");
		println(th);
		dumpDefault(messageHolder);
	}
	
	private void dumpAspectJProperties () {
		println("---- AspectJ Properties ---");
		println("AspectJ Compiler " + Version.text + " built on " + Version.time_text);
	}
	
	private void dumpDumpConfiguration () {
		println("---- Dump Properties ---");
		println("Dump file: " + fileName);
		println("Dump reason: " + reason);
		println("Dump on exception: " + (exceptionClass != null));
		println("Dump at exit condition: " + conditionKind);
	}
	
	private void dumpFullClasspath () {
		println("---- Full Classpath ---");
		if (savedFullClasspath != null && savedFullClasspath.size() > 0) {
			for (Iterator iter = savedFullClasspath.iterator(); iter.hasNext(); ) {
				String fileName = (String)iter.next();
				File file = new File(fileName);
				println(file);
			}
		}
		else {
			println(NULL_OR_EMPTY);
		}
	}
	
	private void dumpSytemProperties () {
		println("---- System Properties ---");
		Properties props = System.getProperties();
		println(props);
	}
	
	private void dumpCommandLine () {
		println("---- Command Line ---");
		println(savedCommandLine);
	}
	
	private void dumpCompilerMessages (IMessageHolder messageHolder) {
		println("---- Compiler Messages ---");
		if (messageHolder != null) for (Iterator i = messageHolder.getUnmodifiableListView().iterator(); i.hasNext(); ) {
			IMessage message = (IMessage)i.next();
			println(message.toString());
		}
		else {
			println(NULL_OR_EMPTY);
		}
	}

	/*
	 * Dump output
	 */	
	private void openDump () {
		if (print != null) return;
		
		Date now = new Date();
		fileName = FILENAME_PREFIX + "."
			+ new SimpleDateFormat("yyyyMMdd").format(now) + "."
			+ new SimpleDateFormat("HHmmss.SSS").format(now) + "."
			+ FILENAME_SUFFIX;
		try {
			File file = new File(directory,fileName);
			print = new PrintStream(new FileOutputStream(file),true);
			trace.info("Dumping to " + file.getAbsolutePath());
		}
		catch (Exception ex) {
			print = System.err;
			trace.info("Dumping to stderr");
			fileName = UNKNOWN_FILENAME;
		}
		
		lastDumpFileName = fileName;
	}
	
	public void close () {
		print.close();
	}
	
	private void println (Object obj) {
		print.println(obj);
	}
	
	private void println (Object[] array) {
		if (array == null) {
			println(NULL_OR_EMPTY);
			return;
		}
		
		for (int i = 0; i < array.length; i++) {
			print.println(array[i]);
		}
	}
	
	private void println (Properties props) {
		Iterator iter = props.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String)iter.next();
			String value = props.getProperty(key);
			print.println(key + "=" + value);
		}
	}
	
	private void println (Throwable th) {
		th.printStackTrace(print);
	}
	
	private void println (File file) {
		print.print(file.getAbsolutePath());
		if (!file.exists()) {
			println("(missing)");			
		}
		else if (file.isDirectory()) {
			int count = file.listFiles().length;
			println("(" + count + " entries)");			
		}
		else {
			println("(" + file.length() + " bytes)");			
		}
	}
	
	private void println (List list) {
		if (list == null || list.isEmpty()) println(NULL_OR_EMPTY);
		else for (Iterator i = list.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof Exception) {
                println((Exception)o);
            } else {
            	println(o.toString());
            }
		}
	}
	
	private static Object formatObj(Object obj) {
		
		/* These classes have a safe implementation of toString() */
		if (obj == null
				|| obj instanceof String
			    || obj instanceof Number
			    || obj instanceof Boolean
			    || obj instanceof Exception
			    || obj instanceof Character
			    || obj instanceof Class
			    || obj instanceof File
			    || obj instanceof StringBuffer
			    || obj instanceof URL
		    ) return obj;
		else try {
			
			/* Classes can provide an alternative implementation of toString() */
			if (obj instanceof Traceable) {
				Traceable t = (Traceable)obj;
				return t.toTraceString();
			}
			
			/* Use classname@hashcode */
			else return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
		
		/* Object.hashCode() can be override and may thow an exception */	
		} catch (Exception ex) {
			return obj.getClass().getName() + "@FFFFFFFF";
		}
	}
	
	static {
		String exceptionName = System.getProperty("org.aspectj.weaver.Dump.exception","true");
		if (!exceptionName.equals("false")) setDumpOnException(true);
		
		String conditionName = System.getProperty(DUMP_CONDITION_PROPERTY);
		if (conditionName != null) setDumpOnExit(conditionName);
		
		String directoryName = System.getProperty(DUMP_DIRECTORY_PROPERTY);
		if (directoryName != null) setDumpDirectory(directoryName);
	}

	public interface INode {
		
		public void accept (IVisitor visior);
		
	}

	public interface IVisitor {
		
		public void visitObject (Object s);
		public void visitList (List list);
	}

}
