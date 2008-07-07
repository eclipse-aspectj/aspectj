import java.lang.annotation.*;
import java.io.*;

@Entity(i=1)
public class Foo4 {
  public static void main(String []argv) {
    Foo4 f = new Foo4();
    Goo g = new Goo();
    if (f instanceof Serializable) {
       throw new RuntimeException("Foo4 should not implement it");        
    }
    if (!(g instanceof Serializable)) {
       throw new RuntimeException("Goo should implement it");        
    }
    if (!(new Hoo() instanceof Serializable)) {
       throw new RuntimeException("Hoo should implement it");        
    }
  }
}

@Entity(i=2)
class Goo {
}

@Entity // default is 2
class Hoo {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Entity {
  int i() default 2;
}

aspect X {
  declare parents: (@Entity(i=2) *) implements java.io.Serializable;
}
