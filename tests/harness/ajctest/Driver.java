

/** Drive normal, system.exit, error or exception result from main */
public class Driver {

    /**
     * @param args {[-exit <number>|[-error|-exception] <string>]}
     */
    public static void main (String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-exit".equals(arg)) {
                System.exit(Integer.valueOf(args[i+1]).intValue());
            } else if ("-error".equals(arg)) {
                throw new Error(args[i+1]);
            } else if ("-exception".equals(arg)) {
                throw new RuntimeException(args[i+1]);
            }
        } 
    } 
}
