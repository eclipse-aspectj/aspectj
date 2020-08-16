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

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.bridge.ICommand;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.ajde.CompileCommand;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.WrappedRunIterator;
import org.aspectj.testing.util.StructureModelUtil;
import org.aspectj.testing.util.StructureModelUtil.ModelIncorrectException;
import org.aspectj.testing.xml.AjcSpecXmlReader;
import org.aspectj.testing.xml.IXmlWritable;
import org.aspectj.testing.xml.SoftMessage;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * An incremental compiler run takes an existing compiler commmand
 * from the sandbox, updates the staging directory, and recompiles.
 * The staging directory is updated by prefix/suffix rules applied
 * to files found below Sandbox.testBaseSrcDir.
 * Files with suffix .{tag}.java are owned by this run
 * and are copied to the staging directory
 * unless they are prefixed "delete.", in which case the
 * corresponding file is deleted.  Any "owned" file is passed to
 * the compiler as the list of changed files.
 * The files entry contains the expected files recompiled. XXX underinclusive
 * XXX prefer messages for expected files?
 * XXX later: also support specified paths, etc.
 */
public class IncCompilerRun implements IAjcRun {

	final Spec spec; // nonfinal later to make re-runnable
    Sandbox sandbox;

	/**
	 * @param handler must not be null, but may be reused in the same thread
	 */
	public IncCompilerRun(Spec spec) {
		LangUtil.throwIaxIfNull(spec, "spec");
		this.spec = spec;
	}

	/**
	 * Initialize this from the sandbox, using compiler and changedFiles.
	 * @param sandbox the Sandbox setup for this test, including copying
	 *         any changed files, etc.
	 * @see org.aspectj.testing.harness.bridge.AjcTest.IAjcRun#setup(File, File)
	 * @throws AbortException containing IOException or IllegalArgumentException
	 *          if the staging operations fail
	 */
	public boolean setupAjcRun(Sandbox sandbox, Validator validator) {
		LangUtil.throwIaxIfNull(validator, "validator");
		if (!validator.nullcheck(sandbox, "sandbox")
			|| !validator.nullcheck(spec, "spec")
			|| !validator.nullcheck(spec.tag, "fileSuffix")) {
			return false;
		}
		File srcDir = sandbox.getTestBaseSrcDir(this);
		File destDir = sandbox.stagingDir;
		if (!validator.canReadDir(srcDir, "testBaseSrcDir")
			|| !validator.canReadDir(destDir, "stagingDir")) {
			return false;
		}

        this.sandbox = sandbox;
        return doStaging(validator);
	}

    /**
     * Handle copying and deleting of files per tag.
     * This returns false unless
     * (1) tag is "same", or
     * (2) some file was copied or deleted successfully
     * and there were no failures copying or deleting files.
     * @return true if staging completed successfully
     */
    boolean doStaging(final Validator validator) {
        if ("same".equals(spec.tag)) {
            return true;
        }
        boolean result = false;
        try {
            final String toSuffix = ".java";
            final String fromSuffix = "." + spec.tag + toSuffix;
            // copy our tagged generation of files to the staging directory,
            // deleting any with ChangedFilesCollector.DELETE_SUFFIX
            // sigh - delay until after last last-mod-time
            intHolder holder = new intHolder();
			List<File> copied = new ArrayList<>();
            doStaging(validator,".java",holder,copied);
            doStaging(validator,".jar",holder,copied);
            doStaging(validator,".class",holder,copied);
            doStaging(validator,".properties",holder,copied); // arbitrary resource extension
            doStaging(validator,".xml",holder,copied); // arbitrary resource extension
            if ((0 == holder.numCopies) && (0 == holder.numDeletes)) {
                validator.fail("no files changed??");
            } else {
                result = (0 == holder.numFails);
            }
            if (0 < copied.size()) {
                File[] files = copied.toArray(new File[0]);
                FileUtil.sleepPastFinalModifiedTime(files);
            }
        } catch (NullPointerException npe) {
            validator.fail("staging - input", npe);
        } catch (IOException e) {
            validator.fail("staging - operations", e);
        }
        return result;
    }


