
//package debugger;

/**
 * Main.java
 *
 *
 * Created: Wed Sep 06 15:54:41 2000
 *
 * @author <a href="mailto:palm@parc.xerox.com"Jeffrey Palm</a>
 */

public class Main  {

    public Main(Tester tester, String[] args) {
        String classPath = getArg(args, "-classpath");
        String root      = getArg(args, "-root");
        String verbose   = getSwitch(args, "-verbose");
        String dbg       = getSwitch(args, "-debug");
        boolean debug    = !dbg.equals("");
        Tester.setClassPath(classPath);
        Tester.setRoot(root);
        if (verbose.equals("true")) {
            Tester.setVerbose(true);
        }
        if (dbg.equals("true")) {
            Tester.setDebug(true);
        }
        if (!root.equals("")) {
            Tester.setRoot(root);
        }
        tester.go(args);
//          new BreakpointTester(debug).go(args);
//          new ThreadTester(debug).go(args);
//          new ArgumentTester(debug).go(args);
    }

    static void fail(Object o) {
        System.err.println("ERROR: " + o);
        System.exit(1);
    }

    public static String getSwitch(String[] args, String arg) {
        return getArg(args, arg, false);
    }

    public static String getArg(String[] args, String arg) {
        return getArg(args, arg, true);
    }

    public static String getArg(String[] args, String arg, boolean needArg) {
        String s = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(arg)) {
                try {
                    s = args[i+1];
                    break;
                } catch (Exception e) {
                    if (needArg) {
                        e.printStackTrace();
                        fail("Need to set a value for switch " + arg);
                    }
                }
                if (needArg) {
                    return s;
                } else {
                    return "true";
                }                
            }
        }
        return "";

    }
}
