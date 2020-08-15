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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//import org.aspectj.bridge.*;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.Runner;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.LangUtil;

/**
 * An AjcTest has child subruns (compile, [inc-compile|run]*).
 * XXX title keys shared between all instances
 * (add Thread to key to restrict access?)
 */
public class AjcTest extends RunSpecIterator {
    
    /** Unwrap an AjcTest.Spec from an IRunStatus around an AjcTest */
    public static Spec unwrapSpec(IRunStatus status) {
        if (null != status) {
            Object id = status.getIdentifier();
            if (id instanceof Runner.IteratorWrapper) {
                IRunIterator iter = ((Runner.IteratorWrapper) id).iterator;
                if (iter instanceof AjcTest) {
                    return (Spec) ((AjcTest) iter).spec;
                }
            }
        }
        return null;        
    }
    
    /** Unwrap initial CompilerRun.Spec from an AjcTest.Spec */
    public static CompilerRun.Spec unwrapCompilerRunSpec(Spec spec) {
        if (null != spec) {
            List kids = spec.getChildren();
            if (0 < kids.size()) {
                Object o = kids.get(0);
                if (o instanceof CompilerRun.Spec) {
                    return (CompilerRun.Spec) o;
                }
            }
        }
        return null;        
    }

    /** The spec creates the sandbox, so we use it throughout */
 	public AjcTest(Spec spec, Sandbox sandbox, Validator validator) {
	   super(spec, sandbox, validator, true);
    }
    
    /**
     * Clear the command from the sandbox, to avoid memory leaks.
	 * @see org.aspectj.testing.harness.bridge.RunSpecIterator#iterationCompleted()
	 */
	public void iterationCompleted() {
		super.iterationCompleted();
        sandbox.clearCommand(this);
	}
    
    
    /** 
     * Specification for an ajc test.
     * Keyword directives are global/parent options passed, e.g., as
     * <pre>-ajctest[Require|Skip]Keywords=keyword{,keyword}..</pre>.
     * See VALID_SUFFIXES for complete list.
     */
    public static class Spec extends AbstractRunSpec {
        public static final String XMLNAME = "ajc-test";
        /**
         * do description as title, do sourceLocation, 
         * do keywords, do options, skip paths, do comment,
         * skip staging, skip badInput,
         * skip dirChanges, do messages and do children
         * (though we do children directly). 
         */
        private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
                "title", null, null, null, "", null, "", "", true, false, false);
        
        private static final String OPTION_PREFIX = "-ajctest";
        private static final String[] VALID_OPTIONS = new String[] { OPTION_PREFIX };

        private static final String TITLE_LIST = "TitleList=";
        private static final String TITLE_FAIL_LIST = "TitleFailList=";
        private static final String TITLE_CONTAINS= "TitleContains=";
        private static final String REQUIRE_KEYWORDS = "RequireKeywords=";
        private static final String SKIP_KEYWORDS = "SkipKeywords=";
        private static final String PICK_PR = "PR=";
        private static final List<String> VALID_SUFFIXES 
            = Collections.unmodifiableList(Arrays.asList(new String[] 
            { TITLE_LIST, TITLE_FAIL_LIST, TITLE_CONTAINS, 
                REQUIRE_KEYWORDS, SKIP_KEYWORDS, PICK_PR }));
        
        /** Map String titlesName to List (String) of titles to accept */
        private static final Map<String,List<String>> TITLES = new HashMap<>();
        
        private static List<String> getTitles(String titlesName) {
            return getTitles(titlesName, false);
        }
        private static List<String> getTitles(String titlesName, boolean fail) {
            if (LangUtil.isEmpty(titlesName)) {
                return Collections.emptyList();
            }
            List<String> result = (List<String>) TITLES.get(titlesName);
            if (null == result) {
                result = makeTitlesList(titlesName, fail);
                TITLES.put(titlesName, result);
            }
            return result;
        }
        
        /**
         * Make titles list per titlesKey, either a path to a file
         * containing "[PASS|FAIL] {title}(..)" entries,
         * or a comma-delimited list of titles.
         * @param titlesKey a String, either a path to a file
         * containing "[PASS|FAIL] {title}(..)" entries,
         * or a comma-delimited list of titles.
         * @param fail if true, only read titles prefixed "FAIL" from files
         * @return the unmodifiable List of titles (maybe empty, never null)
         */
        private static List<String> makeTitlesList(String titlesKey, boolean fail) {
            File file = new File(titlesKey);
            return file.canRead() 
                ? readTitlesFile(file, fail)
                : parseTitlesList(titlesKey);
        }

