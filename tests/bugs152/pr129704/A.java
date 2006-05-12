import java.lang.annotation.*;

interface GDO {}

@Retention(RetentionPolicy.RUNTIME) @interface Marker { }

class DCP<T extends GDO> {
 @Marker void getData(){}
}

aspect X {
  before(Marker a): execution(* getData(..)) && @annotation(a) { System.err.println(a);  }
}

public class A {
	public static void main(String[] args) {
		new DCP().getData();
	}
}