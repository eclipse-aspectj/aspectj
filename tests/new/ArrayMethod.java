import org.aspectj.testing.*;

public class ArrayMethod {
    public static void main(String[] args) {
        new ArrayMethod().go();
        Tester.check(true, "compiled");
    }

    void go() {
        try {
            int[] array1 = array1();
            int[] array2 = array2();
            for (int i = 0; i < array1.length; i++) {
                Tester.checkEqual(array1[i],i);
                Tester.checkEqual(array2[i],i);                
            }
        } catch (Exception e) {
        }
    }

    int array1()[] throws Exception {        
        return new int[] {0,1,2};
    }
    int[] array2() throws Exception {        
        return new int[] {0,1,2};
    }    
}
