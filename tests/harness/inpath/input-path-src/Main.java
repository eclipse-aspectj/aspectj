
import pack.Util;

public class Main {
	public static void main(String[] args) throws java.io.IOException {
		Util.log(args);
        boolean expectResourceCopy = false; // XXXX check
        if (expectResourceCopy) {
            java.io.InputStream in = 
                Main.class.getClassLoader().getResourceAsStream("pack/resource.txt");
            if (null == in) {
                throw new Error("unable to read pack/resource.txt");
            }
            byte[] buf = new byte[7];
            int read = in.read(buf);
            String val = new String(buf);
            if (!"testing".equals(val)) {
                throw new Error("expected \"testing\", got: " + val);
            }
        }
	}
}
