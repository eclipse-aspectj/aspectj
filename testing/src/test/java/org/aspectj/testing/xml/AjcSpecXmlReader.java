/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     resolver
 * ******************************************************************/

package org.aspectj.testing.xml;

//import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.digester.Digester;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.testing.harness.bridge.AjcTest;
import org.aspectj.testing.harness.bridge.CompilerRun;
import org.aspectj.testing.harness.bridge.DirChanges;
import org.aspectj.testing.harness.bridge.IncCompilerRun;
import org.aspectj.testing.harness.bridge.JavaRun;
import org.aspectj.testing.harness.bridge.Sandbox;
import org.aspectj.testing.harness.bridge.Validator;
import org.aspectj.testing.util.RunUtils;
import org.aspectj.util.LangUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** 
 * Read an ajc test specification in xml form. 
 * Input files should comply with DOCTYPE
 */
public class AjcSpecXmlReader {
    /*
     * To add new elements or attributes:
     * - update the DOCTYPE
     * - update setupDigester(..)
     *   - new sub-elements should be created 
     *   - new attributes should have values set as bean properties
     *     (possibly by mapping names)
     *   - new sub-elements should be added to parents
     *     => the parents need the add method - e.g., add{foo}({foo} toadd)
     * - add tests
     *   - add compile-time checks for mapping APIs in
     *     setupDigesterComipileTimeCheck
     *   - when adding an attribute set by bean introspection,
     *     add to the list returned by expectedProperties()
     * - update any client writers referring to the DOCTYPE, as necessary.
     *   - the parent IXmlWriter should delegate to the child component
     *     as IXmlWriter (or write the subelement itself)
     * 
     * Debugging
     * - use logLevel = 2 for tracing
     * - common mistakes
     *   - dtd has to match input
     *   - no rule defined (or misdefined) so element ignored
     *   - property read-only (?)
     */
    
//    private static final String EOL = "\n";
    
    /** presumed relative-path to dtd file for any XML files written by writeSuiteToXmlFile */
    public static final String DTD_PATH = "../tests/ajcTestSuite.dtd";
    
    /** expected doc type of AjcSpec XML files */
    public static final String DOCTYPE = "<!DOCTYPE " 
        + AjcTest.Suite.Spec.XMLNAME + " SYSTEM \"" + DTD_PATH + "\">";

    private static final AjcSpecXmlReader ME 
        = new AjcSpecXmlReader();
    
    /** @return shared instance */
    public static final AjcSpecXmlReader getReader() {
        return ME;
    }
    
    public static void main(String[] a) throws IOException {
        writeDTD(new File("../tests/ajcTestSuite2.dtd"));
    }
    
    /** 
     * Write a DTD to dtdFile.
     * @deprecated
     * @param dtdFile the File to write to
     */
    public static void writeDTD(File dtdFile) throws IOException {
        LangUtil.throwIaxIfNull(dtdFile, "dtdFile");
        PrintWriter out = new PrintWriter(new FileWriter(dtdFile));
        try {
            out.println("<!-- document type for ajc test suite - see " 
                        + AjcSpecXmlReader.class.getName() + " -->");
            //out.println(getDocType());
        } finally {
            out.close();
        }        
    }

    private static final String[] LOG = new String[] {"info", "debug", "trace" };
    
    // XXX logLevel n>0 causes JUnit tests to fail!
    private int logLevel = 0; // use 2 for tracing 
    
    private AjcSpecXmlReader() {}

    /** @param level 0..2, info..trace */
    public void setLogLevel(int level) {
        if (level < 0) {
            level = 0;
        }
        if (level > 2) {
            level = 2;
        }
        logLevel = level;        
    }

