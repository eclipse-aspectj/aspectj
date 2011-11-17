// nested values, more complex than just a marker
import java.lang.annotation.*;

aspect X {
  declare parents: @Bar(value = "123") * implements java.io.Serializable;
}

@Bar(value="123")
@NamedQuery(name = "Department.findAll",query = "select d from Department d order by d.name ASC",hints = {@QueryHint(name = "org.hibernate.cacheable",value = "true")})
public class Example2 { 

  public static void main(String []argv) {
    Example2 e = new Example2();
    if (e instanceof java.io.Serializable) {
      System.out.println("yes");
    } else {
      System.out.println("no");
    }
  }

}

@Retention(RetentionPolicy.RUNTIME)
@interface QueryHint {
  String name();
  String value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface NamedQuery {
  String name();
  String query();
  QueryHint[] hints();
}

@Retention(RetentionPolicy.RUNTIME)
@interface Bar {
  String value();
}


