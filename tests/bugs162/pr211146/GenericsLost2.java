import java.util.*;
import java.lang.reflect.*;

aspect Foo {

   public List<String> GenericsLost2.getStrings() {
     return null;
   }
}

class GenericsLost2 {
}
	