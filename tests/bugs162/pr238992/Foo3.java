import java.lang.annotation.*;
import java.io.*;

@Entity(i=1)
public class Foo3 {
  public static void main(String []argv) {
    Foo3 f = new Foo3();
    Goo g = new Goo();
    if (f instanceof Serializable) {
       throw new RuntimeException("Foo3 should not implement it");        
    }
    if (!(g instanceof Serializable)) {
       throw new RuntimeException("Goo should implement it");        
    }
    if (new Hoo() instanceof Serializable) {
       throw new RuntimeException("Hoo should not implement it");        
    }
  }
}

@Entity(i=2)
class Goo {
}

@Entity // default is 1
class Hoo {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Entity {
  int i() default 1;
}

aspect X {
  declare parents: (@Entity(i=2) *) implements java.io.Serializable;
}
