
import java.io.*;
import java.util.*;
import common.OutputComparator;
//import org.aspectj.testing.Tester;

public class JavadocCompareClassMode {

    /** wait a minimum (of 1 second) for processes to complete */
    static final int MIN_SECS = 1;
    /** wait a maximum (of 4 hours) for processes to complete */
    static final int MAX_SECS = 4*60*60; 

    static final String INPUT_FILES = "input/applesJava/*.java";
    static final String FILE_1 = "Apple.html";
    static final String FILE_2 = "AppleCrate.html";
    static final String OUTPUT_DIR   = "output";
    static final String AJDOC_DIR   = OUTPUT_DIR + File.separator + "ajdoc";
    static final String JAVADOC_DIR = OUTPUT_DIR + File.separator + "javadoc";
    static final String AJDOC_CALL   = "java org.aspectj.tools.ajdoc.Main -d " + AJDOC_DIR + " " + INPUT_FILES;
    static final String JAVADOC_CALL = "javadoc -package -d " + JAVADOC_DIR + " " + INPUT_FILES;

    public static void main(String[] args) { test(System.out); }

    public static boolean ensureDir(String dirPath, StringBuffer errSink) {
        boolean result = false;
        if (dirPath != null) {
            try {
                File dir = new File(dirPath);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                result = (dir.exists() && dir.isDirectory());
            } catch (SecurityException e) {
                if (null != errSink) {
                    errSink.append(e.getClass().getName());
                    errSink.append(" ensuring directory ");
                    errSink.append(dirPath);
                    errSink.append(": ");
                    errSink.append(e.getMessage());
                }
            }
        }
        return result;
    } // ensureDir

    /**
     * This implements a basic three-step test:
     * <UL>
     *   <LI>step 1: exec ajdoc as a command on INPUT_FILES
     *   <LI>step 2: exec javadoc in the same way
     *   <LI>step 3: find differences in files FILE_1 and FILE_2
     * </UL>
     */
    public static void test(PrintStream sink) {
        OutputComparator outputComparator = new OutputComparator();
        
        sink.println("> Setup directories");
        StringBuffer errSink = new StringBuffer();
        if (! ensureDir(OUTPUT_DIR, errSink)) {
            sink.println("Error: " + errSink.toString());
            return;
        }
        if (! ensureDir(AJDOC_DIR, errSink)) {
            sink.println("Error: " + errSink.toString());
            return;
        }
        if (! ensureDir(JAVADOC_DIR, errSink)) {
            sink.println("Error: " + errSink.toString());
            return;
        }

        String toolName = "> ajdoc";
        sink.println(toolName + " running ");
        int result = runCommand(AJDOC_CALL);
        sink.println(toolName + " result " + result);

        toolName = "> javadoc";
        sink.println(toolName + " running ");
        result = runCommand(JAVADOC_CALL);
        sink.println(toolName + " result " + result);

        toolName = "> compare";
        sink.println(toolName + " running ");
        String[] files = new String[] { FILE_1, FILE_2 };
        Vector diffs = null;
        result = -2;
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            String ajdocFile = AJDOC_DIR + "/" + file;
            String javadocFile = JAVADOC_DIR + "/" + file;
            try { 
                diffs = outputComparator.compareFilesByLine(ajdocFile, javadocFile);
                if (diffs == null) {
                    sink.println("No differences in file " + file);
                    result = 0;
                } else {
                    result = diffs.size();
                    sink.println("Start of Differences in file " + FILE_1);
                    sink.println(diffs.toString());
                    sink.println("end of Differences in file " + FILE_1);
                }
            } catch (IOException e) {
                sink.println("Exception comparing: " + file);
                e.printStackTrace(sink);
                result = -1;
            } 
        }
        sink.println(toolName + " result " + result);
    }

    /** write in to out */
    static void writeStream(InputStream in, PrintStream out) {
        if ((null == in) || (null == out)) {
            return;
        }
        try {
            BufferedReader lines = new BufferedReader(new InputStreamReader(in));
            String line;
            while (null != (line = lines.readLine())) {
                out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace(out);
        }
    }

    /**
     * Complete a running process, handling timeout and streams appropriately.
     * @param process the Process to run
     * @param secsToWait an int for the number of seconds to wait before timing out
     *        - use Integer.MAXVALUE to mean no timeout (otherwise,
     *        IllegalArgumentException unless (MIN_SECS <= secsToWait <= MAX_SECS))
     * @param outSink the PrintStream sink for the process output stream
     *                (use null to ignore process output stream).
     * @param errSink the PrintStream sink for the process error stream
     *                (use null to ignore process error stream).
     * @returns Integer.MIN_VALUE if interrupted while waiting for process to complete,
     *          Integer.MAX_VALUE if timed out,
     *          or the int returned by <code>Process.waitFor()</code> otherwise.
     * @throws IllegalArgumentException if any parms are null or invalid
     */
    public static int completeProcess(final Process process, int secsToWait,
                                 PrintStream outSink, 
                                 PrintStream errSink) {
        if (null == process) throw new IllegalArgumentException("null process");
        if ((Integer.MAX_VALUE != secsToWait)
            && ((MIN_SECS > secsToWait) || ((MAX_SECS < secsToWait)))) {
            throw new IllegalArgumentException("invalid time: " + secsToWait);
        }
        // setup timeout
        TimerTask task = null;
        if (Integer.MAX_VALUE != secsToWait) { 
            Timer t = new Timer(true);
            task = new TimerTask() {
                    public void run() {
                        process.destroy();
                    }
                };
            t.schedule(task, secsToWait*1000l);
        }

        // try to wait for the process
        int  status = Integer.MAX_VALUE;
        try { 
            status = process.waitFor(); 
        } catch (InterruptedException ie) {
            status = Integer.MIN_VALUE; // ignore
        }
        finally {
            if (null != task) task.cancel();
            if (errSink != null) writeStream(process.getErrorStream(), errSink);
            // misnamed API: the "input" stream is our input from the process output
            if (outSink != null) writeStream(process.getInputStream(), outSink);
        }
        return status;
    } // completeProcess

    /**
     * Run command, delegating process handling to runProcess.
     * @param command the String passed to Runtime.exec
     * @return the int returned from process.waitFor();
     */
    public static int runCommand(String command) {
        int result = -1;
        try {
            System.out.println("Running " + command);
            Process process = Runtime.getRuntime().exec(command);
            System.out.println("waiting for Result.." );
            final int seconds = 60;
            result = completeProcess(process, seconds, System.out, System.err);
            System.out.println("Result: " + result + " for " + command);
        } catch (Exception e) {
            throw new RuntimeException("could not execute: " + command +
                ", " + e.getMessage() );
        }
        return result;
    }   
    
}
