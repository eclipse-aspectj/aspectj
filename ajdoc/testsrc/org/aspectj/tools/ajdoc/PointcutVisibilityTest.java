/*
 * Created on Jan 12, 2005
  */
package org.aspectj.tools.ajdoc;

import java.io.File;

import junit.framework.TestCase;

/**
 * @author Mik Kersten
 */
public class PointcutVisibilityTest extends TestCase {

    protected File file1 = new File("testdata/bug82340/Pointcuts.java");
    protected File outdir = new File("testdata/bug82340/doc");
    
    public void testCoveragePublicMode() {
        outdir.delete();
        String[] args = { 
              "-XajdocDebug",
            "-protected",
            "-d", 
            outdir.getAbsolutePath(),
            file1.getAbsolutePath()
        };
        org.aspectj.tools.ajdoc.Main.main(args);
    }
}
