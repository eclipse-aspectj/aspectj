
import java.io.IOException;
import java.util.Vector;
import common.OutputComparator;
import org.aspectj.testing.Tester;

public class JavadocComparePackageMode {

    static final String INPUT_FILES = "-classpath input/pkgExample aPack bPack.cPack";
    static final String FILE_1 = "aPack/Class2.html";
    static final String FILE_2 = "bPack/cPack/Class3.html";
    static final String AJDOC_DIR   = "output/packageMode1";
    static final String JAVADOC_DIR = "output/packageMode2";
    static final String AJDOC_CALL   = "java org.aspectj.tools.ajdoc.Main -d " + AJDOC_DIR + " " + INPUT_FILES;
    static final String JAVADOC_CALL = "javadoc -package -d " + JAVADOC_DIR + " " + INPUT_FILES;

    public static void main(String[] args) { test(); }

    /**
     * <UL>
     *   <LI>step 1: run ajdoc as a command
     *   <LI>step 2: run javadoc
     *   <LI>step 3: compare differences
     * </UL>
     */
    public static void test() {
        OutputComparator outputComparator = new OutputComparator();
        
        System.out.println("> running ajdoc");
        runCommand(AJDOC_CALL);
        System.out.println("> running javadoc");
        runCommand(JAVADOC_CALL);
        
        Vector diffs1 = null;
        Vector diffs2 = null;
        try { 
            diffs1 = outputComparator.compareFilesByLine(AJDOC_DIR + "/" + FILE_1, 
                                                         JAVADOC_DIR + "/" + FILE_1);
            diffs2 = outputComparator.compareFilesByLine(AJDOC_DIR + "/" + FILE_1, 
                                                         JAVADOC_DIR + "/" + FILE_1);
        }
        catch (IOException ioe) {
            System.out.println("Couldn't compare files: " + ioe.getMessage());
        } 
        String result1 = "";
        String result2 = "";
        if (diffs1 != null) result1 = diffs1.toString();
        if (diffs2 != null) result2 = diffs2.toString();
        Tester.checkEqual(result1, "", "diffs from: " + FILE_1);
        Tester.checkEqual(result2, "", "diffs from: " + FILE_2);
    }

    public static void runCommand(String command) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process result = runtime.exec(command);
        }
        catch ( Exception ioe ) {
            throw new RuntimeException("could not execute: " + command +
                ", " + ioe.getMessage() );	
        }
    }   
    
}
