import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

import java.io.FileInputStream;

import static java.lang.invoke.MethodHandles.Lookup.ClassOption.NESTMATE;

public class HiddenClassDemo {
  public static void main(String[] args) throws Throwable {
    // Step 1: Create lookup object
    MethodHandles.Lookup lookup = MethodHandles.lookup();

    // Step 2: Fetch or create the class bytes we want to define
    byte[] bytes = Thread.currentThread().getContextClassLoader()
      .getResourceAsStream("HiddenClass.class")
      .readAllBytes();

    // Step 3: Define hidden class
    Class<?> clazz = lookup.defineHiddenClass(bytes, true, NESTMATE).lookupClass();
    // Hidden classes have class names like my.package.MyClass/0x2a23f5, but no canonical name (null)
    System.out.println("Hidden class name = " + clazz.getName());
    System.out.println("Hidden class canonical name = " + clazz.getCanonicalName ());
    // Hidden classes cannot be resolved by any class loader (ClassNotFoundException)
    try {
      Class.forName(clazz.getName());
    }
    catch (ClassNotFoundException e) {
      System.out.println("Class.forName resolution error = " + e);
    }

    //Step 4: Create instance of hidden class object and call interface method
    Test test = (Test) clazz.getConstructor(null).newInstance(null);
    test.concat("Hello", "from", "dynamically", "defined", "hidden", "class");
  }
}

interface Test {
  void concat(String... words);
}
