import java.lang.annotation.*;
import java.io.*;

@Entity(indexed=false)
public class Foo {
  public static void main(String []argv) {
    Foo f = new Foo();
    Goo g = new Goo();
    if (f instanceof Serializable) {
       throw new RuntimeException("Foo should not implement it");        
    }
    if (!(g instanceof Serializable)) {
       throw new RuntimeException("Goo should implement it");        
    }
    if (new Hoo() instanceof Serializable) {
       throw new RuntimeException("Hoo should not implement it");        
    }
  }
}

@Entity(indexed=true)
class Goo {
}

@Entity // default is false
class Hoo {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Entity {
  boolean indexed() default false;
}

aspect X {
  declare parents: (@Entity(indexed=true) *) implements java.io.Serializable;
}
