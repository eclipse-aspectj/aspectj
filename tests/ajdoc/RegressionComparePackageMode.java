
import java.io.IOException;
import java.util.Vector;
import common.OutputComparator;
import org.aspectj.testing.Tester;

public class RegressionComparePackageMode {
    
    static final String CORRECT_RESULTS_DIR = "output/regression1";
    static final String RUN_RESULTS_DIR     = "output/regression2";
    static final String[] FILES_TO_COMPARE = { "/bPack/cPack/Class3.html",
                                             "/coordination/Coordinator.html",
                                             "/spacewar/Ship.html",
                                             "/spacewar/Debug.html" }   ;
    static final String[] AJDOC_ARGS = { "-d",
                                         RUN_RESULTS_DIR,
                                         "-sourcepath", 
                                         "input;input/pkgExample",
                                         "spacewar",
                                         "coordination",
                                         "bPack.cPack" };

    

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
        org.aspectj.tools.ajdoc.Main.main( AJDOC_ARGS );
        
        for ( int i = 0; i < FILES_TO_COMPARE.length; i++ ) {
            Vector diffs = null;
            try { 
                diffs = outputComparator.compareFilesByLine(CORRECT_RESULTS_DIR + FILES_TO_COMPARE[i], 
                                                             RUN_RESULTS_DIR + FILES_TO_COMPARE[i]);
            }
            catch (IOException ioe) {
                System.out.println("Couldn't compare files: " + ioe.getMessage());
            } 
            String result = "";
            if (diffs != null) result = diffs.toString();
            Tester.checkEqual(result, "", "diffs from: " + FILES_TO_COMPARE[i]);
        }
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
