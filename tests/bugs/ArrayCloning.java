
public class ArrayCloning {

    public static void main(String[] args) {
        ArrayCloning ArrayCloning = new ArrayCloning();
        Integer[] clonedStaticField = ArrayCloning.clone1();
        checkIdentical(clonedStaticField,ArrayCloning.staticField);
    
        Integer[] clonedField = ArrayCloning.clone2();
        checkIdentical(clonedField,ArrayCloning.nonStaticField);

        Integer[] clown = null;

        clown = ArrayCloning.clone3();
        clown = ArrayCloning.clone4();
        Integer[][] ArrayCloningArrayCloning = ArrayCloning.clone5();
    }

    public static void checkIdentical(Integer[] one, Integer[] two) {
      if (one[0]!=two[0]) throw new RuntimeException("Not the same (a)");
      if (one[1]!=two[1]) throw new RuntimeException("Not the same (b)");
    }

    private static Integer[] staticField = new Integer[2];

    private Integer[] nonStaticField = new Integer[2];

    public ArrayCloning() {
      nonStaticField[0] = new Integer(32);
      nonStaticField[1] = new Integer(64);
    }

    static {
      staticField[0] = new Integer(1);
      staticField[1] = new Integer(2);
    }
    
    public Integer[] clone1() {
      System.err.println("Clone call on a static field");
      return (Integer[])staticField.clone();
    }

    public Integer[] clone2() {
       System.err.println("Clone call on a non-static field");
       return (Integer[])nonStaticField.clone();
    }

    public Integer[] clone3() {
       System.err.println("Clone call on a local variable");
       Integer[] ArrayCloningArrayCloning = staticField;
       return (Integer[])ArrayCloningArrayCloning.clone();
    }


    // Clone call on anonymous 'thing' !
    public Integer[] clone4() {
      System.err.println("Clone call on a 1 dimensional anonymous integer array");
      return (Integer[])new Integer[5].clone();
    }

    // Sick
    public Integer[][] clone5() {
      System.err.println("Clone call on a 2 dimensional anonymous integer array");
      return (Integer[][])new Integer[5][3].clone();
    }


}

aspect HelloWorldAspect {
    Object[] around(): call(* java.lang.Object.clone()) && within(ArrayCloning) {
  	Object[] ret = proceed(); 
	return (Object[])ret.clone();
    }
}
