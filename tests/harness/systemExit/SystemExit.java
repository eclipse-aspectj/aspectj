
public class SystemExit {

    /**
     * Exit via System.exit(int);
     * @param args null/empty String[], or integer exit value in args[0]
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int result = 0;
        if ((null != args) && (0 < args.length)) {
            result = Integer.valueOf(args[0]).intValue();
        }
        System.exit(result);
    }
}