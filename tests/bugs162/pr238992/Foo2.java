import java.lang.annotation.*;
import java.io.*;

@Entity(indexed=false)
public class Foo2 {
  public static void main(String []argv) {
    Foo2 f = new Foo2();
    Goo g = new Goo();
    if (f instanceof Serializable) {
       throw new RuntimeException("Foo2 should not implement it");        
    }
    if (!(g instanceof Serializable)) {
       throw new RuntimeException("Goo should implement it");        
    }
    if (!(new Hoo() instanceof Serializable)) {
       throw new RuntimeException("Hoo should implement it");        
    }
  }
}

@Entity(indexed=true)
class Goo {
}

@Entity // default is true
class Hoo {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Entity {
  boolean indexed() default true;
}

aspect X {
  declare parents: (@Entity(indexed=true) *) implements java.io.Serializable;
}