    private void doStaging(final Validator validator, final String toSuffix,
    					   final intHolder holder,final List copied)
	throws IOException
	{
    	final String fromSuffix = "." + spec.tag + toSuffix;
        final String clip = ".delete" + toSuffix;
        FileFilter deleteOrCount = new FileFilter() {
            /** do copy unless file should be deleted */
			public boolean accept(File file) {
                boolean doCopy = true;
                String path = file.getAbsolutePath();
                if (!path.endsWith(clip)) {
                    holder.numCopies++;
                    validator.info("copying file: " + path);
                    copied.add(file);
                } else {
                    doCopy = false;
                    path = path.substring(0, path.length()-clip.length()) + toSuffix;
                    File toDelete = new File(path);
                    if (toDelete.delete()) {
                        validator.info("deleted file: " + path);
                        holder.numDeletes++;
                    } else {
                        validator.fail("unable to delete file: " + path);
                        holder.numFails++;
                    }
                }
				return doCopy;
			}

        };
        File srcDir = sandbox.getTestBaseSrcDir(this);
        File destDir = sandbox.stagingDir;
        FileUtil.copyDir(srcDir, destDir, fromSuffix, toSuffix, deleteOrCount);
    }

    private static class intHolder {
        int numCopies;
        int numDeletes;
        int numFails;
    }

	/**
	 * @see org.aspectj.testing.run.IRun#run(IRunStatus)
	 */
	public boolean run(IRunStatus status) {

		ICommand compiler = sandbox.getCommand(this);
		if (null == compiler) {
			MessageUtil.abort(status, "null compiler");
		}

//        // This is a list of expected classes (in File-normal form
//        // relative to base class/src dir, without .class suffix
//        // -- like "org/aspectj/tools/ajc/Main")
//        // A preliminary list is generated in doStaging.
//        ArrayList expectedClasses = doStaging(status);
//        if (null == expectedClasses) {
//            return false;
//        }
//
//        // now add any (additional) expected-class entries listed in the spec
//        // normalize to a similar file path (and do info messages for redundancies).
//
//        List alsoChanged = spec.getPathsAsFile(sandbox.stagingDir);
//        for (Iterator iter = alsoChanged.iterator(); iter.hasNext();) {
//			File f = (File) iter.next();
//
//            if (expectedClasses.contains(f)) {
//                // XXX remove old comment changed.contains() works b/c getPathsAsFile producing both File
//                // normalizes the paths, and File.equals(..) compares these lexically
//                String s = "specification of changed file redundant with tagged file: ";
//                MessageUtil.info(status, s + f);
//            } else {
//                expectedClasses.add(f);
//            }
//		}
//
//        // now can create handler, use it for reporting
//        List errors = spec.getMessages(IMessage.ERROR);
//        List warnings = spec.getMessages(IMessage.WARNING);
//        AjcMessageHandler handler = new AjcMessageHandler(errors, warnings, expectedClasses);

        // same DirChanges handling for JavaRun, CompilerRun, IncCompilerRun
        // XXX around advice or template method/class
        DirChanges dirChanges = null;
        if (!LangUtil.isEmpty(spec.dirChanges)) {
            LangUtil.throwIaxIfFalse(1 == spec.dirChanges.size(), "expecting only 1 dirChanges");
            dirChanges = new DirChanges(spec.dirChanges.get(0));
            if (!dirChanges.start(status, sandbox.classesDir)) {
                return false; // setup failed
            }
        }
        AjcMessageHandler handler = new AjcMessageHandler(spec.getMessages());
        boolean handlerResult = false;
        boolean commandResult = false;
        boolean result = false;
        boolean report = false;
        try {
            handler.init();
            if (spec.fresh) {
                if (compiler instanceof CompileCommand) { // urk
                    ((CompileCommand) compiler).buildNextFresh();
                } else {
                    MessageUtil.info(handler, "fresh not supported by compiler: " + compiler);
                }
            }
//            final long startTime = System.currentTimeMillis();
            commandResult = compiler.repeatCommand(handler);
            if (!spec.checkModel.equals("")) {
					StructureModelUtil.checkModel(spec.checkModel);
            }
            // XXX disabled LangUtil.throwIaxIfNotAllAssignable(actualRecompiled, File.class, "recompiled");
            report = true;
            // handler does not verify sandbox...
            handlerResult = handler.passed();
            if (!handlerResult) {
                result = false;
            } else {
                result = (commandResult == handler.expectingCommandTrue());
                if (! result) {
                    String m = commandResult
                        ? "incremental compile command did not return false as expected"
                        : "incremental compile command returned false unexpectedly";
                    MessageUtil.fail(status, m);
                } else if (null != dirChanges) {
                    result = dirChanges.end(status, sandbox.testBaseDir);
                }
            }
        } catch (ModelIncorrectException e) {
        	MessageUtil.fail(status,e.getMessage());
        } finally {
            if (!result || spec.runtime.isVerbose()) { // more debugging context in case of failure
                MessageUtil.info(handler, "spec: " + spec.toLongString());
                MessageUtil.info(handler, "sandbox: " + sandbox);
                String[] classes = FileUtil.listFiles(sandbox.classesDir);
                MessageUtil.info(handler, "sandbox.classes: " + Arrays.asList(classes));
            }
            // XXX weak - actual messages not reported in real-time, no fast-fail
            if (report) {
                handler.report(status);
            }
        }
        return result;
	}

//	private boolean hasFile(ArrayList changed, File f) {
//		return changed.contains(f); // d
//	}