        /**
         * Parse list of titles from comma-delmited list
         * titlesList, trimming each entry and permitting
         * comma to be escaped with '\'.
         * @param titlesList a comma-delimited String of titles
         * @return the unmodifiable List of titles (maybe empty, never null)
         */
        private static List<String> parseTitlesList(String titlesList) {
            List<String> result = new ArrayList<>();
            String last = null;
            StringTokenizer st = new StringTokenizer(titlesList, ",");
            while (st.hasMoreTokens()) {
                String next = st.nextToken().trim();
                if (next.endsWith("\\")) {
                    next = next.substring(0, next.length()-1);
                    if (null == last) {
                        last = next;
                    } else {
                        last += next;
                    }
                    next = null;
                } else if (null != last) {
                    next = (last + next).trim();
                    last = null;
                } else {
                    next = next.trim();
                }
                if (!LangUtil.isEmpty(next)) {
                    result.add(next);
                }
            }
            if (null != last) { 
                String m = "unterminated entry \"" + last; // XXX messages
                System.err.println(m + "\" in " + titlesList);
                result.add(last.trim());
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Read titles from a test result file, accepting
         * only those prefixed with [PASS|FAIL] and
         * excluding the "[PASS|FAIL] Suite.Spec(.." entry.
         * @param titlesFile the File containing a
         * list of titles from test results,
         * with some lines of the form 
         * <code>[PASS|FAIL] {title}()<code> (excluding
         * <code>[PASS|FAIL] Suite.Spec(...<code>.
         * @param titlesFile the File path to the file containing titles
         * @param fail if true, only select titles prefixed "FAIL"
         * @return the unmodifiable List of titles (maybe empty, never null)
         */
        private static List<String> readTitlesFile(File titlesFile, boolean fail) {
            List<String> result = new ArrayList<>();
            Reader reader = null;
            try {
                reader = new FileReader(titlesFile);
                BufferedReader lines = new BufferedReader(reader);
                String line;
                while (null != (line = lines.readLine())) {
                    if ((line.startsWith("FAIL ")
                        || (!fail && line.startsWith("PASS ")))
                        && (!line.substring(5).startsWith("Suite.Spec("))) {
                        String title = line.substring(5);
                        int loc = title.lastIndexOf("(");
                        if (-1 != loc) {
                            title = title.substring(0, loc);
                        }
                        result.add(title);
                    }
                }
            } catch (IOException e) {
                System.err.println("ignoring titles in " + titlesFile); // XXX messages
                e.printStackTrace(System.err);
            } finally {
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
            return Collections.unmodifiableList(result);
        }
        
        /** base directory of the test suite - set before making run */
        private File suiteDir;
        
        /** path offset from suite directory to base of test directory */
        String testDirOffset; // XXX revert to private after fixes
        
        /** id of bug - if 0, then no bug associated with this test */
        private int bugId;

        public Spec() {
            super(XMLNAME);
            setXMLNames(NAMES);
        }
        
        protected void initClone(Spec spec) 
                throws CloneNotSupportedException {
            super.initClone(spec);
            spec.bugId = bugId;
            spec.suiteDir = suiteDir;
            spec.testDirOffset = testDirOffset;
        }
        
        public Object clone() throws CloneNotSupportedException {
            Spec result = new Spec();
            initClone(result);
            return result;    
        }
        
        public void setSuiteDir(File suiteDir) {
            this.suiteDir = suiteDir;
        }
        
        public File getSuiteDir() {
            return suiteDir;
        }
        
        /** @param bugId 100..999999 */
        public void setBugId(int bugId) {
            LangUtil.throwIaxIfFalse((bugId > 10) && (bugId < 1000000), "bad bug id: " + bugId);
            this.bugId = bugId;
        }
        
        public int getBugId() {
            return bugId;
        }
        
        public void setTestDirOffset(String testDirOffset) {
            if (!LangUtil.isEmpty(testDirOffset)) {
                this.testDirOffset = testDirOffset;
            }
        }
        
        public String getTestDirOffset() {
            return (null == testDirOffset ? "" : testDirOffset);
        }
        
        /**
         * @param sandbox ignored
         * @see org.aspectj.testing.harness.bridge.AbstractRunSpec#makeAjcRun(Sandbox, Validator)
         */
        public IRunIterator makeRunIterator(Sandbox sandbox, Validator validator) {
            LangUtil.throwIaxIfNull(validator, "validator");

            // if no one set suiteDir, see if we have a source location
            if (null == suiteDir) {
                ISourceLocation loc = getSourceLocation();
                if (!validator.nullcheck(loc, "suite file location")
                    || !validator.nullcheck(loc.getSourceFile(), "suite file")) {
                    return null;
                }
                File locDir = loc.getSourceFile().getParentFile();
                if (!validator.canReadDir(locDir, "source location dir")) {
                    return null;
                }
                suiteDir = locDir;
            }

            // we make a new sandbox with more state for our subruns, keep that, 
            // in order to defer initialization to nextRun()
            File testBaseDir;
            String testDirOffset = getTestDirOffset();
            if (LangUtil.isEmpty(testDirOffset)) {
                testBaseDir = suiteDir;
            } else {
                testBaseDir = new File(suiteDir, testDirOffset);
                if (!validator.canReadDir(testBaseDir, "testBaseDir")) {
                    return null;
                }
            }
            Sandbox childSandbox = null;
            try {
                childSandbox = new Sandbox(testBaseDir, validator);
                validator.registerSandbox(childSandbox);
            } catch (IllegalArgumentException e) {
                validator.fail(e.getMessage());
                return null;
            }
            return new AjcTest(this, childSandbox, validator);
        }

        /** @see IXmlWritable#writeXml(XMLWriter) */
        public void writeXml(XMLWriter out) {
            out.println("");
            String value = (null == testDirOffset? "" : testDirOffset);
            String attr  = XMLWriter.makeAttribute("dir", value);
            if (0 != bugId) {
                attr += " " + XMLWriter.makeAttribute("pr", ""+bugId);
            }
            out.startElement(xmlElementName, attr, false);
            super.writeAttributes(out);
            out.endAttributes();
            super.writeChildren(out);
            out.endElement(xmlElementName);
        } 
               
        /**
         * AjcTest overrides this to skip if 
         * <ul>
         * <li>the spec has a keyword the parent wants to skip</li>
         * <li>the spec does not have a required keyword</li>
         * <li>the spec does not have a required bugId</li>
         * <li>the spec does not have a required title (description)n</li>
         * </ul>
         * When skipping, this issues a messages as to why skipped.
         * Skip combinations are not guaranteed to work correctly. XXX
         * @return false if this wants to be skipped, true otherwise
         * @throws Error if selected option is not of the form
         *          <pre>-ajctest[Require|Skip]Keywords=keyword{,keyword}..</pre>.
         */
        protected boolean doAdoptParentValues(RT parentRuntime, IMessageHandler handler) {
            if (!super.doAdoptParentValues(parentRuntime, handler)) {
                return false;
            }
            runtime.copy(parentRuntime);
            
            String[] globalOptions = runtime.extractOptions(VALID_OPTIONS, true);
			for (String globalOption : globalOptions) {
				String option = globalOption;
				if (!option.startsWith(OPTION_PREFIX)) {
					throw new Error("only expecting " + OPTION_PREFIX + "..: " + option);
				}
				option = option.substring(OPTION_PREFIX.length());
				boolean keywordMustExist = false;
				List<String> permittedTitles = null;
				List<String> permittedTitleStrings = null;
				String havePr = null;
				if (option.startsWith(REQUIRE_KEYWORDS)) {
					option = option.substring(REQUIRE_KEYWORDS.length());
					keywordMustExist = true;
				} else if (option.startsWith(SKIP_KEYWORDS)) {
					option = option.substring(SKIP_KEYWORDS.length());
				} else if (option.startsWith(TITLE_LIST)) {
					option = option.substring(TITLE_LIST.length());
					permittedTitles = getTitles(option);
				} else if (option.startsWith(TITLE_FAIL_LIST)) {
					option = option.substring(TITLE_FAIL_LIST.length());
					permittedTitles = getTitles(option, true);
				} else if (option.startsWith(TITLE_CONTAINS)) {
					option = option.substring(TITLE_CONTAINS.length());
					permittedTitleStrings = getTitles(option);
				} else if (option.startsWith(PICK_PR)) {
					if (0 == bugId) {
						skipMessage(handler, "bugId required, but no bugId for this test");
						return false;
					} else {
						havePr = "" + bugId;
					}
					option = option.substring(PICK_PR.length());
				} else {
					throw new Error("unrecognized suffix: " + globalOption
							+ " (expecting: " + OPTION_PREFIX + VALID_SUFFIXES + "...)");
				}
				if (null != permittedTitleStrings) {
					boolean gotHit = false;
					for (Iterator<String> iter = permittedTitleStrings.iterator();
						 !gotHit && iter.hasNext();
					) {
						String substring = (String) iter.next();
						if (this.description.contains(substring)) {
							gotHit = true;
						}
					}
					if (!gotHit) {
						String reason = "title "
								+ this.description
								+ " does not contain any of "
								+ option;
						skipMessage(handler, reason);
						return false;
					}
				} else if (null != permittedTitles) {
					if (!permittedTitles.contains(this.description)) {
						String reason = "titlesList "
								+ option
								+ " did not contain "
								+ this.description;
						skipMessage(handler, reason);
						return false;
					}
				} else {
					// all other options handled as comma-delimited lists
					List<String> specs = LangUtil.commaSplit(option);
					// XXX also throw Error on empty specs...
					for (String spec : specs) {
						if (null != havePr) {
							if (havePr.equals(spec)) { // String.equals()
								havePr = null;
							}
						} else if (keywordMustExist != keywords.contains(spec)) {
							String reason = "keyword " + spec
									+ " was " + (keywordMustExist ? "not found" : "found");
							skipMessage(handler, reason);
							return false;
						}
					}
					if (null != havePr) {
						skipMessage(handler, "bugId required, but not matched for this test");
						return false;
					}
				}
			}
            return true;
        }  

    } // AjcTest.Spec
    
    /** 
     * A suite of AjcTest has children for each AjcTest
     * and flows all options down as globals 
     */
    public static class Suite extends RunSpecIterator {
        final Spec spec;
        
        /**
         * Count the number of AjcTest in this suite.
         * @param spec
         * @return
         */
        public static int countTests(Suite.Spec spec) {
            return spec.children.size();
        }

        public static AjcTest.Spec[] getTests(Suite.Spec spec) {
            if (null == spec) {
                return new AjcTest.Spec[0];
            }
            return (AjcTest.Spec[]) spec.children.toArray(new AjcTest.Spec[0]);
        }

        public Suite(Spec spec, Sandbox sandbox, Validator validator) {
            super(spec, sandbox, validator, false);
            this.spec = spec;
        }
        
        /**
         * While being called to make the sandbox for the child,
         * set up the child's suite dir based on ours.
         * @param child must be instanceof AjcTest.Spec
		 * @see org.aspectj.testing.harness.bridge.RunSpecIterator#makeSandbox(IRunSpec, Validator)
         * @return super.makeSandbox(child, validator)
		 */
		protected Sandbox makeSandbox(
			IRunSpec child,
			Validator validator) {
            if (!(child instanceof AjcTest.Spec)) {
                validator.fail("only expecting AjcTest children");
                return null;
            }
            if (!validator.canReadDir(spec.suiteDir, "spec.suiteDir")) {
                return null;
            }
            ((AjcTest.Spec) child).setSuiteDir(spec.suiteDir);
			return super.makeSandbox(child, validator);
		}

        /** 
         * A suite spec contains AjcTest children.
         * The suite dir or source location should be set 
         * if the tests do not each have a source location
         * with a source file in the suite dir.
         * XXX whether to write out suiteDir in XML?
         */
        public static class Spec extends AbstractRunSpec {
            public static final String XMLNAME = "suite";
            /**
             * do description, do sourceLocation, 
             * do keywords, do options, skip paths, do comment,
             * skip staging, skip badInput,
             * skip dirChanges, skip messages and do children
             * (though we do children directly). 
             */
//            private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
//                    null, null, null, null, "", null, "", "", true, true, false);
            File suiteDir;
            public Spec() {
                super(XMLNAME, false); // do not skip this even if children skip
            }
            
            public Object clone() throws CloneNotSupportedException {
                Spec spec = new Spec();
                super.initClone(spec);
                spec.suiteDir = suiteDir;                
                return spec;
            }
            
            /** @param suiteDirPath the String path to the base suite dir */
            public void setSuiteDir(String suiteDirPath) {
                if (!LangUtil.isEmpty(suiteDirPath)) {
                    this.suiteDir = new File(suiteDirPath);
                }
            }
            
            /** @param suiteDirFile the File for the base suite dir */
            public void setSuiteDirFile(File suiteDir) {
                this.suiteDir = suiteDir;
            }
            
            /** @return suiteDir from any set or source location if set */
            public File getSuiteDirFile() {
                if (null == suiteDir) {
                    ISourceLocation loc = getSourceLocation();
                    if (null != loc) {
                        File sourceFile = loc.getSourceFile();
                        if (null != sourceFile) {
                            suiteDir = sourceFile.getParentFile();
                        }
                    }
                }
                return suiteDir;
            }
            
            /**
             * @return
             * @see org.aspectj.testing.harness.bridge.AbstractRunSpec#makeRunIterator(Sandbox, Validator)
             */
            public IRunIterator makeRunIterator(
                Sandbox sandbox, 
                Validator validator) {
                return new Suite(this, sandbox, validator);
            }

            public String toString() {
                // removed nKids as misleading, since children.size() may change
                //int nKids = children.size();
                //return "Suite.Spec(" + suiteDir + ", " + nKids + " tests)"; 
                return "Suite.Spec(" + suiteDir + ")"; 
            }
        }        
    }    
}
