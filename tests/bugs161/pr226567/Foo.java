package b;

import java.util.ArrayList;
import java.util.Collection;

public class Foo {

   public Collection<Foo> getFoos() {
      return new ArrayList<Foo>() {{ add(new Foo()); }};
   }
}