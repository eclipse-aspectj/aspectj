import org.aspectj.testing.Tester;
public class FinalConstructorParm {
    int i;
    public FinalConstructorParm(final int i) { // bad compiler error here - not in 10b1
        this.i = i;
    }
    public int lessOne() { return i-1;}
    public static void main(String[] args) {
        Tester.check(1== (new FinalConstructorParm(2)).lessOne(),
                     "final failed");
    }
}
