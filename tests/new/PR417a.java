import org.aspectj.testing.*;
public class PR417a {

    public interface Types {
        public Integer INT = new Integer(3);
        public int i = 0;
        public byte b = (byte)1;
        public long l = 2L;
        public double d = (double)3;
        public float f = (float)4;
        public short s = (short)5;
        public char c = 'c';
    }

    public static interface StaticTypes {
        public Integer INT = new Integer(3);
        public int i = 0;
        public byte b = (byte)1;
        public long l = 2L;
        public double d = (double)3;
        public float f = (float)4;
        public short s = (short)5;
        public char c = 'c';
    }

    public static void main (String[] args) {
        new PR417a().run();
    }

    public void run() {
        Tester.check(Types.INT.equals(new Integer(3)), "INT != 3");
        Tester.checkEqual(Types.i,0);
        Tester.checkEqual((int)Types.b,1);
        Tester.checkEqual((int)Types.l,2);
        Tester.checkEqual((int)Types.d,3);
        Tester.checkEqual((int)Types.f,4);
        Tester.checkEqual((int)Types.s,5);
        Tester.checkEqual(Types.c,'c');
        Tester.check(StaticTypes.INT.equals(new Integer(3)), "INT != 3");
        Tester.checkEqual(StaticTypes.i,0);
        Tester.checkEqual((int)StaticTypes.b,1);
        Tester.checkEqual((int)StaticTypes.l,2);
        Tester.checkEqual((int)StaticTypes.d,3);
        Tester.checkEqual((int)StaticTypes.f,4);
        Tester.checkEqual((int)StaticTypes.s,5);
        Tester.checkEqual(StaticTypes.c,'c');        
    }
}