	public String toString() {
      return "" + spec;
      //		return "IncCompilerRun(" + spec + ")"; // XXX
	}

	/**
     * initializer/factory for IncCompilerRun.
     */
	public static class Spec extends AbstractRunSpec {
        public static final String XMLNAME = "inc-compile";

        protected boolean fresh;
		protected ArrayList<String> classesAdded;
		protected ArrayList<String> classesRemoved;
		protected ArrayList<String> classesUpdated;

        protected String checkModel;

        /**
         * skip description, skip sourceLocation,
         * do keywords, skip options, do paths as classes, do comment,
         * skip staging (always true),  skip badInput (irrelevant)
         * do dirChanges, do messages but skip children.
         */
//        private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
//                "", "", null, "", "classes", null, "", "", false, false, true);
//
		/** identifies files this run owns, so {name}.{tag}.java maps to {name}.java */
		String tag;

		public Spec() {
            super(XMLNAME);
			setStaging(true);
			classesAdded = new ArrayList<>();
			classesRemoved = new ArrayList<>();
			classesUpdated = new ArrayList<>();
            checkModel="";
		}

        protected void initClone(Spec spec)
                throws CloneNotSupportedException {
            super.initClone(spec);
            spec.fresh = fresh;
            spec.tag = tag;
            spec.classesAdded.clear();
            spec.classesAdded.addAll(classesAdded);
            spec.classesRemoved.clear();
            spec.classesRemoved.addAll(classesRemoved);
            spec.classesUpdated.clear();
            spec.classesUpdated.addAll(classesUpdated);
        }

        public Object clone() throws CloneNotSupportedException {
            Spec result = new Spec();
            initClone(result);
            return result;
        }


        public void setFresh(boolean fresh) {
            this.fresh = fresh;
        }

		public void setTag(String input) {
			tag = input;
		}

		public void setCheckModel(String thingsToCheck) {
			this.checkModel=thingsToCheck;
		}

        public String toString() {
            return "IncCompile.Spec(" + tag + ", " + super.toString() + ",["+checkModel+"])";
        }

        /** override to set dirToken to Sandbox.CLASSES and default suffix to ".class" */
        public void addDirChanges(DirChanges.Spec spec) { // XXX copy/paste of CompilerRun.Spec...
            if (null == spec) {
                return;
            }
            spec.setDirToken(Sandbox.CLASSES_DIR);
            spec.setDefaultSuffix(".class");
            super.addDirChanges(spec);
        }

        /** @return a IncCompilerRun with this as spec if setup completes successfully. */
        public IRunIterator makeRunIterator(Sandbox sandbox, Validator validator) {
            IncCompilerRun run = new IncCompilerRun(this);
            if (run.setupAjcRun(sandbox, validator)) {
                // XXX need name
                return new WrappedRunIterator(this, run);
            }
            return null;
        }

