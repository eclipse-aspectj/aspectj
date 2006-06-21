import java.lang.reflect.Method;

public class Foo  implements PreparedStatement {

  public static void main(String []argv) throws Exception {
    new Foo().getParameterMetaData();
    Method[] m = Foo.class.getMethods();
    int count = 1;
    for (int i = 0; i < m.length; i++) {
		Method method = m[i];
		if (method.toString().indexOf("Foo.getParameterMetaData")!=-1)
			System.err.println((count++)+") "+method);
	}
  }
  public Sub getParameterMetaData() throws MyException {
     return null;
  }
}

class Sub {}

interface PreparedStatement {
//   public ParameterMetaData getParameterMetaData() throws MyException;
}

class MyException extends Exception {}

interface ParameterMetaData {}

interface SubParameterMetaData extends ParameterMetaData {}
