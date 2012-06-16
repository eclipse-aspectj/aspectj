package bug;

import java.util.List;

public class A {
  public static void main(String[] argv) {
    new A().getList();
  }
}//extends B<String> { }

abstract class B<T> {
	public abstract List<List<T>> getList();
}

privileged aspect A_ITD {
	declare parents: A extends B<String>;

	public List<List<String>> A.getList(){
		return null;
	}
}
