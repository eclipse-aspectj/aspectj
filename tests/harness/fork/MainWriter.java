
public class MainWriter {
    public static void main(String[] args) {
        if (args.length > 0) {
            if ("err".equals(args[0])) {
                System.err.println("System.err");
            } else if ("out".equals(args[0])) {
                System.out.println("System.out");
            } else if ("Error".equals(args[0])) {
                throw new Error();
            }
        }
    }
}