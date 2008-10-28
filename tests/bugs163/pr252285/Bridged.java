import java.lang.reflect.Method;
import java.util.*;

interface Super<R extends Number> {
}

aspect X {
 private T Super<T>.getterA;
}


public class Bridged implements Super<Integer> {

        // Print BRIDGE status of all getter* methods
        public static void main(String[] argv) {
                Method[] ms = Bridged.class.getMethods();
                List results = new ArrayList(); 
                for (int i = 0; i < ms.length; i++) {
                        if (ms[i].getName().indexOf("getter")!=-1) {
                               
results.add(ms[i].getName()+"()"+ms[i].getReturnType().getName()+ " isBridged?"+((ms[i].getModifiers() & 0x0040) != 0));
                        }
                }
                Collections.sort(results);
                for (Iterator iterator = results.iterator(); iterator.hasNext();) {
                        String entry = (String) iterator.next();
                        System.out.println(entry);
                }
        }
}

