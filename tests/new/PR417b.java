import org.aspectj.testing.*;
public class PR417b {

    public static class StaticTypes {
        public static Integer INT = new Integer(3);
        public static int i = 0;
        public static byte b = (byte)1;
        public static long l = 2L;
        public static double d = (double)3;
        public static float f = (float)4;
        public static short s = (short)5;
        public static char c = 'c';
    }

    public static void main (String[] args) {
        new PR417b().run();
    }

    public void run() {
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
