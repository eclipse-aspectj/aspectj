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


import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

/**
 * Tests the Ajdoc ant task.
 */
public class AjdocTaskTester extends AntTaskTester {

    /** todo correlate with basedir, local.properties in test-ant-task */
    protected final static String TEST_DOCDIR  
        = "test-docs";
    protected final static String TEST_SOURCES = "../src";
    protected File docDir = null;

    /**
     * We use <code>"tests/ant/etc/ajc.xml"</code>.
     */
    public String getAntFile() {
        return "tests/ant/etc/ajdoc.xml";
    }

    /**
     * Put {@link #TEST_DOCDIR} and {@link #TEST_SOURCES}
     * into the user properties.
     */
    protected Map getUserProperties() {
        Map userProps = new HashMap();
        // these are in local.properties per test-ant-tasks.xml
        //userProps.put("ajdoc.relative.doc.dir", TEST_DOCDIR );
        //userProps.put("ajdoc.relative.src.dir", TEST_SOURCES);        
        return userProps;
    }

    ////// Begin tests //////////////////////////////////////////////

    public void test_stylesheetfile() {
        avoid(STYLESHEET_CSS);
        wantFiles("One.html");
        wantFiles("mystylesheet.css");
    }

    public void test_helpfile() {
        avoid(HELP_DOC_HTML);
        wantFiles("One.html");
        wantFiles("myhelp.html");
    }

    public void test_nodeprecatedlist_no() {
        wantFiles("One.html");
    }
    public void test_nodeprecatedlist_yes() {
        avoid(DEPRECATED_LIST_HTML);
        wantFiles("One.html");
    }

    public void test_nodeprecated_no() { wantFiles("One.html"); }
    public void test_nodeprecated_yes() { wantFiles("One.html"); }

    public void test_use_no() {
        wantFiles("One.html");
    }
    public void test_use_yes() {
        wantFiles("One.html");
        wantFiles("class-use/One.html");
    }

    public void test_standard_no() {
        wantFiles("One.html");
    }
    public void test_standard_yes() {
        wantFiles("One.html");
    }

    public void test_author_no() { wantFiles("One.html"); }
    public void test_author_yes() { wantFiles("One.html"); }

    public void test_public_no() { wantFiles("One.html"); }
    public void test_public_yes() { wantFiles("One.html"); }
    public void test_package_no() { wantFiles("One.html"); }
    public void test_package_yes() { wantFiles("One.html"); }
    public void test_protected_no() { wantFiles("One.html"); }
    public void test_protected_yes() { wantFiles("One.html"); }
    public void test_private_no() { wantFiles("One.html"); }
    public void test_private_yes() { wantFiles("One.html"); }

    public void test_splitindex_no() {
        wantFiles("One.html");
    }
    public void test_splitindex_yes() {
        avoid(INDEX_ALL_HTML);
        wantFiles("One.html");
    }
    
    public void test_windowtitle() {
        wantFiles("One.html");
    }
    public void test_doctitle() {
        wantFiles("One.html");
    }
    public void test_bottom() {
        wantFiles("One.html");
    }
    public void test_footer() {
        wantFiles("One.html");
    }
    public void test_header() {
        wantFiles("One.html");
    }

    public void test_nohelp_no() {
        wantFiles("One.html");
    }
    public void test_nohelp_yes() {
        avoid(HELP_DOC_HTML);
        wantFiles("One.html");
    }

    public void test_noindex_no() {
        wantFiles("One.html");
    }
    public void test_noindex_yes() {
        avoid(INDEX_ALL_HTML);
        wantFiles("One.html");
    }

    public void test_notree_no() {
        wantFiles("One.html");
    }
    public void test_notree_yes() {
        avoid(OVERVIEW_TREE_HTML);
        wantFiles("One.html");
    }

    public void test985() {
        wantFiles("p1/One.html,p1/pp1/One.html");
        wantFiles("p2/Two.html,p2/pp2/Two.html");
    }
    public void test986() {
        wantFiles("p1/One.html,p1/pp1/One.html");
    }
    public void test987() {
        wantFiles("p1/One.html");
        wantFiles("p2/Two.html");
    }
    public void test988() {
        wantFiles("p1/One.html");
    }
    public void test989() {
        wantFiles("p1/One.html,p1/pp1/One.html");
        wantFiles("p2/Two.html,p2/pp2/Two.html");
    }
    public void test990() {
        wantFiles("p1/One.html,p1/pp1/One.html");
        wantFiles("p2/Two.html,p2/pp2/Two.html");
    }
    public void test991() {
        wantFiles("p1/One.html,p1/pp1/One.html");
        wantFiles("p2/Two.html");
    }
    public void test992() {
        wantFiles("p1/One.html,p2/Two.html");
    }
    public void test993() {
        wantFiles("p1/One.html,p1/pp1/One.html");
    }
    public void test994() {
        wantFiles("p1/One.html,p1/pp1/One.html");
    }
    public void test995() {
        wantFiles("p1/One.html");
    }
    public void test996() {
        wantFiles("One.html,Two.html");
    }
    public void test997() {
        wantFiles("One.html");
    }
    public void test998() {
        wantFiles("One.html,Two.html");
    }
    public void test999() {
        wantFiles("One.html");
    }



