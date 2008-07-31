import java.lang.annotation.*;
import java.io.*;

@Entity(s="xxx")
public class Foo5 {
  public static void main(String []argv) {
    Foo5 f = new Foo5();
    Goo g = new Goo();
    if (f instanceof Serializable) {
       throw new RuntimeException("Foo5 should not implement it");        
    }
    if (!(g instanceof Serializable)) {
       throw new RuntimeException("Goo should implement it");        
    }
    if (!(new Hoo() instanceof Serializable)) {
       throw new RuntimeException("Hoo should implement it");        
    }
  }
}

@Entity(s="yyy")
class Goo {
}

@Entity // default is "yyy"
class Hoo {
}

@Retention(RetentionPolicy.RUNTIME)
@interface Entity {
  String s() default "yyy";
}

aspect X {
  declare parents: (@Entity(s="yyy") *) implements java.io.Serializable;
}
