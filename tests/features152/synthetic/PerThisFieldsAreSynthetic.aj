import java.lang.reflect.*;

public class PerThisFieldsAreSynthetic {

  public static void main(String[] args) throws Exception {
    new PerThisFieldsAreSynthetic().foo();
    Field[] fields = PerThisFieldsAreSynthetic.class.getDeclaredFields();
    for (Field f : fields) {
      if (!f.isSynthetic()) {
         System.out.println("Found unexpected non-synthetic field: " + f.getName());
         throw new IllegalStateException("non-synthetic field " + f.getName());
      }
    }
  }

  public void foo() {}

}

aspect PerThis perthis(execution(* PerThisFieldsAreSynthetic.*(..))) {

   before() : execution(* *(..)) { System.out.println("before"); }

}