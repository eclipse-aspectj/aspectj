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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.aspectj.ajdt.internal.core.builder.AjBuildConfig;
import org.aspectj.ajdt.internal.core.builder.AjCompilerOptions;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.ConfigParser;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class BuildArgParser extends Main {

	private static final String BUNDLE_NAME = "org.aspectj.ajdt.ajc.messages";
    private static boolean LOADED_BUNDLE = false;
    
    /** to initialize super's PrintWriter but refer to underlying StringWriter */
    private static class StringPrintWriter extends PrintWriter {
        public final StringWriter stringWriter;
        StringPrintWriter(StringWriter sw) {
          super(sw);
          this.stringWriter = sw;
        }
    }

    /** @return multi-line String usage for the compiler */    
    public static String getUsage() {
        if (!LOADED_BUNDLE) { // get eclipse usage unless bundle loaded...
            new BuildArgParser();
        }
        return Main.bind("misc.usage", Main.bind("compiler.version"));
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
		super(writer, writer, false);
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        if (!LOADED_BUNDLE) {
            LOADED_BUNDLE = true;
        }
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
    
    /**
     * Generate build configuration for the input args,
     * passing to handler any error messages.
     * @param args the String[] arguments for the build configuration
     * @param handler the IMessageHandler handler for any errors
     * @return AjBuildConfig per args, 
     *         which will be invalid unless there are no handler errors.
     */
	public AjBuildConfig genBuildConfig(String[] args, IMessageHandler handler) {
		AjBuildConfig buildConfig = new AjBuildConfig();
		try {
			// sets filenames to be non-null in order to make sure that file paramters are ignored
			super.filenames = new String[] { "" }; 
			
			List fileList = new ArrayList();
			
			AjcConfigParser parser = new AjcConfigParser(buildConfig, handler);
			parser.parseCommandLine(args);
            
            boolean incrementalMode = buildConfig.isIncrementalMode()
                        || buildConfig.isIncrementalFileMode();
			
            List files = parser.getFiles();
            if (!LangUtil.isEmpty(files)) {
                if (incrementalMode) {
                    MessageUtil.error(handler, "incremental mode only handles source files using -sourceroots"); 
                } else {
                    fileList.addAll(files);
                }
            }
				
			List javaArgList = new ArrayList();
			
			//	disable all special eclipse warnings by default
			//??? might want to instead override getDefaultOptions()
			javaArgList.add("-warn:none");
			
			// these next four lines are some nonsense to fool the eclipse batch compiler
			// without these it will go searching for reasonable values from properties
			//TODO fix org.eclipse.jdt.internal.compiler.batch.Main so this hack isn't needed
			javaArgList.add("-classpath");
			javaArgList.add(System.getProperty("user.dir"));
			javaArgList.add("-bootclasspath");
			javaArgList.add(System.getProperty("user.dir"));
			
			javaArgList.addAll(parser.getUnparsedArgs());

//			if (javaArgList.size() != 0) {
				super.configure((String[])javaArgList.toArray(new String[javaArgList.size()]));
//			}
			
			if (buildConfig.getSourceRoots() != null) {
				for (Iterator i = buildConfig.getSourceRoots().iterator(); i.hasNext(); ) {
					fileList.addAll(collectSourceRootFiles((File)i.next()));
				}
			}
			
			buildConfig.setFiles(fileList);
			if (destinationPath != null) { // XXX ?? unparsed but set?
				buildConfig.setOutputDir(new File(destinationPath));
			}
			
			buildConfig.setClasspath(getClasspath(parser));
			
			if (incrementalMode 
                && (0 == buildConfig.getSourceRoots().size())) {
                    MessageUtil.error(handler, "specify a source root when in incremental mode");
			}
			
			setDebugOptions();
			buildConfig.setJavaOptions(options);
		} catch (InvalidInputException iie) {
            MessageUtil.error(handler, iie.getMessage());
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

		if (parser.classpath == null) {
			//??? this puts the compiler's classes on the classpath
			//??? this is ajc-1.0 compatible
			addClasspath(System.getProperty("java.class.path", ""), ret);
		} else {
	    	addClasspath(parser.classpath, ret);
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
		private String classpath = null;
        private String extdirs = null;
        private List unparsedArgs = new ArrayList();
		private AjBuildConfig buildConfig;
		private IMessageHandler handler;
		
        public AjcConfigParser(AjBuildConfig buildConfig, IMessageHandler handler) {
        	this.buildConfig = buildConfig;	
        	this.handler = handler;
        }  
        
        public List getUnparsedArgs() {
        	return unparsedArgs;	
        }
        
        /**
         * Extract AspectJ-specific options (except for argfiles).
         * Caller should warn when sourceroots is empty but in 
         * incremental mode.
         * Signals warnings or errors through handler set in constructor.
         */
        public void parseOption(String arg, LinkedList args) { // XXX use ListIterator.remove()
			int nextArgIndex = args.indexOf(arg)+1; // XXX assumes unique
            // trim arg?
            if (LangUtil.isEmpty(arg)) {
                showWarning("empty arg found");
            } else if (arg.equals("-injars")) {;
				if (args.size() > nextArgIndex) {
					buildConfig.getAjOptions().put(AjCompilerOptions.OPTION_InJARs, CompilerOptions.PRESERVE);
					
					StringTokenizer st = new StringTokenizer(
						((ConfigParser.Arg)args.get(nextArgIndex)).getValue(), 
						File.pathSeparator);
		            while (st.hasMoreTokens()) {
		            	String filename = st.nextToken();
		            	File jarFile = makeFile(filename);
		            	if (jarFile.exists() && FileUtil.hasZipSuffix(filename)) {
			            	buildConfig.getInJars().add(jarFile);    
		            	} else {
                            showError("bad injar: " + filename);
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
                        if (jarFile.exists() && FileUtil.hasZipSuffix(filename)) {
			            	buildConfig.getAspectpath().add(jarFile);    
		            	} else {
                            showError("bad aspectpath: " + filename);
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
		            	if (f.isDirectory() && f.canRead()) {
			                sourceRoots.add(f);
		            	} else {
                            showError("bad sourceroot: " + f);
		            	}		            		
		            }
				    if (0 < sourceRoots.size()) {
						buildConfig.setSourceRoots(sourceRoots);	
					}
					args.remove(args.get(nextArgIndex));
				} else {
					showError("-sourceroots requires list of directories");
				}
			} else if (arg.equals("-outjar")) { 
				if (args.size() > nextArgIndex) {
					buildConfig.getAjOptions().put(AjCompilerOptions.OPTION_OutJAR, CompilerOptions.GENERATE);
					File jarFile = makeFile(((ConfigParser.Arg)args.get(nextArgIndex)).getValue());
					if (FileUtil.hasZipSuffix(jarFile)) {
						try {
							if (!jarFile.exists()) {
                                jarFile.createNewFile();
                            }
                            buildConfig.setOutputJar(jarFile);  
						} catch (IOException ioe) { 
                            showError("unable to create outjar file: " + jarFile);
						}
					} else {
						showError("invalid -outjar file: " + jarFile);
					}
					args.remove(args.get(nextArgIndex));
				} else {
					showError("-outjar requires jar path argument");
				}
            } else if (arg.equals("-incremental")) {
                buildConfig.setIncrementalMode(true);
            } else if (arg.equals("-XincrementalFile")) {
                if (args.size() > nextArgIndex) {
                    File file = makeFile(((ConfigParser.Arg)args.get(nextArgIndex)).getValue());
                    buildConfig.setIncrementalFile(file);
                    if (!file.canRead()) {
                        showError("bad -XincrementalFile : " + file);
                        // if not created before recompile test, stop after first compile
                    }
                    args.remove(args.get(nextArgIndex));
                } else {
                    showError("-XincrementalFile requires file argument");
                }
			} else if (arg.equals("-emacssym")) {
				buildConfig.setEmacsSymMode(true);
				buildConfig.setGenerateModelMode(true);
			} else if (arg.equals("-noweave") || arg.equals( "-XnoWeave")) {
				buildConfig.setNoWeave(true);
			} else if (arg.equals("-XserializableAspects")) {
				buildConfig.setXserializableAspects(true);
			} else if (arg.equals("-XnoInline")) {
				buildConfig.setXnoInline(true);
			} else if (arg.equals("-Xlintfile")) { 
				if (args.size() > nextArgIndex) {
					File lintSpecFile = makeFile(((ConfigParser.Arg)args.get(nextArgIndex)).getValue());
                    // XXX relax restriction on props file suffix?
					if (lintSpecFile.canRead() && lintSpecFile.getName().endsWith(".properties")) {
						buildConfig.setLintSpecFile(lintSpecFile);	
					} else {
						showError("bad -Xlintfile file: " + lintSpecFile);
						buildConfig.setLintSpecFile(null);
					}
					args.remove(args.get(nextArgIndex));
				} else {
					showError("-Xlintfile requires .properties file argument");
				}
            } else if (arg.equals("-Xlint")) {
                buildConfig.getAjOptions().put(
                    AjCompilerOptions.OPTION_Xlint,
                    CompilerOptions.GENERATE);
                buildConfig.setLintMode(AjBuildConfig.AJLINT_DEFAULT);
            } else if (arg.startsWith("-Xlint:")) {
                if (7 < arg.length()) {
                    buildConfig.setLintMode(arg.substring(7));
                } else {
                    showError("invalid lint option " + arg);
                }
			} else if (arg.equals("-bootclasspath")) {
				if (args.size() > nextArgIndex) {
					bootclasspath = ((ConfigParser.Arg)args.get(nextArgIndex)).getValue();
					args.remove(args.get(nextArgIndex));	
				} else {
					showError("-bootclasspath requires classpath entries");
				}
			} else if (arg.equals("-classpath")) {
				if (args.size() > nextArgIndex) {
					classpath = ((ConfigParser.Arg)args.get(nextArgIndex)).getValue();
					args.remove(args.get(nextArgIndex));	
				} else {
					showError("-classpath requires classpath entries");
				}
			} else if (arg.equals("-extdirs")) {
				if (args.size() > nextArgIndex) {
					extdirs = ((ConfigParser.Arg)args.get(nextArgIndex)).getValue();
					args.remove(args.get(nextArgIndex));
                } else {
                    showError("-extdirs requires list of external directories");
                }
            // error on directory unless -d, -{boot}classpath, or -extdirs
            } else if (arg.equals("-d")) {
                dirLookahead(arg, args, nextArgIndex);
//            } else if (arg.equals("-classpath")) {
//                dirLookahead(arg, args, nextArgIndex);
//            } else if (arg.equals("-bootclasspath")) {
//                dirLookahead(arg, args, nextArgIndex);
//            } else if (arg.equals("-extdirs")) {
//                dirLookahead(arg, args, nextArgIndex);
            } else if (new File(arg).isDirectory()) {
                showError("dir arg not permitted: " + arg);
			} else {
                // argfile, @file parsed by superclass
                // no eclipse options parsed:
                // -d args, -help (handled), 
                // -classpath, -target, -1.3, -1.4, -source [1.3|1.4]
                // -nowarn, -warn:[...], -deprecation, -noImportError,
                // -proceedOnError, -g:[...], -preserveAllLocals,
                // -referenceInfo, -encoding, -verbose, -log, -time
                // -noExit, -repeat
		    	unparsedArgs.add(arg);
			}
        }
        protected void dirLookahead(String arg, LinkedList argList, int nextArgIndex) {
            unparsedArgs.add(arg);
            ConfigParser.Arg next = (ConfigParser.Arg) argList.get(nextArgIndex);
            String value = next.getValue();
            if (!LangUtil.isEmpty(value)) {
                if (new File(value).isDirectory()) {
                    unparsedArgs.add(value);
                    argList.remove(next);
                    return;
                }
            }
        }

        public void showError(String message) {
            MessageUtil.error(handler, message);
        }
        
		protected void showWarning(String message) {
            MessageUtil.warn(handler, message);
        }

    }
}
