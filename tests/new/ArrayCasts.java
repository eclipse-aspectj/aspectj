import org.aspectj.testing.Tester;

public class ArrayCasts {
    public static void main(String[] args) {
        new ArrayCasts().realMain(args);
    }

    int[]     ints   = new int[1];
    int[][]   intss  = new int[2][];
    int[][][] intsss = new int[3][][];

    Integer[]     integers   = new Integer[4];
    Integer[][]   integerss  = new Integer[5][];
    Integer[][][] integersss = new Integer[6][][];
    
    public void realMain(String[] args) {

        ints   = (int[])    new ArrayCasts().ints.clone();
        intss  = (int[][])  new ArrayCasts().intss.clone();
        intsss = (int[][][])new ArrayCasts().intsss.clone();

        integers   = (Integer[])    new ArrayCasts().integers.clone();
        integerss  = (Integer[][])  new ArrayCasts().integerss.clone();
        integersss = (Integer[][][])new ArrayCasts().integersss.clone();

        Tester.checkEqual(ints.length,   1);
        Tester.checkEqual(intss.length,  2);
        Tester.checkEqual(intsss.length, 3);

        Tester.checkEqual(integers.length,   4);
        Tester.checkEqual(integerss.length,  5);
        Tester.checkEqual(integersss.length, 6);
    }
}

aspect A {
    //pointcut callstoSets(): callsto(receptions(void set*(..)) && instanceof(ArrayCasts));
    pointcut callstoSets(): call(void set*(..)) && target(ArrayCasts);
    before()             : callstoSets() {}
}