       /**
         * Write this out as a compile element as defined in
         * AjcSpecXmlReader.DOCTYPE.
         * @see AjcSpecXmlReader#DOCTYPE
         * @see IXmlWritable#writeXml(XMLWriter)
         */
        public void writeXml(XMLWriter out) {
            String attr = XMLWriter.makeAttribute("tag", tag);
            out.startElement(xmlElementName, attr, false);
            if (fresh) {
                out.printAttribute("fresh", "true");
            }
            super.writeAttributes(out);
            out.endAttributes();
            if (!LangUtil.isEmpty(dirChanges)) {
                DirChanges.Spec.writeXml(out, dirChanges);
            }
            SoftMessage.writeXml(out, getMessages());
            out.endElement(xmlElementName);
        }

        public void setClassesAdded(String items) {
            addItems(classesAdded, items);
        }

        public void setClassesUpdated(String items) {
            addItems(classesUpdated, items);
        }

        public void setClassesRemoved(String items) {
            addItems(classesRemoved, items);
        }

		private void addItems(List<String> list, String items) {
            if (null != items) {
                String[] classes = XMLWriter.unflattenList(items);
                if (!LangUtil.isEmpty(classes)) {
					for (String aClass : classes) {
						if (!LangUtil.isEmpty(aClass)) {
							list.add(aClass);
						}
					}
                }
            }
        }
	} // class IncCompilerRun.Spec
}
//   // XXX replaced with method-local class - revisit if useful
//
//	/**
//	 * This class collects the list of all changed files and
//	 * deletes the corresponding file for those prefixed "delete."
//	 */
//	static class ChangedFilesCollector implements FileFilter {
//        static final String DELETE_SUFFIX = ".delete.java";
//        static final String REPLACE_SUFFIX = ".java";
//		final ArrayList changed;
//		final Validator validator;
//        /** need this to generate paths by clipping */
//        final File destDir;
//
//		/** @param changed the sink for all files changed (full paths) */
//		public ChangedFilesCollector(ArrayList changed, File destDir, Validator validator) {
//			LangUtil.throwIaxIfNull(validator, "ChangedFilesCollector - handler");
//			this.changed = changed;
//			this.validator = validator;
//            this.destDir = destDir;
//        }
//
//		/**
//		 * This converts the input File to normal String path form
//         * (without any source suffix) and adds it to the list changed.
//         * If the name of the file is suffixed ".delete..", then
//		 * delete the corresponding file, and return false (no copy).
//         * Return true otherwise (copy file).
//         * @see java.io.FileFilter#accept(File)
//		 */
//		public boolean accept(File file) {
//            final String aname = file.getAbsolutePath();
//            String name = file.getName();
//            boolean doCopy = true;
//            boolean failed = false;
//            if (name.endsWith(DELETE_SUFFIX)) {
//                name = name.substring(0,name.length()-DELETE_SUFFIX.length());
//                file = file.getParentFile();
//                file = new File(file, name + REPLACE_SUFFIX);
//                if (!file.canWrite()) {
//                    validator.fail("file to delete is not writable: " + file);
//                    failed = true;
//                } else if (!file.delete()) {
//                    validator.fail("unable to delete file: " + file);
//                    failed = true;
//                }
//                doCopy = false;
//            }
//            if (!failed && doCopy) {
//                int clip = FileUtil.sourceSuffixLength(file);
//                if (-1 != clip) {
//                    name.substring(0, name.length()-clip);
//                }
//                if (null != destDir) {
//                    String path = destDir.getPath();
//                    if (!LangUtil.isEmpty(path)) {
//                        // XXX incomplete
//                        if (name.startsWith(path)) {
//                        } else {
//                            int loc = name.lastIndexOf(path);
//                            if (-1 == loc) { // sigh
//
//                            } else {
//
//                            }
//                        }
//                    }
//                }
//                name = FileUtil.weakNormalize(name);
//                changed.add(file);
//            }
//            return doCopy;
//		}
//	};