    /**
     * Print an IXmlWritable to the output file
     * with our leader and DOCTYPE.
     * @param output the File to write to - overwritten
     * @param tests the List of IXmlWritable to write
     * @return null if no warnings detected, warnings otherwise
     */
    public String writeSuiteToXmlFile(File output, IXmlWritable topNode) throws IOException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(output));
        XMLWriter printSink = new XMLWriter(writer);
        writer.println("");
        writer.println(AjcSpecXmlReader.DOCTYPE);
        writer.println("");
        topNode.writeXml(printSink);
        writer.close();
        String parent = output.getParent();
        if (null == parent) {
            parent = ".";
        }        
        String dtdPath = parent + "/" + DTD_PATH;
        File dtdFile = new File(dtdPath);
        if (!dtdFile.canRead()) {
            return "expecting dtd file: " + dtdFile.getPath();
        }
        return null;
    }
        
    /** 
     * Read the specifications for a suite of AjcTest from an XML file.
     * This also sets the suite dir in the specification.
     * @param file the File must be readable, comply with DOCTYPE.
     * @return AjcTest.Suite.Spec read from file
     * @see setLogLevel(int)
     */
    public AjcTest.Suite.Spec readAjcSuite(File file) throws IOException, AbortException {
        // setup loggers for digester and beanutils...
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); // XXX
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", LOG[logLevel]); // trace debug XXX

        final Digester digester = makeDigester(file);
        SuiteHolder holder = new SuiteHolder();
        digester.push(holder);
        FileInputStream input = new FileInputStream(file);
        try {
            digester.parse(input);
        } catch (SAXException e) {
            MessageUtil.fail("parsing " + file, e);
        } finally {
            if (null != input) {
                input.close();
                input = null;
            }            
        }
        AjcTest.Suite.Spec result = holder.spec;
        if (null != result) {
            file = file.getAbsoluteFile();
            result.setSourceLocation(new SourceLocation(file, 1));
            File suiteDir = file.getParentFile();
            if (null == suiteDir) {
                // should not be the case if absolute
                suiteDir = new File("."); // user.dir?
            }
            result.setSuiteDirFile(suiteDir);
            if (result.runtime.isVerbose()) { // XXX hack fixup
                RunUtils.enableVerbose(result);
            }
        }
        return result;
    }
   
   private Digester makeDigester(final File suiteFile) {
       // implement EntityResolver directly; set is failing
       Digester result = new Digester() {
           final SuiteResolver resolver = new SuiteResolver(suiteFile);
           public InputSource resolveEntity(
                   String publicId, 
                   String systemId)
                   throws SAXException {
               return resolver.resolveEntity(publicId, systemId);
           }
      };
      setupDigester(result);
      return result;
   }
   
    /** set up the mapping between the xml and Java. */
    private void setupDigester(Digester digester) {
        // XXX supply sax parser to ignore white space?
        digester.setValidating(true);
//        try {
//            // this is the correct approach, but the commons parser
//            // fails to accept a second, overriding registration - see
//            // http://lists.xml.org/archives/xml-dev/200111/msg00959.html
//            digester.getXMLReader().setEntityResolver(new SuiteResolver(suiteFile));
//        } catch (SAXException e) {
//            System.err.println("unable to set entity resolver");
//            e.printStackTrace(System.err);
//        }

        // element names come from the element components
        final String suiteX = AjcTest.Suite.Spec.XMLNAME;
        final String ajctestX = suiteX + "/" + AjcTest.Spec.XMLNAME;
        final String compileX = ajctestX + "/" + CompilerRun.Spec.XMLNAME;
        final String inccompileX = ajctestX + "/" + IncCompilerRun.Spec.XMLNAME;
        final String runX = ajctestX + "/" + JavaRun.Spec.XMLNAME;
        final String dirchangesX = "*/" + DirChanges.Spec.XMLNAME;
        final String messageX = "*/" + SoftMessage.XMLNAME;
        final String messageSrcLocX = messageX + "/" +SoftSourceLocation.XMLNAME;

        // ---- each sub-element needs to be created
        // handle messages the same at any level
        digester.addObjectCreate(suiteX,               AjcTest.Suite.Spec.class.getName());
        digester.addObjectCreate(ajctestX,             AjcTest.Spec.class.getName());
        digester.addObjectCreate(compileX,             CompilerRun.Spec.class.getName());
        //digester.addObjectCreate(compileX + "/file",   AbstractRunSpec.WrapFile.class.getName());
        digester.addObjectCreate(inccompileX,          IncCompilerRun.Spec.class.getName());
        digester.addObjectCreate(runX,                 JavaRun.Spec.class.getName()); 
        digester.addObjectCreate(messageX,             SoftMessage.class.getName());
        digester.addObjectCreate(messageSrcLocX,       SoftSourceLocation.class.getName());
        digester.addObjectCreate(dirchangesX,          DirChanges.Spec.class.getName());
        
        // ---- set bean properties for sub-elements created automatically
        // -- some remapped - warnings
        //   - if property exists, map will not be used
        digester.addSetProperties(suiteX); // ok to have suite messages and global suite options, etc.
        digester.addSetProperties(ajctestX, 
            new String[] { "title", "dir", "pr"},
            new String[] { "description", "testDirOffset", "bugId"});
        digester.addSetProperties(compileX, 
            new String[] { "files", "argfiles"},
            new String[] { "paths", "argfiles"});
        digester.addSetProperties(compileX + "/file");
        digester.addSetProperties(inccompileX, "classes", "paths");
        digester.addSetProperties(runX, 
            new String[] { "class", "vm", "skipTester", "fork", "vmargs", "aspectpath", "module"},
            new String[] { "className", "javaVersion", "skipTester", "fork", "vmArgs", "aspectpath", "module"});
        digester.addSetProperties(dirchangesX);
        digester.addSetProperties(messageX);
        digester.addSetProperties(messageSrcLocX, "line", "lineAsString");
        digester.addSetProperties(messageX, "kind", "kindAsString");
        digester.addSetProperties(messageX, "line", "lineAsString");
        //digester.addSetProperties(messageX, "details", "details");
        // only file subelement of compile uses text as path... XXX vestigial
        digester.addCallMethod(compileX + "/file", "setFile", 0);

        // ---- when subelements are created, add to parent 
        // add ajctest to suite, runs to ajctest, files to compile, messages to any parent...
        // the method name (e.g., "addSuite") is in the parent (SuiteHolder)
        // the class (e.g., AjcTest.Suite.Spec) refers to the type of the object created
        digester.addSetNext(suiteX,               "addSuite", AjcTest.Suite.Spec.class.getName());
        digester.addSetNext(ajctestX,             "addChild", AjcTest.Spec.class.getName());
        digester.addSetNext(compileX,             "addChild", CompilerRun.Spec.class.getName());
        digester.addSetNext(inccompileX,          "addChild", IncCompilerRun.Spec.class.getName());
        digester.addSetNext(runX,                 "addChild", JavaRun.Spec.class.getName());
        //digester.addSetNext(compileX + "/file",   "addWrapFile", AbstractRunSpec.WrapFile.class.getName());
        digester.addSetNext(messageX,             "addMessage", IMessage.class.getName());
        // setSourceLocation is for the inline variant
        // addSourceLocation is for the extra
        digester.addSetNext(messageSrcLocX,       "addSourceLocation", ISourceLocation.class.getName());
        digester.addSetNext(dirchangesX,          "addDirChanges", DirChanges.Spec.class.getName());
        
        // can set parent, but prefer to have "knows-about" flow down only...
    }

    // ------------------------------------------------------------ testing code
    /** 
     * Get expected bean properties for introspection tests. 
     * This should return an expected property for every attribute in DOCTYPE,
     * using any mapped-to names from setupDigester.
     */
    static BProps[] expectedProperties() {
        return new BProps[] 
            {
                new BProps(AjcTest.Suite.Spec.class, 
                    new String[] { "suiteDir"}), // verbose removed
                new BProps(AjcTest.Spec.class, 
                    new String[] { "description", "testDirOffset", "bugId"}),
                    // mapped from { "title", "dir", "pr"}
                new BProps(CompilerRun.Spec.class, 
                    new String[] { "files", "options", 
                    "staging", "badInput", "reuseCompiler", "includeClassesDir",
                    "argfiles", "aspectpath", "classpath", "extdirs", 
                    "sourceroots", "xlintfile", "outjar"}),
                new BProps(IncCompilerRun.Spec.class, 
                    new String[] { "tag" }),
                new BProps(JavaRun.Spec.class, 
                    new String[] { "className", "skipTester", "options", 
                    "javaVersion", "errStreamIsError", "outStreamIsError",
                    "fork", "vmArgs", "aspectpath"}),
                    // mapped from { "class", ...}
                new BProps(DirChanges.Spec.class, 
                    new String[] { "added", "removed", "updated", "unchanged", "dirToken", "defaultSuffix"}),
//                new BProps(AbstractRunSpec.WrapFile.class, 
//                    new String[] { "path"}),
                new BProps(SoftMessage.class, 
                    new String[] { "kindAsString", "lineAsString", "text", "details", "file"})
                    // mapped from { "kind", "line", ...}
            };
    }
        
    /** 
     * This is only to do compile-time checking for the APIs impliedly
     * used in setupDigester(..).
     * The property setter checks are redundant with tests based on
     * expectedProperties().
     */
    private static void setupDigesterCompileTimeCheck() { 
        if (true) { throw new Error("never invoked"); }
        AjcTest.Suite.Spec suite = new AjcTest.Suite.Spec();
        AjcTest.Spec test = new AjcTest.Spec();
        Sandbox sandbox = null;
        Validator validator = null;
//        AjcTest ajctest = new AjcTest(test, sandbox, validator);
//        ajctest.addRunSpec((AbstractRunSpec) null);
////        test.makeIncCompilerRun((IncCompilerRun.Spec) null);
////        test.makeJavaRun((JavaRun.Spec) null);
//        ajctest.setDescription((String) null);
//        ajctest.setTestBaseDirOffset((String) null);
//        ajctest.setBugId((String) null);
//        ajctest.setTestSourceLocation((ISourceLocation) null);
            
        CompilerRun.Spec crunSpec = new CompilerRun.Spec();
        crunSpec.addMessage((IMessage) null);
        // XXX crunSpec.addSourceLocation((ISourceLocation) null);
//        crunSpec.addWrapFile((AbstractRunSpec.WrapFile) null);
        crunSpec.setOptions((String) null);
        crunSpec.setPaths((String) null);
        crunSpec.setIncludeClassesDir(false);
        crunSpec.setReuseCompiler(false);
        crunSpec.setXlintfile((String) null);
        crunSpec.setOutjar((String)null);
        
        IncCompilerRun.Spec icrunSpec = new IncCompilerRun.Spec();
        icrunSpec.addMessage((IMessage) null);
        icrunSpec.setTag((String) null);
        icrunSpec.setFresh(false);

        JavaRun.Spec jrunspec = new JavaRun.Spec();
        jrunspec.addMessage((IMessage) null);
        jrunspec.setClassName((String) null);
        jrunspec.addMessage((IMessage) null);
        // input s.b. interpretable by Boolean.valueOf(String)
        jrunspec.setSkipTester(true); 
        jrunspec.setErrStreamIsError("false");
        jrunspec.setOutStreamIsError("false");
        jrunspec.setAspectpath("");
        jrunspec.setClasspath("");
        jrunspec.setFork(false);
        jrunspec.setLTW("false");
        jrunspec.setException("Error");
        
        
        DirChanges.Spec dcspec = new DirChanges.Spec();
        dcspec.setAdded((String) null);
        dcspec.setRemoved((String) null);
        dcspec.setUpdated((String) null);
        dcspec.setDefaultSuffix((String) null);
        dcspec.setDirToken((String) null);

        SoftMessage m = new SoftMessage();
        m.setSourceLocation((ISourceLocation) null);
        m.setText((String) null);
        m.setKindAsString((String) null);
        m.setDetails((String) null);
        
        SoftSourceLocation sl = new SoftSourceLocation();
        sl.setFile((String) null); 
        sl.setLine((String) null); 
        sl.setColumn((String) null); 
        sl.setEndLine((String) null); 
        
        // add attribute setters to validate?
    }
    
    /** top element on Digester stack holds the test suite */
    public static class SuiteHolder {
        AjcTest.Suite.Spec spec;
        public void addSuite(AjcTest.Suite.Spec spec) {
            this.spec = spec;
        }
    }
    
    /** hold class/properties association for testing */
    static class BProps {
        final Class cl;
        final String[] props;
        BProps(Class cl, String[] props) {
            this.cl = cl;
            this.props = props;
        }
    }
        
    /**
     * Find file NAME=="ajcTestSuite.dtd" from some reasonably-local
     * relative directories.
     * XXX bug: commons parser doesn't accept second registration,
     * so we override Digester's implementation instead.
     * XXX cannot JUnit test SuiteResolver since they run from 
     * local directory with valid reference
     * XXX does not fix JDK 1.4 parser message "unable to resolve without base URI"
     * XXX should be able to just set BaseURI instead...
     */
    static class SuiteResolver implements EntityResolver {
        public static final String NAME = "ajcTestSuite.dtd";
        private static boolean isDir(File dir) {
            return ((null != dir) && dir.canRead() && dir.isDirectory());
        }
        private final File suiteFile;
        public SuiteResolver(File suiteFile) {
            this.suiteFile = suiteFile;
        }
        

        private String getPath(String id) {
            // first, try id relative to suite file
            final File suiteFileDir = suiteFile.getParentFile();
            if (isDir(suiteFileDir)) {
                String path = suiteFileDir.getPath()
                    + "/" + id;
                File result = new File(path);
                if (result.canRead() && result.isFile()) {
                    return result.getPath();
                }
            }
            // then try misc paths relative to suite file or current dir            
            final File[] baseDirs = new File[]
            { suiteFileDir, new File(".")
            };
            final String[] locations = new String[]
            {  ".", "..", "../tests", "../../tests", 
                "../../../tests", "tests", "modules/tests"
            };
            File baseDir;
			for (File file : baseDirs) {
				baseDir = file;
				if (!isDir(baseDir)) {
					continue;
				}
				for (String location : locations) {
					File dir = new File(baseDir, location);
					if (isDir(dir)) {
						File temp = new File(dir, NAME);
						if (temp.isFile() && temp.canRead()) {
							return temp.getPath();
						}
					}
				}
			}
            return null;
        }
        public InputSource resolveEntity(
                String publicId, 
                String systemId)
                throws SAXException {
            InputSource result = null;
            if ((null != systemId) && 
                systemId.endsWith(NAME)) {
                String path = getPath(systemId);
                if (null != path) {
                    result = new InputSource(path);
                    result.setSystemId(path);
                    result.setPublicId(path);
                }
            }
            return result;
        }
    }
}

//private String getDocTypePath() {
//  String result = null;
//  if (null != suiteFile) {
//      FileReader fr = null;
//      try {
//          fr = new FileReader(suiteFile);
//          BufferedReader reader = new BufferedReader(fr);
//          String line;
//          while (null != (line = reader.readLine())) {
//              String upper = line.trim();
//              line = upper.toLowerCase();
//              if (line.startsWith("<")) {
//                  if (line.startsWith("<!doctype ")) {
//                      int start = line.indexOf("\"");
//                      int end = line.lastIndexOf(NAME + "\"");
//                      if ((0 < start) && (start < end)) {
//                          return upper.substring(start+1, 
//                              end + NAME.length());                                    
//                      }
//                  } else if (!line.startsWith("<?xml")) {
//                      break; // something else...
//                  }
//              }
//          }
//      } catch (IOException e) {
//          // ignore
//      } finally {
//          if (null != fr) {
//              try {
//                  fr.close();
//              } catch (IOException e1) { // ignore
//              }
//          }
//      }
//  }
//  return null;
//}

