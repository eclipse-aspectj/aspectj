



public class WarnDeprecated {

    /** @deprecated */
    public static void main(String[] args) {
       if (null == args) {
            main(new String[0]); // CE 10 deprecated if warn:deprecated
       }
    }
}
