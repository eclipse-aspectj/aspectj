/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.Runner;
import org.aspectj.testing.xml.XMLWriter;
import org.aspectj.util.LangUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An AjcTest has child subruns (compile, [inc-compile|run]*).
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
     * Keyword directives are global/parent options passed as
     * <pre>-ajctest[Require|Skip]Keywords=keyword{,keyword}..</pre>.
     */
    public static class Spec extends AbstractRunSpec {
        public static final String XMLNAME = "ajc-test";
        /**
         * do description as title, do sourceLocation, 
         * do keywords, do options, skip paths, do comment,
         * skip dirChanges, do messages and do children
         * (though we do children directly). 
         */
        private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
                "title", null, null, null, "", null, true, false, false);
        
        private static final String OPTION_PREFIX = "-ajctest";
        private static final String[] VALID_OPTIONS = new String[] { OPTION_PREFIX };

        private static final String REQUIRE_KEYWORDS = "RequireKeywords=";
        private static final String SKIP_KEYWORDS = "SkipKeywords=";
        private static final String PICK_PR = "PR=";
        private static final List VALID_SUFFIXES 
            = Collections.unmodifiableList(Arrays.asList(new String[] 
            { REQUIRE_KEYWORDS, SKIP_KEYWORDS, PICK_PR }));
        
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
        
        public void setSuiteDir(File suiteDir) {
            this.suiteDir = suiteDir;
        }
        
        public File getSuiteDir() {
            return suiteDir;
        }
        
        /** @param bugId 100..9999 */
        public void setBugId(int bugId) {
            LangUtil.throwIaxIfFalse((bugId > 10) && (bugId < 10000), "bad bug id: " + bugId);
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
            String attr  = out.makeAttribute("dir", value);
            if (0 != bugId) {
                attr += " " + out.makeAttribute("pr", ""+bugId);
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
         * <li>the spec does not have keyword the parent requires</li>
         * <li>the spec does not have the bugId required</li>
         * </ul>
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
            for (int i = 0; i < globalOptions.length; i++) {
				String option  = globalOptions[i];
                if (!option.startsWith(OPTION_PREFIX)) {
                    throw new Error("only expecting " + OPTION_PREFIX + "..: " + option);
                }
                option = option.substring(OPTION_PREFIX.length());
                boolean keywordMustExist = false;
                String havePr = null;
                if (option.startsWith(REQUIRE_KEYWORDS)) {
                    option = option.substring(REQUIRE_KEYWORDS.length());
                    keywordMustExist = true;
                } else if (option.startsWith(SKIP_KEYWORDS)) {
                    option = option.substring(SKIP_KEYWORDS.length());
                } else if (option.startsWith(PICK_PR)) {
                    if (0 == bugId) {
                        skipMessage(handler, "bugId required, but no bugId for this test");
                        return false;
                    } else {
                        havePr = "" + bugId;
                    }
                    option = option.substring(PICK_PR.length());
                } else {
                    throw new Error("unrecognized suffix: " + globalOptions[i]
                        + " (expecting: " + OPTION_PREFIX + VALID_SUFFIXES + "...)");
                }
                List specs = LangUtil.commaSplit(option);
                // XXX also throw Error on empty specs...
                for (Iterator iter = specs.iterator(); iter.hasNext();) {
					String spec = (String) iter.next();
                    if (null != havePr) {
                        if (havePr.equals(spec)) { // String.equals()
                            return true;
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
            return true;
        }  

    } // AjcTest.Spec
    
    /** 
     * A suite of AjcTest has children for each AjcTest
     * and flows all options down as globals 
     */
    public static class Suite extends RunSpecIterator {
        final Spec spec;
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
             * do keywords, do options, skip paths, do comment
             * skip dirChanges, skip messages and do children
             * (though we do children directly). 
             */
            private static final XMLNames NAMES = new XMLNames(XMLNames.DEFAULT,
                    null, null, null, null, "", null, true, true, false);
            File suiteDir;
            public Spec() {
                super(XMLNAME, false); // do not skip this even if children skip
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
