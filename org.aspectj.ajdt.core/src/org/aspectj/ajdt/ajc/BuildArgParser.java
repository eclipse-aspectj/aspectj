/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/



package org.aspectj.ajdt.ajc;

import java.io.*;
import java.util.*;
import org.aspectj.ajdt.internal.core.builder.*;
import org.aspectj.weaver.bcel.*;
import org.apache.bcel.util.ClassPath;
import org.aspectj.bridge.*;
import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.util.*;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.batch.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.core.util.*;
import org.aspectj.ajdt.internal.core.builder.AjCompilerOptions;
import org.aspectj.util.ConfigParser;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class BuildArgParser extends org.eclipse.jdt.internal.compiler.batch.Main {

	private static final String BUNDLE_NAME = "org.aspectj.ajdt.ajc.messages";
    
    /** to initialize super's PrintWriter but refer to underlying StringWriter */
    private static class StringPrintWriter extends PrintWriter {
        public final StringWriter stringWriter;
        StringPrintWriter(StringWriter sw) {
          super(sw);
          this.stringWriter = sw;
        }
    }
    
    /** 
     * StringWriter sink for some errors.
     * This only captures errors not handled by any IMessageHandler parameter
     * and only when no PrintWriter is set in the constructor.
     * XXX This relies on (Sun's) implementation of StringWriter, 
     * which returns the actual (not copy) internal StringBuffer.
     */
    private final StringBuffer errorSink;
    
	/**
	 * Overrides super's bundle.
	 */
	public BuildArgParser(PrintWriter writer) {
		super(writer, false);
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        if (writer instanceof StringPrintWriter) {
            errorSink = ((StringPrintWriter) writer).stringWriter.getBuffer();
        } else {
            errorSink = null;
        }
	}

    /** Set up to capture messages using getOtherMessages(boolean) */
	public BuildArgParser() { 
		this(new StringPrintWriter(new StringWriter()));
	}
    
	public AjBuildConfig genBuildConfig(String[] args, IMessageHandler handler) {
		AjBuildConfig buildConfig = new AjBuildConfig();
		try {
			// sets filenames to be non-null in order to make sure that file paramters are ignored
			super.filenames = new String[] { "" }; 
			
			List optionsList = new ArrayList(Arrays.asList(args));
			List fileList = new ArrayList();
			boolean incrementalMode = false;
			if (optionsList.remove("-incremental")) {
				incrementalMode = true;
				args = (String[])optionsList.toArray(new String[optionsList.size()]);
			}
			
			AjcConfigParser parser = new AjcConfigParser(buildConfig, handler, incrementalMode);
			parser.parseCommandLine(args);
			
			if (!incrementalMode) {
				if (parser.getFiles() != null) {
					for (Iterator it = parser.getFiles().iterator(); it.hasNext(); ) {
						fileList.add((File)it.next());
					}	
				} 	
			} else {
				if (parser.getFiles() != null && !parser.getFiles().isEmpty()) {
					handler.handleMessage(new Message("can not directly specify files in incremental mode, use -sourceroots instead", 
						Message.ERROR, null, null));	
				}
			}
				
			List javaArgList = parser.getUnparsedArgs();
			if (javaArgList.size() != 0) {
				super.configure((String[])javaArgList.toArray(new String[javaArgList.size()]));
			}
			
			if (buildConfig.getSourceRoots() != null) {
				for (Iterator i = buildConfig.getSourceRoots().iterator(); i.hasNext(); ) {
					fileList.addAll(collectSourceRootFiles((File)i.next()));
				}
			}
			
			buildConfig.setFiles(fileList);
			if (destinationPath != null) {
				buildConfig.setOutputDir(new File(destinationPath));
			}
			
			buildConfig.setClasspath(getClasspath(parser));
			
			if (incrementalMode) {
				if (buildConfig.getSourceRoots().size() == 0) {
					handler.handleMessage(new Message("must specify a source root when in incremental mode", 
						Message.ERROR, null, null));	
				}
			}
			
			setDebugOptions();
			buildConfig.setJavaOptions(options);
		} catch (InvalidInputException iie) {
			handler.handleMessage(new Message(iie.getMessage(), Message.ERROR, null, null));
			printUsage();
		}
		return buildConfig;
	}

    /** 
     * Get messages not dumped to handler or any PrintWriter.
     * @param flush if true, empty errors
     * @return null if none, String otherwise
     * @see BuildArgParser()
     */
    public String getOtherMessages(boolean flush) {
        if (null == errorSink) {
            return null;
        }
        
        String result = errorSink.toString().trim();
        if (0 == result.length()) {
            result = null;
        }
        if (flush) {
            errorSink.setLength(0);
        }
        return result;
    }

	private void setDebugOptions() {  
		options.put(
			CompilerOptions.OPTION_LocalVariableAttribute,
			CompilerOptions.GENERATE);
		options.put(
			CompilerOptions.OPTION_LineNumberAttribute,
			CompilerOptions.GENERATE);
		options.put(
			CompilerOptions.OPTION_SourceFileAttribute,
			CompilerOptions.GENERATE);
	}


	private Collection collectSourceRootFiles(File dir) {
		return Arrays.asList(FileUtil.listFiles(dir, FileUtil.aspectjSourceFileFilter));
	}

    
    public List getClasspath(AjcConfigParser parser) {
    	List ret = new ArrayList();
    	
    	if (parser.bootclasspath == null) {
    		addClasspath(System.getProperty("sun.boot.class.path", ""), ret);
    	} else {
    		addClasspath(parser.bootclasspath, ret);
    	}

		String extdirs = parser.extdirs;
		if (extdirs == null) {
            extdirs = System.getProperty("java.ext.dirs", "");
        }
        addExtDirs(extdirs, ret);

		if ((classpaths == null || classpaths.length == 0) ||
			(classpaths != null && classpaths[0] == ".")) {
			//??? this puts the compiler's classes on the classpath
			//??? this is ajc-1.0 compatible
			addClasspath(System.getProperty("java.class.path", ""), ret);
		} else {
	    	ret.addAll(Arrays.asList(classpaths));
		}	
		
		//??? eclipse seems to put outdir on the classpath
		//??? we're brave and believe we don't need it	    
	    
	    return ret;
    }

	private void addExtDirs(String extdirs, List classpathCollector) {
		StringTokenizer tokenizer = new StringTokenizer(extdirs, File.pathSeparator);
		while (tokenizer.hasMoreTokens()) {
//			classpathCollector.add(tokenizer.nextToken());
			File dirFile = new File((String)tokenizer.nextToken());
			if (dirFile.exists() && dirFile.isDirectory()) {
				File[] files = FileUtil.listFiles(dirFile, new FileFilter() {
					public boolean accept(File pathname) {
						return pathname.isFile() && pathname.getName().endsWith(".jar"); 
					}
				});
				for (int i = 0; i < files.length; i++) {
					classpathCollector.add(files[i].getAbsolutePath());	
				}
			}
		}	
	}

    
	private void addClasspath(String classpath, List classpathCollector) {
		StringTokenizer tokenizer = new StringTokenizer(classpath, File.pathSeparator);
		while (tokenizer.hasMoreTokens()) {
			classpathCollector.add(tokenizer.nextToken());
		}		
	}
    
    // !!! extract error handling to be common so that the IDEs can use it
    private class AjcConfigParser extends ConfigParser {
        private String bootclasspath = null;
        private String extdirs = null;
        private boolean incrementalArgsMode = false;
        private List unparsedArgs = new ArrayList();
		private AjBuildConfig buildConfig;
		private IMessageHandler handler;
		
        public AjcConfigParser(AjBuildConfig buildConfig, IMessageHandler handler, boolean incrementalMode) {
        	this.buildConfig = buildConfig;	
        	this.handler = handler;
        	this.incrementalArgsMode = incrementalMode;
        }  
        
        public List getUnparsedArgs() {
        	return unparsedArgs;	
        }
        
        public void parseOption(String arg, LinkedList args) {
			int nextArgIndex = args.indexOf(arg)+1;
			if (arg.equals("-Xlint")) {;
				buildConfig.getAjOptions().put(
					AjCompilerOptions.OPTION_Xlint,
					CompilerOptions.GENERATE);
			} else if (arg.equals("-injars")) {;
				if (args.size() > nextArgIndex) {
					buildConfig.getAjOptions().put(AjCompilerOptions.OPTION_InJARs, CompilerOptions.PRESERVE);
					
					StringTokenizer st = new StringTokenizer(
						((ConfigParser.Arg)args.get(nextArgIndex)).getValue(), 
						File.pathSeparator);
		            while (st.hasMoreTokens()) {
		            	String filename = st.nextToken();
		            	File jarFile = makeFile(filename);
		            	if (filename.endsWith(".jar") && jarFile.exists()) {
			            	buildConfig.getInJars().add(jarFile);    
		            	} else {
		                	handler.handleMessage(new Message(
								"ignoring bad injar: " + filename, 
								Message.WARNING, null, null));
		            	}
		            }
					
					args.remove(args.get(nextArgIndex));	
				}
			} else if (arg.equals("-aspectpath")) {;
				if (args.size() > nextArgIndex) {
					StringTokenizer st = new StringTokenizer(
						((ConfigParser.Arg)args.get(nextArgIndex)).getValue(), 
						File.pathSeparator);
		            while (st.hasMoreTokens()) {
		            	String filename = st.nextToken();
		            	File jarFile = makeFile(filename);
		            	if (filename.endsWith(".jar") && jarFile.exists()) {
			            	buildConfig.getAspectpath().add(jarFile);    
		            	} else {
		                	handler.handleMessage(new Message(
								"ignoring bad injar: " + filename, 
								Message.WARNING, null, null));
		            	}
		            }
					
					args.remove(args.get(nextArgIndex));	
				}
			} else if (arg.equals("-sourceroots")) {
				if (args.size() > nextArgIndex) {
					List sourceRoots = new ArrayList();
					StringTokenizer st = new StringTokenizer(
						((ConfigParser.Arg)args.get(nextArgIndex)).getValue(), 
						File.pathSeparator);
		            while (st.hasMoreTokens()) {
		            	File f = makeFile(st.nextToken());
		            	if (f.isDirectory()) {
			                sourceRoots.add(f);
		            	} else {
							handler.handleMessage(new Message(
								f.getName() + " is not a file, not adding to sourceroots", 
								Message.WARNING, null, null));	
		            	}		            		
		            }
					 
									
//					if (sourceRoots.size() > 1) {
//						handler.handleMessage(new Message(
//							"can not specify more than one source root (compiler limitation)\n"
//							+ "using source root: " + sourceRoots.get(0), 
//							Message.WARNING, null, null));	
//					} else 
					if (sourceRoots.size() < 1) {
						out.println("must specify a valid source root in incremental mode");
					} else {
						buildConfig.setSourceRoots(sourceRoots);	
					}
					args.remove(args.get(nextArgIndex));
				} else {
					out.println("must specify a valid source root in incremental mode");
				}
			} else if (arg.equals("-outjar")) { 
				if (args.size() > nextArgIndex) {
					buildConfig.getAjOptions().put(AjCompilerOptions.OPTION_OutJAR, CompilerOptions.GENERATE);
					File jarFile = makeFile(((ConfigParser.Arg)args.get(nextArgIndex)).getValue());
					if (jarFile.getName().endsWith(".jar")) {
						try {
							if (!jarFile.exists()) jarFile.createNewFile();
						} catch (IOException ioe) { 
							// fail siltenty 
						}
						buildConfig.setOutputJar(jarFile);	
					} else {
						out.println("file specified with -outjar is not a valid JAR file, ignoring");
						buildConfig.setLintSpecFile(null);
					}
					args.remove(args.get(nextArgIndex));
				} else {
					out.println("must specify a file for -outjar");
				}
			} else if (arg.equals("-emacssym")) {
				buildConfig.setEmacsSymMode(true);
				buildConfig.setGenerateModelMode(true);
			} else if (arg.equals("-emacssym")) {
				buildConfig.setEmacsSymMode(true);
				buildConfig.setGenerateModelMode(true);
			} else if (arg.equals("-noweave")) {
				buildConfig.setNoWeave(true);
			} else if (arg.equals("-XserializableAspects")) {
				buildConfig.setXserializableAspects(true);
			} else if (arg.equals("-XnoInline")) {
				buildConfig.setXnoInline(true);
			} else if (arg.equals("-Xlintfile")) { 
				if (args.size() > nextArgIndex) {
					File lintSpecFile = makeFile(((ConfigParser.Arg)args.get(nextArgIndex)).getValue());
					if (lintSpecFile.exists() && lintSpecFile.getName().endsWith(".properties")) {
						buildConfig.setLintSpecFile(lintSpecFile);	
					} else {
						out.println("file specified with -Xlintfile does not exist, ignoring");
						buildConfig.setLintSpecFile(null);
					}
					args.remove(args.get(nextArgIndex));
				} else {
					out.println("must specify a file for -outjar");
				}
			} else if (arg.startsWith("-Xlint")) {
				int index = arg.indexOf(":");
				if (index != -1) {
					buildConfig.setLintMode(arg.substring(index+1));
				} else {
					buildConfig.setLintMode(AjBuildConfig.AJLINT_DEFAULT);
				}
			} else if (arg.equals("-bootclasspath")) {
				if (args.size() > nextArgIndex) {
					bootclasspath = ((ConfigParser.Arg)args.get(nextArgIndex)).getValue();
					args.remove(args.get(nextArgIndex));	
				}
			} else if (arg.equals("-extdirs")) {
				if (args.size() > nextArgIndex) {
					extdirs = ((ConfigParser.Arg)args.get(nextArgIndex)).getValue();
					args.remove(args.get(nextArgIndex));
				}	
			} else {
//				if (arg.equals("-d")) {
//					int nextArgIndex = args.indexOf(arg)+1;
//					if (args.size() > nextArgIndex) {
//						ConfigParser.Arg path = (ConfigParser.Arg)args.get(nextArgIndex);
//						path.setValue(makeFile(path.getValue()).getPath());
//					}
//		    	} 
		    	unparsedArgs.add(arg);
			}
        }

        public void showError(String message) {
//			out.println(message);
        	handler.handleMessage(new Message(message, Message.ERROR, null, null));
        }
        
        
		protected void showWarning(String message) {
			handler.handleMessage(new Message(message, Message.WARNING, null, null));
        }

    }
}
