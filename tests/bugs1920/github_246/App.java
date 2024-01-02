import java.lang.reflect.Method;
import java.util.Arrays;

public class App {
  public static void main(String[] args) {
    for (Method method : App.class.getDeclaredMethods()) {
      if (method.getName().equals("foo"))
        System.out.println(Arrays.toString(method.getDeclaredAnnotations()) + " " + method);
    }
  }
}
