public class NonConstants {
    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 3;
    private static final int D = 4;
    private static final int E = 5;

    public static void main(String[] args) {
        NonConstants nc = new NonConstants();
        int x = 10;

        switch(x) {
        case (NonConstants).A: break;
        case ((NonConstants)null).B: break;  //ERR
        case (nc).C: break;                  //ERR
        case nc.D: break;                    //ERR
        case ((((NonConstants)))).E: break;
        }
    }
}
