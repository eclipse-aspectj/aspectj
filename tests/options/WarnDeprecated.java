



public class WarnDeprecated {

    /** */
    public static void main(String[] args) {
       if (null == args) {
            String s = new String(new byte[] {}, 0); // CE 10 deprecated if warn:deprecated
       }
    }
}