    ////// End tests ////////////////////////////////////////////////

    private final static int OVERVIEW_TREE_HTML           = 0x000001;
    private final static int INDEX_ALL_HTML               = 0x000002;
    private final static int DEPRECATED_LIST_HTML         = 0x000004;
    private final static int ALLCLASSES_FRAME_HTML        = 0x000008;
    private final static int INDEX_HTML                   = 0x000010;
    private final static int PACKAGES_HTML                = 0x000020;
    private final static int OVERVIEW_SUMMARY_HTML        = 0x000040;
    private final static int PACKAGE_LIST                 = 0x000080;
    private final static int HELP_DOC_HTML                = 0x000100;
    private final static int STYLESHEET_CSS               = 0x000200;
    private final static int ALL                          = 0x0003ff;
    private final static int TOP                          = ((ALL<<1)|1)&~ALL;
    private final static String[] FILES = new String[] {
        "overview-tree.html",
        "index-all.html",
        "deprecated-list.html",
        "allclasses-frame.html",
        "index.html",
        "packages.html",
        "overview-summary.html",
        "package-list",
        "help-doc.html",
        "stylesheet.css",
    };

    private void wantFiles(int mods) {
        mods &= (ALL | TOP);

        for (int c = 0; mods != 0x1; c++, mods >>= 0x1) {
            if ((mods & 0x1) == 0x1) {
                wantFiles(FILES[c]);
            } else {
                avoidFiles(FILES[c]);
            }
        }
    }

    private int MODS = ALL;
    private void avoid(int mods) {
        MODS &= ~mods;
    }

    /**
     * Make the doc dir -- e.g. call {@link #makeDocDir}
     */
    protected void beforeEveryTask() {
        makeDocDir();
        wantFiles(MODS);
    }

    /**
     * Assert classes and clear doc dir.
     *
     * @see #checkDocs()
     * @see #clearDocDir()
     */
    protected void afterEveryTask() {
        checkDocs();
        clearDocDir();
        MODS = ALL;
    }

    protected void avoidFiles(String filesWithoutHtmlExtensions) {
        List list = new ArrayList();
        for (StringTokenizer tok =
                 new StringTokenizer(filesWithoutHtmlExtensions, " ,;");
             tok.hasMoreTokens();) {
            list.add(tok.nextToken());
        }
        avoidFiles(list);
    }

    protected void avoidFiles(List filesWithoutHtmlExtensions) {
        for (Iterator iter = filesWithoutHtmlExtensions.iterator(); iter.hasNext();) {
            dont(iter.next()+"");
        }
    }

    protected void wantFiles(String filesWithoutHtmlExtensions) {
        List list = new ArrayList();
        for (StringTokenizer tok =
                 new StringTokenizer(filesWithoutHtmlExtensions, " ,;");
             tok.hasMoreTokens();) {
            list.add(tok.nextToken());
        }
        wantFiles(list);
    }

    protected void wantFiles(List filesWithoutHtmlExtensions) {
        for (Iterator iter = filesWithoutHtmlExtensions.iterator(); iter.hasNext();) {
            want(iter.next()+"");
        }
    }

    protected void checkDocs() {
        for (Iterator iter = wants.iterator(); iter.hasNext();) {
            String filename = iter.next() + "";
            File file = new File(docDir, filename);
            if (file != null && file.exists()) {
                have(filename);
            } else {
                //System.err.println("westodo expected " + file.getPath());
            }
        }
        for (Iterator iter = donts.iterator(); iter.hasNext();) {
            String filename = iter.next() + "";
            File file = new File(docDir, filename);
            if (file != null && file.exists()) {
                have(filename);
            } else {
                //System.err.println("westodo avoiding " + file.getPath());
            }
        }
    }

    /**
     * Create a new doc dir.
     */
    protected void init() {
        docDir = new File(project.getBaseDir(), TEST_DOCDIR);
    }

    /**
     * Make a new doc dir using ANT.
     */
    protected void makeDocDir() {
        try {
            Mkdir mkdir = (Mkdir)project.createTask("mkdir");
            mkdir.setDir(docDir);
            mkdir.execute();
        } catch (BuildException be) {
            be.printStackTrace();
        }
    }

    /**
     * Clear the build dir using ANT.
     */
    protected void clearDocDir() {
        try {
            Delete delete = (Delete)project.createTask("delete");
            FileSet fileset = new FileSet();
            fileset.setDir(docDir);
            fileset.setIncludes("**");
            delete.addFileset(fileset);
            delete.execute();
        } catch (BuildException be) {
            be.printStackTrace();
        }        
    }

    /**
     * Invoke {@link #runTests(String[])} on a
     * new instanceof {@link #AjdocTaskTester}.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        new AjdocTaskTester().runTests(args);
    }
}
